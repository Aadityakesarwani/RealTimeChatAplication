package com.innovativetools.firebase.chat.activities.models

class LocationAddress(
    var name: String,
    var address: String,
    var latitude: Double,
    var longitude: Double
) {

    override fun toString(): String {
        return "LocationAddress{" +
                "name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                '}'
    }
}