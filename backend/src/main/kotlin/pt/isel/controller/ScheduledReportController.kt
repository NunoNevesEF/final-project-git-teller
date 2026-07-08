package pt.isel.controller

import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import pt.isel.model.scheduledReport.CreateScheduleReportDTO
import pt.isel.model.scheduledReport.GetScheduledReportDTO
import pt.isel.infraestructure.principal.UserPrincipal
import pt.isel.model.scheduledReport.ScheduledReportJobListItemDTO
import pt.isel.service.ScheduledReportNotFound
import pt.isel.service.ScheduledReportService
import pt.isel.utils.Failure
import pt.isel.utils.Success
import java.net.URI

@CrossOrigin(origins = ["https://frontend-production-fc0c.up.railway.app"])
@RestController
@RequestMapping("/api/private/schedule")
class ScheduledReportController(
    private val scheduledReportService: ScheduledReportService,
) {
    @PostMapping("/create")
    fun createScheduledReport(
        @AuthenticationPrincipal principal: UserPrincipal,
        @RequestBody dto: CreateScheduleReportDTO
    ): ResponseEntity<Int> {
        return when(val id = scheduledReportService.createScheduledReport(dto, principal.getUserId())){
            is Success -> ResponseEntity.created(URI("/api/private/schedule/get/${id.right}")).body(id.right)
            is Failure -> ResponseEntity.badRequest().build()
        }
    }

    @GetMapping("/get")
    fun getUserScheduledReports(
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<List<GetScheduledReportDTO>> {
        return ResponseEntity.ok(scheduledReportService.getUserScheduledReports(principal.getUserId()))
    }

    @PostMapping("/pause-schedule/{scheduledReportId}")
    fun pauseScheduledReport(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable scheduledReportId: Int,
    ): ResponseEntity<Void>{
        return when(val pauseResult = scheduledReportService.pauseReport(scheduledReportId, principal.getUserId())){
            is Failure -> when(pauseResult.left){
                is ScheduledReportNotFound -> ResponseEntity.notFound().build()
                else -> ResponseEntity.badRequest().build()
            }
            else -> ResponseEntity.noContent().build()
        }
    }

    @PostMapping("/resume-schedule/{scheduledReportId}")
    fun resumeScheduledReport(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable scheduledReportId: Int,
    ): ResponseEntity<Void>{
        return when(val resumeResult = scheduledReportService.resumeReport(scheduledReportId, principal.getUserId())){
            is Failure -> when(resumeResult.left){
                is ScheduledReportNotFound -> ResponseEntity.notFound().build()
                else -> ResponseEntity.badRequest().build()
            }
            else -> ResponseEntity.noContent().build()
        }
    }

    @PostMapping("/delete-schedule/{scheduledReportId}")
    fun deleteScheduledReport(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable scheduledReportId: Int,
    ): ResponseEntity<Void>{
        return when(scheduledReportService.deleteReport(scheduledReportId, principal.getUserId())){
            is Success -> ResponseEntity.noContent().build()
            is Failure -> ResponseEntity.notFound().build()
        }
    }

    @GetMapping("/get-queued-jobs")
    fun getUserQueuedJobs(
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<List<ScheduledReportJobListItemDTO>> {
        return ResponseEntity.ok(scheduledReportService.getUserQueuedJobs(principal.getUserId()))
    }
}