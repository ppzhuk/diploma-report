@Language("Regexp")
internal const val ATTACHMENT_REGEX = 
		"""!\[.*\]\([^\s]*\)"""		
@Language("Regexp")
internal const val whitespace = 
		"""[\p{C}\p{Z}\s]"""
@Language("Regexp")
internal const val WWW_REGEX = 
		"""(\b)www\.\S+\.\S+"""
@Language("Regexp")
internal const val EMAIL_REGEX = """\S+@\S+"""
@Language("Regexp")
internal const val APOSTROPHE_REGEX = 
		"""\b\w{1,5}[`'\u00B4\u2018\u2019]\w+\b"""
@Language("Regexp")
internal const val PATH_REGEX = 
		"""[^\p{C}\p{Z}\s]*[/\\]((program )|(Program Files ))?[^\p{C}\p{Z}\s]+[/\\][^\p{C}\p{Z}\s]*"""
@Language("Regexp")
internal const val URI_REGEX = 
		"""[^\p{C}\p{Z}\s]+://[^\p{C}\p{Z}\s]*"""