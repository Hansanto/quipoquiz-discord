package io.github.hansanto.quipoquiz.quipoquiz

import com.apollographql.apollo.ApolloClient
import com.apollographql.mockserver.MockServer
import com.apollographql.mockserver.enqueueError
import com.apollographql.mockserver.enqueueGraphQLString
import io.github.hansanto.generated.graphql.CountQuizQuery
import io.github.hansanto.generated.graphql.GetCategoriesQuery
import io.github.hansanto.generated.graphql.GetQuizzesQuery
import io.github.hansanto.generated.graphql.fragment.AnswerChoice
import io.github.hansanto.generated.graphql.fragment.DetailedCategory
import io.github.hansanto.generated.graphql.fragment.QuestionMultipleChoices
import io.github.hansanto.generated.graphql.fragment.QuestionTrueOrFalse
import io.github.hansanto.generated.graphql.fragment.QuestionTrueOrFalse.Image
import io.github.hansanto.generated.graphql.fragment.Quiz
import io.github.hansanto.generated.graphql.fragment.Quiz.Questions_multiple_choice
import io.github.hansanto.generated.graphql.fragment.Quiz.Questions_true_or_false
import io.github.hansanto.generated.graphql.fragment.Quiz.Quiz_category
import io.github.hansanto.generated.graphql.fragment.Quiz.Quiz_image
import io.github.hansanto.quipoquiz.config.QuipoQuizConfiguration
import io.github.hansanto.quipoquiz.config.QuipoQuizConfiguration.pageSize
import io.github.hansanto.quipoquiz.util.graphql.GetQuizzesGraphQLVariable
import io.github.hansanto.quipoquiz.util.graphql.SiteIdGraphQLVariable
import io.github.hansanto.quipoquiz.util.graphql.parseBody
import io.github.hansanto.quipoquiz.util.graphql.verifyContent
import io.github.hansanto.quipoquiz.util.randomString
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.toList

private const val TYPENAME_CATEGORY = "categories_default_Entry"
private const val TYPENAME_QUIZ = "quiz_default_Entry"
private const val TYPENAME_QUESTION = "questions_default_Entry"
private const val TYPENAME_ANSWER_CHOICE = "anwser_choices_TableRow"

