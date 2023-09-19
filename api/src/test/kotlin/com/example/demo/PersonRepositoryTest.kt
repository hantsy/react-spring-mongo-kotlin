package com.example.demo

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageRequest
import java.time.LocalDate

@DataMongoTest
@Import(TestDemoApplication::class)
class PersonRepositoryTest {
    companion object {
        private val log = LoggerFactory.getLogger(PersonRepositoryTest::class.java)
    }

    @Autowired
    lateinit var persons: PersonRepository

    @BeforeEach
    fun setup() = runTest {
        persons.deleteAll()
    }

    @Test
    fun `save and find persons`() = runTest {
        val person = persons.save(
            Person(
                firstName = "foo",
                lastName = "bar",
                birthOfData = LocalDate.now().minusYears(30),
                email = Email("foo@example.com"),
                address = Address(
                    line1 = "test line 1",
                    city = "NY",
                    zipCode = "12345"
                )
            )
        )

        log.debug("saved person: $person")
        person.id shouldNotBe null

        val found = persons.findById(person.id!!)!!
        found shouldNotBe null
        found.firstName shouldBe person.firstName
        found.lastName shouldBe person.lastName

        val found2 = persons.findByFirstNameLikeIgnoreCaseOrLastNameLikeIgnoreCaseOrEmailLikeIgnoreCase(
            ".*f.*",
            PageRequest.of(0, 10)
        )
        found2.count() shouldBe 1

        val found3 = persons.findByFirstNameLikeIgnoreCaseOrLastNameLikeIgnoreCaseOrEmailLikeIgnoreCase(
            ".*test.*",
            PageRequest.of(0, 10)
        )
        found3.count() shouldBe 0

        val found4 = persons.findByFirstNameLikeIgnoreCaseOrLastNameLikeIgnoreCaseOrEmailLikeIgnoreCase(
            ".*@example.*",
            PageRequest.of(0, 10)
        )
        found4.count() shouldBe 1

    }
}