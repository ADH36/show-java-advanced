

package com.argonaut.showjava.activities.about

import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.argonaut.showjava.BuildConfig
import com.argonaut.showjava.R
import com.argonaut.showjava.activities.BaseActivity
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics

import kotlinx.android.synthetic.main.activity_about.*

/**
 * Show information about the app, its version & licenses to all open source libraries used
 */
class AboutActivity : BaseActivity() {
    private var mInterstitialAd: InterstitialAd? = null
    private final var TAG = "MainActivity"
    override fun init(savedInstanceState: Bundle?) {
        setupLayout(R.layout.activity_about)

        var adRequest = AdRequest.Builder().build()

        InterstitialAd.load(this,"ca-app-pub-6353875969990470/9189591124", adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.d(TAG, adError?.message)
                mInterstitialAd = null
            }

            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                Log.d(TAG, "Ad was loaded.")
                mInterstitialAd = interstitialAd
                ShowAd()

            }
        })




        if (BuildConfig.GIT_SHA.isNotEmpty()) {
            version.setText(R.string.appVersionExtendedWithHash)

        }



        viewOpenSourceLicenses.setOnClickListener {
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            startActivity(Intent(context, OssLicensesMenuActivity::class.java))
        }
    }
    fun ShowAd (){
        if (!isPro()) {
            if (mInterstitialAd != null) {
                mInterstitialAd?.show(this)
            } else {
                Log.d("TAG", "The interstitial ad wasn't ready yet.")
            }
        }
    }
}