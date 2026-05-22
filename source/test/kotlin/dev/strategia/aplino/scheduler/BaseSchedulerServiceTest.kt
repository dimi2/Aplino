package dev.strategia.aplino.scheduler

import dev.strategia.aplino.TestBase
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime

internal class BaseSchedulerServiceTest : TestBase() {

    private lateinit var service: SchedulerService

    @BeforeEach
    fun begin() {
        service = BaseSchedulerService()
        service.start()
    }

    @AfterEach
    fun end() {
        service.stop()
    }

    private fun awaitCalls(expected: Int, timeoutMs: Long = 3000) {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (GreetingJob.getCalls() < expected && System.currentTimeMillis() < deadline) {
            Thread.sleep(10)
        } //
    }

    @Test
    fun simpleScheduling() {
        val jobName1 = "Start Greeting 1"
        val triggerName1 = "Greeting Starter"
        val repeatCount = 2
        val repeatIntervalMs: Long = 100
        GreetingJob.setCalls(0)
        service.addJob(jobName1, GreetingJob::class.java)
        service.addJobTrigger(triggerName1, jobName1, repeatCount, repeatIntervalMs, null, null)
        awaitCalls(1 + repeatCount)
        assertEquals(1 + repeatCount, GreetingJob.getCalls())
        val jobNames = service.getJobNames()
        service.removeJob(jobName1)
        val job1 = jobNames[0]
        assertEquals(jobName1, job1)
        assertEquals(1, jobNames.size)
        val triggers = service.getJobTriggers(null)
        assertEquals(0, triggers.size)
    }

    @Test
    fun explicitScheduling() {
        val jobName2 = "Start Greeting 2"
        service.addJob(jobName2, GreetingJob::class.java)
        val params = HashMap<String, Any?>()
        params[GreetingJob.NAME_PARAM] = "Haho"
        GreetingJob.setCalls(0)
        service.startJob(jobName2, params)
        Thread.sleep(100)
        assertEquals(1, GreetingJob.getCalls())
        service.stopJob(jobName2)
        val jobNames = service.getJobNames()
        assertEquals(1, jobNames.size)
        assertEquals(1, GreetingJob.getCalls())
    }

    @Test
    fun cronScheduling() {
        val jobName3 = "Start Greeting 3"
        service.addJob(jobName3, GreetingJob::class.java)
        GreetingJob.setCalls(0)
        val triggerName1 = "Trig"
        var startHour = OffsetDateTime.now().hour.minus(1)
        val endHour = startHour.plus(1)
        if (startHour < 0) {
            startHour = 0
        }
        service.addJobTrigger(triggerName1, jobName3, "* * $startHour,$endHour * * ?")
        Thread.sleep(100)
        val triggers = service.getJobTriggers(jobName3)
        assertEquals(1, triggers.size)
        service.removeTrigger(triggerName1)
        service.removeJob(jobName3)
        val jobNames = service.getJobNames()
        assertEquals(0, jobNames.size)
        assertTrue(GreetingJob.getCalls() > 0)
    }

}
