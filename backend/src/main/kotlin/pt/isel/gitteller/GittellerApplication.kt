package pt.isel.gitteller

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication(scanBasePackages = ["pt.isel"], exclude = [SecurityAutoConfiguration::class])
@EnableScheduling
class GitTellerApplication


fun main(args: Array<String>) {
    runApplication<GitTellerApplication>(*args)
}