package io.github.hansanto.quipoquiz.util.graphql

import com.apollographql.mockserver.MockRequest
import io.kotest.matchers.shouldBe
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

@Serializable
data class SiteIdGraphQLVariable(
    val siteId: List<String>
)

@Serializable
data class GetQuizzesGraphQLVariable(
    val siteId: List<String>,
    val limit: Int,
    val offset: Int
)

inline fun <reified T> MockRequest.verifyContent(operationName: String, token: String, expectedVariables: T) {
    val headers = headers
    headers["Authorization"] shouldBe "Bearer $token"

    val body = body.utf8()
    val bodyJson = Json.parseToJsonElement(body).jsonObject
    bodyJson["operationName"]?.jsonPrimitive?.content shouldBe operationName

    val variables: T = parseBody()
    variables shouldBe expectedVariables
}

inline fun <reified T> MockRequest.parseBody(): T {
    val body = this.body.utf8()
    val bodyJson = Json.parseToJsonElement(body).jsonObject
    return Json.decodeFromJsonElement(bodyJson["variables"]!!)
}
