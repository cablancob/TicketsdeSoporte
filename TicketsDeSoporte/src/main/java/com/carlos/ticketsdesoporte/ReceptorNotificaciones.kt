package com.carlos.ticketsdesoporte

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.RemoteMessage
import java.util.*

class ReceptorNotificaciones : BroadcastReceiver() {

    private var casos: MutableList<Casos> = mutableListOf()
    private lateinit var auth: FirebaseAuth


    override fun onReceive(context: Context, intent: Intent) {
        auth = FirebaseAuth.getInstance()
        if (intent.action.equals("com.ticketsdesoporte.notificacion")) {
            if (auth.currentUser != null) {
                FirebaseDatabase.getInstance().getReference("Casos").orderByChild("programador").equalTo(FirebaseAuth.getInstance().currentUser!!.uid).addValueEventListener(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {
                    }

                    override fun onDataChange(p0: DataSnapshot) {
                        if (p0.exists()) {
                            casos.clear()
                            p0.children.mapNotNullTo(casos) {
                                it.getValue<Casos>(Casos::class.java)
                            }
                            casos = casos.filter { it.estatus == "1" } as MutableList<Casos>
                            if (casos.count() > 0 && intent.action.equals("com.ticketsdesoporte.notificacion") && (Calendar.getInstance().get(Calendar.HOUR_OF_DAY).toString().equals("10"))) {
                                Funciones().Notificaciones(context,null, "${casos[0].nombre_programador} tiene(s) ${casos.count()} caso(s) pendiente(s)")
                                intent.action = ""
                            }

                        }

                    }


                })
            }

        }
    }
}
