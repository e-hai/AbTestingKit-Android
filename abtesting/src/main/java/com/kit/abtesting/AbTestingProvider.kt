package com.kit.abtesting

/**
 * Backend contract for A/B testing / remote config providers.
 */
interface AbTestingProvider {
    suspend fun setDefaults(defaults: Map<String, Any>)
    suspend fun fetchAndActivate(): Boolean
    fun getBoolean(key: String, default: Boolean): Boolean
    fun getString(key: String, default: String): String
    fun getLong(key: String, default: Long): Long
    fun getDouble(key: String, default: Double): Double
}
