package dev.strategia.aplino.util

import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.Locale
import java.util.regex.Pattern

/**
 * Handy methods for file operations:
 * - Get file name extension.
 * - Delete a directory even if it contains subdirectories and files.
 * - Select files matching specified mask, optionally including these from subdirectories.
 * - Move/delete file with single line of code.
 */
open class FileUtil {
    companion object {
        protected const val UP_DIR = ".."

        /**
         * Get file object for specified file name (the file must exist).
         * @param fileName File name.
         * @return Corresponding file object.
         * @throws IllegalArgumentException if the file does not exist.
         */
        fun getFile(fileName: String?): File {
            return getFile(fileName, true)
        }

        /**
         * Get file object for specified file name.
         * @param fileName File name. If the name is relative, it is considered against the working
         * (user) directory.
         * @param mustExist True if the file must exist.
         * @param relativeTo Parent directory to use, if the fileName represents relative path.
         * @return Corresponding file object.
         * @throws IllegalArgumentException if the file does not exist, but it must.
         */
        fun getFile(fileName: String?, mustExist: Boolean, relativeTo: String? = null): File {
            var file: File? = null
            if (fileName != null) {
                try {
                    file = File(fileName)
                    if (!file.isAbsolute) {
                        val parentDir = relativeTo ?: System.getProperty("user.dir", "")
                        file = File(parentDir, fileName)
                    }
                    file = file.canonicalFile
                } catch (e: Exception) {
                    throw IllegalStateException("Invalid file path for: $fileName", e)
                }
            }
            if (mustExist) {
                require(!(file == null || !file.isFile)) { "Missing file: $file" }
            } else {
                file!!.parentFile.mkdirs()
            }
            return file
        }

        /**
         * Get file object for specified directory name (the directory must exist).
         * @param dirName Directory name.
         * @return Corresponding file object.
         * @throws IllegalArgumentException if the directory does not exist.
         */
        fun getDirectory(dirName: String?): File {
            return getDirectory(dirName, true)
        }

        /**
         * Get file object for specified directory name.
         * @param dirName Directory name.
         * @param mustExist True if the directory must already exist.
         * @return Corresponding file object.
         * @throws IllegalArgumentException if the directory does not exist but it must.
         */
        fun getDirectory(dirName: String?, mustExist: Boolean): File {
            var dir: File? = null
            if (dirName != null) {
                try {
                    dir = File(dirName)
                    if (!dir.isAbsolute) {
                        dir = File(System.getProperty("user.dir", ""), dirName)
                    }
                    dir = dir.canonicalFile
                } catch (e: IOException) {
                    throw IllegalStateException("Invalid file path for: $dirName", e)
                }
            }
            if (mustExist) {
                require(!(dir == null || !dir.isDirectory)) { "Missing directory: $dir" }
            } else {
                dir!!.mkdirs()
            }
            return dir
        }

        /**
         * Collect files matching given mask. This is simpler than 'Files.walkFileTree'.
         * @param dir Directory to look at. Null means current directory.
         * @param mask File mask regex. Null means all files.
         * @param includeSubDirs True to include subdirectories in the search. By default, it is false.
         * @return List of matched files in alphabetical order.
         */
        fun collectFiles(dir: String?, mask: String?, includeSubDirs: Boolean): List<File> {
            // Validate the parameters.
            val fDir = File(dir ?: "").absoluteFile
            if (!fDir.isDirectory) {
                throw IllegalArgumentException("Invalid directory name '$fDir'")
            }

            // Normalize the file mask.
            var pattern: Pattern? = null
            if (mask != null) {
                pattern = Pattern.compile(mask)
            }

            // Collect matching files.
            val ret = ArrayList<File>()
            collectFiles(fDir, pattern, includeSubDirs, ret)

            // Sort the result.
            ret.sort()
            return ret
        }

        /**
         * Move a file. If the target file already exists it will be overwritten.
         * @param from The name of the file to be moved.
         * @param to The new file name.
         * @return True if the file was successfully moved.
         * @see #deleteFile
         */
        fun moveFile(from: String?, to: String?): Boolean {
            var ret = false
            if (from != null && to != null) {
                val fromFile = Paths.get(from)
                val toFile = Paths.get(to)
                try {
                    Files.move(fromFile, toFile, StandardCopyOption.REPLACE_EXISTING)
                    ret = true
                } catch (_: IOException) {
                    // Probably the file is locked by unclosed stream or other program.
                }

            }
            return ret
        }

        /**
         * Delete a specific file. This method will not remove a directory.
         * @param file Name of the file to be deleted.
         * @return True if the file was successfully deleted.
         * @see #moveFile
         */
        fun deleteFile(file: String?): Boolean {
            var ret = false
            if (file != null) {
                val f = Paths.get(file)
                try {
                    Files.delete(f)
                    ret = true
                } catch (_: IOException) {
                    // The file may be locked by some other program.
                }

            }
            return ret
        }

        /**
         * Delete directory (even if it is not empty and contains subdirectories).
         * There is protection against hack with special file names.
         * @param directoryName Directory to be deleted.
         * @return True if the directory is deleted successfully.
         */
        fun deleteDirectory(directoryName: String?): Boolean {
            var ret = false
            if (directoryName != null) {
                val dir = File(secureFileName(directoryName)!!)
                ret = deleteDirectory(dir)
            }
            return ret
        }

        /**
         * Delete directory and its subdirectories recursively.
         * @param dir Directory to be deleted.
         * @return True if the directory is deleted successfully.
         */
        fun deleteDirectory(dir: File): Boolean {
            if (dir.isDirectory) {
                val subDirs = dir.listFiles()
                if (subDirs != null) {
                    for (subDir in subDirs) {
                        if (!deleteDirectory(subDir)) {
                            // Was not able to delete a subdirectory.
                            return false
                        }
                    } //
                }
            }
            return dir.delete()
        }

        /**
         * Clear the contents of specific directory (without the directory itself).
         * @param directory The directory to delete.
         * @return True if the directory contents was deleted successfully.
         */
        fun clearDirectory(directory: File?): Boolean {
            var ret = true
            if (directory != null) {
                val files = directory.listFiles()
                if (files != null) {
                    for (file in files) {
                        if (file.isDirectory) {
                            clearDirectory(file)
                        }
                        file.delete()
                    } //
                }
                val fList = File(directory.name).list()
                if (fList != null) {
                    ret = fList.isEmpty()
                }
            }
            return ret
        }

        /**
         * Get file name extension of specified file. Example : 'file1.ext' = '.ext'
         * @param fileName The file name.
         * @return File extension (in lowercase) or empty string if no extension.
         */
        fun getExtension(fileName: String?): String {
            var ret = ""
            if (fileName != null) {
                val idx = fileName.lastIndexOf('.')
                if (idx > 0 && idx < fileName.length - 1) {
                    ret = fileName.substring(idx).lowercase(Locale.getDefault())
                }
            }
            return ret
        }

        /**
         * Collect files which match specified mask.
         * @param dir Directory to look at. Null means current directory.
         * @param mask File mask. Null means all files.
         * @param includeSubDirs True to include subdirectories in the search. By default, it is false.
         * @param collection Collection where to put matched files.
         */
        protected fun collectFiles(dir: File, mask: Pattern?, includeSubDirs: Boolean,
                                   collection: MutableList<File>) {
            val files = dir.listFiles()
            if (files != null) {
                for (f in files) {
                    if (f.isFile) {
                        // File.
                        if (mask == null || mask.matcher(f.name).matches()) {
                            // The name matches.
                            collection.add(f)
                        }
                    } else {
                        // Directory.
                        if (includeSubDirs) {
                            // Scan subdirectories.
                            collectFiles(f, mask, true, collection)
                        }
                    }
                } //
            }
        }

        /**
         * Secure the file name to prevent hacking attempts with special file names (containing `..`
         * for example).
         * @param fileName The provided file name.
         * @return Secured file name.
         */
        protected fun secureFileName(fileName: String?): String? {
            var ret = fileName
            if (ret != null) {
                while (ret!!.contains(UP_DIR)) {
                    ret = ret.replace(UP_DIR, "")
                } //
            }
            return ret
        }

    }
}
