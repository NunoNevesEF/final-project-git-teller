package pt.isel.gitteller

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration

@SpringBootApplication(scanBasePackages = ["pt.isel"], exclude = [SecurityAutoConfiguration::class])
class GitTellerApplication


fun main(args: Array<String>) {
	runApplication<GitTellerApplication>(*args)
}
