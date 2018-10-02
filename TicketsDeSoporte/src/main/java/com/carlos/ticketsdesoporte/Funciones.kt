package com.carlos.ticketsdesoporte

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.RemoteViews
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import okhttp3.*
import java.io.IOException
import java.util.*

class Funciones {

    fun ActualizarObservaciones(uid: String, observaciones: String) {
        var caso = FirebaseDatabase.getInstance()
        caso.getReference("Casos").child(uid).child("observaciones").setValue(observaciones)
    }


    fun EstatusCaso(uid: String, estatus: String) {
        var caso = FirebaseDatabase.getInstance()
        caso.getReference("Casos").child(uid).child("estatus").setValue(estatus)
    }

    fun CerrarFragment(view: View, tag: String? = null) {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            var frag = (view.rootView.context as AppCompatActivity).supportFragmentManager.findFragmentByTag(tag)
            (view.rootView.context as AppCompatActivity).supportFragmentManager.beginTransaction().setCustomAnimations(R.anim.animacion_entrada, R.anim.animacion_salida,R.anim.animacion_entrada, R.anim.animacion_salida)
                    .remove(frag).commit()
        } else {
            var frag = (view.context as AppCompatActivity).supportFragmentManager.findFragmentByTag(tag)
            (view.context as AppCompatActivity).supportFragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.animacion_entrada, R.anim.animacion_salida,R.anim.animacion_entrada, R.anim.animacion_salida)
                    .remove(frag).commit()
        }
    }


    fun MensajeAlerta(context: Context, titulo: String, error: String) {
        AlertDialog.Builder(context).setIcon(R.drawable.ic_error).setTitle(titulo)
                .setMessage(error)
                .setPositiveButton("OK") { _, _ -> }.show()

    }

    fun MensajeToast(context: Context, string: String) {
        Toast.makeText(context, string, Toast.LENGTH_LONG).show()
    }


    fun ActivarFragmento(fragment: Fragment, view: View, casos: Casos? = null, url: String? = null, tag: String? = null) {

        val args = Bundle()
        args.putSerializable("caso", casos)
        args.putString("url", url)
        val fragFinal = fragment
        fragment.arguments = args


        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            (view.rootView.context as AppCompatActivity)
                    .supportFragmentManager
                    .beginTransaction()
                    .setCustomAnimations(R.anim.animacion_entrada, R.anim.animacion_salida,R.anim.animacion_entrada, R.anim.animacion_salida)
                    .replace(R.id.Fragmento, fragFinal, tag)
                    .commitAllowingStateLoss()
        } else {
            (view.context as AppCompatActivity)
                    .supportFragmentManager
                    .beginTransaction()
                    .setCustomAnimations(R.anim.animacion_entrada, R.anim.animacion_salida,R.anim.animacion_entrada, R.anim.animacion_salida)
                    .replace(R.id.Fragmento, fragFinal, tag)
                    .commitAllowingStateLoss()
        }

    }


    fun Notificaciones(context: Context, remoteMessage: RemoteMessage?=null, mensaje: String?=null) {
        lateinit var notificationManager: NotificationManager
        lateinit var notificationChannel: NotificationChannel
        lateinit var builder: Notification.Builder
        var channelId = "ticketsdesoporte"
        var description = "Notificacion de Tickets de Soporte"
        val intent = Intent(context, ActividadPrincipal::class.java)

        if (remoteMessage != null) {
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            intent.putExtra("CasoUid", remoteMessage.data.get("caso_uid").toString())
        }

        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        var contentView = RemoteViews("com.carlos.ticketsdesoporte", R.layout.notificacion_layout)
        if (remoteMessage == null) {
            contentView.setTextViewText(R.id.NotificacionContenido, mensaje)
        } else {
            contentView.setTextViewText(R.id.NotificacionContenido, remoteMessage!!.data.get("body"))
        }
        contentView.setTextViewText(R.id.NotificacionHora, "- Hora: ${Calendar.getInstance().get(Calendar.HOUR_OF_DAY).toString().padStart(2, '0')}:${Calendar.getInstance().get(Calendar.MINUTE).toString().padStart(2, '0')}")




        notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannel = NotificationChannel(channelId, description, NotificationManager.IMPORTANCE_HIGH)
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.GREEN
            notificationManager.createNotificationChannel(notificationChannel)




            builder = Notification.Builder(context, channelId)
                    .setContent(contentView)
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setLargeIcon(BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher))
                    .setContentIntent(pendingIntent)

        } else {
            builder = Notification.Builder(context)
                    .setContent(contentView)
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setLargeIcon(BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher))
                    .setContentIntent(pendingIntent)
        }

        notificationManager.notify(1234, builder.build())
    }

    fun FCM(para: String, mensaje: String, data: String, callback: Callback) {
        FirebaseDatabase.getInstance().getReference("FCM").child(para).addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0!!.exists()) {
                    var client = OkHttpClient()
                    var JSON = MediaType.get("application/json")

                    var json = json()
                    json.to = p0.child("fcm_key").value.toString()
                    json.data.body = mensaje
                    json.data.caso_uid = data

                    var gson = Gson().toJson(json)

                    val body = RequestBody.create(JSON, gson)
                    val request = Request.Builder()
                            .url(PersistenciaFirebase().url)
                            .post(body)
                            .addHeader("Authorization", PersistenciaFirebase().ServerKey)
                            .build()
                    val call = client.newCall(request)
                    call.enqueue(callback)
                }
            }

        })

    }

    fun FCMNotificaciones(para: String, mensaje: String, data: String? = null) {

        FCM(para, mensaje, data!!, object : Callback {
            override fun onFailure(call: Call?, e: IOException?) {

            }

            override fun onResponse(call: Call?, response: Response?) {

            }
        })
    }
}
