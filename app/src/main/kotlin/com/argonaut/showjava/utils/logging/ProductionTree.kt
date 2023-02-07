

package com.argonaut.showjava.utils.logging

import android.util.Log
//import com.crashlytics.android.Crashlytics
import timber.log.Timber

/**
 * Logs all exceptions and anything with priority higher than [Log.WARN] to [Crashlytics] and ignores
 * the rest.
 */
class ProductionTree : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority == Log.VERBOSE || priority == Log.DEBUG || priority == Log.INFO) {
            return
        }

        if (t !== null && t !is OutOfMemoryError && t !is StackOverflowError && t !is NoClassDefFoundError) {
            if (message.isNotEmpty()) {
                //Crashlytics.log("[$tag] $message")
            }
           // Crashlytics.logException(t)
            return
        }

        if (priority > Log.WARN) {
           // Crashlytics.logException(Throwable("[$tag] $message"))
        }
    }
}
