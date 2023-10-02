package com.example.demo

import jakarta.validation.ConstraintViolationException
import jakarta.validation.Validator
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Past
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import org.jetbrains.annotations.NotNull
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.data.annotation.Id
import org.springframework.data.domain.PageRequest
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.data.repository.kotlin.CoroutineSortingRepository
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.stereotype.Component
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.CorsConfigurationSource
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource
import org.springframework.web.reactive.function.server.*
import org.springframework.web.reactive.function.server.ServerResponse.*
import java.net.URI
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.jvm.optionals.getOrNull


@SpringBootApplication
class DemoApplication

fun main(args: Array<String>) {
    runApplication<DemoApplication>(*args)
}

@Component
@Profile("!prod")
class DataInitializer(private val persons: PersonRepository) : ApplicationRunner {
    companion object {
        private val log = LoggerFactory.getLogger(DataInitializer::class.java)
    }

    override fun run(args: ApplicationArguments) {
        runBlocking {
            persons.deleteAll()
            persons
                .saveAll(
                    listOf(
                        Person(
                            firstName = "foo",
                            lastName = "bar",
                            birthOfDate = LocalDate.now().minusYears(30),
                            email = Email("foo@example.com")
                        ),
                        Person(
                            firstName = "Java",
                            lastName = "Duck",
                            birthOfDate = LocalDate.now().minusYears(28),
                            email = Email("java@example.com")
                        )
                    )
                )
                .onCompletion { log.debug("data initialization is done at: {}", LocalDateTime.now()) }
                .collect()

            persons.findAll()
                .onEach {
                    log.debug("saved person: {}", it)
                }
                .collect()
        }
    }
}

@Configuration
class CorsConfig {

    @Bean
    fun corsConfiguration(): CorsConfigurationSource {
        val configuration = CorsConfiguration().applyPermitDefaultValues()
            .apply {
                allowedOrigins = listOf("localhost", "mytrustedwebsite.com")
                exposedHeaders = listOf(CorsConfiguration.ALL, HttpHeaders.AUTHORIZATION)
                allowCredentials = true
            }

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }
}

@Configuration
class RouterConfig {

    @Bean
    fun mainRouter(handler: PersonHandler) = coRouter {
        "persons".nest {
            GET("", handler::all)
            POST("", handler::create)
            "{id}".nest {
                GET("", handler::getById)
                PUT("", handler::update)
                DELETE("", handler::deleteById)
            }
        }
        filter(::handleValidationException)
    }

    private suspend fun handleValidationException(
        request: ServerRequest,
        next: suspend (ServerRequest) -> ServerResponse
    ): ServerResponse {
        return try {
            next(request)
        } catch (error: Exception) {
            if (error is ConstraintViolationException) {
                val errors = error.constraintViolations.map {
                    it.propertyPath.toString() to it.message
                }
                val detail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, error.message ?: "")
                detail.setProperty("errors", errors)
                val builder = status(HttpStatus.BAD_REQUEST)
                return builder.bodyValueAndAwait(detail)
            }

            val builder = status(HttpStatus.INTERNAL_SERVER_ERROR)
            error.printStackTrace()
            return builder.buildAndAwait()
        }
    }
}

@Component
class PersonHandler(private val persons: PersonRepository, private val validator: Validator) {
    companion object {
        private val log = LoggerFactory.getLogger(PersonHandler::class.java)
    }

    suspend fun all(req: ServerRequest): ServerResponse {
        val offset = req.queryParam("offset").getOrNull()?.toInt() ?: 0
        val limit = req.queryParam("limit").getOrNull()?.toInt() ?: 10
        log.debug("query params offset:$offset, limit: $limit")

        val query = req.queryParam("q").getOrNull() ?: run {
            return ok().bodyValueAndAwait(
                PaginatedResult(
                    persons.findAll().drop(offset).take(limit)
                        .map {
                            PersonSummary(
                                id = it.id,
                                name = "${it.firstName} ${it.lastName}",
                                email = it.email?.value,
                                birthOfDate = it.birthOfDate
                            )
                        }
                        .toList(),
                    persons.count()
                )
            )
        }
        log.debug("has extra query: $query")
        return ok().bodyValueAndAwait(
            PaginatedResult(
                persons
                    .findByFirstNameLikeIgnoreCaseOrLastNameLikeIgnoreCaseOrEmailLikeIgnoreCase(
                        ".*$query.*",
                        PageRequest.of(offset / limit, limit)
                    )
                    .map {
                        PersonSummary(
                            id = it.id,
                            name = "${it.firstName} ${it.lastName}",
                            email = it.email?.value,
                            birthOfDate = it.birthOfDate
                        )
                    }
                    .toList(),
                persons.countByFirstNameLikeIgnoreCaseOrLastNameLikeIgnoreCaseOrEmailLikeIgnoreCase(".*$query.*")
            )
        )
    }

