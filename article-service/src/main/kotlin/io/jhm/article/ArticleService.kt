package io.jhm.article

import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.netflix.eureka.EnableEurekaClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.rest.core.annotation.RepositoryRestResource
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo
import org.springframework.http.ResponseEntity
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.web.bind.annotation.*
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.patchForObject
import java.util.*
import javax.persistence.*
import javax.validation.groups.Default

@EnableEurekaClient
@SpringBootApplication
class ArticleService

fun main(args: Array<String>) {
	runApplication<ArticleService>(*args)

}

@Configuration
class RootConfig{

	@Bean
	fun restTemplate(): RestTemplate {
		return RestTemplate(HttpComponentsClientHttpRequestFactory())
	}

}

@RestController
class ArticleController(
	private val repo: ArticleRepository,
	private val rest: RestTemplate
) {

	@PostMapping("/articles")
	fun create(@RequestBody article: Article): ResponseEntity<Article> {
		repo.save(article)
		val rel = linkTo(ArticleController::class.java).slash(article.id).withSelfRel()

		this.sendArticleCount(article.author.id!!)

		return ResponseEntity.created(rel.toUri()).body(article)
	}

	private fun sendArticleCount(authorId: String) {
		val articleCount = repo.accountByAuthor(authorId)
		rest.patchForObject<String>("http://account-service/$authorId/article-count", mapOf("articleCount" to articleCount))
	}


}

@RepositoryRestResource(path = "articles")
interface ArticleRepository: JpaRepository<Article, String> {

	@Query("select count(a) from Article a where a.author.id = :authorId")
	fun accountByAuthor(authorId: String): String

}

@Entity
@Table(name = "ARTICLES")
data class Article(

	@Id
	val id: String? = null,

	val title: String? = null,

	@Lob
	val content: String? = null,

	@Embedded
	@AttributeOverrides(
		value = [
			AttributeOverride(name = "id", column = Column(name = "AUTHOR_ID")),
			AttributeOverride(name = "name", column = Column(name = "AUTHOR_NAME"))
		]
	)
	val author: Account = Account(),

	@CreationTimestamp
	val createdAt: Date? = Date(),

	@UpdateTimestamp
	val updatedAt: Date? = Date()

)

@Embeddable
data class Account(

	val id: String? = null,

	val name: String? = null

) {
	constructor(): this("TEST-ACCOUNTS-001", "조쉬홍")
}

object ValidationGroups {
	interface Create : Default
}
