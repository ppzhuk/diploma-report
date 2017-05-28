import cc.mallet.topics.ParallelTopicModel
// ...

class LDATopicModel(
        numberOfTopics: Int,
        alphaSum: Double,
        beta: Double
) : ParallelTopicModel(numberOfTopics, alphaSum, beta), Serializable {    

    val instancesCountPerTopic: List<Int>
    val clusters: List<List<Long>>
    val topTopicsProbabilityMap: Map<Long, Pair<Int, Double>>
    val topicsFeatureCountMaps: List<Map<Int, Int>>
    val cosineMap: Map<Long, Pair<Int, Double>>
    val perplexity: Double
    val cosineMED: Double
    val cosineAVG: Double

    fun getTopicTopWords(
            wordsCount: Int = 10
    ): List<List<String>> = // ...
	
	companion object {
        @JvmStatic private val serialVersionUID: Long = 1L
        @JvmStatic private val logger = LoggerFactory.getLogger(object {}::class.java)

        fun read(
                modelFile: File,
                stateFile: File? = null
        ): LDATopicModel = // ...
    }

    override fun write(serializedModelFile: File) {
        // ...
    }
	
    // ...       
}
