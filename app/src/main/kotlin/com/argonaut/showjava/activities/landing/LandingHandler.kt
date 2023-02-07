

package com.argonaut.showjava.activities.landing

import android.content.Context
import androidx.work.WorkManager
import com.argonaut.showjava.data.SourceInfo
import com.argonaut.showjava.utils.ktx.appStorage
import io.reactivex.Observable
import timber.log.Timber
import java.io.File
import java.io.IOException

class LandingHandler(private var context: Context) {


    private fun isDecompilerRunning(): Boolean {
        return WorkManager.getInstance().getWorkInfosByTagLiveData("decompiler").value?.any {
            !it.state.isFinished
        } ?: false
    }

    fun loadHistory(): Observable<ArrayList<SourceInfo>> {
        return Observable.fromCallable {
            val historyItems = ArrayList<SourceInfo>()
            appStorage.mkdirs()
            val nomedia = File(appStorage, ".nomedia")
            if (!nomedia.exists() || !nomedia.isFile) {
                try {
                    nomedia.createNewFile()
                } catch (e: IOException) {
                    Timber.e(e)
                }
            }


            val sourcesDir = appStorage.resolve("sources")
            if (sourcesDir.exists()) {
                val files = sourcesDir.listFiles()
                if (files != null && files.isNotEmpty())
                    files.forEach { file ->
                        if (SourceInfo.exists(file)) {
                            SourceInfo.from(file).let {
                                historyItems.add(it)
                            }
                        } else {
/* // Not deleting directories.
                            if (!isDecompilerRunning()) {
                                try {
                                    if (file.exists()) {
                                        if (file.isDirectory) {
                                            FileUtils.deleteDirectory(file)
                                        } else {
                                            file.delete()
                                        }
                                    }

                                } catch (e: Exception) {
                                    Timber.d(e)
                                }
                                if (file.exists() && !file.isDirectory) {
                                    file.delete()
                                }
                            }
*/
                        }
                    }
            }
            historyItems
        }
    }
}
