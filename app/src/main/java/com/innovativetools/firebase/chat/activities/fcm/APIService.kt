package com.innovativetools.firebase.chat.activities.fcm

import retrofit2.http.POST
import com.innovativetools.firebase.chat.activities.fcmmodels.Sender
import com.innovativetools.firebase.chat.activities.fcmmodels.MyResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers

interface APIService {
    @Headers(
        "Content-Type:application/json",
        "Authorization: key=AAAAUTX7VYY:APA91bGQfaCf47qzE01QeFGR4pF54DSL3m8CaErrNQUtM-OqaJJKEoX8VpVTn3PkcYZqVM_2ZOghF7rhJ9FSMlMIXXASFH07XrmCHP9tMxwxeW6rp3VF4WhqWj9StKZBJjyfvKp2b5Iw"
    )
    @POST("fcm/send")
    fun sendNotification(@Body body: Sender?): Call<MyResponse?>?
}