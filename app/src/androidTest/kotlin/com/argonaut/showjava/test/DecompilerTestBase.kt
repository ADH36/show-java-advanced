

package com.argonaut.showjava.test

import android.os.Build
import android.os.Environment
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import androidx.work.ListenableWorker
import com.argonaut.showjava.data.PackageInfo
import com.argonaut.showjava.decompilers.BaseDecompiler
import com.argonaut.showjava.decompilers.JarExtractionWorker
import com.argonaut.showjava.decompilers.JavaExtractionWorker
import com.argonaut.showjava.decompilers.ResourcesExtractionWorker
import junit.framework.TestCase
import org.junit.Assume
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.File

abstract class DecompilerTestBase {

    abstract val decompiler: String
    abstract val type: PackageInfo.Type

    private val testAssets: File
        get() = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "ShowJavaPro")
            .resolve("test-assets")

    private val testApplicationFile: File
        get() = testAssets.resolve("test-application.${type.name.toLowerCase()}")

    @Rule
    @JvmField
    val grantPermissionRule: GrantPermissionRule =
        GrantPermissionRule.grant(
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.INTERNET,
            android.Manifest.permission.ACCESS_NETWORK_STATE,
            android.Manifest.permission.WAKE_LOCK,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

    @Before
    fun initializeEnvironment() {
        val appContext = InstrumentationRegistry.getInstrumentation()
        if (testAssets.exists() && testAssets.isFile) {
            testAssets.delete()
        }
        if (!testAssets.exists()) {
            testAssets.mkdirs()
        }
        if (!testApplicationFile.exists()) {
            testApplicationFile.outputStream().use {
                appContext.context.assets
                    .open("test-application.${type.name.toLowerCase()}")
                    .copyTo(it)
            }
        }
    }

    @Before
    fun checkDecompilerAvailability() {
        Assume.assumeTrue(
            "Assume $decompiler is available on API ${Build.VERSION.SDK_INT}.",
            BaseDecompiler.isAvailable(decompiler)
        )
    }

    @Test
    fun testDecompiler() {
        val data = BaseDecompiler.formData(hashMapOf(
            "shouldIgnoreLibs" to true,
            "keepIntermediateFiles" to true,
            "chunkSize" to 2000,
            "maxAttempts" to 1,
            "memoryThreshold" to 80,
            "decompiler" to decompiler,
            "name" to "xyz.codezero.testapplication.$decompiler.${type.name}",
            "label" to "TestApplication-$decompiler-${type.name}",
            "inputPackageFile" to testApplicationFile.canonicalPath,
            "type" to type.ordinal
        ))

        val packageInfo = PackageInfo.fromFile(
            InstrumentationRegistry.getInstrumentation().targetContext,
            testApplicationFile
        )

        TestCase.assertNotNull("Can parse PackageInfo from file", packageInfo)

        val outputDirectory = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "ShowJavaPro/sources/${data.getString("name")}"
        )

        if (outputDirectory.exists()) {
            outputDirectory.deleteRecursively()
        }

        val appContext = InstrumentationRegistry.getInstrumentation()

        var result: ListenableWorker.Result
        var worker: BaseDecompiler

        worker = JarExtractionWorker(appContext.targetContext, data)
        result = worker.doWork()
        worker.onStopped()
        TestCase.assertEquals("Can extract JAR", ListenableWorker.Result.success(), result)

        worker = JavaExtractionWorker(appContext.targetContext, data)
        result = worker.doWork()
        worker.onStopped()
        TestCase.assertEquals("Can extract JAVA Code", ListenableWorker.Result.success(), result)

        worker = ResourcesExtractionWorker(appContext.targetContext, data)
        result = worker.doWork()
        worker.onStopped()
        TestCase.assertEquals("Can extract resources", ListenableWorker.Result.success(), result)
    }
}