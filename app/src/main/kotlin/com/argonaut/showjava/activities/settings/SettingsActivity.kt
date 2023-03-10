

package com.argonaut.showjava.activities.settings

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.Toast
import androidx.preference.ListPreference
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.firebase.analytics.FirebaseAnalytics
import com.argonaut.showjava.Constants
import com.argonaut.showjava.R
import com.argonaut.showjava.activities.BaseActivity
import com.argonaut.showjava.utils.Ads
import com.argonaut.showjava.utils.UserPreferences

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers


class SettingsActivity : BaseActivity() {

    override fun init(savedInstanceState: Bundle?) {
        setupLayout(R.layout.activity_settings)

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.container, PrefsFragment())
            .commit()
    }


    class PrefsFragment : PreferenceFragmentCompat() {

        private lateinit var settingsHandler: SettingsHandler
        private lateinit var deleteSubscription: Disposable

        private var progressBarView: View? = null
        private var containerView: View? = null

        private val numericKeys = arrayOf("chunkSize", "maxAttempts", "memoryThreshold")

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            preferenceManager.sharedPreferencesName = UserPreferences.NAME
            preferenceManager.sharedPreferencesMode = Context.MODE_PRIVATE

            val activity = activity as SettingsActivity

            progressBarView = activity.findViewById(R.id.progressBar)
            containerView = activity.findViewById(R.id.container)

            settingsHandler = SettingsHandler(requireContext())

            setPreferencesFromResource(R.xml.preferences, rootKey)

            bindPreferenceSummaryToValue(findPreference("chunkSize")!!)
            bindPreferenceSummaryToValue(findPreference("maxAttempts")!!)
            bindPreferenceSummaryToValue(findPreference("memoryThreshold")!!)

            val adPreferences = findPreference("adPreferences")

            if (!activity.inEea || activity.isPro()) {
                adPreferences.parent?.removePreference(adPreferences)
            } else {
                adPreferences.setOnPreferenceClickListener {
                    Ads(activity).loadConsentScreen()
                    return@setOnPreferenceClickListener true
                }
            }

            this.findPreference("clearSourceHistory").setOnPreferenceClickListener {
                activity.firebaseAnalytics.logEvent(Constants.EVENTS.CLEAR_SOURCE_HISTORY, null)
                AlertDialog.Builder(requireContext())
                    .setTitle(getString(R.string.deleteSourceHistory))
                    .setMessage(getString(R.string.deleteSourceHistoryConfirm))
                    .setIcon(R.drawable.ic_error_outline_black)
                    .setPositiveButton(
                        android.R.string.yes
                    ) { _, _ ->
                        deleteSources()
                    }
                    .setNegativeButton(android.R.string.no, null)
                    .show()
                true
            }

            findPreference("customFont").setOnPreferenceChangeListener { _, newValue ->
                val bundle = Bundle()
                bundle.putString(FirebaseAnalytics.Param.VALUE, newValue.toString())
                activity.firebaseAnalytics.logEvent(Constants.EVENTS.CHANGE_FONT, bundle)
                Toast.makeText(context, R.string.themeChangeCloseInfo, Toast.LENGTH_SHORT).show()
                activity.let {
                    it.startActivity(Intent(it, SettingsActivity::class.java))
                    it.finish()
                }
                true
            }

            findPreference("darkMode").setOnPreferenceChangeListener { _, newValue ->
                val bundle = Bundle()
                bundle.putString(FirebaseAnalytics.Param.VALUE, newValue.toString())
                activity.firebaseAnalytics.logEvent(Constants.EVENTS.TOGGLE_DARK_MODE, bundle)

                AppCompatDelegate.setDefaultNightMode(
                    if (newValue as Boolean)
                        AppCompatDelegate.MODE_NIGHT_YES
                    else
                        AppCompatDelegate.MODE_NIGHT_NO
                )

                Toast.makeText(context, R.string.themeChangeCloseInfo, Toast.LENGTH_SHORT).show()

                activity.let {
                    it.startActivity(Intent(it, SettingsActivity::class.java))
                    it.finish()
                }

                true
            }
        }

        private fun deleteSources() {
            progressBarView?.visibility = View.VISIBLE
            containerView?.visibility = View.INVISIBLE
            deleteSubscription = settingsHandler.deleteHistory()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    Toast.makeText(
                        context,
                        getString(R.string.sourceHistoryDeleted),
                        Toast.LENGTH_SHORT
                    ).show()
                    progressBarView?.visibility = View.GONE
                    containerView?.visibility = View.VISIBLE
                }
        }

        override fun onDestroy() {
            super.onDestroy()
            if (::deleteSubscription.isInitialized && !deleteSubscription.isDisposed) {
                deleteSubscription.dispose()
            }
        }

        private fun numberCheck(newValue: Any): Boolean {
            return if (newValue.toString().trim() != "" && newValue.toString().trim().matches("\\d*".toRegex())) {
                true
            } else {
                Toast.makeText(activity, R.string.onlyPositiveIntegersAllowed, Toast.LENGTH_SHORT).show()
                false
            }
        }

        private val bindPreferenceSummaryToValueListener =
            Preference.OnPreferenceChangeListener { preference, value ->
                if (numericKeys.contains(preference.key) && !numberCheck(value)) {
                    return@OnPreferenceChangeListener false
                }

                var stringValue = value.toString()
                if (preference is ListPreference) {
                    val index = preference.findIndexOfValue(stringValue)
                    stringValue = if (index >= 0) preference.entries[index].toString() else ""
                }
                if (stringValue != "") {
                    preference.summary = stringValue.trim()
                    return@OnPreferenceChangeListener true
                }
                preference.summary = null
                true
            }

        private fun bindPreferenceSummaryToValue(preference: Preference) {
            preference.onPreferenceChangeListener = bindPreferenceSummaryToValueListener
            bindPreferenceSummaryToValueListener.onPreferenceChange(
                preference,
                preference.context.getSharedPreferences(UserPreferences.NAME, Context.MODE_PRIVATE)
                    .getString(preference.key, "")!!
            )
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        return true
    }
}