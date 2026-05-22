package dev.strategia.aplino.error

import com.sun.management.HotSpotDiagnosticMXBean
import java.io.FileWriter
import java.io.Writer
import java.lang.management.ManagementFactory
import java.lang.management.ThreadInfo
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Creator of heap dumps.
 * Note: This uses internal JVM Api.
 */
open class JvmDumper {
    companion object {
        private const val HOTSPOT_BEAN_NAME = "com.sun.management:type=HotSpotDiagnostic"
        @Volatile
        private var hotSpotBean: HotSpotDiagnosticMXBean? = null
        private val checkScheduler = Executors.newScheduledThreadPool(1)

        /**
         * Dump the JVM heap (memory) into a file.
         * @param fileName Output file name.
         * @param live Include live data.
         */
        fun createHeapDump(fileName: String, live: Boolean) {
            initHotSpotMBean()
            try {
                hotSpotBean!!.dumpHeap(fileName, live)
            } catch (exp: Exception) {
                throw RuntimeException(exp)
            }
        }

        /**
         * Dump the current JVM threads into a file.
         * @param fileName Output file name.
         */
        fun createThreadDump(fileName: String) {
            try {
                FileWriter(fileName).use { dumpFile ->
                    val threadDump = getThreadDump()
                    dumpFile.write(threadDump)
                }
            } catch (ex: Exception) {
                throw RuntimeException(ex)
            }
        }

        /**
         * Detect thread deadlocks in the currently running JVM.
         * @param fileName Output file name.
         */
        fun createDeadlocksDump(fileName: String) {
            try {
                FileWriter(fileName).use { dumpFile ->
                    // The check should be executed in separate thread.
                    val dumpResult = checkScheduler.submit { dumpDeadlocks(dumpFile) }
                    dumpResult.get(2, TimeUnit.SECONDS)
                }
            } catch (ex: Exception) {
                throw RuntimeException(ex)
            }
        }

        protected fun dumpDeadlocks(writer: Writer, finished: AtomicBoolean? = null) {
            val mx = ManagementFactory.getThreadMXBean()
            val threadIds = mx.findDeadlockedThreads()
            if (threadIds != null) {
                val dump = StringBuilder()
                val threadInfos = mx.getThreadInfo(threadIds)
                for (threadInfo in threadInfos) {
                    dump.append(threadInfoToString(threadInfo))
                } //
                try {
                    writer.write(dump.toString())
                } catch (ex: Exception) {
                    throw RuntimeException(ex)
                } finally {
                    finished?.set(true)
                }
            }
        }

        protected fun getThreadDump(): String {
            val result: String
            val threadMXBean = ManagementFactory.getThreadMXBean()
            val threadInfos = threadMXBean.dumpAllThreads(true, true)
            val dump = StringBuilder()
            for (threadInfo in threadInfos) {
                dump.append(threadInfoToString(threadInfo))
            } //
            result = dump.toString()
            return result
        }

        protected fun threadInfoToString(threadInfo: ThreadInfo): String {
            val str = StringBuilder(512)
            str.append('"').append(threadInfo.threadName).append("\" ")
            str.append("\n   java.lang.Thread.State: ").append(threadInfo.threadState)
            for (stackTraceElement in threadInfo.stackTrace) {
                str.append("\n        at ").append(stackTraceElement)
            }
            str.append("\n\n")
            return str.toString()
        }

        private fun initHotSpotMBean() {
            if (hotSpotBean == null) {
                synchronized(JvmDumper::class.java) {
                    if (hotSpotBean == null) {
                        hotSpotBean = getHotSpotMBean()
                    }
                }
            }
        }

        private fun getHotSpotMBean(): HotSpotDiagnosticMXBean {
            try {
                val server = ManagementFactory.getPlatformMBeanServer()
                return ManagementFactory.newPlatformMXBeanProxy(server,
                    HOTSPOT_BEAN_NAME, HotSpotDiagnosticMXBean::class.java)
            } catch (exp: Exception) {
                throw RuntimeException(exp)
            }
        }

    }
}
