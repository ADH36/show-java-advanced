

package com.argonaut.showjava.utils.ktx

import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.os.Environment
import java.io.File

/**
 * Path to the priamry storage directory on the user's internal memory
 */
val appStorage: File = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "ShowJavaPro")

/**
 * Check if the given packageInfo points to a system application
 */
fun isSystemPackage(pkgInfo: PackageInfo): Boolean {
    return pkgInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0
}

/**
 * Get the source directory for a given package name
 */
fun sourceDir(packageName: String): File {
    return appStorage.resolve("sources/$packageName")
}

/**
 * Convert an arbitrary file name into a generated package name that we can use
 */
fun jarPackageName(jarFileName: String): String {
    val slug = toSlug(jarFileName)
    return "$slug-${hashString("SHA-1", slug).slice(0..7)}".toLowerCase()
}