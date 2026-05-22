package dev.strategia.aplino.scheduler

import java.io.Serializable
import java.util.Date

/**
 * Represents a trigger/start condition for scheduler job execution.
 * This abstracts the scheduler library specific implementation.
 */
open class SchedulerJobTrigger() : Serializable {
    protected var name: String? = null
    protected var jobName: String? = null
    var startTime: Date? = null
    var endTime: Date? = null
    var previousFireTime: Date? = null
    var nextFireTime: Date? = null

    constructor(name: String?, jobName: String?) : this() {
        this.name = name
        this.jobName = jobName
    }

    override fun toString(): String {
        return "{name='$name', jobName='$jobName'}"
    }
}
