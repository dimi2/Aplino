package dev.strategia.aplino.error

import java.text.SimpleDateFormat
import java.util.Date

/**
 * Base for all application exceptions, which contains detailed error information.
 */
abstract class BaseException : RuntimeException {
    companion object {
        /** Bug hunting instruction name.  */
        const val BUG_HUNTING_INSTRUCTION = "bugHunting"
    }

    var errorInfo: ErrorInfo? = null

    constructor(details: String) : super(details) {
        onInstanceCreation(null)
    }

    constructor(details: String, exception: Throwable?) : super(details, exception) {
        onInstanceCreation(exception)
    }

    constructor(errorInfo: ErrorInfo, exception: Throwable?) : super(errorInfo.details, exception) {
        this.errorInfo = errorInfo
    }

    /**
     * Called when exception instance is created. If system property '{@value BUG_HUNTING_INSTRUCTION}' is
     * provided, its value will be expected in format
     * 'exceptionClass:threadDumpFile:heapDumpFile'. Asterisk value uses the defaults. Examples:
     *
     * - "java.util.ConcurrentModificationException:*:*"- on ConcurrentModificationException
     * creation will generate default thread dump and memory dump files (with timestamp in their
     * names).
     * - "java.util.IOException:temp/latestThreadDump.txt:" - on IOException will generate thread
     * dump file with name 'latestThreadDump.txt' in subdirectory 'temp' (relative to current directory).
     *
     * - "*:threadDump.txt:" - On every base exception creation will generate thread dump into file
     * 'threadDump.txt' inside the current directory.
     *
     * @param cause The cause exception. Can be null.
     */
    protected fun onInstanceCreation(cause: Throwable?) {
        val bugHuntingInstruction = System.getProperty(BUG_HUNTING_INSTRUCTION)
        if (bugHuntingInstruction != null) {
            val instr = bugHuntingInstruction.split(":")
            val cClass = cause?.javaClass ?: javaClass
            if (instr[0] == "*" || cClass.canonicalName == instr[0]) {
                val df = SimpleDateFormat("yyyy-MM-ddThh:mm:ss")
                // Generate thread dump?
                val threadDumpFile: String
                if (instr[0].isNotEmpty()) {
                    if ("*".endsWith(instr[1])) {
                        threadDumpFile = "threadDump_" + df.format(Date()) + ".txt"
                    } else {
                        threadDumpFile = instr[1]
                    }
                    JvmDumper.createThreadDump(threadDumpFile)
                }
                // Generate heap dump?
                if (instr[2].isNotEmpty()) {
                    val heapDumpFile: String
                    if ("*".endsWith(instr[2])) {
                        heapDumpFile = "heapDump_" + df.format(Date()) + ".hprof"
                    } else {
                        heapDumpFile = instr[2]
                    }
                    JvmDumper.createHeapDump(heapDumpFile, true)
                }
            }
        }
    }

    override fun toString(): String {
        return errorInfo?.toString() ?: ("" + message)
    }
}
