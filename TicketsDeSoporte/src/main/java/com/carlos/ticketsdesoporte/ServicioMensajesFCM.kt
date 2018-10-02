package com.carlos.ticketsdesoporte


import android.util.Log
import android.widget.Toast
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class ServicioMensajesFCM : FirebaseMessagingService() {
    override fun onMessageReceived(p0: RemoteMessage?) {
        super.onMessageReceived(p0)


        if (p0!!.data.isNotEmpty()) {
//            Log.d("DATA NOTIFCACION", "Message data : " + p0.getData().get("caso_uid"))
            Funciones().Notificaciones(applicationContext,p0)
        }

        if (p0!!.notification != null) {

        }
    }
}
