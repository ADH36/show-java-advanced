

package com.argonaut.showjava.activities.decompiler

import android.os.Bundle
import android.widget.Toast
import com.argonaut.showjava.Constants
import com.argonaut.showjava.R
import com.argonaut.showjava.activities.BaseActivity
import com.argonaut.showjava.data.PackageInfo
import com.argonaut.showjava.utils.ktx.toBundle
import kotlinx.android.synthetic.main.activity_low_memory.*

/**
 * If an app's decompilation was stopped due to low memory, explain what happened to the user
 * And also provide user a way to report the app that failed to decompile. This can then
 * be investigated later on to see what can be done to reduce the memory usage.
 */
class LowMemoryActivity : BaseActivity() {

    override fun init(savedInstanceState: Bundle?) {
        setupLayout(R.layout.activity_low_memory)
        val packageInfo = intent.getParcelableExtra<PackageInfo>("packageInfo")
        val decompiler = intent.getStringExtra("decompiler")

        reportButton.setOnClickListener {

            if (packageInfo != null) {
                firebaseAnalytics.logEvent(Constants.EVENTS.REPORT_APP_LOW_MEMORY, mapOf(
                    "shouldIgnoreLibs" to userPreferences.ignoreLibraries,
                    "maxAttempts" to userPreferences.maxAttempts,
                    "chunkSize" to userPreferences.chunkSize,
                    "memoryThreshold" to userPreferences.memoryThreshold,
                    "label" to packageInfo.label,
                    "name" to packageInfo.name,
                    "type" to packageInfo.type.name,
                    "decompiler" to decompiler
                ).toBundle())
            }

            Toast.makeText(context, R.string.appReportThanks, Toast.LENGTH_LONG).show()
        }
    }

}