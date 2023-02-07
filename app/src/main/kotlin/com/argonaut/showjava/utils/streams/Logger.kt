

package com.argonaut.showjava.utils.streams

import jadx.api.JadxArgs
import jadx.core.dex.nodes.ClassNode
import java.io.File


object Logger {

    /**
     * This method will be invoked by a JaDX method that is used to save classes.
     */
    @Suppress("UNUSED_PARAMETER")
    @JvmStatic
    fun logJadxClassWrite(dir: File, args: JadxArgs, cls: ClassNode) {
        println("Decompiling " + cls.fullName)
    }
}