class QuipoQuizRepositoryImplTest : ShouldSpec({

    lateinit var server: MockServer
    lateinit var client: ApolloClient
    lateinit var token: String
    lateinit var repository: QuipoQuizRepositoryImpl

    beforeTest {
        server = MockServer()
        token = randomString()
        client = QuipoQuizConfiguration.createClient(
            url = server.url(),
            token = token
        )

        repository = QuipoQuizRepositoryImpl(
            client = client
        )
    }

    afterTest {
        server.close()
        client.close()
    }

    should("getCategories sends the correct query to the server") {
        suspend fun verify(siteId: QuipoQuizSiteId) {
            enqueueEmptyCategoriesResponse(server)
            repository.getCategories(siteId).toList()

            server.takeRequest().verifyContent(
                operationName = GetCategoriesQuery.OPERATION_NAME,
                token = token,
                expectedVariables = SiteIdGraphQLVariable(
                    siteId = listOf(siteId.id)
                )
            )
        }

        QuipoQuizSiteId.entries.forEach {
            verify(it)
        }
    }

    should("getCategories throws an error when the server returns an error") {
        suspend fun verify(siteId: QuipoQuizSiteId) {
            server.enqueueError(500)
            val error = shouldThrow<Exception> {
                repository.getCategories(siteId).toList()
            }
            error.message shouldBe "Unable to retrieve categories for site [${siteId.name}]"
        }

        QuipoQuizSiteId.entries.forEach {
            verify(it)
        }
    }

    should("getCategories returns empty list when the server returns an empty list") {
        enqueueEmptyCategoriesResponse(server)
        repository.getCategories(QuipoQuizSiteId.FR).toList() shouldBe emptyList()
    }

    should("getCategories returns a list of categories") {
        server.enqueueGraphQLString(
            """  
                {
                    "data": {
                        "categoriesEntries": [
                            ${createCategoryEntryString(1)},
                            ${createCategoryEntryString(2)},
                            ${createCategoryEntryString(3)}
                        ]
                    }
                }
            """.trimIndent()
        )

        val list = repository.getCategories(QuipoQuizSiteId.FR).toList()
        list shouldContainExactly listOf(
            DetailedCategory(
                __typename = TYPENAME_CATEGORY,
                id = "1",
                color = "#f7e24c",
                title = "category 1",
                language = "fr-CA",
                image = listOf(
                    DetailedCategory.Image(
                        url = "image1"
                    )
                ),
                icon = listOf(
                    DetailedCategory.Icon(
                        url = "icon1"
                    )
                )
            ),
            DetailedCategory(
                __typename = TYPENAME_CATEGORY,
                id = "2",
                color = "#f7e24c",
                title = "category 2",
                language = "fr-CA",
                image = listOf(
                    DetailedCategory.Image(
                        url = "image2"
                    )
                ),
                icon = listOf(
                    DetailedCategory.Icon(
                        url = "icon2"
                    )
                )
            ),
            DetailedCategory(
                __typename = TYPENAME_CATEGORY,
                id = "3",
                color = "#f7e24c",
                title = "category 3",
                language = "fr-CA",
                image = listOf(
                    DetailedCategory.Image(
                        url = "image3"
                    )
                ),
                icon = listOf(
                    DetailedCategory.Icon(
                        url = "icon3"
                    )
                )
            )
        )
    }

    should("getCategories returns a distinct list of categories by checking the id") {
        server.enqueueGraphQLString(
            """  
                {
                    "data": {
                        "categoriesEntries": [
                            ${createCategoryEntryString(1, "category 1")},
                            ${createCategoryEntryString(1, "category 2")},
                            ${createCategoryEntryString(2, "category 3")},
                            ${createCategoryEntryString(2, "category 4")},
                            ${createCategoryEntryString(2, "category 5")}
                        ]
                    }
                }
            """.trimIndent()
        )

        val list = repository.getCategories(QuipoQuizSiteId.FR).toList()
        list shouldContainExactly listOf(
            DetailedCategory(
                __typename = TYPENAME_CATEGORY,
                id = "1",
                color = "#f7e24c",
                title = "category 1",
                language = "fr-CA",
                image = listOf(
                    DetailedCategory.Image(
                        url = "image1"
                    )
                ),
                icon = listOf(
                    DetailedCategory.Icon(
                        url = "icon1"
                    )
                )
            ),
            DetailedCategory(
                __typename = TYPENAME_CATEGORY,
                id = "2",
                color = "#f7e24c",
                title = "category 3",
                language = "fr-CA",
                image = listOf(
                    DetailedCategory.Image(
                        url = "image2"
                    )
                ),
                icon = listOf(
                    DetailedCategory.Icon(
                        url = "icon2"
                    )
                )
            )
        )
    }

    should("getCountQuiz sends the correct query to the server") {
        suspend fun verify(siteId: QuipoQuizSiteId) {
            enqueueCountResponse(server, 152)

            repository.getCountQuiz(siteId)

            server.takeRequest().verifyContent(
                operationName = CountQuizQuery.OPERATION_NAME,
                token = token,
                expectedVariables = SiteIdGraphQLVariable(
                    siteId = listOf(siteId.id)
                )
            )
        }

        QuipoQuizSiteId.entries.forEach {
            verify(it)
        }
    }

    should("getCountQuiz throws an error when the server returns an error") {
        suspend fun verify(siteId: QuipoQuizSiteId) {
            server.enqueueError(500)
            val error = shouldThrow<Exception> {
                repository.getCountQuiz(siteId)
            }
            error.message shouldBe "Unable to retrieve the number of quiz for siteId [${siteId.name}]"
        }

        QuipoQuizSiteId.entries.forEach {
            verify(it)
        }
    }

    should("getCountQuiz returns the number of quizzes") {
        enqueueCountResponse(server, 532)
        repository.getCountQuiz(QuipoQuizSiteId.FR) shouldBe 532
    }

    should("getQuizzes throws an error when the server returns an error") {
        suspend fun verify(siteId: QuipoQuizSiteId) {
            enqueueCountResponse(server, 1)
            server.enqueueError(500)
            val error = shouldThrow<Exception> {
                repository.getQuizzes(siteId).toList()
            }
            // 10 is the default page size
            error.message shouldBe "Unable to retrieve quizzes for siteId [$siteId] page [0] with page size [10]"
        }

        QuipoQuizSiteId.entries.forEach {
            verify(it)
        }
    }

    should("getQuizzes does not send a request when the number of quizzes is 0") {
        suspend fun verify(siteId: QuipoQuizSiteId) {
            enqueueCountResponse(server, 0)

            repository.getQuizzes(siteId).toList()

            server.takeRequest() // pass count request
            shouldThrow<Exception> {
                server.takeRequest() // pass get quizzes
            }
        }

        QuipoQuizSiteId.entries.forEach {
            verify(it)
        }
    }

    should("getQuizzes sends only one request when the number of quizzes is less or equals than the page size") {
        suspend fun verify(customRepository: QuipoQuizRepositoryImpl, count: Int, siteId: QuipoQuizSiteId) {
            enqueueCountResponse(server, count)
            enqueueEmptyQuizzesResponse(server)

            customRepository.getQuizzes(siteId).toList()

            server.takeRequest() // pass count request

            server.takeRequest().verifyContent(
                operationName = GetQuizzesQuery.OPERATION_NAME,
                token = token,
                expectedVariables = GetQuizzesGraphQLVariable(
                    limit = count,
                    offset = 0,
                    siteId = listOf(siteId.id)
                )
            )

            shouldThrow<Exception> {
                server.takeRequest() // Only one request to get quizzes
            }
        }

        for (pageSize in 1..23) {
            val customRepository = QuipoQuizRepositoryImpl(
                client = client,
                pageSize = pageSize
            )
            QuipoQuizSiteId.entries.forEach {
                verify(customRepository, pageSize, it)
            }
        }
    }

    should("getQuizzes returns a distinct list of quizzes by checking the id") {
        suspend fun verify(customRepository: QuipoQuizRepositoryImpl, siteId: QuipoQuizSiteId) {
            enqueueCountResponse(server, 3)
            server.enqueueGraphQLString(
                """
                        {
                          "data": {
                            "quizEntries": [
                              ${createEmptyQuizEntryString(1, "Quiz title 1")},
                              ${createEmptyQuizEntryString(1, "Quiz title 2")},
                              ${createEmptyQuizEntryString(2, "Quiz title 3")}
                            ]
                          }
                        }
                """.trimIndent()
            )
            customRepository.getQuizzes(siteId).toList() shouldContainExactly listOf(
                Quiz(
                    __typename = TYPENAME_QUIZ,
                    id = "1",
                    answer_type = "true_or_false",
                    language = "fr-CA",
                    title = "Quiz title 1",
                    quiz_category = emptyList(),
                    quiz_image = emptyList(),
                    questions_true_or_false = emptyList(),
                    questions_multiple_choices = emptyList()
                ),
                Quiz(
                    __typename = TYPENAME_QUIZ,
                    id = "2",
                    answer_type = "true_or_false",
                    language = "fr-CA",
                    title = "Quiz title 3",
                    quiz_category = emptyList(),
                    quiz_image = emptyList(),
                    questions_true_or_false = emptyList(),
                    questions_multiple_choices = emptyList()
                )
            )
        }

        val customRepository = QuipoQuizRepositoryImpl(
            client = client,
            pageSize = pageSize
        )
        QuipoQuizSiteId.entries.forEach {
            verify(customRepository, it)
        }
    }

    should("getQuizzes sends all requests to retrieve all quizzes when number of quizzes is even") {
        val countQuiz = 42
        val pageSize = 1
        val numberOfPages = 42
        suspend fun verify(customRepository: QuipoQuizRepositoryImpl, siteId: QuipoQuizSiteId) {
            enqueueCountResponse(server, countQuiz)
            repeat(numberOfPages) {
                server.enqueueGraphQLString(
                    """
                        {
                          "data": {
                            "quizEntries": [
                              ${createEmptyQuizEntryString(it)}
                            ]
                          }
                        }
                    """.trimIndent()
                )
            }
            customRepository.getQuizzes(siteId).toList().size shouldBe countQuiz

            server.takeRequest() // pass count request

            val requests = List(numberOfPages) {
                server.takeRequest()
            }.associateWith {
                it.parseBody<GetQuizzesGraphQLVariable>()
            }

            shouldThrow<Exception> {
                server.takeRequest() // Only one request to get quizzes
            }

            // Offset 0, 1, 2, 3, 4, ..., 40, 41 should be present
            requests.values.map { it.offset } shouldContainExactlyInAnyOrder List(numberOfPages) { it }

            requests.forEach { (request, variables) ->
                request.verifyContent(
                    operationName = GetQuizzesQuery.OPERATION_NAME,
                    token = token,
                    expectedVariables = GetQuizzesGraphQLVariable(
                        limit = 1,
                        offset = variables.offset,
                        siteId = listOf(siteId.id)
                    )
                )
            }
        }

        val customRepository = QuipoQuizRepositoryImpl(
            client = client,
            pageSize = pageSize
        )
        QuipoQuizSiteId.entries.forEach {
            verify(customRepository, it)
        }
    }

    should("getQuizzes sends all requests to retrieve all quizzes when number of quizzes is odd") {
        val countQuiz = 53
        val pageSize = 2
        val numberOfPages = 27
        suspend fun verify(customRepository: QuipoQuizRepositoryImpl, siteId: QuipoQuizSiteId) {
            enqueueCountResponse(server, countQuiz)
            var id = 0
            repeat(numberOfPages) {
                val quizzes = if (id == countQuiz - 1) {
                    """
                        ${createEmptyQuizEntryString(id++)}
                    """.trimIndent()
                } else {
                    """
                        ${createEmptyQuizEntryString(id++)},
                        ${createEmptyQuizEntryString(id++)}
                    """.trimIndent()
                }

                server.enqueueGraphQLString(
                    """
                        {
                          "data": {
                            "quizEntries": [
                              $quizzes
                            ]
                          }
                        }
                    """.trimIndent()
                )
            }
            customRepository.getQuizzes(siteId).toList().size shouldBe countQuiz

            server.takeRequest() // pass count request

            val requests = List(numberOfPages) {
                server.takeRequest()
            }.associateWith {
                it.parseBody<GetQuizzesGraphQLVariable>()
            }

            shouldThrow<Exception> {
                server.takeRequest() // Only one request to get quizzes
            }

            // Offset 0, 2, 4, 6, 8, ..., 50, 52 should be present
            requests.values.map { it.offset } shouldContainExactlyInAnyOrder List(numberOfPages) { it * pageSize }

            requests.forEach { (request, variables) ->
                request.verifyContent(
                    operationName = GetQuizzesQuery.OPERATION_NAME,
                    token = token,
                    expectedVariables = GetQuizzesGraphQLVariable(
                        limit = pageSize,
                        offset = variables.offset,
                        siteId = listOf(siteId.id)
                    )
                )
            }
        }

        val customRepository = QuipoQuizRepositoryImpl(
            client = client,
            pageSize = pageSize
        )
        QuipoQuizSiteId.entries.forEach {
            verify(customRepository, it)
        }
    }

    should("getQuizzes returns a list of quizzes") {
        val countQuiz = 200
        val pageSize = 2
        val numberOfPages = countQuiz / pageSize
        suspend fun verify(customRepository: QuipoQuizRepositoryImpl, siteId: QuipoQuizSiteId) {
            enqueueCountResponse(server, countQuiz)
            var id = 0
            repeat(numberOfPages) {
                server.enqueueGraphQLString(
                    """
                        {
                          "data": {
                            "quizEntries": [
                              ${createQuizEntryString(id++, siteId)},
                              ${createQuizEntryString(id++, siteId)}
                            ]
                          }
                        }
                    """.trimIndent()
                )
            }
            val quizzes = customRepository.getQuizzes(siteId).toList()

            quizzes shouldContainExactlyInAnyOrder List(countQuiz) {
                Quiz(
                    __typename = TYPENAME_QUIZ,
                    id = "$it",
                    answer_type = "true_or_false",
                    language = siteId.name,
                    title = "Quiz title $it",
                    quiz_category = listOf(
                        Quiz_category(
                            id = "category of quiz $it"
                        )
                    ),
                    quiz_image = listOf(
                        Quiz_image(
                            url = "image of quiz $it"
                        )
                    ),
                    questions_true_or_false = listOf(
                        Questions_true_or_false(
                            __typename = TYPENAME_QUESTION,
                            questionTrueOrFalse = QuestionTrueOrFalse(
                                __typename = TYPENAME_QUESTION,
                                id = "question 1 of quiz $it",
                                title = "title of question 1 of quiz $it",
                                answer = true,
                                anwser_explanation = "explanation of question 1 of quiz $it",
                                image = listOf(
                                    Image(
                                        url = "image of question 1 of quiz $it"
                                    )
                                )
                            )
                        ),
                        Questions_true_or_false(
                            __typename = TYPENAME_QUESTION,
                            questionTrueOrFalse = QuestionTrueOrFalse(
                                __typename = TYPENAME_QUESTION,
                                id = "question 2 of quiz $it",
                                title = "title of question 2 of quiz $it",
                                answer = false,
                                anwser_explanation = "explanation of question 2 of quiz $it",
                                image = listOf(
                                    Image(
                                        url = "image of question 2 of quiz $it"
                                    )
                                )
                            )
                        )
                    ),
                    questions_multiple_choices = listOf(
                        Questions_multiple_choice(
                            __typename = TYPENAME_QUESTION,
                            questionMultipleChoices = QuestionMultipleChoices(
                                __typename = TYPENAME_QUESTION,
                                id = "question 3 of quiz $it",
                                title = "title of question 3 of quiz $it",
                                anwser_explanation = "explanation of question 3 of quiz $it",
                                image = listOf(
                                    QuestionMultipleChoices.Image(
                                        url = "image of question 3 of quiz $it"
                                    )
                                ),
                                anwser_choices = listOf(
                                    QuestionMultipleChoices.Anwser_choice(
                                        __typename = TYPENAME_ANSWER_CHOICE,
                                        answerChoice = AnswerChoice(
                                            __typename = TYPENAME_ANSWER_CHOICE,
                                            choice = "choice 1 of question 3 of quiz $it",
                                            good_answer = false
                                        )
                                    ),
                                    QuestionMultipleChoices.Anwser_choice(
                                        __typename = TYPENAME_ANSWER_CHOICE,
                                        answerChoice = AnswerChoice(
                                            __typename = TYPENAME_ANSWER_CHOICE,
                                            choice = "choice 2 of question 3 of quiz $it",
                                            good_answer = true
                                        )
                                    )
                                )
                            )
                        ),
                        Questions_multiple_choice(
                            __typename = TYPENAME_QUESTION,
                            questionMultipleChoices = QuestionMultipleChoices(
                                __typename = TYPENAME_QUESTION,
                                id = "question 4 of quiz $it",
                                title = "title of question 4 of quiz $it",
                                anwser_explanation = "explanation of question 4 of quiz $it",
                                image = listOf(
                                    QuestionMultipleChoices.Image(
                                        url = "image of question 4 of quiz $it"
                                    )
                                ),
                                anwser_choices = listOf(
                                    QuestionMultipleChoices.Anwser_choice(
                                        __typename = TYPENAME_ANSWER_CHOICE,
                                        answerChoice = AnswerChoice(
                                            __typename = TYPENAME_ANSWER_CHOICE,
                                            choice = "choice 1 of question 4 of quiz $it",
                                            good_answer = true
                                        )
                                    ),
                                    QuestionMultipleChoices.Anwser_choice(
                                        __typename = TYPENAME_ANSWER_CHOICE,
                                        answerChoice = AnswerChoice(
                                            __typename = TYPENAME_ANSWER_CHOICE,
                                            choice = "choice 2 of question 4 of quiz $it",
                                            good_answer = false
                                        )
                                    )
                                )
                            )
                        )
                    )
                )
            }
        }

        val customRepository = QuipoQuizRepositoryImpl(
            client = client,
            pageSize = pageSize
        )
        QuipoQuizSiteId.entries.forEach {
            verify(customRepository, it)
        }
    }
})

