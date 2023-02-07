

package com.argonaut.showjava

import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatDelegate
import androidx.multidex.MultiDexApplication
import androidx.preference.PreferenceManager
import androidx.work.WorkManager
import com.google.firebase.FirebaseApp
import com.argonaut.showjava.utils.Ads
import com.argonaut.showjava.utils.UserPreferences
import com.argonaut.showjava.utils.logging.ProductionTree
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.onesignal.OneSignal
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
//import com.crashlytics.android.Crashlytics
//import com.google.firebase.iid.FirebaseInstanceId
//import io.fabric.sdk.android.Fabric
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;


class MainApplication : MultiDexApplication() {
    val ONESIGNAL_APP_ID = "45af2679-3cb8-4642-90f1-748f8be0a21b"



    val disposables = CompositeDisposable()
    lateinit var instanceId: String

    override fun onCreate() {
        super.onCreate()
       instanceId = FirebaseApp.getInstance().toString()

        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE)




        // OneSignal Initialization
        OneSignal.initWithContext(this)
        OneSignal.setAppId(ONESIGNAL_APP_ID)

        PreferenceManager.setDefaultValues(
            applicationContext,
            UserPreferences.NAME,
            Context.MODE_PRIVATE,
            R.xml.preferences,
            false
        )

        val preferences =
            UserPreferences(
                applicationContext.getSharedPreferences(
                    UserPreferences.NAME,
                    Context.MODE_PRIVATE
                )
            )

        AppCompatDelegate.setDefaultNightMode(
            if (preferences.darkMode)
                AppCompatDelegate.MODE_NIGHT_YES
            else
                AppCompatDelegate.MODE_NIGHT_NO
        )

        
        Ads(this).init()
     //   Fabric.with(this, Crashlytics())
      //  Crashlytics.setUserIdentifier(instanceId)

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(ProductionTree())
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            disposables.add(
                cleanStaleNotifications()
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io())
                    .onErrorReturn {}
                    .subscribe()
            )
        }
    }

    /**
     * Clean any stale notifications not linked to any decompiler process
     */
    @RequiresApi(Build.VERSION_CODES.M)
    fun cleanStaleNotifications(): Observable<Unit> {
        return Observable.fromCallable {
            val manager = applicationContext
                .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val workManager = WorkManager.getInstance()
            manager.activeNotifications.forEach { notification ->
                val status = workManager.getWorkInfosForUniqueWorkLiveData(notification.tag)
                    .value?.any { it.state.isFinished }
                if (status == null || status == true) {
                    manager.cancel(notification.tag, notification.id)
                }
            }
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        disposables.clear()
    }
}


private fun InterstitialAd.show(mainApplication: MainApplication) {

}
