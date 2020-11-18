package duid

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

open class FsDuidDatabase(context: DuidDatabaseContext) : AbstractDuidDatabaseImpl(context) {
    override fun writeKeys(map: Map<String, Int>, file: File) {
        FileOutputStream(file).use {
            writeSortedKeys(it, map)
        }
    }

    override fun readKeys(file: File): Map<String, Int> {
        return if (file.exists()) {
            FileInputStream(file).use {
                readSortedKeys(it)
            }
        } else {
            emptyMap()
        }
    }
}


