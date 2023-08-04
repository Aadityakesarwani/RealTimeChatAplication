package com.innovativetools.firebase.chat.activities.views.files

import android.text.TextUtils
import com.innovativetools.firebase.chat.activities.views.files.SDUtil
import android.os.Build
import android.os.Environment
import android.annotation.SuppressLint
import android.content.Context
import androidx.annotation.RequiresApi
import java.io.File
import java.lang.reflect.TypeVariable
import java.util.*
import java.util.function.IntFunction

// This has not been tested extensively
// Feedback is needed from developers
// Some devices do not accept accessing the SD Card UID
// If the device doesn't allow using the UID, we have to replace it with the name of the SD Card.
object SDUtil {
    private val EXTERNAL_STORAGE = System.getenv("EXTERNAL_STORAGE")
    private val SECONDARY_STORAGES = System.getenv("SECONDARY_STORAGE")
    private val EMULATED_STORAGE_TARGET = System.getenv("EMULATED_STORAGE_TARGET")
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @JvmStatic
    fun getStorageDirectories(context: Context): Array<String> {
        val availableDirectoriesSet: MutableSet<String> = HashSet()
        if (!TextUtils.isEmpty(EMULATED_STORAGE_TARGET)) {
            availableDirectoriesSet.add(emulatedStorageTarget)
        } else {
            availableDirectoriesSet.addAll(getExternalStorage(context))
        }
        Collections.addAll(availableDirectoriesSet as
                MutableCollection<in String?>, *allSecondaryStorages)
        return availableDirectoriesSet.toTypedArray()
    }


    private fun getExternalStorage(context: Context): Set<String> {
        val availableDirectoriesSet: MutableSet<String> = HashSet()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val files = getExternalFilesDirs(context)
            for (file in files) {
                if (file != null) {
                    val applicationSpecificAbsolutePath = file.absolutePath
                    var rootPath = applicationSpecificAbsolutePath.substring(
                        9, applicationSpecificAbsolutePath.indexOf("Android/data")
                    )
                    rootPath = rootPath.substring(rootPath.indexOf("/storage/") + 1)
                    rootPath = rootPath.substring(0, rootPath.indexOf("/"))
                    if (rootPath != "emulated") {
                        availableDirectoriesSet.add(rootPath)
                    }
                }
            }
        } else {
            if (TextUtils.isEmpty(EXTERNAL_STORAGE)) {
                availableDirectoriesSet.addAll(availablePhysicalPaths)
            } else {
                availableDirectoriesSet.add(EXTERNAL_STORAGE)
            }
        }
        return availableDirectoriesSet
    }

    private val emulatedStorageTarget: String
        private get() {
            var rawStorageId = ""
            val path = Environment.getExternalStorageDirectory().absolutePath
            val folders = path.split(File.separator).toTypedArray()
            val lastSegment = folders[folders.size - 1]
            if (!TextUtils.isEmpty(lastSegment) && TextUtils.isDigitsOnly(lastSegment)) {
                rawStorageId = lastSegment
            }
            return if (TextUtils.isEmpty(rawStorageId)) {
                EMULATED_STORAGE_TARGET
            } else {
                EMULATED_STORAGE_TARGET + File.separator + rawStorageId
            }
        }
    private val allSecondaryStorages: Array<String?>
        private get() {
            if (!TextUtils.isEmpty(SECONDARY_STORAGES)) {
                assert(SECONDARY_STORAGES != null)
                return SECONDARY_STORAGES!!.split(File.pathSeparator).toTypedArray()
            }
            return arrayOfNulls(0)
        }
    private val availablePhysicalPaths: List<String>
        private get() {
            val availablePhysicalPaths: MutableList<String> = ArrayList()
            for (physicalPath in KNOWN_PHYSICAL_PATHS) {
                val file = File(physicalPath)
                if (file.exists()) {
                    availablePhysicalPaths.add(physicalPath)
                }
            }
            return availablePhysicalPaths
        }

    private fun getExternalFilesDirs(context: Context): Array<File> {
        return context.getExternalFilesDirs(null)
    }

    @SuppressLint("SdCardPath")
    private val KNOWN_PHYSICAL_PATHS = arrayOf(
        "/storage/sdcard0",
        "/storage/sdcard1",
        "/storage/extsdcard",
        "/storage/sdcard0/external_sdcard",
        "/mnt/extsdcard",
        "/mnt/sdcard/external_sd",
        "/mnt/sdcard/ext_sd",
        "/mnt/external_sd",
        "/mnt/media_rw/sdcard1",
        "/removable/microsd",
        "/mnt/emmc",
        "/storage/external_SD",
        "/storage/ext_sd",
        "/storage/removable/sdcard1",
        "/data/sdext",
        "/data/sdext2",
        "/data/sdext3",
        "/data/sdext4",
        "/sdcard1",
        "/sdcard2",
        "/storage/microsd"
    )
}