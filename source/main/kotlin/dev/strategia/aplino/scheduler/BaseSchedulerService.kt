package dev.strategia.aplino.scheduler

import org.knowm.sundial.SundialJobScheduler
import java.util.Date

/**
 * Base implementation for application scheduler service.
 * The internal implementation is based on [Sundial scheduler](https://knowm.org/open-source/sundial/)
 * library, which is stripped down version of the Quartz library.
 */
open class BaseSchedulerService : SchedulerService {
    protected var threadPoolSize = 1

    override fun addJob(name: String?, classToRun: Class<out SchedulerJob>) {
        val jobClass = classToRun.canonicalName
        SundialJobScheduler.addJob(name, jobClass)
    }

    override fun addJob(name: String?, classToRun: Class<out SchedulerJob>, params: Map<String, Any?>,
                        isConcurrencyAllowed: Boolean) {
        val jobClass = classToRun.canonicalName
        SundialJobScheduler.addJob(name, jobClass, params, isConcurrencyAllowed)
    }

    override fun getJobNames(): List<String> {
        return SundialJobScheduler.getAllJobNames()
    }

    override fun removeJob(name: String) {
        SundialJobScheduler.removeJob(name)
    }

    override fun addJobTrigger(name: String?, jobName: String, cronExpression: String?) {
        SundialJobScheduler.addCronTrigger(getTriggerName(name, jobName), jobName, cronExpression)
    }

    override fun addJobTrigger(name: String?, jobName: String, repeatCount: Int, repeatIntervalMs: Long,
                           startTime: Date?, endTime: Date?) {
        SundialJobScheduler.addSimpleTrigger(getTriggerName(name, jobName), jobName, repeatCount,
            repeatIntervalMs, startTime, endTime)
    }

    override fun getJobTriggers(name: String?): List<SchedulerJobTrigger> {
        val list: MutableList<SchedulerJobTrigger> = ArrayList()
        val jobTriggers = SundialJobScheduler.getAllJobsAndTriggers()
        for ((trigJobName, value) in jobTriggers) {
            if (name != null && name != trigJobName) {
                continue
            }
            for (trig in value) {
                val trigger = SchedulerJobTrigger(trig.name, trig.jobName)
                trigger.startTime = trig.startTime
                trigger.endTime = trig.endTime
                trigger.previousFireTime = trig.previousFireTime
                trigger.nextFireTime = trig.nextFireTime
                list.add(trigger)
            } //
        } //
        return list
    }

    override fun removeTrigger(name: String) {
        SundialJobScheduler.removeTrigger(name)
    }

    override fun startJob(name: String, params: Map<String, Any?>) {
        SundialJobScheduler.startJob(name, params)
    }

    override fun stopJob(name: String) {
        SundialJobScheduler.stopJob(name)
    }

    override fun isJobWorking(name: String): Boolean {
        return SundialJobScheduler.isJobRunning(name)
    }

    override fun getTriggerName(name: String?, jobName: String): String? {
        var sName = name
        if (name.isNullOrEmpty()) {
            sName = "Start$jobName"
        }
        return sName
    }

    override fun start() {
        SundialJobScheduler.startScheduler(threadPoolSize)
    }

    override fun stop() {
        SundialJobScheduler.shutdown()
    }

}
