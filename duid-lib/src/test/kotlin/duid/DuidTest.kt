package duid

import org.junit.Assert.*
import org.junit.Test
import java.io.File
import java.nio.file.Files

class DuidTest {

    @Test
    fun testOpen() {
        FsDuidDatabase(DuidDatabaseContext(File("src/test/database"))).apply {
            pull()
            assertEquals(3, root.maps.size)
            root.getOrCreateNamespace("namespace1").also { namespace ->
                namespace.getOrCreateMap("map1").also { map ->
                    assertEquals(2, map.keys.size)
                    assertEquals(100, map.getOrCreateValue("key100"))
                    assertFalse(isDirty())
                    assertEquals(101, map.getOrCreateValue("key101"))
                }
            }
            assertTrue(isDirty())
        }
    }

    @Test
    fun testWriteMap() {
        val basedir = Files.createTempDirectory(File("target").toPath(), "database").toFile()
        FsDuidDatabase(DuidDatabaseContext(basedir)).apply {
            root.getOrCreateMap("testMap").also { map ->
                map.getOrCreateValue("testKey")
            }
            root.write()
            assertFalse(isDirty())
        }

        FsDuidDatabase(DuidDatabaseContext(basedir)).apply {
            pull()
            assertEquals(1, root.maps.size)
            root.getMap("testMap").also { map ->
                assertEquals(1, map.getValue("testKey"))
            }
            assertFalse(isDirty())
        }
    }

    @Test
    fun testWriteNamespace() {
        val basedir = Files.createTempDirectory(File("target").toPath(), "database").toFile()
        FsDuidDatabase(DuidDatabaseContext(basedir)).apply {
            root.getOrCreateNamespace("test1").also { namespace ->
            }
            root.write()
            assertFalse(isDirty())
        }

        FsDuidDatabase(DuidDatabaseContext(basedir)).apply {
            pull()
            assertEquals(1, root.getNamespaces().size)
            root.getNamespace("test1").also { namespace ->
                assertNotNull(namespace)
            }
            assertFalse(isDirty())
        }
    }

    @Test
    fun testResourceDatabase() {
        val basedir = File("target/test-classes/database1")
        FsDuidDatabase(DuidDatabaseContext(basedir)).apply {
            root.getOrCreateNamespace("test1").also { namespace ->
            }
            root.write()
            assertFalse(isDirty())
        }

        ResourceDuidDatabase(ResourcesDuidDatabaseContext().apply {
            this.basedir = File("database1")
        }).apply {
            pull()
            assertEquals(1, root.getNamespaces().size)
            root.getNamespace("test1").also { namespace ->
                assertNotNull(namespace)
            }
            assertFalse(isDirty())
        }
    }
}