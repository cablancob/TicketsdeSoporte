package com.carlos.ticketsdesoporte

import android.app.Application
import com.google.firebase.database.FirebaseDatabase


class PersistenciaFirebase: Application() {


    val ServerKey = "key=AAAAPbnccco:APA91bFIsT6QwPL2yJmgo0eMABXOHrKctIIqsgV9kUoQsPtksTDXh9liNwl2Lzed-ouI3KJagk-a3m7qBVEyF-Rv4SNYbjkVaJ8edzj5Zkj2wnQIcLakXtuU1vK6a6X4IFAZxE4u933Jufq-h4zgfaYKbsdpsxO9kw"
    var url = "https://fcm.googleapis.com/fcm/send"




    override fun onCreate() {
        super.onCreate()
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)


    }

}