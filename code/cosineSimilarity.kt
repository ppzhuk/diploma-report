package org.jetbrains.zkb.qa

import cc.mallet.types.FeatureSequence
import cc.mallet.types.Instance
import org.jetbrains.zkb.lda.LDATopicModel
import org.jetbrains.zkb.lda.getFeatureCountMap


internal fun getVectors(
        wordCountMapText: Map<Int, Int>,
        wordCountMapTopic: Map<Int, Int>
): Pair<List<Int>, List<Int>> {
    checkIsAllPositive(
            wordCountMapText.keys.toList(),
            wordCountMapText.values.toList()
    )
    checkIsAllPositive(
            wordCountMapTopic.keys.toList(),
            wordCountMapTopic.values.toList()
    )

    val mixed: MutableMap<Int, Pair<Int, Int>> = mutableMapOf()

    wordCountMapTopic.forEach {
        mixed[it.key] = Pair(0, it.value)
    }
    wordCountMapText.forEach {
        if (it.key in mixed) {
            val pair = mixed[it.key]!!
            mixed[it.key] = pair.copy(first = it.value)
        } else {
            mixed[it.key] = Pair(it.value, 0)
        }
    }
    return mixed.map { it.value }
            .unzip()
}

private fun vectorLength(vector: List<Int>) =
        Math.sqrt(vector.sumBy { it * it }.toDouble())

fun cosine(textVector: List<Int>, topicVector: List<Int>): Double {
    checkSizes(textVector, topicVector)
    checkIsAllPositive(textVector, topicVector)
    checkNoZeroPairs(textVector, topicVector)

    val nominator = textVector.zip(topicVector)
            .sumBy { it.first * it.second }
    val denominator = vectorLength(textVector) * vectorLength(topicVector)
    return nominator / denominator
}

private fun checkIsAllPositive(textVector: List<Int>, topicVector: List<Int>) {
    if (textVector.any { it < 0 } || topicVector.any { it < 0 }) {
        throw IllegalArgumentException("Text data vectors can't have negative values.")
    }
}

private fun checkNoZeroPairs(textVector: List<Int>, topicVector: List<Int>) {
    val list = textVector.zip(topicVector)
    val anyMatches = list.any {
        it.first == 0 && it.second == 0
    }
    if (anyMatches) {
        throw IllegalArgumentException("Zero pair found!")
    }
}


private fun checkSizes(textVector: List<Int>, topicVector: List<Int>) {
    if (textVector.size != topicVector.size) {
        throw IllegalArgumentException(
                "Vectors have different sizes: " +
                        "textVector - ${textVector.size}" +
                        "topicVector - ${topicVector.size}"
        )
    }
}

internal fun cosine(
        wordCountMapText: Map<Int, Int>,
        wordCountMapTopic: Map<Int, Int>
): Double {
    val (textVector, topicVector) = getVectors(wordCountMapText, wordCountMapTopic)
    return cosine(textVector, topicVector)
}

internal fun topicCommentCosine(
        model: LDATopicModel,
        topicIdx: Int,
        commentInstance: Instance
) =
        cosine(
                model.topicsFeatureCountMaps[topicIdx],
                (commentInstance.data as FeatureSequence).getFeatureCountMap()
        )