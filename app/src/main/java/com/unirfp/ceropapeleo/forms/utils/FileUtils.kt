package com.unirfp.ceropapeleo.forms.utils

import android.os.Environment
import java.io.File

fun findLatestDownloadedPdf(): File? {
    val downloadsDir = Environment.getExternalStoragePublicDirectory(
        Environment.DIRECTORY_DOWNLOADS
    )

    return downloadsDir.listFiles()
        ?.filter { file ->
            file.isFile &&
                    file.name.startsWith("formulario-790-006_es_es") &&
                    file.extension.equals("pdf", ignoreCase = true)
        }
        ?.maxByOrNull { it.lastModified() }
}