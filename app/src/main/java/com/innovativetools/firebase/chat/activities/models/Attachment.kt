package com.innovativetools.firebase.chat.activities.models

import android.os.Parcelable
import android.os.Parcel
import android.os.Parcelable.Creator

class Attachment : Parcelable {
    var name: String? = null
    var fileName: String? = null
    var data: String? = null
    var url: String? = null
    var duration: String? = null
    var bytesCount: Long = 0

    constructor() {}
    protected constructor(`in`: Parcel) {
        name = `in`.readString()
        fileName = `in`.readString()
        data = `in`.readString()
        url = `in`.readString()
        duration = `in`.readString()
        bytesCount = `in`.readLong()
    }

//    fun getFileName(): String {
//        return if (fileName != null) fileName!! else ""
//    }

//    fun setFileName(name: String?) {
//        fileName = name
//    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(name)
        dest.writeString(fileName)
        dest.writeString(data)
        dest.writeString(url)
        dest.writeString(duration)
        dest.writeLong(bytesCount)
    }

    companion object {
        @JvmField
        val CREATOR: Creator<Attachment?> = object : Creator<Attachment?> {
            override fun createFromParcel(`in`: Parcel): Attachment? {
                return Attachment(`in`)
            }

            override fun newArray(size: Int): Array<Attachment?> {
                return arrayOfNulls(size)
            }
        }
    }


}