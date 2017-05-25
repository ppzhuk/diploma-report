@JsonIgnoreProperties(
        value = *arrayOf("bodyEmpty")
)
class Comment() : Comparable<Comment> {
    @JsonProperty("ticket_id")
    var ticketId: Long? = null
    var id: Long? = null
    var body: String? = null
    @JsonProperty("lda_body")
    var ldaBody: String? = null

    // ...

    companion object {
        private val serialVersionUID = 1L
    }
}