

package com.argonaut.showjava.test.decompilers.apk

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.argonaut.showjava.test.DecompilerTestBase
import com.argonaut.showjava.data.PackageInfo
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ApkViaCfr: DecompilerTestBase() {
    override val type: PackageInfo.Type = PackageInfo.Type.APK
    override val decompiler: String = "cfr"
}