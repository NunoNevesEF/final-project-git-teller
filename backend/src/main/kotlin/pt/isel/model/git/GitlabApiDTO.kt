package pt.isel.model.git

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Instant

//TODO: DOCUMENT

data class GitLabProjectDTO(
    val id: Int,
    val name: String,
    @JsonProperty("name_with_namespace") val nameWithNamespace : String,
    @JsonProperty("web_url") val webUrl : String,
    val description: String?,

    @JsonProperty("visibility") val visibility: GitLabVisibility,

    @JsonProperty("star_count") val starCount: Int,
    @JsonProperty("forks_count") val forksCount: Int,
    @JsonProperty("updated_at") val updatedAt: String
){
    fun toSummary(language: String) = RepositorySummary(
        id = id.toLong(),
        name = name,
        fullName = nameWithNamespace,
        htmlUrl = webUrl,
        description = description,
        private = visibility != GitLabVisibility.PUBLIC,
        language = language,
        starsCount = starCount,
        forksCount = forksCount,
        updatedAt = Instant.parse(updatedAt)
    )
}

typealias GitLabProjectLanguagesDTO = Map<String, Double>

fun GitLabProjectLanguagesDTO.mainLanguage(): String =
    maxByOrNull { it.value }?.key ?: "None"

enum class GitLabVisibility {
    @JsonProperty("private") PRIVATE,
    @JsonProperty("internal") INTERNAL,
    @JsonProperty("public") PUBLIC
}