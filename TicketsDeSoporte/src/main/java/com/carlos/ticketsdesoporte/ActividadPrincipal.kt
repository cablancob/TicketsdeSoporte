package com.carlos.ticketsdesoporte

import android.app.AlarmManager
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.activity_actividad_principal.*
import kotlinx.android.synthetic.main.app_bar_actividad_principal.*
import kotlinx.android.synthetic.main.content_actividad_principal.*
import kotlinx.android.synthetic.main.content_actividad_principal.view.*
import kotlinx.android.synthetic.main.nav_header_actividad_principal.view.*
import java.util.*


class ActividadPrincipal : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, GoogleApiClient.OnConnectionFailedListener {


    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    val lista_casos: MutableList<Casos> = mutableListOf()


    var tipo_usuario = ""

    var mGoogleApiClient: GoogleApiClient? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_actividad_principal)
        setSupportActionBar(toolbar)


        database = FirebaseDatabase.getInstance()
        auth = FirebaseAuth.getInstance()

        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginGoogle::class.java))
        } else {

            val fcmBD = database.getReference("FCM").child(auth!!.currentUser!!.uid)
            if (FirebaseInstanceId.getInstance().token != null) {
                fcmBD.setValue(FCMUsuario(auth!!.currentUser!!.displayName.toString(), FirebaseInstanceId.getInstance().token!!))
            }

            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, 10)
            calendar.set(Calendar.MINUTE, 50)
            calendar.set(Calendar.SECOND, 0)

            val am = this!!.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            var intent = Intent(this,ReceptorNotificaciones::class.java)
            intent.action="com.ticketsdesoporte.notificacion"
            val pi = PendingIntent.getBroadcast(this,0,intent, PendingIntent.FLAG_UPDATE_CURRENT)


            am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, AlarmManager.INTERVAL_DAY, pi)



            casos_recyclerView.layoutManager = LinearLayoutManager(this)
            casos_recyclerView.layoutAnimation = AnimationUtils.loadLayoutAnimation(casos_recyclerView.context,R.anim.animacion_recycle_layout)



            TipoUsuario(database)


            nav_view.getHeaderView(0).nombre_usuario.text = auth.currentUser!!.displayName
            nav_view.getHeaderView(0).correo_usuario.text = auth.currentUser!!.email
            Glide.with(this).load(auth.currentUser!!.photoUrl).error(Glide.with(this).load(R.mipmap.ic_no_contact)).apply(RequestOptions().circleCrop()).into(nav_view.getHeaderView(0).foto_usuario)


            val toggle = ActionBarDrawerToggle(
                    this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
            drawer_layout.addDrawerListener(toggle)
            toggle.syncState()

            nav_view.setNavigationItemSelectedListener(this)
            mGoogleApiClient = GoogleApiClient.Builder(this)
                    .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                    .addApi(Auth.GOOGLE_SIGN_IN_API)
                    .build()


        }
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            if (this.supportFragmentManager.fragments.count() > 2) {
                if (this.supportFragmentManager.findFragmentByTag("CREAR") != null) {
                    title = "Tickets de Soporte"
                    Funciones().CerrarFragment(view, "CREAR")
                }
                if (this.supportFragmentManager.findFragmentByTag("VER") != null) {
                    title = "Tickets de Soporte"
                    Funciones().CerrarFragment(view, "VER")
                }
                if (this.supportFragmentManager.findFragmentByTag("LISTA_ADJUNTOS") != null) {
                    var caso = (this.supportFragmentManager.findFragmentByTag("LISTA_ADJUNTOS").arguments?.getSerializable("caso")) as Casos
                    Funciones().CerrarFragment(view, "LISTA_ADJUNTOS")
                    Funciones().ActivarFragmento(VerCarso(), view, caso, tipo_usuario, "VER")
                }
                if (this.supportFragmentManager.findFragmentByTag("ADJUNTO") != null) {
                    var caso = (this.supportFragmentManager.findFragmentByTag("ADJUNTO").arguments?.getSerializable("caso")) as Casos
                    Funciones().CerrarFragment(view, "ADJUNTO")
                    toolbar?.visibility = View.VISIBLE
                    Funciones().ActivarFragmento(ListarAdjuntos(), view, caso, tipo_usuario, "LISTA_ADJUNTOS")
                }


            } else {
                AlertDialog.Builder(this).setIcon(R.drawable.ic_error).setTitle("Cerrar Aplicación")
                        .setMessage("¿Seguro que quiere salir de la aplicación?")
                        .setPositiveButton("SI") { _, _ ->
                            finish()
                        }
                        .setNegativeButton("NO") { _, _ -> }
                        .show()
            }


        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.action_settings -> return true
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_desloguear -> {
                cerrar_session()
            }

            R.id.nav_crearcaso -> {
                Funciones().ActivarFragmento(CrearCaso(), view, Casos(), "", "CREAR")
            }

        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    fun cerrar_session() {

        auth = FirebaseAuth.getInstance()
        auth.signOut()
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback { }
        startActivity(Intent(this, LoginGoogle::class.java))

    }

    private fun listar_casos(firebaseDatabase: FirebaseDatabase, tipo_usuario: String, uid: String) {

        if (tipo_usuario.equals("1")) {

            firebaseDatabase.getReference("Casos").orderByChild("estatus").addValueEventListener(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {

                }

                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.exists()) {
                        lista_casos.clear()
                        p0.children.mapNotNullTo(lista_casos) {
                            it.getValue<Casos>(Casos::class.java)
                        }
                        lista_casos.sortBy { it.estatus }
                        var contar_casos = lista_casos.filter { it.estatus == "1" } as MutableList<Casos>
                        cantidad_casos_titulo_cotenido.text = "${contar_casos.count()} Caso(s)"
                        casos_recyclerView.adapter = AdaptadorPrincipal(lista_casos, tipo_usuario)
                        casos_recyclerView.scheduleLayoutAnimation()
                        if (intent.getStringExtra("CasoUid") != null) {
                            var caso_uid = intent.getStringExtra("CasoUid")
                            var ver_caso = lista_casos.filter { it.uid == caso_uid }
                            Funciones().ActivarFragmento(VerCarso(),view,ver_caso.get(0), tipo_usuario, "VER")
                        }
                    } else {
                        cantidad_casos_titulo_cotenido.text = "0 Caso(s)"
                    }
                }

            })
        } else {
            firebaseDatabase.getReference("Casos").orderByChild("programador").equalTo(uid).addValueEventListener(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {

                }

                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.exists()) {
                        lista_casos.clear()
                        p0.children.mapNotNullTo(lista_casos) {
                            it.getValue<Casos>(Casos::class.java)
                        }
                        lista_casos.sortBy { it.estatus }
                        var contar_casos = lista_casos.filter { it.estatus == "1" } as MutableList<Casos>
                        cantidad_casos_titulo_cotenido.text = "${contar_casos.count()} Caso(s)"
                        casos_recyclerView.adapter = AdaptadorPrincipal(lista_casos, tipo_usuario)
                        casos_recyclerView.scheduleLayoutAnimation()
                        if (intent.getStringExtra("CasoUid") != null) {
                            var caso_uid = intent.getStringExtra("CasoUid")
                            var ver_caso = lista_casos.filter { it.uid == caso_uid }
                            Funciones().ActivarFragmento(VerCarso(),view,ver_caso.get(0), tipo_usuario, "VER")
                        }
                    } else {
                        cantidad_casos_titulo_cotenido.text = "0 Caso(s)"
                    }
                }

            })
        }


    }


    private fun TipoUsuario(firebaseDatabase: FirebaseDatabase) {
        firebaseDatabase.getReference("Usuarios").child(auth.currentUser!!.uid).addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    tipo_usuario = p0.child("tipo_usuario").value.toString()
                    if (tipo_usuario.equals("2")) {
                        nav_view.getHeaderView(0)
                        nav_view.menu.removeItem(R.id.nav_crearcaso)
                    }

                    listar_casos(database, tipo_usuario, auth.currentUser!!.uid)
                }
            }

        })
    }


    override fun onConnectionFailed(p0: ConnectionResult) {

    }
}


