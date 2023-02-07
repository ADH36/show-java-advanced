

package com.argonaut.showjava.test.decompilers.dex

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.argonaut.showjava.test.DecompilerTestBase
import com.argonaut.showjava.data.PackageInfo
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DexViaFernFlower: DecompilerTestBase() {
    override val type: PackageInfo.Type = PackageInfo.Type.DEX
    override val decompiler: String = "fernflower"
}