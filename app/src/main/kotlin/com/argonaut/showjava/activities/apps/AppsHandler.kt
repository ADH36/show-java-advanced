

package com.argonaut.showjava.activities.apps

import android.content.Context
import com.argonaut.showjava.R
import com.argonaut.showjava.data.PackageInfo
import com.argonaut.showjava.utils.ktx.isSystemPackage
import com.argonaut.showjava.utils.rx.ProcessStatus
import io.reactivex.Observable
import io.reactivex.ObservableEmitter

class AppsHandler(private var context: Context) {

    /**
     * Load all installed applications.
     *
     * @return [Observable] which can be used to track the loading progress and completion state.
     */
    fun loadApps(withSystemApps: Boolean): Observable<ProcessStatus<ArrayList<PackageInfo>>> {
        return Observable.create { emitter: ObservableEmitter<ProcessStatus<ArrayList<PackageInfo>>> ->
            val installedApps = ArrayList<PackageInfo>()
            var packages = context.packageManager.getInstalledPackages(0)
            packages = packages.filter { pack ->
                withSystemApps || !isSystemPackage(pack)
            }
            packages.forEachIndexed { index, pack ->
                val packageInfo = PackageInfo.fromApkPackageInfo(context, pack)
                packageInfo.icon = pack.applicationInfo.loadIcon(context.packageManager)
                packageInfo.isSystemPackage = isSystemPackage(pack)
                installedApps.add(packageInfo)
                val currentCount = index + 1
                emitter.onNext(
                    ProcessStatus(
                        (currentCount.toFloat() / packages.size.toFloat()) * 100f,
                        context.getString(R.string.loadingApp, packageInfo.label),
                        context.getString(R.string.loadingStatistic, currentCount, packages.size)
                    )
                )
            }
            installedApps.sortBy {
                it.label.toLowerCase()
            }
            emitter.onNext(ProcessStatus(installedApps))
            emitter.onComplete()
        }
    }
}