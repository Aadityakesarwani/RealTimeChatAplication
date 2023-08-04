package com.innovativetools.firebase.chat.activities.views.voiceplayer

import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.lang.Exception

object FileUtils {
    @JvmStatic
    fun fileToBytes(file: File): ByteArray {
        val size = file.length().toInt()
        val bytes = ByteArray(size)
        try {
            val buf = BufferedInputStream(FileInputStream(file))
            buf.read(bytes, 0, bytes.size)
            buf.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return bytes
    }
}