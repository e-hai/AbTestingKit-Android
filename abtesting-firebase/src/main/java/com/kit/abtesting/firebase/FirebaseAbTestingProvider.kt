package com.kit.abtesting.firebase

import android.content.Context
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.kit.abtesting.AbTestingConfig
import com.kit.abtesting.AbTestingProvider
import kotlinx.coroutines.tasks.await

/**
 * [AbTestingProvider] backed by Firebase Remote Config.
 *
 * The host app must initialize Firebase (e.g. via `google-services` plugin +
 * `google-services.json`) before installing this provider.
 *
 * @param context Application or Activity context (Firebase must already be initialized).
 * @param config Fetch / interval settings applied to Remote Config.
 * @param remoteConfig Injectable for tests; defaults to [FirebaseRemoteConfig.getInstance].
 */
class FirebaseAbTestingProvider(
    @Suppress("UNUSED_PARAMETER") context: Context,
    config: AbTestingConfig = AbTestingConfig(),
    private val remoteConfig: FirebaseRemoteConfig = FirebaseRemoteConfig.getInstance(),
) : AbTestingProvider {

    init {
        val settings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(config.minimumFetchIntervalInSeconds)
            .build()
        remoteConfig.setConfigSettingsAsync(settings)
    }

    override suspend fun setDefaults(defaults: Map<String, Any>) {
        remoteConfig.setDefaultsAsync(defaults).await()
    }

    override suspend fun fetchAndActivate(): Boolean =
        remoteConfig.fetchAndActivate().await()

    override fun getBoolean(key: String, default: Boolean): Boolean {
        val value = remoteConfig.getValue(key)
        return if (value.source == FirebaseRemoteConfig.VALUE_SOURCE_STATIC) {
            default
        } else {
            value.asBoolean()
        }
    }

    override fun getString(key: String, default: String): String {
        val value = remoteConfig.getValue(key)
        return if (value.source == FirebaseRemoteConfig.VALUE_SOURCE_STATIC) {
            default
        } else {
            value.asString()
        }
    }

    override fun getLong(key: String, default: Long): Long {
        val value = remoteConfig.getValue(key)
        return if (value.source == FirebaseRemoteConfig.VALUE_SOURCE_STATIC) {
            default
        } else {
            value.asLong()
        }
    }

    override fun getDouble(key: String, default: Double): Double {
        val value = remoteConfig.getValue(key)
        return if (value.source == FirebaseRemoteConfig.VALUE_SOURCE_STATIC) {
            default
        } else {
            value.asDouble()
        }
    }
}
