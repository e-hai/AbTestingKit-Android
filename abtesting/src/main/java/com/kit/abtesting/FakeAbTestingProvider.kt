package com.kit.abtesting

/**
 * Test fake for [AbTestingProvider].
 *
 * Use in unit tests (and local verification without a real backend).
 * [fetchAndActivate] activates values previously set via [setRemoteValues],
 * simulating a successful remote fetch. Until then, [setDefaults] values are used.
 */
class FakeAbTestingProvider : AbTestingProvider {
    private val defaults = mutableMapOf<String, Any>()
    private val remote = mutableMapOf<String, Any>()
    private var activated = false

    /** Simulates remote payload that becomes active after [fetchAndActivate]. */
    fun setRemoteValues(values: Map<String, Any>) {
        remote.clear()
        remote.putAll(values)
    }

    override suspend fun setDefaults(defaults: Map<String, Any>) {
        this.defaults.clear()
        this.defaults.putAll(defaults)
    }

    override suspend fun fetchAndActivate(): Boolean {
        activated = true
        return true
    }

    override fun getBoolean(key: String, default: Boolean): Boolean =
        read(key, default) { it as? Boolean ?: (it as? String)?.toBooleanStrictOrNull() }

    override fun getString(key: String, default: String): String =
        read(key, default) { it?.toString() }

    override fun getLong(key: String, default: Long): Long =
        read(key, default) {
            when (it) {
                is Long -> it
                is Int -> it.toLong()
                is Number -> it.toLong()
                is String -> it.toLongOrNull()
                else -> null
            }
        }

    override fun getDouble(key: String, default: Double): Double =
        read(key, default) {
            when (it) {
                is Double -> it
                is Float -> it.toDouble()
                is Number -> it.toDouble()
                is String -> it.toDoubleOrNull()
                else -> null
            }
        }

    private fun <T> read(key: String, default: T, convert: (Any?) -> T?): T {
        val raw = if (activated && remote.containsKey(key)) {
            remote[key]
        } else {
            defaults[key]
        }
        return convert(raw) ?: default
    }
}
