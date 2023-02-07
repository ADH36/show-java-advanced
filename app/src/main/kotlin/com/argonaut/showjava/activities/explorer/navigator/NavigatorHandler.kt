

package com.argonaut.showjava.activities.explorer.navigator

import android.content.Context
//import com.crashlytics.android.Crashlytics
import com.argonaut.showjava.data.FileItem
import com.argonaut.showjava.utils.ZipUtils
import io.reactivex.Observable
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.IOException
import java.text.DateFormat
import java.util.Date
import kotlin.collections.ArrayList
import kotlin.collections.forEach
import kotlin.collections.sortBy

class NavigatorHandler(private var context: Context) {

    /**
     * Load all files in the given directory
     */
    fun loadFiles(currentDirectory: File): Observable<ArrayList<FileItem>> {
        return Observable.fromCallable {
            val directories = ArrayList<FileItem>()
            val files = ArrayList<FileItem>()
            val items = currentDirectory.listFiles()
            if (items.isNullOrEmpty()) {
                return@fromCallable directories
            }
            items.forEach { file ->
                val lastModDate = DateFormat.getDateTimeInstance()
                    .format(
                        Date(
                            file.lastModified()
                        )
                    )
                if (file.isDirectory) {
                    val children = file.listFiles()
                    val noOfChildren = children?.size ?: 0
                    val fileSize = "$noOfChildren ${if (noOfChildren == 1) "item" else "items"}"
                    directories.add(FileItem(file, fileSize, lastModDate))
                } else {
                    val fileSize = FileUtils.byteCountToDisplaySize(file.length())
                    files.add(FileItem(file, fileSize, lastModDate))
                }
            }
            directories.sortBy { it.name?.toLowerCase() }
            files.sortBy { it.name?.toLowerCase() }
            directories.addAll(files)
            directories
        }
    }

    /**
     * Package an entire directory containing the source code into a .zip archive.
     */
    fun archiveDirectory(sourceDirectory: File, packageName: String): Observable<File> {
        return Observable.fromCallable {
            ZipUtils.zipDir(sourceDirectory, packageName)
        }
    }

    /**
     * Delete the source directory
     */
    fun deleteDirectory(sourceDirectory: File): Observable<Unit> {
        return Observable.fromCallable {
            try {
                if (sourceDirectory.exists()) {
                    FileUtils.deleteDirectory(sourceDirectory)
                }
            } catch (e: IOException) {
             //   Crashlytics.logException(e)
            }
        }
    }
}
