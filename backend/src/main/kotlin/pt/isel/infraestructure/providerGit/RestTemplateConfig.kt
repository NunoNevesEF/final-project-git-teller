package pt.isel.infraestructure.providerGit

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate

//TODO: DOCUMENT

@Configuration
class RestTemplateConfig {
    @Bean
    fun restTemplate() = RestTemplate()
}