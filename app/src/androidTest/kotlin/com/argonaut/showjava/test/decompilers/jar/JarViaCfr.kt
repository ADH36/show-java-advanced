

package com.argonaut.showjava.test.decompilers.jar

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.argonaut.showjava.test.DecompilerTestBase
import com.argonaut.showjava.data.PackageInfo
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class JarViaCfr: DecompilerTestBase() {
    override val type: PackageInfo.Type = PackageInfo.Type.JAR
    override val decompiler: String = "cfr"
}