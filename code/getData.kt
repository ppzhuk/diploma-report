package org.jetbrains.zkb.zddownload

import org.jetbrains.zkb.db.DBReader
import org.jetbrains.zkb.db.DBWriter
import org.jetbrains.zkb.executable.setup.CollectionNotExistsException
import org.jetbrains.zkb.executable.setup.Config
import org.jetbrains.zkb.model.Comment
import org.slf4j.LoggerFactory
import org.zendesk.client.v2.Zendesk
import org.zendesk.client.v2.ZendeskResponseRateLimitException
import org.zendesk.client.v2.model.Ticket
import java.text.SimpleDateFormat
import java.util.*

private val logger = LoggerFactory.getLogger(object {}::class.java)

private var newTicketsCount = 0
private var newCommentsCount = 0
private var existingNoChangesTicketsCount = 0
private var existingUpdatedTicketsCount = 0

internal fun getData(
        configFilename: String,
        startTimestamp_ms: Long,
        skipSaving: Boolean,
        verbose: Boolean
) {
    val config = Config.parse(configFilename)
    config.setupLoggers(verbose)

    val zd = Zendesk.Builder(config.zdUrl)
            .setUsername(config.zdLogin)
            .setToken(config.zdToken)
            .setReqInterval(config.zdReqInterval)
            .setMinRemainingApiCalls(config.zdMinRemainingApiCalls)
            .build()!!
    val dbWriter = DBWriter(config.dbHost, config.dbPort, config.dbNameRawData,
            config.dbNameAuth, config.dbLogin, config.dbPassword)
    val dbReader = DBReader(config.dbHost, config.dbPort, config.dbNameRawData,
            config.dbNameAuth, config.dbLogin, config.dbPassword)
    zd.use { zd ->
        dbWriter.use { writer ->
            dbReader.use { reader ->
                getData(zd, reader, writer, startTimestamp_ms, config, skipSaving)
            }
        }
    }
}

private fun getData(
        zd: Zendesk,
        reader: DBReader,
        writer: DBWriter,
        startTimestamp_ms: Long,
        config: Config,
        skipSaving: Boolean
) {
    val startTime = Date(startTimestamp_ms)
    val ticketsLazyIterable = zd.getLazyTicketsSince(startTime)
    val lazyIterator = ticketsLazyIterable.iterator()
    var hasNext: Boolean = wrapZendeskAPICall(config.zdMaxRetry) {
        // Возможно обращение к API в:
        // org.zendesk.client.v2.Zendesk.PagedIterable.hasNext
        lazyIterator.hasNext()
    }
    while (hasNext) {
        val ticket = lazyIterator.next()

        if (checkBrands(ticket, config.zdBrands) &&
                checkUpdatedAt(ticket, startTime)) {
            val comments: List<Comment> = wrapZendeskAPICall(config.zdMaxRetry) {
                // Обращение к API в:
                // org.zendesk.client.v2.Zendesk.PagedIterable.hasNext
                zd.getComments(ticket)
            }

            if (!skipSaving && comments.size > 1) {
                saveData(reader, writer, ticket, comments)
            }
        }
        hasNext = wrapZendeskAPICall(config.zdMaxRetry) { lazyIterator.hasNext() }
    }
}

private fun saveData(
        reader: DBReader,
        writer: DBWriter,
        ticket: Ticket,
        comments: List<Comment>
) {
    val oldTicket = try {
        reader.readTicket(ticket.id)
    } catch (e: CollectionNotExistsException) {
        null
    }
    when {
    // New ticket
        oldTicket == null -> {
            writer.writeTickets(listOf(ticket))
            writer.writeComments(comments)

            logger.info("New ticket found, id: ${ticket.id}.")
            newTicketsCount++
            newCommentsCount += comments.size
        }
    // Existing ticket is updated
        ticket.updatedAt > oldTicket.updatedAt -> {
            writer.updateTicket(ticket)
            val oldCommentsIds = reader.readComments(ticket.id).map { it.id }
            val newComments = comments.filter { it.id !in oldCommentsIds }
            writer.writeComments(newComments)

            logger.info("Existing ticket is updated, id: ${ticket.id}\n" +
                    "New comments: ${newComments.size}")
            newCommentsCount += newComments.size
            existingUpdatedTicketsCount++;
        }
        ticket.updatedAt == oldTicket.updatedAt -> {
            logger.info("Existing ticket with no changes, id: ${ticket.id}")
            existingNoChangesTicketsCount++
        }
    // Existing ticket is newer then downloaded one is.
    // This is impossible but exception is thrown just in case.
        ticket.updatedAt < oldTicket.updatedAt -> {
            throw IllegalStateException("Existing ticket is newer then downloaded one is")
        }
    }
}

private fun Zendesk.getComments(ticket: Ticket): List<Comment> {
    return getTicketComments(ticket.id!!) // returns lazy iterable
            .toList()       // <-- triggers API calls
            .map { comment ->
                Comment.transform(comment, ticket.id!!)
            }
}

private fun Zendesk.getLazyTicketsSince(startTime: Date): Iterable<Ticket> {
    val sdf = SimpleDateFormat("dd-MM-yyyy'T'HH:mm:ss")
    sdf.timeZone = TimeZone.getTimeZone("GMT+0")
    logger.info("Incremental download of the tickets since ${sdf.format(startTime)}")

    return getTicketsIncrementally(startTime)
}

private fun <T> wrapZendeskAPICall(maxRetry: Int, apiCall: () -> T): T
{
    var retry = 0
    while (true) {
        return try {
            apiCall()
        } catch (e: ZendeskResponseRateLimitException) {
            retry++
            logger.warn("Rate limit exceeded! ${maxRetry - retry} attempts left.")
            if (retry >= maxRetry) {
                logger.error("There is no api calls available. Aborting.")
                throw e
            }
            logger.warn("Retrying after ${e.retryAfter} sec...")
            Thread.sleep(e.retryAfter!! * 1000)
            continue
        }
    }
}

private fun checkBrands(t: Ticket, brands: List<Long>) =
        if (brands.size == 1 && brands[0] == -1L) { // each brand
            true
        } else {
            t.brandId in brands
        }

private fun checkUpdatedAt(t: Ticket, startTime: Date) = t.updatedAt!! >= startTime