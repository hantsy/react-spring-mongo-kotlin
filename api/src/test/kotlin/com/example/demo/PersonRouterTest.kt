package com.example.demo

import com.ninjasquad.springmockk.MockkBean
import io.mockk.coEvery
import io.mockk.coVerify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.equalTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration
import org.springframework.boot.autoconfigure.web.reactive.WebFluxAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerResponse
import java.time.LocalDate
import java.util.*

@SpringBootTest(classes = [PersonRouterTest.TestConfig::class])
class PersonRouterTest {

    @Configuration
    @Import(RouterConfig::class, PersonHandler::class)
    @ImportAutoConfiguration(
        WebFluxAutoConfiguration::class,
        ValidationAutoConfiguration::class
    )
    class TestConfig

    @Autowired
    private lateinit var routerFunction: RouterFunction<ServerResponse>

    private lateinit var client: WebTestClient

    @MockkBean
    lateinit var persons: PersonRepository

    @BeforeEach
    fun setup() {
        client = WebTestClient.bindToRouterFunction(routerFunction)
            .configureClient()
            .build()
    }

    @Test
    fun `get all persons`() = runTest {
        coEvery { persons.findAll() } returns flowOf(
            Person(
                id = UUID.randomUUID().toString(),
                firstName = "foo",
                lastName = "bar",
                birthOfDate = LocalDate.now().minusYears(20),
                email = Email("foo@example.com"),
                phoneNumber = PhoneNumber("+12223334444"),
                address = Address(
                    line1 = "test line1",
                    city = "NY",
                    zipCode = "12345"
                )
            )
        )
        client.get().uri("/persons")
            .exchange()
            .expectStatus().isOk
            .expectBody().jsonPath("$.[0].firstName", equalTo("foo"))

        coVerify(exactly = 1) { persons.findAll() }
    }

    @Test
    fun `get all persons with query params`() = runTest {
        coEvery {
            persons.findByFirstNameLikeIgnoreCaseOrLastNameLikeIgnoreCaseOrEmailLikeIgnoreCase(
                any(),
                any()
            )
        } returns flowOf(
            Person(
                id = UUID.randomUUID().toString(),
                firstName = "foo",
                lastName = "bar",
                birthOfDate = LocalDate.now().minusYears(20),
                email = Email("foo@example.com"),
                phoneNumber = PhoneNumber("+12223334444"),
                address = Address(
                    line1 = "test line1",
                    city = "NY",
                    zipCode = "12345"
                )
            )
        )
        client.get().uri { builder ->
            builder.path("/persons").queryParam("q", "foo")
                .queryParam("offset", 0)
                .queryParam("limit", 10)
                .build()
        }
            .exchange()
            .expectStatus().isOk
            .expectBody().jsonPath("$.[0].firstName", equalTo("foo"))

        coVerify(exactly = 1) {
            persons.findByFirstNameLikeIgnoreCaseOrLastNameLikeIgnoreCaseOrEmailLikeIgnoreCase(
                any(),
                any()
            )
        }
    }

    @Test
    fun `get person by id`() = runTest {
        val id = UUID.randomUUID().toString()
        coEvery { persons.findById(any()) } returns
                Person(
                    id = id,
                    firstName = "foo",
                    lastName = "bar",
                    birthOfDate = LocalDate.now().minusYears(20),
                    email = Email("foo@example.com"),
                    phoneNumber = PhoneNumber("+12223334444"),
                    address = Address(
                        line1 = "test line1",
                        city = "NY",
                        zipCode = "12345"
                    )
                )

        client.get().uri("/persons/$id")
            .exchange()
            .expectStatus().isOk
            .expectBody().jsonPath("$.firstName", equalTo("foo"))

        coVerify(exactly = 1) { persons.findById(any()) }
    }

    @Test
    fun `get none-existing person by id`() = runTest {
        val id = UUID.randomUUID().toString()
        coEvery { persons.findById(any()) } returns null

        client.get().uri("/persons/$id")
            .exchange()
            .expectStatus().isNotFound

        coVerify(exactly = 1) { persons.findById(any()) }
    }

