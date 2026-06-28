package pt.isel.domain.report

data class AnalysisRequestWrapper(
    val flag: Boolean,
    val byShas: CommitShasAnalysisRequest?,
    val byDetailedSettings: CommitDetailedSettingsAnalysisRequest?
)