package com.kit.abtesting

/**
 * Process-wide facade. Install a [AbTestingProvider] once at app startup,
 * then read values through this client so call sites stay backend-agnostic.
 */
object AbTestingClient {
    @Volatile
    private var provider: AbTestingProvider? = null

    fun install(provider: AbTestingProvider) {
        this.provider = provider
    }

    fun isInstalled(): Boolean = provider != null

    suspend fun setDefaults(defaults: Map<String, Any>) {
        requireProvider().setDefaults(defaults)
    }

    suspend fun fetchAndActivate(): Boolean = requireProvider().fetchAndActivate()

    fun getBoolean(key: String, default: Boolean): Boolean =
        requireProvider().getBoolean(key, default)

    fun getString(key: String, default: String): String =
        requireProvider().getString(key, default)

    fun getLong(key: String, default: Long): Long =
        requireProvider().getLong(key, default)

    fun getDouble(key: String, default: Double): Double =
        requireProvider().getDouble(key, default)

    /** Clears the installed provider. Intended for tests. */
    fun reset() {
        provider = null
    }

    private fun requireProvider(): AbTestingProvider =
        provider
            ?: error("AbTestingClient is not installed. Call AbTestingClient.install(...) first.")
}
