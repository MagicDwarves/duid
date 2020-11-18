package duid

import org.slf4j.LoggerFactory
import java.util.*

class DuidService(private val database: DuidDatabase, classLoader: ClassLoader) {
    companion object {
        val LOGGER = LoggerFactory.getLogger(DuidService::class.java)!!
    }

    private val serviceLoader: ServiceLoader<DuidProvider> = ServiceLoader.load(
            DuidProvider::class.java,
            classLoader);

    init {
        LOGGER.info("Find DuidProviders:")
        serviceLoader.forEach { LOGGER.info("found ${it.javaClass.typeName}") }
    }

    fun update() {
        while (true) {
            LOGGER.info("pull")
            database.pull()
            LOGGER.info("update")
            serviceLoader.forEach { it.apply(database.root) }
            try {
                if (database.isDirty) {
                    LOGGER.info("commit")
                    database.commit()
                    LOGGER.info("push")
                    database.push()
                }
                break
            } catch (e: DuidException) {
                LOGGER.info("error", e)
                LOGGER.info("reset")
                database.reset()
                continue
            }
        }
    }

}