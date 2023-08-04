package com.innovativetools.firebase.chat.activities.views.files


import android.mtp.MtpConstants
import java.util.HashMap


object MediaFile {
    private val sFileTypeMap = HashMap<String, MediaFileType>()
    private val sMimeTypeMap = HashMap<String, Int>()

    // maps file extension to MTP format code
    private val sFileTypeToFormatMap = HashMap<String, Int>()

    // maps mime type to MTP format code
    private val sMimeTypeToFormatMap = HashMap<String, Int>()

    // maps MTP format code to mime type
    private val sFormatToMimeTypeMap = HashMap<Int, String>()

    // Audio file types -  aac, amr, awb, mp3, mp4, ogg, wav, opus,
    const val FILE_TYPE_AAC = 1
    const val FILE_TYPE_AMR = 2
    const val FILE_TYPE_AWB = 3
    const val FILE_TYPE_MP3 = 4
    const val FILE_TYPE_OGG = 5
    const val FILE_TYPE_WAV = 6
    const val FILE_TYPE_M4A = 7
    const val FILE_TYPE_MKA = 8
    private const val FIRST_AUDIO_FILE_TYPE = FILE_TYPE_AAC
    private const val LAST_AUDIO_FILE_TYPE = FILE_TYPE_MKA

    // MIDI file types
    const val FILE_TYPE_MID = 11
    const val FILE_TYPE_SMF = 12
    const val FILE_TYPE_IMY = 13
    private const val FIRST_MIDI_FILE_TYPE = FILE_TYPE_MID
    private const val LAST_MIDI_FILE_TYPE = FILE_TYPE_IMY

    // Video file types -  avi, mov, mp4, webm, 3gp, mkv, mpeg
    const val FILE_TYPE_AVI = 21
    const val FILE_TYPE_MOV = 22
    const val FILE_TYPE_MOV_QUICK = 23
    const val FILE_TYPE_MP4 = 24
    const val FILE_TYPE_WEBM = 25
    const val FILE_TYPE_3GPP = 26
    const val FILE_TYPE_MKV = 27

    //    public static final int FILE_TYPE_M4V = 27;
    //    public static final int FILE_TYPE_3GPP2 = 28;
    //    public static final int FILE_TYPE_WMV = 29;
    //    public static final int FILE_TYPE_ASF = 30;
    //    public static final int FILE_TYPE_MP2TS = 31;
    private const val FIRST_VIDEO_FILE_TYPE = FILE_TYPE_AVI
    private const val LAST_VIDEO_FILE_TYPE = FILE_TYPE_MKV

    // More video file types
    const val FILE_TYPE_MP2PS = 200
    private const val FIRST_VIDEO_FILE_TYPE2 = FILE_TYPE_MP2PS
    private const val LAST_VIDEO_FILE_TYPE2 = FILE_TYPE_MP2PS

    // Other popular file types
    const val FILE_TYPE_TEXT = 100
    const val FILE_TYPE_PDF = 102
    const val FILE_TYPE_MS_WORD = 104
    const val FILE_TYPE_MS_EXCEL = 105
    const val FILE_TYPE_MS_POWERPOINT = 106
    const val FILE_TYPE_ZIP = 107
    private const val FIRST_DOC_FILE_TYPE = FILE_TYPE_TEXT
    private const val LAST_DOC_FILE_TYPE = FILE_TYPE_ZIP
    fun addFileType(extension: String, fileType: Int, mimeType: String) {
        sFileTypeMap[extension] = MediaFileType(fileType, mimeType)
        sMimeTypeMap[mimeType] = fileType
    }

    fun addFileType(extension: String, fileType: Int, mimeType: String, mtpFormatCode: Int) {
        addFileType(extension, fileType, mimeType)
        sFileTypeToFormatMap[extension] = mtpFormatCode
        sMimeTypeToFormatMap[mimeType] = mtpFormatCode
        sFormatToMimeTypeMap[mtpFormatCode] = mimeType
    }

