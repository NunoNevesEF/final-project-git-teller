package pt.isel.repository.memory.schedule

import org.springframework.stereotype.Repository
import pt.isel.domain.schedule.ScheduledReport
import pt.isel.repository.IScheduledReportJobRepository
import pt.isel.repository.IScheduledReportRepository
import java.time.Duration
import java.time.Instant
import java.util.concurrent.atomic.AtomicInteger

@Repository
class ScheduledReportRepoMem(
    private val scheduledReportJobRepo: IScheduledReportJobRepository
): IScheduledReportRepository {
    private val idCounter = AtomicInteger(0)
    private val scheduledReports = mutableMapOf<Int, ScheduledReport>()


    override fun create(entity: ScheduledReport): ScheduledReport =
        entity.scheduledReportCopy(nextId()).also{ scheduledReport ->
            scheduledReports[scheduledReport.id] = scheduledReport
        }

    override fun read(id: Int): ScheduledReport? = scheduledReports[id]

    override fun readScheduledReportsByUser(userId: Int): List<ScheduledReport> =
        scheduledReports.values.filter{ it.userId == userId }

    override fun readPending(): List<ScheduledReport> {
        val limit = Instant.now().plus(Duration.ofMinutes(15))
        val activeJobs = scheduledReportJobRepo.readIncompleteJobs()

        return scheduledReports.values.filter{ scheduledReport ->
            val nextRun = scheduledReport.nextRun
            nextRun != null && nextRun <= limit && activeJobs.none{ it.scheduledReportId == scheduledReport.id }
        }.sortedBy{ it.nextRun }
    }

    override fun readAll(): List<ScheduledReport> = scheduledReports.values.toList()

    override fun update(entity: ScheduledReport): ScheduledReport? {
        return if(scheduledReports.containsKey(entity.id)){
            entity.also{ scheduledReports[entity.id] = entity }
        } else null
    }

    override fun delete(id: Int): ScheduledReport? = scheduledReports.remove(id)

    fun nextId(): Int = idCounter.getAndIncrement()
    fun currId(): Int = idCounter.get()
}