

package com.argonaut.showjava.decompilers

import android.content.Context
import androidx.work.Data
import androidx.work.ListenableWorker
import com.argonaut.showjava.R
import com.argonaut.showjava.data.SourceInfo
import com.argonaut.showjava.utils.ZipUtils
import com.argonaut.showjava.utils.ktx.cleanMemory
import jadx.api.JadxArgs
import jadx.api.JadxDecompiler
import jadx.api.impl.NoOpCodeCache
import org.apache.commons.io.FileUtils
import org.benf.cfr.reader.api.CfrDriver
import org.jetbrains.java.decompiler.main.decompiler.ConsoleDecompiler
import timber.log.Timber
import java.io.File
import java.io.FileNotFoundException


/**
 * The [JavaExtractionWorker] does the actual decompilation of extracting the `java` source from
 * the inputs. All of the three decompiler that we support, allow passing in multiple input files.
 * So, we pass in all of the chunks of either dex (in case of JaDX) or jar files (in case of CFR &
 * fernflower) as the input.
 */
class JavaExtractionWorker(context: Context, data: Data) : BaseDecompiler(context, data) {

    /**
     * Do the decompilation using the CFR decompiler.
     *
     * We set the `lowmem` flag as true to let CFR know that it has to be more aggressive in terms
     * of garbage collection and less-aggressive caching. This results in reduced performance. But
     * increase success rates for large inputs. Which is a good trade-off.
     */
    @Throws(Exception::class)
    private fun decompileWithCFR(jarInputFiles: File, javaOutputDir: File) {
        cleanMemory()
        val jarFiles = jarInputFiles.listFiles()
        val options = mapOf<String, String>(
            "outputdir" to javaOutputDir.canonicalPath,
            "lomem" to "true"
        )
        val cfrDriver = CfrDriver.Builder().withOptions(options).build()
        cfrDriver.analyse(jarFiles.map { it.canonicalPath })
    }

    /**
     * Do the decompilation using the JaDX decompiler.
     *
     * We set `threadsCount` as 1. This instructs JaDX to not spawn additional threads to prevent
     * issues on some devices.
     */
    @Throws(Exception::class)
    private fun decompileWithJaDX(dexInputFiles: File, javaOutputDir: File) {
        cleanMemory()

        val args = JadxArgs()
        args.outDirSrc = javaOutputDir
        args.inputFiles = dexInputFiles.listFiles().toMutableList()
      //  args.codeCache = NoOpCodeCache()
      //  args.isExportAsGradleProject = true
        args.threadsCount = 1

        val jadx = JadxDecompiler(args)

        jadx.load()
        jadx.saveSources()
        if (dexInputFiles.exists() && dexInputFiles.isDirectory && !keepIntermediateFiles) {
            dexInputFiles.deleteRecursively()
        }
    }

    /**
     * Do the decompilation using FernFlower decompiler.
     *
     * The out of the decompiler is a jar archive containing the decompiled java files. So, we look
     * for and extract the archive after the decompilation.
     */
    @Throws(Exception::class)
    private fun decompileWithFernFlower(jarInputFiles: File, javaOutputDir: File) {
        cleanMemory()

        ConsoleDecompiler.main(
            arrayOf(
                jarInputFiles.canonicalPath, javaOutputDir.canonicalPath
            )
        )

        javaOutputDir.listFiles().forEach { decompiledJarFile ->
            if (decompiledJarFile.exists() && decompiledJarFile.isFile && decompiledJarFile.extension == "jar") {
                ZipUtils.unzip(decompiledJarFile, javaOutputDir, printStream!!)
                decompiledJarFile.delete()
            } else {
                throw FileNotFoundException("Decompiled jar does not exist")
            }
        }
    }

    override fun doWork(): ListenableWorker.Result {
        Timber.tag("JavaExtraction")
        context.getString(R.string.decompilingToJava).let {
            buildNotification(it)
            setStep(it)
        }

        super.doWork()

        val sourceInfo = SourceInfo.from(workingDirectory)
            .setPackageLabel(packageLabel)
            .setPackageName(packageName)
            .persist()

        try {
            when (decompiler) {
                "jadx" -> decompileWithJaDX(outputDexFiles, outputJavaSrcDirectory)
                "cfr" -> decompileWithCFR(outputJarFiles, outputJavaSrcDirectory)
                "fernflower" -> decompileWithFernFlower(outputJarFiles, outputJavaSrcDirectory)
            }
        } catch (e: Exception) {
            return exit(e)
        }

        if (outputDexFiles.exists() && outputDexFiles.isDirectory && !keepIntermediateFiles) {
            outputDexFiles.deleteRecursively()
        }

        if (outputJarFiles.exists() && outputJarFiles.isDirectory && !keepIntermediateFiles) {
            outputJarFiles.deleteRecursively()
        }

        sourceInfo
            .setJavaSourcePresence(true)
            .setSourceSize(FileUtils.sizeOfDirectory(workingDirectory))
            .persist()

        return successIf(!outputJavaSrcDirectory.list().isNullOrEmpty())
    }
}
