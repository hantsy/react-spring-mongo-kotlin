package com.example.demo

import io.kotest.inspectors.forAny
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitEntity
import org.springframework.web.reactive.function.client.awaitExchange

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestDemoApplication::class)
class IntegrationTests {

    @LocalServerPort
    var port: Int = 0

    lateinit var client: WebClient

    @BeforeEach
    fun setup() {
        client = WebClient.builder().baseUrl("http://localhost:$port")
            .build()
    }

    @Test
    fun `get all persons`() = runTest {
        client.get().uri("/persons")
            .awaitExchange { clientResponse ->
                val entity = clientResponse.awaitEntity<List<PersonSummary>>()
                entity.statusCode shouldBe HttpStatus.OK
                entity.body!!.forAny { it.name shouldBe "foo" }
            }
    }

}
