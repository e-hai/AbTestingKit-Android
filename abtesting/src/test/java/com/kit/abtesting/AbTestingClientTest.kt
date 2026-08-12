package com.kit.abtesting

import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AbTestingClientTest {

    @After
    fun tearDown() {
        AbTestingClient.reset()
    }

    @Test
    fun install_and_readDefaults() = runBlocking {
        val provider = FakeAbTestingProvider()
        AbTestingClient.install(provider)
        AbTestingClient.setDefaults(
            mapOf(
                "feature_x" to true,
                "title" to "default",
                "count" to 3L,
                "ratio" to 1.5,
            ),
        )

        assertTrue(AbTestingClient.isInstalled())
        assertTrue(AbTestingClient.getBoolean("feature_x", false))
        assertEquals("default", AbTestingClient.getString("title", ""))
        assertEquals(3L, AbTestingClient.getLong("count", 0L))
        assertEquals(1.5, AbTestingClient.getDouble("ratio", 0.0), 0.0)
    }

    @Test
    fun fetchAndActivate_usesRemoteOverDefaults() = runBlocking {
        val provider = FakeAbTestingProvider()
        provider.setRemoteValues(
            mapOf(
                "feature_x" to true,
                "title" to "remote",
            ),
        )
        AbTestingClient.install(provider)
        AbTestingClient.setDefaults(
            mapOf(
                "feature_x" to false,
                "title" to "default",
            ),
        )

        assertFalse(AbTestingClient.getBoolean("feature_x", true))
        assertEquals("default", AbTestingClient.getString("title", ""))

        assertTrue(AbTestingClient.fetchAndActivate())
        assertTrue(AbTestingClient.getBoolean("feature_x", false))
        assertEquals("remote", AbTestingClient.getString("title", ""))
    }

    @Test
    fun getters_fallBackToMethodDefaultWhenKeyMissing() = runBlocking {
        AbTestingClient.install(FakeAbTestingProvider())
        AbTestingClient.setDefaults(emptyMap())
        AbTestingClient.fetchAndActivate()

        assertFalse(AbTestingClient.getBoolean("missing_bool", false))
        assertEquals("fallback", AbTestingClient.getString("missing_str", "fallback"))
        assertEquals(99L, AbTestingClient.getLong("missing_long", 99L))
        assertEquals(2.5, AbTestingClient.getDouble("missing_double", 2.5), 0.0)
    }

    @Test(expected = IllegalStateException::class)
    fun get_withoutInstall_throws() {
        AbTestingClient.getBoolean("x", false)
    }
}
