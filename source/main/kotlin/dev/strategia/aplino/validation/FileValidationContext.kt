package dev.strategia.aplino.validation

/**
 * File validation context holder.
 */
open class FileValidationContext : ValidationContext() {
    /** Currently validated row number.  */
    var currentRow: Int = 0
    /** Currently validated column name.  */
    var currentColumn: String? = null
    /** Currently validated file offset (binary files has no rows and columns).  */
    var currentOffset: Long = 0
    /** Currently validated file name.  */
    var currentFile: String? = null
}
