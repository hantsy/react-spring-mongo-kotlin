package com.example.demo

import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.context.annotation.Import
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.ReadOnlyProperty
import org.springframework.data.mongodb.core.ReactiveMongoOperations
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.DocumentReference
import org.springframework.data.mongodb.core.remove

@DataMongoTest
@Import(TestDemoApplication::class)
class PostRepositoryTest {
    companion object {
        private val log = LoggerFactory.getLogger(PostRepositoryTest::class.java)
    }

    @Autowired
    lateinit var template: ReactiveMongoOperations

    @BeforeEach
    fun setup() = runTest {
        val deletedComments = template.remove<Comment>().all().awaitSingle().deletedCount
        log.debug("deleting comments: $deletedComments")

        val deletedPost = template.remove<Post>().all().awaitSingle().deletedCount
        log.debug("deleting posts: $deletedPost")
    }

    @Test
    fun `save and find posts`() = runTest {
        val post = template.save(Post(title = "test posts", body = "test body")).awaitSingle()
        val comment = template.save(Comment(body = "test comment", post = post.id)).awaitSingle()

        log.debug("found comment: $comment")
        log.debug("related post: ${comment?.post}")

//        template.update(Post::class.java)
//            .matching(where("id").isEqualTo(post.id))
//            .apply(Update().push("comments", comment))
//            .allAndAwait()

        val foundPost = template.findById(post.id!!, Post::class.java)
            .awaitFirst()
        log.debug("found post: $foundPost")
        log.debug("comments of post: ${foundPost.comments}")
    }
}


@Document
data class Post(
    @field:Id
    val id: String? = null,
    var title: String,
    var body: String,

    @field:ReadOnlyProperty
    @field:DocumentReference(lookup = "{'post':?#{#self._id} }" )
    // @field:DocumentReference()
    var comments:  List<Comment>? = emptyList()
)

@Document
data class Comment(
    @field:Id
    val id: String? = null,
    var body: String,

    //@field:DocumentReference(lazy =true)
    val post: String? = null
)