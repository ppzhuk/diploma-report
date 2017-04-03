package executable

import executable.setup.commonOptionsMap
import executable.setup.outerResourcesDir
import executable.setup.performAction
import executable.setup.resourcesDir
import org.apache.commons.cli.Option
import org.apache.commons.cli.Options
import org.jetbrains.zkb.db.DBReader
import org.jetbrains.zkb.lda.lda
import java.io.File

fun main(args: Array<String>) =
        performAction(
                defineOptions(),
                args,
                listOf(
                        setOf("f", "dicts", "mp", "nt") to { cmd ->
                            processData(
                                    cmd.getOptionValue("f"),
                                    cmd.getOptionValue("dicts"),
                                    cmd.getOptionValue("mp"),
                                    cmd.getOptionValue("nt").toInt(),
                                    inResourceDir = cmd.hasOption("ird"),
                                    verbose = true
                            )
                        }
                )
        )


private fun defineOptions(): Options {
    val options = Options()
    options.addOption(commonOptionsMap["f"])
    options.addOption(commonOptionsMap["dicts"])
    options.addOption(commonOptionsMap["ird"])
    options.addOption(commonOptionsMap["mp"])
    options.addOption(specificOptionsMap["nt"])
    return options
}

private val specificOptionsMap = mapOf(
        "nt" to Option("nt", "num-topics", true, "Number of topics for LDA"
        )
)


private fun processData(
        sourceFilename: String,
        dictionariesDir: String,
        mongoPropFilename: String,
        numTopics: Int,
        inResourceDir: Boolean = false,
        verbose: Boolean = true
) {
    val defaultPath = if (inResourceDir) resourcesDir else ""

    val model = lda(
            numTopics,
            File("$defaultPath$sourceFilename"),
            File("$defaultPath$dictionariesDir").listFiles().toList(),
            iterations = 2000,
            alpha_t = 0.1
    )

    DBReader("$defaultPath$mongoPropFilename", verbose).use { reader ->
        model.printClusters(
                reader.readTicketThreads(),
                outDir = File(
                        "${outerResourcesDir}out${File.separator}" +
                                "${System.currentTimeMillis()}_${numTopics}"
                ),
                verbose = verbose
        )
    }
}