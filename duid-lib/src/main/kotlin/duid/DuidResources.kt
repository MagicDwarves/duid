package duid

import java.io.File

open class ResourcesDuidDatabaseContext(var classLoader: ClassLoader = Thread.currentThread().contextClassLoader)
    : DuidDatabaseContext(File("database"))

class ResourceDuidDatabase(context: ResourcesDuidDatabaseContext) : AbstractDuidDatabaseImpl(context) {
    private val classLoader: ClassLoader = context.getRequired(context.classLoader, "classLoader")

    override fun writeKeys(map: Map<String, Int>, file: File) {
        throw DuidException("unsupported")
    }

    override fun readKeys(file: File): Map<String, Int> {
        val fileName = file.toString()
        val resources = classLoader.getResources(fileName)
                ?: throw DuidException("resourceNotFound(file=$fileName)")
        val list = resources.toList();
        if (list.isEmpty()) {
            throw DuidException("resourceNotFound2(file=$fileName}")
        }
        if (list.size > 1) {
            throw DuidException("resourceTooManyFound(file=$fileName,count=${list.size})")
        }

        return list[0].openStream().use {
            readSortedKeys(it)
        }
    }
}


