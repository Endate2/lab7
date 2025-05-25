package utils

import kotlinx.serialization.Serializable

@Serializable
class Answer (val answerType: AnswerType, val message: String, val receiver: String)