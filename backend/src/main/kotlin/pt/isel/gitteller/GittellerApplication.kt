package pt.isel.gitteller

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["pt.isel"])
class GitTellerApplication

fun main(args: Array<String>) {
	runApplication<GitTellerApplication>(*args)
}
