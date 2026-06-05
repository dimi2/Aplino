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

    constructor(errorInfo: ErrorInfo, exception: Throwable?) : super(errorInfo.formattedDetails(),
                exception) {
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
            val classSpec = instr.getOrElse(0) { "" }
            val cClass = cause?.javaClass ?: javaClass
            if (classSpec == "*" || cClass.canonicalName == classSpec) {
                // ':' is illegal in file names on some platforms, so keep the timestamp file-name safe.
                val df = SimpleDateFormat("yyyy-MM-dd'T'HH-mm-ss")
                // Generate thread dump (default name when unspecified or '*').
                val threadDumpSpec = instr.getOrElse(1) { "" }
                val threadDumpFile = if (threadDumpSpec.isEmpty() || threadDumpSpec == "*") {
                    "threadDump_" + df.format(Date()) + ".txt"
                } else {
                    threadDumpSpec
                }
                JvmDumper.createThreadDump(threadDumpFile)
                // Generate heap dump only if requested.
                val heapDumpSpec = instr.getOrElse(2) { "" }
                if (heapDumpSpec.isNotEmpty()) {
                    val heapDumpFile = if (heapDumpSpec == "*") {
                        "heapDump_" + df.format(Date()) + ".hprof"
                    } else {
                        heapDumpSpec
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
