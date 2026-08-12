package com.kit.abtesting

/**
 * Shared configuration applied by providers that support fetch intervals.
 *
 * @param minimumFetchIntervalInSeconds Minimum interval between remote fetches.
 *   Use a small value (e.g. 0) in debug builds; production typically uses 3600+.
 */
data class AbTestingConfig(
    val minimumFetchIntervalInSeconds: Long = 3600L,
)
