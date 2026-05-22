package dev.strategia.aplino.scheduler

import org.apache.logging.log4j.LogManager
import java.util.concurrent.atomic.AtomicInteger

/**
 * Test class for scheduler job.
 */
internal class GreetingJob : SchedulerJob() {
    private val log = LogManager.getLogger(GreetingJob::class.java)

    companion object {
        const val NAME_PARAM = "name"
        private val calls = AtomicInteger(0)

        fun setCalls(count: Int) {
            calls.set(count)
        }

        fun getCalls(): Int {
            return calls.get()
        }
    }

    override fun execute(jobParams: Map<String, Any?>) {
        val count = calls.incrementAndGet()
        val name = jobParams[NAME_PARAM] as String?
        val greeting = String.format("Hello %s (%d)", name, count)
        log.info("Scheduled greeting: $greeting")
    }

}
