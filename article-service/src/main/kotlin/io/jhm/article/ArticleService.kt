package io.jhm.article

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.netflix.eureka.EnableEurekaClient
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table
import javax.validation.constraints.NotEmpty
import javax.validation.groups.Default

@EnableEurekaClient
@SpringBootApplication
class ArticleService

fun main(args: Array<String>) {
	runApplication<ArticleService>(*args)

}

@RestController
class ArticleController(
	private val repo: ArticleRepository
) {

	@GetMapping
	fun list(): List<Article> = repo.findAll()

}

interface ArticleRepository: JpaRepository<Article, String>

@Entity
@Table(name = "ARTICLES")
data class Article(

	@Id
	val id: String? = null,

	@get:NotEmpty(groups = [ValidationGroups.Create::class])
	val title: String? = null,

	@get:NotEmpty(groups = [ValidationGroups.Create::class])
	val content: String? = null,

	@get:NotEmpty(groups = [ValidationGroups.Create::class])
	val authorId: String? = null,

	@get:NotEmpty(groups = [ValidationGroups.Create::class])
	val authorName: String? = null,

	val createdAt: Date? = Date(),

	val updatedAt: Date? = Date()

)

object ValidationGroups {
	interface Create : Default
}