private fun createCategoryEntryString(id: Int, title: String = "category $id"): String {
    return """
        {
          "__typename": "$TYPENAME_CATEGORY",
          "id": "$id",
          "color": "#f7e24c",
          "title": "$title",
          "language": "fr-CA",
          "image": [
            {
              "url": "image$id"
            }
          ],
          "icon": [
            {
              "url": "icon$id"
            }
          ]
        }
    """.trimIndent()
}

private fun createEmptyQuizEntryString(id: Int, title: String = "Quiz title $id"): String {
    return """
        {
          "__typename": "$TYPENAME_QUIZ",
          "id": "$id",
          "answer_type": "true_or_false",
          "language": "fr-CA",
          "title": "$title",
          "quiz_category": [
          ],
          "quiz_image": [
          ],
          "questions_true_or_false": [
          ],
          "questions_multiple_choices": [
          ]
        }
    """.trimIndent()
}

private fun createQuizEntryString(id: Int, siteId: QuipoQuizSiteId): String {
    return """
        {
          "__typename": "$TYPENAME_QUIZ",
          "id": "$id",
          "answer_type": "true_or_false",
          "language": "${siteId.name}",
          "title": "Quiz title $id",
          "quiz_category": [
            {
              "id": "category of quiz $id"
            }
          ],
          "quiz_image": [
            {
              "url": "image of quiz $id"
            }
          ],
          "questions_true_or_false": [
            {
              "__typename": "$TYPENAME_QUESTION",
              "id": "question 1 of quiz $id",
              "title": "title of question 1 of quiz $id",
              "answer": true,
              "anwser_explanation": "explanation of question 1 of quiz $id",
              "image": [
                {
                  "url": "image of question 1 of quiz $id"
                }
              ]
            },
            {
              "__typename": "$TYPENAME_QUESTION",
              "id": "question 2 of quiz $id",
              "title": "title of question 2 of quiz $id",
              "answer": false,
              "anwser_explanation": "explanation of question 2 of quiz $id",
              "image": [
                {
                  "url": "image of question 2 of quiz $id"
                }
              ]
            }
          ],
          "questions_multiple_choices": [
            {
              "__typename": "$TYPENAME_QUESTION",
              "id": "question 3 of quiz $id",
              "title": "title of question 3 of quiz $id",
              "answer": "answer of question 3 of quiz $id",
              "anwser_explanation": "explanation of question 3 of quiz $id",
              "image": [
                {
                  "url": "image of question 3 of quiz $id"
                }
              ],
              "anwser_choices": [
                {
                  "__typename": "$TYPENAME_ANSWER_CHOICE",
                  "choice": "choice 1 of question 3 of quiz $id",
                  "good_answer": false
                },
                {
                  "__typename": "$TYPENAME_ANSWER_CHOICE",
                  "choice": "choice 2 of question 3 of quiz $id",
                  "good_answer": true
                }
              ]
            },
            {
              "__typename": "$TYPENAME_QUESTION",
              "id": "question 4 of quiz $id",
              "title": "title of question 4 of quiz $id",
              "answer": "answer of question 4 of quiz $id",
              "anwser_explanation": "explanation of question 4 of quiz $id",
              "image": [
                {
                  "url": "image of question 4 of quiz $id"
                }
              ],
              "anwser_choices": [
                {
                  "__typename": "$TYPENAME_ANSWER_CHOICE",
                  "choice": "choice 1 of question 4 of quiz $id",
                  "good_answer": true
                },
                {
                  "__typename": "$TYPENAME_ANSWER_CHOICE",
                  "choice": "choice 2 of question 4 of quiz $id",
                  "good_answer": false
                }
              ]
            }
          ]
        }
    """.trimIndent()
}

private fun enqueueEmptyQuizzesResponse(server: MockServer) {
    server.enqueueGraphQLString(
        """
            {
                "data": {
                    "quizEntries": []
                }
            }
        """.trimIndent()
    )
}

private fun enqueueEmptyCategoriesResponse(server: MockServer) {
    server.enqueueGraphQLString(
        """
            {
                "data": {
                    "categoriesEntries": []
                }
            }
        """.trimIndent()
    )
}

private fun enqueueCountResponse(server: MockServer, count: Int) {
    server.enqueueGraphQLString(
        """
            {
                "data": {
                    "entryCount": $count
                }
            }
        """.trimIndent()
    )
}
