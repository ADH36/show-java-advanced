

package com.argonaut.showjava.data

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Parcel
import android.os.Parcelable
import com.argonaut.showjava.utils.Identicon
import com.argonaut.showjava.utils.ktx.getVersion
import com.argonaut.showjava.utils.ktx.isSystemPackage
import com.argonaut.showjava.utils.ktx.jarPackageName
import java.io.File
import java.lang.NullPointerException

/**
 * [PackageInfo] holds information about an apk/jar/dex file in preparation for sending it for
 * decompilation. It also providers helpers to auto-generate an instance from a given [File]
 */
class PackageInfo() : Parcelable {
    var label = ""
    var name = ""
    var version = ""
    var filePath = ""
    var file: File = File("")
    var icon: Drawable? = null
    var type = Type.APK
    var isSystemPackage = false

    constructor(parcel: Parcel) : this() {
        label = parcel.readString()!!
        name = parcel.readString()!!
        version = parcel.readString()!!
        filePath = parcel.readString()!!
        type = Type.values()[parcel.readInt()]
        isSystemPackage = parcel.readInt() == 1
        file = File(filePath)
    }

    constructor(label: String, name: String, version: String, filePath: String, type: Type, isSystemPackage: Boolean = false) : this() {
        this.label = label
        this.name = name
        this.version = version
        this.filePath = filePath
        this.type = type
        this.isSystemPackage = isSystemPackage
        file = File(filePath)
    }

    constructor(label: String, name: String) : this() {
        this.label = label
        this.name = name
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(label)
        parcel.writeString(name)
        parcel.writeString(version)
        parcel.writeString(filePath)
        parcel.writeInt(type.ordinal)
        parcel.writeInt(if (isSystemPackage) 1 else 0)
    }

    fun loadIcon(context: Context): Drawable? {
        return when(type) {
            Type.APK -> context.packageManager.getPackageArchiveInfo(filePath, 0)
                ?.applicationInfo?.loadIcon(context.packageManager)
            Type.JAR, Type.DEX ->
                BitmapDrawable(context.resources, Identicon.createFromObject(this.name + this.label))
        }
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun toString(): String {
        return String.format("filePath: %s", filePath)
    }

    enum class Type {
        APK, JAR, DEX
    }

    companion object CREATOR : Parcelable.Creator<PackageInfo> {

        /**
         * Get [PackageInfo] for an apk using the [context] and [android.content.pm.PackageInfo] instance.
         */
        fun fromApkPackageInfo(context: Context, pack: android.content.pm.PackageInfo): PackageInfo {
            return PackageInfo(
                pack.applicationInfo.loadLabel(context.packageManager).toString(),
                pack.packageName,
                getVersion(pack),
                pack.applicationInfo.publicSourceDir,
                Type.APK,
                isSystemPackage(pack)
            )
        }

        /**
         * Get [PackageInfo] for an apk using the [context] and the [file].
         */
        private fun fromApk(context: Context, file: File): PackageInfo? {
            val pack = context.packageManager.getPackageArchiveInfo(file.canonicalPath, 0)
            return pack?.let {
                PackageInfo(
                    pack.applicationInfo?.loadLabel(context.packageManager).toString(),
                    it.packageName,
                    getVersion(pack),
                    file.canonicalPath,
                    Type.APK,
                    isSystemPackage(pack)
                )
            }
        }

        /**
         * Get [PackageInfo] for a jar from the [file].
         */
        private fun fromJar(file: File, type: Type = Type.JAR): PackageInfo? {
            return PackageInfo(
                file.name,
                jarPackageName(file.name),
                (System.currentTimeMillis() / 1000).toString(),
                file.canonicalPath,
                type
            )
        }

        /**
         * Get [PackageInfo] for a dex from the [file].
         */
        private fun fromDex(file: File): PackageInfo? {
            return fromJar(file, Type.DEX)
        }


        /**
         * Get [PackageInfo] from a [file].
         */
        fun fromFile(context: Context, file: File): PackageInfo? {
            return try {
                when(file.extension) {
                    "apk" -> fromApk(context, file)
                    "jar" -> fromJar(file)
                    "dex", "odex" -> fromDex(file)
                    else -> null
                }
            } catch (e: NullPointerException) {
                null
            }
        }

        override fun createFromParcel(parcel: Parcel): PackageInfo {
            return PackageInfo(parcel)
        }

        override fun newArray(size: Int): Array<PackageInfo?> {
            return arrayOfNulls(size)
        }
    }
}
