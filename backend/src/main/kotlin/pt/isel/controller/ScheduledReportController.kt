package pt.isel.controller

import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import pt.isel.domain.schedule.ScheduledReport
import pt.isel.domain.schedule.ScheduledReportJob
import pt.isel.entity.schedule.ScheduledReportEntity
import pt.isel.model.CreateScheduleReportDTO
import pt.isel.security.principal.UserPrincipal
import pt.isel.service.ScheduledReportService
import pt.isel.utils.Failure
import pt.isel.utils.Success

@CrossOrigin(origins = ["http://localhost:8081"])
@RestController
@RequestMapping("/api/private/schedule")
class ScheduledReportController(
    private val scheduledReportService: ScheduledReportService,
) {
    @PostMapping("/create")
    fun createSchedule(
        @AuthenticationPrincipal principal: UserPrincipal,
        @RequestBody dto: CreateScheduleReportDTO<*>
    ): ResponseEntity<ScheduledReport<*,*>> {
        return when(val scheduledReport = scheduledReportService.createScheduledReport(dto, principal.getUserId())){
            is Success -> ResponseEntity.ok(scheduledReport.right)
            is Failure -> ResponseEntity.badRequest().build()
        }
    }

    @GetMapping("/get")
    fun getSchedules(
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<List<ScheduledReport<*, out ScheduledReportEntity<*, *>>>> {
        return when(val scheduledReports = scheduledReportService.getUserScheduledReports(principal.getUserId())){
            is Success -> ResponseEntity.ok(scheduledReports.right)
            is Failure -> ResponseEntity.badRequest().build()
        }
    }

    @GetMapping("/get-job")
    fun getJobs(
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<List<List<ScheduledReportJob>>> {
        return when(val scheduledReports = scheduledReportService.getUserScheduledJobs(principal.getUserId())){
            is Success -> ResponseEntity.ok(scheduledReports.right)
            is Failure -> ResponseEntity.badRequest().build()
        }
    }
}