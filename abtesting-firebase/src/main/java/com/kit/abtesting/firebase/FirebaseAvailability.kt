package com.kit.abtesting.firebase

import android.content.Context
import com.google.firebase.FirebaseApp

/** Returns true when a default [FirebaseApp] has been initialized in this process. */
fun isFirebaseInitialized(context: Context): Boolean =
    try {
        FirebaseApp.getApps(context).isNotEmpty()
    } catch (_: Exception) {
        false
    }
