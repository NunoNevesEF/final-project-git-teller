package pt.isel.model.git

import org.springframework.http.HttpStatus

//TODO: DOCUMENT

sealed class GitProviderServiceError{
    abstract val status: HttpStatus
}

object RepositoryNotFoundError : GitProviderServiceError() {
    override val status = HttpStatus.NOT_FOUND
}

object InvalidProviderTokenError : GitProviderServiceError() {
    override val status = HttpStatus.FORBIDDEN
}

object RateLimitError : GitProviderServiceError() {
    override val status = HttpStatus.TOO_MANY_REQUESTS
}

object NetworkError : GitProviderServiceError() {
    override val status = HttpStatus.SERVICE_UNAVAILABLE
}

object LinkedAccountNotFoundError : GitProviderServiceError() {
    override val status = HttpStatus.NOT_FOUND
}

object InvalidProviderError : GitProviderServiceError() {
    override val status = HttpStatus.BAD_REQUEST
}