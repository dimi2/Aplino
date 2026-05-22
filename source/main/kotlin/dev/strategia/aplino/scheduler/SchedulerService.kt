package dev.strategia.aplino.scheduler

import dev.strategia.aplino.application.AppService
import java.util.Date

/**
 * Interface for application startup service.
 * It schedules actions (jobs) for future execution.
 */
interface SchedulerService : AppService {

    fun addJob(name: String?, classToRun: Class<out SchedulerJob>)

    fun addJob(name: String?, classToRun: Class<out SchedulerJob>, params: Map<String, Any?>,
               isConcurrencyAllowed: Boolean)

    fun getJobNames(): List<String>

    fun removeJob(name: String)

    fun addJobTrigger(name: String?, jobName: String, cronExpression: String?)

    fun addJobTrigger(name: String?, jobName: String, repeatCount: Int, repeatIntervalMs: Long,
                           startTime: Date? = null, endTime: Date? = null)

    fun getJobTriggers(name: String?): List<SchedulerJobTrigger>

    fun removeTrigger(name: String)

    fun startJob(name: String, params: Map<String, Any?>)

    fun stopJob(name: String)

    fun isJobWorking(name: String): Boolean

    fun getTriggerName(name: String?, jobName: String): String?
}
