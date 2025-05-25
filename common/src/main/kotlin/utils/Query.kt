package utils

import kotlinx.serialization.Serializable

@Serializable
data class Query (val queryType: ExecuteType,var message: String, val args: Map<String, String>,var token: String = "") {
    companion object
}
