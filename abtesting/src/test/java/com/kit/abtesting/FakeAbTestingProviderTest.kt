package com.kit.abtesting

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FakeAbTestingProviderTest {

    @Test
    fun defaults_usedBeforeFetch() = runBlocking {
        val provider = FakeAbTestingProvider()
        provider.setDefaults(mapOf("flag" to true, "name" to "local"))

        assertTrue(provider.getBoolean("flag", false))
        assertEquals("local", provider.getString("name", ""))
    }

    @Test
    fun remote_overridesDefaultsAfterActivate() = runBlocking {
        val provider = FakeAbTestingProvider()
        provider.setDefaults(mapOf("flag" to false, "count" to 1L))
        provider.setRemoteValues(mapOf("flag" to true, "count" to 7L))

        assertFalse(provider.getBoolean("flag", true))
        assertEquals(1L, provider.getLong("count", 0L))

        assertTrue(provider.fetchAndActivate())
        assertTrue(provider.getBoolean("flag", false))
        assertEquals(7L, provider.getLong("count", 0L))
    }

    @Test
    fun remote_keyMissing_fallsBackToDefaultsAfterActivate() = runBlocking {
        val provider = FakeAbTestingProvider()
        provider.setDefaults(mapOf("only_default" to "kept"))
        provider.setRemoteValues(mapOf("other" to "remote"))
        provider.fetchAndActivate()

        assertEquals("kept", provider.getString("only_default", "fallback"))
        assertEquals("remote", provider.getString("other", ""))
    }
}