    init {
        addFileType("AAC", FILE_TYPE_AAC, "audio/aac", MtpConstants.FORMAT_AAC)
        addFileType("AAC", FILE_TYPE_AAC, "audio/aac-adts", MtpConstants.FORMAT_AAC)
        addFileType("AMR", FILE_TYPE_AMR, "audio/amr")
        addFileType("AWB", FILE_TYPE_AWB, "audio/amr-wb")
        addFileType("MP3", FILE_TYPE_MP3, "audio/mpeg", MtpConstants.FORMAT_MP3)
        addFileType("MPGA", FILE_TYPE_MP3, "audio/mpeg", MtpConstants.FORMAT_MP3)
        addFileType("OGG", FILE_TYPE_OGG, "audio/ogg", MtpConstants.FORMAT_OGG)
        addFileType("OGG", FILE_TYPE_OGG, "application/ogg", MtpConstants.FORMAT_OGG)
        addFileType("OGA", FILE_TYPE_OGG, "application/ogg", MtpConstants.FORMAT_OGG)
        addFileType("WAV", FILE_TYPE_WAV, "audio/x-wav", MtpConstants.FORMAT_WAV)
        addFileType("M4A", FILE_TYPE_M4A, "audio/mp4", MtpConstants.FORMAT_MPEG)
        addFileType("MKA", FILE_TYPE_MKA, "audio/x-matroska")
        addFileType("MID", FILE_TYPE_MID, "audio/midi")
        addFileType("MIDI", FILE_TYPE_MID, "audio/midi")
        addFileType("XMF", FILE_TYPE_MID, "audio/midi")
        addFileType("RTTTL", FILE_TYPE_MID, "audio/midi")
        addFileType("SMF", FILE_TYPE_SMF, "audio/sp-midi")
        addFileType("IMY", FILE_TYPE_IMY, "audio/imelody")
        addFileType("RTX", FILE_TYPE_MID, "audio/midi")
        addFileType("OTA", FILE_TYPE_MID, "audio/midi")
        addFileType("MXMF", FILE_TYPE_MID, "audio/midi")
        addFileType("AVI", FILE_TYPE_AVI, "video/avi")
        addFileType("MOV", FILE_TYPE_MOV, "video/mov")
        addFileType("MOV", FILE_TYPE_MOV_QUICK, "video/quicktime")
        addFileType("MP4", FILE_TYPE_MP4, "video/mp4", MtpConstants.FORMAT_MPEG)
        addFileType("WEBM", FILE_TYPE_WEBM, "video/webm")
        addFileType("3GP", FILE_TYPE_3GPP, "video/3gpp", MtpConstants.FORMAT_3GP_CONTAINER)
        addFileType("3GPP", FILE_TYPE_3GPP, "video/3gpp")
        addFileType("MKV", FILE_TYPE_MKV, "video/x-matroska")
        addFileType("MPEG", FILE_TYPE_MP4, "video/mpeg", MtpConstants.FORMAT_MPEG)
        addFileType("MPG", FILE_TYPE_MP4, "video/mpeg", MtpConstants.FORMAT_MPEG)
        addFileType("PDF", FILE_TYPE_PDF, "application/pdf")
        addFileType(
            "DOC",
            FILE_TYPE_MS_WORD,
            "application/msword",
            MtpConstants.FORMAT_MS_WORD_DOCUMENT
        )
        addFileType(
            "XLS",
            FILE_TYPE_MS_EXCEL,
            "application/vnd.ms-excel",
            MtpConstants.FORMAT_MS_EXCEL_SPREADSHEET
        )
        addFileType(
            "PPT",
            FILE_TYPE_MS_POWERPOINT,
            "application/mspowerpoint",
            MtpConstants.FORMAT_MS_POWERPOINT_PRESENTATION
        )
        addFileType("ZIP", FILE_TYPE_ZIP, "application/zip")
    }

    fun isAudioFileType(fileType: Int): Boolean {
        return fileType >= FIRST_AUDIO_FILE_TYPE &&
                fileType <= LAST_AUDIO_FILE_TYPE ||
                fileType >= FIRST_MIDI_FILE_TYPE &&
                fileType <= LAST_MIDI_FILE_TYPE
    }

    fun isVideoFileType(fileType: Int): Boolean {
        return (fileType >= FIRST_VIDEO_FILE_TYPE &&
                fileType <= LAST_VIDEO_FILE_TYPE
                || fileType >= FIRST_VIDEO_FILE_TYPE2 &&
                fileType <= LAST_VIDEO_FILE_TYPE2)
    }

    fun isDocumentFileType(fileType: Int): Boolean {
        return fileType >= FIRST_DOC_FILE_TYPE &&
                fileType <= LAST_DOC_FILE_TYPE
    }

    fun getFileType(path: String): MediaFileType? {
        val lastDot = path.lastIndexOf('.')
        return if (lastDot < 0) null else sFileTypeMap[path.substring(lastDot + 1).uppercase()]
    }

    class MediaFileType internal constructor(val fileType: Int, val mimeType: String) {
        override fun toString(): String {
            return "{" +
                    "fileType=" + fileType +
                    ", mimeType='" + mimeType + '\'' +
                    '}'
        }
    }
}