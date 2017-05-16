@Language("Regexp")
private const val MERGED_TICKET_REGEX = 
		"""This request was closed and merged into request #\d+"""
@Language("Regexp")
private const val YT_INCLOUD_SUBSCRIPTION_CANCEL_REGEX = 
		"""I would like to cancel my YouTrack InCloud subscription"""
@Language("Regexp")
private const val REQUEST_FOR_DETAILS_REGEX = 
		"""could you please ((provide)|(attach)|(send))"""
@Language("Regexp")
internal const val ORIGINAL_TICKET_ID = 
		"""\b(original_ticket_id_)\d*"""
@Language("Regexp")
internal const val TEMPLATE_TICKET_PRODUCT = 
		"""^((\*{2})?Product(\*{2}[\p{C}\p{Z}\s])?: ).*$"""
@Language("Regexp")
internal const val TEMPLATE_TICKET_QUESTION = 
		"""^(Question: ).*$"""
@Language("Regexp")
internal const val TEMPLATE_TICKET_QUESTION_MARKUP = 
		"""^(\*{2}Question\*{2}[\p{C}\p{Z}\s]: ).*$"""
@Language("Regexp")
internal const val TEMPLATE_TICKET_MERGED = 
		"""Request #\d+ ".*" was closed and merged into this request. Last comment in request #\d+:"""