    @Test
    fun `create person`() = runTest {
        val id = UUID.randomUUID().toString()
        coEvery { persons.save(any()) } returns
                Person(
                    id = id,
                    firstName = "foo",
                    lastName = "bar",
                    birthOfDate = LocalDate.now().minusYears(20),
                    email = Email("foo@example.com"),
                    phoneNumber = PhoneNumber("+12223334444"),
                    address = Address(
                        line1 = "test line1",
                        city = "NY",
                        zipCode = "12345"
                    )
                )

        val body = PersonFormData(
            firstName = "foo",
            lastName = "bar",
            birthOfDate = LocalDate.now().minusYears(20),
            email = "foo@example.com",
            phoneNumber = "+12223334444",
            address = Address(
                line1 = "test line1",
                city = "NY",
                zipCode = "12345"
            )
        )

        client.post().uri("/persons").bodyValue(body)
            .exchange()
            .expectStatus().isCreated
            .expectHeader().exists("Location")

        coVerify(exactly = 1) { persons.save(any()) }
    }

    @Test
    fun `create person with invalid data`() = runTest {
        val id = UUID.randomUUID().toString()
        coEvery { persons.save(any()) } returns
                Person(
                    id = id,
                    firstName = "foo",
                    lastName = "bar",
                    birthOfDate = LocalDate.now().minusYears(20),
                    email = Email("foo@example.com"),
                    phoneNumber = PhoneNumber("+12223334444"),
                    address = Address(
                        line1 = "test line1",
                        city = "NY",
                        zipCode = "12345"
                    )
                )

        val body = PersonFormData(
            firstName = "foo",
            lastName = "bar",
            birthOfDate = LocalDate.now().minusYears(20),
            email = "example.com", // invalid email
            phoneNumber = "+12223334444",
            address = Address(
                line1 = "test line1",
                city = "NY",
                zipCode = "12345"
            )
        )

        client.post().uri("/persons").bodyValue(body)
            .exchange()
            .expectStatus().isBadRequest // failed

        coVerify(exactly = 0) { persons.save(any()) } // verify it is not called
    }

    @Test
    fun `update person`() = runTest {
        val id = UUID.randomUUID().toString()
        coEvery { persons.findById(any()) } returns
                Person(
                    id = id,
                    firstName = "foo",
                    lastName = "bar",
                    birthOfDate = LocalDate.now().minusYears(20),
                    email = Email("foo@example.com"),
                    phoneNumber = PhoneNumber("+12223334444"),
                    address = Address(
                        line1 = "test line1",
                        city = "NY",
                        zipCode = "12345"
                    )
                )
        coEvery { persons.save(any()) } returns
                Person(
                    id = id,
                    firstName = "test",
                    lastName = "test",
                    birthOfDate = LocalDate.now().minusYears(20),
                    email = Email("foo@example.com"),
                    phoneNumber = PhoneNumber("+12223334444"),
                    address = Address(
                        line1 = "test line1",
                        city = "NY",
                        zipCode = "12345"
                    )
                )

        val body = PersonFormData(
            firstName = "foo",
            lastName = "bar",
            birthOfDate = LocalDate.now().minusYears(20),
            email = "foo@example.com",
            phoneNumber = "+12223334444",
            address = Address(
                line1 = "test line1",
                city = "NY",
                zipCode = "12345"
            )
        )

        client.put().uri("/persons/$id").bodyValue(body)
            .exchange()
            .expectStatus().isNoContent

        coVerify(exactly = 1) { persons.findById(any()) }
        coVerify(exactly = 1) { persons.save(any()) }
    }

    @Test
    fun `update person when not found`() = runTest {
        val id = UUID.randomUUID().toString()
        coEvery { persons.findById(any()) } returns null
        coEvery { persons.save(any()) } returns
                Person(
                    id = id,
                    firstName = "test",
                    lastName = "test",
                    birthOfDate = LocalDate.now().minusYears(20),
                    email = Email("foo@example.com"),
                    phoneNumber = PhoneNumber("+12223334444"),
                    address = Address(
                        line1 = "test line1",
                        city = "NY",
                        zipCode = "12345"
                    )
                )

        val body = PersonFormData(
            firstName = "foo",
            lastName = "bar",
            birthOfDate = LocalDate.now().minusYears(20),
            email = "foo@example.com",
            phoneNumber = "+12223334444",
            address = Address(
                line1 = "test line1",
                city = "NY",
                zipCode = "12345"
            )
        )

        client.put().uri("/persons/$id").bodyValue(body)
            .exchange()
            .expectStatus().isNotFound

        coVerify(exactly = 1) { persons.findById(any()) }
        coVerify(exactly = 0) { persons.save(any()) }
    }

}