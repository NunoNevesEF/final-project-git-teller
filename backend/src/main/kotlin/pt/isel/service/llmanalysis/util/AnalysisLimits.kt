package pt.isel.service.llmanalysis.util

import pt.isel.domain.CommitAnalysisRequest

data class AnalysisLimits(
    val maxFiles: Int,
    val maxCharsPerFile: Int,
) {
    companion object {
        fun fromSingle(request: CommitAnalysisRequest) =
            AnalysisLimits(request.maxFiles.coerceIn(1, 20), request.maxCharsPerFile.coerceIn(500, 5000))

        fun fromBatch(maxFilesPerCommit: Int, maxCharsPerFile: Int) =
            AnalysisLimits(maxFilesPerCommit.coerceIn(1, 20), maxCharsPerFile.coerceIn(500, 5000))
    }
}