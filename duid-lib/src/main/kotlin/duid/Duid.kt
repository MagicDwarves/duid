package duid

import duid.AbstractDuidDatabaseImpl.Companion.fileName
import java.io.*

open class DuidDatabaseContext(
        open var basedir: File? = null) {
    fun <V> getRequired(value: V?, label: String): V =
            value ?: throw DuidException("valueRequired(field=$label)")
}

abstract class AbstractDuidDatabaseImpl(context: DuidDatabaseContext) : DuidDatabase {
    companion object {
        const val NAMESPACES = "_namespaces_"
        const val MAPS = "_maps_"

        fun fileName(basedir: File, name: String) = File(basedir, name)
    }

    val basedir = context.getRequired(context.basedir, "basedir")!!

    val root = DuidNamespaceImpl(this, 0, basedir).read()

    override fun getRoot(): DuidNamespace = root

    internal abstract fun readKeys(file: File): Map<String, Int>

    internal abstract fun writeKeys(map: Map<String, Int>, file: File)

    protected fun readSortedKeys(inputStream: InputStream): Map<String, Int> {
        val reader = InputStreamReader(inputStream).buffered(4096)
        return reader.readLines()
                // TODO: make it better
                .filter { !it.isBlank() && !it.startsWith("#") }
                .map { it.split("=") }
                .filter { it.size == 2 }
                .map { it[0].trim() to it[1].trim().toInt() }
                .toMap()
    }

    protected fun writeSortedKeys(outputStream: OutputStream, map: Map<String, Int>) {
        val writer = OutputStreamWriter(outputStream).buffered(4096)
        writer.use {
            map
                    .toSortedMap()
                    .forEach { (key, value) -> it.write("$key=$value\n") }
        }
    }


    override fun isDirty(): Boolean {
        return root.isDirty
    }

    internal fun saveKeys(fileName: File, map: Map<String, DuidId>) {
        writeKeys(map.map { it.key to it.value.id }.toMap(), fileName)
    }

    override fun pull() {
        root.clean()
        root.read()
    }

    override fun push() = Unit

    override fun commit() {
        root.write()
    }

    override fun reset() {
        root.clean()
        root.read()
    }


}

open class DuidId(val database: AbstractDuidDatabaseImpl, val id: Int, val basedir: File)

open class DuidContainer(database: AbstractDuidDatabaseImpl, id: Int, basedir: File) :
        DuidId(database, id, basedir) {
    internal var dirty = false
    open fun isDirty() = dirty
}

class DuidNamespaceImpl(database: AbstractDuidDatabaseImpl, id: Int, basedir: File) :
        DuidNamespace, DuidContainer(database, id, basedir) {
    private val namespaces: MutableMap<String, DuidNamespaceImpl> = mutableMapOf()
    internal val maps: MutableMap<String, DuidMapImpl> = mutableMapOf()

    override fun isDirty(): Boolean {
        return dirty
                || namespaces.any { it.value.isDirty }
                || maps.any { it.value.isDirty() }
    }

    override fun getNamespaces(): Map<String, DuidNamespace> = namespaces

    override fun getNamespace(name: String) =
            namespaces.getOrElse(name) { throw DuidException("namespaceNotExistsError(name=$name)", null) }

    override fun getOrCreateNamespace(name: String): DuidNamespaceImpl =
            namespaces.computeIfAbsent(name) {
                dirty = true
                val nextId = (namespaces.values.map { it.id }.max() ?: 0) + 1
                DuidNamespaceImpl(database, nextId, fileName(basedir, "$nextId"))
            }

    override fun getMaps(): Map<String, DuidMap> = maps.mapValues { it.value as DuidMap }

    override fun getMap(name: String) =
            maps.getOrElse(name) { throw DuidException("mapNotExistsError(name=$name)", null) } as DuidMap

    override fun getOrCreateMap(name: String): DuidMap =
            maps.computeIfAbsent(name) {
                dirty = true
                val nextId = (maps.values.map { it.id }.max() ?: 0) + 1
                DuidMapImpl(database, this, nextId, basedir)
            }

    fun read(): DuidNamespaceImpl {
        database.readKeys(File(basedir, AbstractDuidDatabaseImpl.MAPS))
                .forEach { name, id ->
                    maps[name] = DuidMapImpl(database, this, id, basedir).read()
                }
        database.readKeys(File(basedir, AbstractDuidDatabaseImpl.NAMESPACES))
                .forEach { name, id ->
                    // read namespaces recursively
                    namespaces[name] = DuidNamespaceImpl(database, id, fileName(basedir, "$id")).read()
                }
        return this
    }

    internal fun write() {
        if (!basedir.exists()) {
            basedir.mkdirs()
        }
        if (dirty) {
            database.saveKeys(fileName(basedir, AbstractDuidDatabaseImpl.MAPS), maps)
            database.saveKeys(fileName(basedir, AbstractDuidDatabaseImpl.NAMESPACES), namespaces)
        }
        maps.values.forEach { it.write() }
        namespaces.values.forEach { it.write() }
        dirty = false
    }

    fun clean() {
        maps.clear()
        namespaces.clear()
    }


}

class DuidMapImpl(database: AbstractDuidDatabaseImpl, private val parent: DuidContainer, id: Int, basedir: File) :
        DuidMap, DuidContainer(database, id, basedir) {
    private val keys: MutableMap<String, Int> = mutableMapOf()

    override fun getKeys(): Map<String, Int> = keys

    override fun getValue(name: String) =
            keys.getOrElse(name) { throw DuidException("valueNotExistsError(name=$name)", null) }

    override fun getOrCreateValue(name: String): Int {
        return keys.computeIfAbsent(name) {
            dirty = true
            parent.dirty = true
            (keys.values.max() ?: 0) + 1
        }
    }

    private fun mapFile(basedir: File, id: Int) = fileName(basedir, "$id.map")

    fun read(): DuidMapImpl {
        keys.clear()
        keys.putAll(database.readKeys(mapFile(basedir, id)))
        return this
    }

    fun write() {
        database.writeKeys(keys.toSortedMap(), mapFile(basedir, id))
        dirty = false
    }

}


