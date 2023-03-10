

package com.argonaut.showjava.utils.streams

import androidx.annotation.NonNull
import com.argonaut.showjava.decompilers.BaseDecompiler
import timber.log.Timber
import java.io.OutputStream
import java.nio.charset.Charset
import java.util.Arrays


/**
 * A custom output stream that strips unnecessary stuff from raw input stream
 */
class ProgressStream(val decompiler: BaseDecompiler) : OutputStream() {

    private val validProgressRegex = Regex("^[^.][a-zA-Z/\$;\\s0-9.]+\$")

    private fun shouldIgnore(string: String): Boolean {
        if (string.startsWith("[ignored]")) {
            return true
        }
        for (part in arrayOf(
            "TRYBLOCK", "stack info", "Produces", "ASTORE", "targets",
            "WARN jadx", "thread-1", "ERROR jadx", "JadxRuntimeException",
            "java.lang")) {
            if (string.contains(part, true)) {
                return true
            }
        }

        return !validProgressRegex.matches(string)
    }

    override fun write(@NonNull data: ByteArray, offset: Int, length: Int) {
        var str = String(
            Arrays.copyOfRange(data, offset, length),
            Charset.forName("UTF-8")
        )
            .replace("\n", "")
            .replace("\r", "")
            .replace("INFO:".toRegex(RegexOption.IGNORE_CASE), "")
            .replace("ERROR:".toRegex(RegexOption.IGNORE_CASE), "")
            .replace("WARN:".toRegex(RegexOption.IGNORE_CASE), "")
            .replace("\n\r", "")
            .replace("... done", "")
            .replace("at", "")
            .replace("Processing ".toRegex(RegexOption.IGNORE_CASE), "")
            .replace("Decompiling ".toRegex(RegexOption.IGNORE_CASE), "")
            .replace("Extracting ".toRegex(RegexOption.IGNORE_CASE), "")
            .trim()

        if (shouldIgnore(str)) {
            return
        }

        if (str.startsWith("[stdout]")) {
            str = str.removePrefix("[stdout] ")
        }

        if (str.isNotEmpty()) {
            Timber.d("[stdout] %s", str)
            decompiler.sendStatus(str)
        }
    }

    override fun write(byte: Int) {
        // Just a stub. We aren't implementing this.
    }
}