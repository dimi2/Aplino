package dev.strategia.aplino.scheduler

import org.knowm.sundial.Job
import org.knowm.sundial.exceptions.JobInterruptException

/**
 * Represents a scheduler job, which can be executed at some future moment.
 * This abstracts from the scheduler library specific implementation.
 */
abstract class SchedulerJob : Job() {

    /**
     * Execute the scheduled job action.
     * @param jobParams Job context parameters.
     */
    abstract fun execute(jobParams: Map<String, Any?>)

    @Throws(JobInterruptException::class)
    override fun doRun() {
        execute(jobContext.map)
    }
}
