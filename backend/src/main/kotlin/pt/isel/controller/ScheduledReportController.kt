package pt.isel.controller

import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import pt.isel.domain.schedule.ScheduledReportJob
import pt.isel.model.scheduledReport.CreateScheduleReportDTO
import pt.isel.model.scheduledReport.GetScheduledReportDTO
import pt.isel.infraestructure.principal.UserPrincipal
import pt.isel.service.ScheduledReportService
import pt.isel.utils.Failure
import pt.isel.utils.Success
import java.net.URI

@CrossOrigin(origins = ["http://localhost:8081"])
@RestController
@RequestMapping("/api/private/schedule")
class ScheduledReportController(
    private val scheduledReportService: ScheduledReportService,
) {
    @PostMapping("/create")
    fun createScheduledReport(
        @AuthenticationPrincipal principal: UserPrincipal,
        @RequestBody dto: CreateScheduleReportDTO<*>
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
        return when(val scheduledReports = scheduledReportService.getUserScheduledReports(principal.getUserId())){
            is Success -> ResponseEntity.ok(scheduledReports.right)
            is Failure -> ResponseEntity.badRequest().build()
        }
    }

    @GetMapping("/get-job")
    fun getUserJobs(
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<List<List<ScheduledReportJob>>> {
        return when(val scheduledReports = scheduledReportService.getUserScheduledJobs(principal.getUserId())){
            is Success -> ResponseEntity.ok(scheduledReports.right)
            is Failure -> ResponseEntity.badRequest().build()
        }
    }
}