    suspend fun create(req: ServerRequest): ServerResponse {
        val body = req.awaitBody(PersonFormData::class)
        val errors = validator.validate(body)
        if (errors.isNotEmpty()) {
            throw ConstraintViolationException("Input validation failed", errors)
        }
        val created = persons.save(
            Person(
                firstName = body.firstName,
                lastName = body.lastName,
                birthOfDate = body.birthOfDate,
                email = body.email?.let { Email(it) },
                phoneNumber = body.phoneNumber?.let { PhoneNumber(it) },
                address = body.address
            )
        )
        return created(URI.create("/persons/${created.id}")).buildAndAwait()
    }

    suspend fun getById(req: ServerRequest): ServerResponse {
        val id = req.pathVariables()["id"] ?: throw IllegalArgumentException("id is required.")
        return persons.findById(id)?.let {
            ok().bodyValueAndAwait(it)
        } ?: notFound().buildAndAwait()
    }

    suspend fun update(req: ServerRequest): ServerResponse {
        val id = req.pathVariables()["id"] ?: throw IllegalArgumentException("id is required.")

        val person = persons.findById(id) ?: run {
            return notFound().buildAndAwait()
        }
        val body = req.awaitBody(PersonFormData::class)
        val errors = validator.validate(body)
        if (errors.isNotEmpty()) {
            throw ConstraintViolationException("Input validation failed", errors)
        }
        val toUpdated = person.apply {
            firstName = body.firstName
            lastName = body.lastName
            birthOfDate = body.birthOfDate
            email = body.email?.let { Email(it) }
            phoneNumber = body.phoneNumber?.let { PhoneNumber(it) }
            address = body.address
        }
        persons.save(toUpdated)
        return noContent().buildAndAwait()
    }

    suspend fun deleteById(req: ServerRequest): ServerResponse {
        val id = req.pathVariables()["id"] ?: throw IllegalArgumentException("id is required.")
        persons.deleteById(id)
        return noContent().buildAndAwait()
    }
}

data class PaginatedResult<out T>(val data: List<T>, val count: Long)

data class PersonSummary(
    val id: String? = null,
    val name: String,
    val email: String? = null,
    val birthOfDate: LocalDate? = null
)

data class PersonFormData(
    @field:NotBlank
    val firstName: String,
    @field:NotBlank
    val lastName: String,
    @field:NotNull
    @field:Past
    val birthOfDate: LocalDate,

    @field:jakarta.validation.constraints.Email
    val email: String? = null,
    val phoneNumber: String? = null,
    val address: Address? = null
)

interface PersonRepository : CoroutineCrudRepository<Person, String>,
    CoroutineSortingRepository<Person, String> {

    @Query(
        """
       {
           ${'$'}or: [
                {'firstName': {${'$'}regex: ?0, ${'$'}options:'i' }},
                {'firstName': {${'$'}regex: ?0, ${'$'}options:'i' }}, 
                {'email': {${'$'}regex: ?0, ${'$'}options:'i' }}
            ]
       } 
    """
    )
    fun findByFirstNameLikeIgnoreCaseOrLastNameLikeIgnoreCaseOrEmailLikeIgnoreCase(
        query: String,
        of: PageRequest
    ): Flow<Person>

    @Query(
        value = """
       {
           ${'$'}or: [
                {'firstName': {${'$'}regex: ?0, ${'$'}options:'i' }},
                {'firstName': {${'$'}regex: ?0, ${'$'}options:'i' }}, 
                {'email': {${'$'}regex: ?0, ${'$'}options:'i' }}
            ]
       } 
    """,
        count = true
    )
    fun countByFirstNameLikeIgnoreCaseOrLastNameLikeIgnoreCaseOrEmailLikeIgnoreCase(
        query: String
    ): Long
}

@JvmInline
value class Email(val value: String)

@JvmInline
value class PhoneNumber(val value: String)

data class Address(
    val line1: String,
    val line2: String? = null,
    val street: String? = null,
    val city: String? = null,
    val zipCode: String? = null
)

@Document
data class Person(
    @Id
    val id: String? = null,
    var firstName: String,
    var lastName: String,
    var birthOfDate: LocalDate,
    var email: Email? = null,
    var phoneNumber: PhoneNumber? = null,
    var address: Address? = null
)