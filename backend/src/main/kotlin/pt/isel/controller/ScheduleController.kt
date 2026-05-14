package pt.isel.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import pt.isel.domain.schedule.Failure
import pt.isel.domain.schedule.ScheduledReport
import pt.isel.domain.schedule.ScheduledReportJob
import pt.isel.domain.schedule.Success
import pt.isel.model.CreateScheduleReportDTO
import pt.isel.model.UserDTO
import pt.isel.service.ScheduledReportService
import pt.isel.utils.Either

@CrossOrigin(origins = ["http://localhost:8081"])
@RestController
@RequestMapping("/api/public/schedule")
class ScheduleController(
    private val scheduledReportService: ScheduledReportService,
) {
    @PostMapping("/create")
    fun createSchedule(
        @RequestBody dto: CreateScheduleReportDTO
    ): ResponseEntity<Any> {
        return when(val scheduledReport = scheduledReportService.createScheduledReport(dto)){
            is pt.isel.utils.Success -> ResponseEntity.ok(scheduledReport)
            is pt.isel.utils.Failure -> ResponseEntity.badRequest().build()
        }
    }

    @GetMapping("/schedules")
    fun getSchedules(): ResponseEntity<List<ScheduledReport>> {
        return ResponseEntity.ok(
            scheduledReportService.getAllSchedules()
        )
    }

    @GetMapping("/jobs")
    fun getJobs(): ResponseEntity<List<ScheduledReportJob>> {
        return ResponseEntity.ok(
            scheduledReportService.getAllJobs()
        )
    }
}