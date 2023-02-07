

package com.argonaut.showjava.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import com.google.ads.consent.*
import com.google.android.gms.ads.MobileAds
import com.argonaut.showjava.R
import com.argonaut.showjava.activities.purchase.PurchaseActivity
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener
import timber.log.Timber
import java.net.URL

/**
 * Initialize the ads library. Also, takes care of showing a consent screen for users within EU
 * and persisting the consent.
 */
class Ads(val context: Context) {
    private val consentInformation: ConsentInformation = ConsentInformation.getInstance(context)
    private lateinit var consentForm: ConsentForm

    fun getPreferences(): SharedPreferences {
        return context.getSharedPreferences(UserPreferences.NAME, Context.MODE_PRIVATE)
    }

    @SuppressLint("ApplySharedPref")
    fun init() {
        MobileAds.initialize(context, OnInitializationCompleteListener { context.getString(R.string.admobAppId) })
        //MobileAds.initialize(context, context.getString(R.string.admobAppId))
        val publisherIds = arrayOf(context.getString(R.string.admobPublisherId))
        consentInformation.requestConsentInfoUpdate(
            publisherIds,
            object : ConsentInfoUpdateListener {
                override fun onConsentInfoUpdated(consentStatus: ConsentStatus) {
                    getPreferences().edit()
                        .putInt("consentStatus", consentStatus.ordinal)
                        .commit()
                }
                override fun onFailedToUpdateConsentInfo(errorDescription: String) {
                    getPreferences().edit()
                        .putInt("consentStatus", ConsentStatus.UNKNOWN.ordinal)
                        .commit()
                }
            })
    }

    /**
     * Load the consent screen and prepare to display.
     */
    fun loadConsentScreen(): ConsentForm? {
        consentForm = ConsentForm.Builder(context, URL(context.getString(R.string.privacyPolicyUrl)))
            .withListener(object : ConsentFormListener() {
                override fun onConsentFormLoaded() {
                    if (context is Activity && !context.isFinishing) {
                        consentForm.show()
                    }
                }

                override fun onConsentFormOpened() {
                    Timber.d("[consent-screen] onConsentFormOpened")
                }

                override fun onConsentFormClosed(consentStatus: ConsentStatus?, userPrefersAdFree: Boolean?) {
                    consentStatus?.let {
                        ConsentInformation.getInstance(context).consentStatus = it
                        getPreferences().edit().putInt("consentStatus", consentStatus.ordinal).apply()
                    }
                    if (userPrefersAdFree != null && userPrefersAdFree) {
                        context.startActivity(Intent(context, PurchaseActivity::class.java))
                    }
                }

                override fun onConsentFormError(errorDescription: String?) {
                    Timber.d("[consent-screen] onConsentFormError: $errorDescription")
                }
            })
            .withPersonalizedAdsOption()
            .withNonPersonalizedAdsOption()
            .withAdFreeOption()
            .build()
        consentForm.load()
        return consentForm
    }
}