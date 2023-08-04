package com.innovativetools.firebase.chat.activities.models

/**
 * Created by BytesBee.
 *
 * @author BytesBee
 * @link http://bytesbee.com
 */
class Setting {
    var force_version_code //This must be integer and updated versionCode from build.gradle
            = 0
    var isForce_update //1= Yes , 0=No
            = false
    var force_title: String? = null
    var force_message: String? = null
    var force_yes_button: String? = null
    var force_no_button: String? = null
    var force_source //Google Playstore OR Live Server APK URL
            : String? = null
    var force_apk_link: String? = null

    //    private int max_gD:\Projects\Android\BytesBee\CodeCanyon\FirebaseChat\4. Publish-Release\Release_v2.1roup_size = 10;
    var max_group_msg = ""
    var update_app_text = ""
    var max_size_audio = 10
    var max_size_video = 15
    var max_size_document = 5

    //    public int getMax_group_size() {
    //        return max_group_size;
    //    }
    //
    //    public void setMax_group_size(int max_group_size) {
    //        this.max_group_size = max_group_size;
    //    }
    var max_group_size: Int
        get() = 20
        set(max_group_size) {
            var max_group_size = max_group_size
            max_group_size = 20
        }

    override fun toString(): String {
        return "Setting{" +
                "force_version_code=" + force_version_code +
                ", force_update=" + isForce_update +
                ", force_title='" + force_title + '\'' +
                ", force_message='" + force_message + '\'' +
                ", force_yes_button='" + force_yes_button + '\'' +
                ", force_no_button='" + force_no_button + '\'' +
                ", force_source='" + force_source + '\'' +
                ", force_apk_link='" + force_apk_link + '\'' +  //                ", max_group_size='" + max_group_size + '\'' +
                ", max_group_size='" + 20 + '\'' +
                ", max_group_msg='" + max_group_msg + '\'' +
                '}'
    }
}