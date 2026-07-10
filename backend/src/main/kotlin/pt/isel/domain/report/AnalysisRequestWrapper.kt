package pt.isel.domain.report

//TODO: DOCUMENT

data class AnalysisRequestWrapper(
    val flag: Boolean,
    val byShas: CommitShasAnalysisRequest?,
    val byDetailedSettings: CommitDetailedSettingsAnalysisRequest?
)