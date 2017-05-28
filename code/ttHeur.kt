/**
 * @param maxShortParagraphWords
 * @param sentencesFrequency
 * @param maxParagraphLength
 * @param minParagraphLength
 * @param punctuationProportion
 * @param customActions a list of actions modifying comment **body**.
 *                      It is applied *before* others look'n'feel heuristics.
 * @param customLdaActions a list of actions modifying comment **ldaBody**.
 *                      It is applied *before* others LDA heuristics.
 *
 * @see applyRegex
 * @see ldaApplyRegex
 */
fun List<TicketThread>.applyHeuristics(
        maxShortParagraphWords: Int,
        sentencesFrequency: Int,
        maxParagraphLength: Int,
        minParagraphLength: Int,
        punctuationProportion: Double,
        customActions: List<CommentUpdater> = ytDefaultCustomActions,
        customLdaActions: List<CommentUpdater> = listOf()
): List<TicketThread> {
    replaceCRLFtoLF()

    // Эвристики отображения
    applyCustomActions_(customActions)
    removeEmailQuotes_()
    removeShortStartParagraphs_(maxShortParagraphWords)
    removeCommonSuffixes_(globally = true)
    removeShortEndParagraphs_(maxShortParagraphWords)
    removeFrequentSentences_(sentencesFrequency)
    normalizeLookAndFeel_()

    // Эвристики тематического моделирования
    applyCustomLdaActions_(customLdaActions)
    ldaRemoveURIs_()
    ldaRemoveWordsWithApostrophes_()
    ldaRemoveLongParagraphs_(maxParagraphLength)
    ldaRemoveFilesystemPaths_()
    ldaRemoveWWWAndEmails_()
    ldaRemoveParagraphsWithPunctuation_(minParagraphLength, punctuationProportion)

    return this
}