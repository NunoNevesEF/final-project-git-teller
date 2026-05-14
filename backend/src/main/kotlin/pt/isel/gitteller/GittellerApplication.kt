package pt.isel.gitteller

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.persistence.autoconfigure.EntityScan
import org.springframework.boot.runApplication
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication(
    scanBasePackages = ["pt.isel"],
    exclude = [SecurityAutoConfiguration::class]
)
@EnableScheduling
@EnableJpaRepositories(basePackages = ["pt.isel.repository.jpa"])
@EntityScan(basePackages = ["pt.isel.entity"])
class GitTellerApplication


fun main(args: Array<String>) {
    runApplication<GitTellerApplication>(*args)
}