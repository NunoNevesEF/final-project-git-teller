package pt.isel.repository

import jakarta.inject.Named
import pt.isel.domain.GitCommunication

@Named
class GitCommunicationRepo {
    private val gitCommunications = mutableMapOf<String, GitCommunication>()

    fun getOrCreate(repoURI: String): GitCommunication{
        return gitCommunications.getOrPut(repoURI){
            GitCommunication.create(repoURI)
        }
    }
}