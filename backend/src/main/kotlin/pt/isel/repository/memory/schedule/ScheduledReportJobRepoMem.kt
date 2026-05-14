package pt.isel.repository.memory.schedule

import org.springframework.stereotype.Repository
import pt.isel.domain.schedule.ScheduledReport
import pt.isel.domain.schedule.ScheduledReportJob
import pt.isel.repository.IScheduledReportJobRepository
import java.util.concurrent.atomic.AtomicInteger

@Repository
class ScheduledReportJobRepoMem : IScheduledReportJobRepository {
    private val idCounter = AtomicInteger(0)
    private val scheduledReportJobs = mutableMapOf<Int, ScheduledReportJob>()

    override fun create(entity: ScheduledReportJob): ScheduledReportJob =
        entity.copy(id = nextId()).also{ scheduledReportJob ->
            scheduledReportJobs[scheduledReportJob.id] = scheduledReportJob
        }

    override fun read(id: Int): ScheduledReportJob? = scheduledReportJobs[id]

    override fun readIncompleteJobs(): List<ScheduledReportJob> =
        scheduledReportJobs.values.filter { !it.state.isComplete }

    override fun readAll(): List<ScheduledReportJob> = scheduledReportJobs.values.toList()

    override fun update(entity: ScheduledReportJob): ScheduledReportJob? {
        return if(scheduledReportJobs.containsKey(entity.id)){
            entity.also{ scheduledReportJobs[entity.id] = entity }
        } else null
    }

    override fun delete(id: Int): ScheduledReportJob? = scheduledReportJobs.remove(id)

    fun currId(): Int = idCounter.get()
    fun nextId(): Int = idCounter.getAndIncrement()
}