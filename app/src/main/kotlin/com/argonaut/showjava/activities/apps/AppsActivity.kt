

package com.argonaut.showjava.activities.apps

import android.app.ActivityOptions
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import com.argonaut.showjava.BuildConfig
import com.argonaut.showjava.R
import com.argonaut.showjava.activities.BaseActivity
import com.argonaut.showjava.activities.apps.adapters.AppsListAdapter
import com.argonaut.showjava.activities.decompiler.DecompilerActivity
import com.argonaut.showjava.data.PackageInfo
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.OnUserEarnedRewardListener
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_apps.*
import timber.log.Timber


class AppsActivity : BaseActivity(), SearchView.OnQueryTextListener, SearchView.OnCloseListener {
    private lateinit var appsHandler: AppsHandler
    private lateinit var historyListAdapter: AppsListAdapter

    private var searchMenuItem: MenuItem? = null

    private var mInterstitialAd: InterstitialAd? = null
    private final var TAG = "AppsAct"

    private var apps = ArrayList<PackageInfo>()
    private var filteredApps = ArrayList<PackageInfo>()
    private var withSystemApps: Boolean = false

    override fun init(savedInstanceState: Bundle?) {
        setupLayout(R.layout.activity_apps)
        appsHandler = AppsHandler(context)
        withSystemApps = userPreferences.showSystemApps


        var adRequest = AdRequest.Builder().build()

        InterstitialAd.load(this,"ca-app-pub-3940256099942544/1033173712", adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.d(TAG, adError?.message)
                mInterstitialAd = null
            }

            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                Log.d(TAG, "Ad was loaded.")
                mInterstitialAd = interstitialAd

            }
        })



        loadingView.visibility = View.VISIBLE
        appsList.visibility = View.GONE
        typeRadioGroup.visibility = View.GONE
        searchMenuItem?.isVisible = false

        savedInstanceState?.let {
            val apps = it.getParcelableArrayList<PackageInfo>("apps")
            if (!apps.isNullOrEmpty()) {
                this.apps = apps
                this.filteredApps = apps
                setupList()
                filterApps(R.id.userRadioButton)
            }
        }

        if (this.apps.isEmpty( )) {
            loadApps()
        }
        typeRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            filterApps(checkedId)
        }
    }


    private fun loadApps() {
        disposables.add(appsHandler.loadApps(withSystemApps)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { processStatus ->
                    if (!processStatus.isDone) {
                        progressBar.progress = processStatus.progress.toInt()
                        statusText.text = processStatus.status
                        processStatus.secondaryStatus?.let {
                            secondaryStatusText.text = it
                        }
                    } else {
                        if (processStatus.result != null) {
                            apps = processStatus.result
                            filteredApps = processStatus.result
                        }
                        setupList()
                        filterApps(R.id.userRadioButton)

                    }
                },
                { e ->
                    Timber.e(e)
                }
            )
        )
    }

    private fun setupList() {
        loadingView.visibility = View.GONE
        appsList.visibility = View.VISIBLE
        typeRadioGroup.visibility = if (withSystemApps) View.VISIBLE else View.GONE
        searchMenuItem?.isVisible = true
        appsList.setHasFixedSize(true)
        ShowAds()
        appsList.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
        historyListAdapter = AppsListAdapter(apps) { selectedApp: PackageInfo, view: View ->
            Timber.d(selectedApp.name)
            if (selectedApp.name.toLowerCase().contains(BuildConfig.APPLICATION_ID.toLowerCase())) {
                Toast.makeText(
                    applicationContext,
                    getString(R.string.checkoutSourceLink),
                    Toast.LENGTH_SHORT
                ).show()
            }
            openProcessActivity(selectedApp, view)
        }
        appsList.adapter = historyListAdapter
    }

    private fun openProcessActivity(packageInfo: PackageInfo, view: View) {
        val i = Intent(context, DecompilerActivity::class.java)
        i.putExtra("packageInfo", packageInfo)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val options = ActivityOptions
                .makeSceneTransitionAnimation(this, view.findViewById(R.id.itemCard), "appListItem")
            return startActivity(i, options.toBundle())
        }

        startActivity(i)
    }
    fun ShowAds(){
        if (isPro()){
            if (mInterstitialAd != null) {
                mInterstitialAd?.show(this)
            } else {
                Log.d("TAG", "The interstitial ad wasn't ready yet.")
            }
        }
    }

    override fun onSaveInstanceState(bundle: Bundle) {
        super.onSaveInstanceState(bundle)
        bundle.putParcelableArrayList("apps", apps)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_app, menu)
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        searchMenuItem = menu.findItem(R.id.search)
        val searchView = searchMenuItem?.actionView as SearchView
        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        searchView.isSubmitButtonEnabled = true
        searchView.setOnQueryTextListener(this)
        searchView.setOnCloseListener(this)
        return true
    }

    private fun searchApps(query: String?) {
        val cleanedQuery = query?.trim()?.toLowerCase() ?: ""
        historyListAdapter.updateList(filteredApps.filter {
            cleanedQuery == "" || it.label.toLowerCase().contains(cleanedQuery)
        })
    }

    private fun filterApps(filterId: Int) {
        filteredApps = apps.filter {
            when(filterId) {
                R.id.systemRadioButton -> it.isSystemPackage
                R.id.userRadioButton -> !it.isSystemPackage
                else -> true
            }
        } as ArrayList<PackageInfo>
        historyListAdapter.updateList(filteredApps)
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        searchApps(query)
        return true
    }

    override fun onQueryTextChange(query: String?): Boolean {
        searchApps(query)
        return true
    }

    override fun onClose(): Boolean {
        searchApps(null)
        return true
    }
}