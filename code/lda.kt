fun lda(
        numTopics: Int,
        sourceFile: File,
        additionalStopWordsFiles: List<File> = listOf(),
        threads: Int = 4,
        iterations: Int = 2000,
        alpha_t: Double = 0.1
): LDATopicModel {
    val instances = getInstanceList(sourceFile, additionalStopWordsFiles)

    val beta_w = 0.1
    val model = LDATopicModel(numTopics, alpha_t * numTopics, beta_w)

    model.addInstances(instances)
    model.setNumThreads(threads)
    model.setNumIterations(iterations)

    logger.info(
            "Estimating LDA topic model " +
                    "(topics: ${numTopics}, ins_cnt: ${instances.size}, " +
                    "iter: ${iterations}, threads: ${threads})..."
    )
    model.estimate()
    logger.info("Done. ${model.instancesCountPerTopic.count { it > 0 }}/${numTopics} not empty topics.")

    return model
}