package com.carlos.ticketsdesoporte

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.fragment_crear_caso.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream


class CrearCaso : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var dbreference: DatabaseReference
    private lateinit var storage: FirebaseStorage
    private lateinit var streference: StorageReference
    private var cantidad = "0"
    private var adjunto_data: MutableList<Adjunto> = mutableListOf()
    private lateinit var key: String
    private var programadorid = 0



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_crear_caso, container, false)
    }

    override fun onStart() {
        super.onStart()



        database = FirebaseDatabase.getInstance()
        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()
        streference = storage.reference
        key = database.getReference("Casos").push().key.toString()



        NumeroDeCaso(database)
        ProgramadoresCombo(database)


        cBotonCrearCaso.setOnClickListener { _ ->
            GuardarCaso()
        }
        cAdjuntarBoton.setOnClickListener { _ ->
            CargarAdjuntos()
        }


        activity!!.title = "Crear Caso"


    }


    private fun GuardarCaso() {
        var titulo: CharSequence
        var contenido: CharSequence
        var asignado_nombre = ""
        var asignado_por = ""
        var programador_nombre = ""
        var programador = ""
        var observaciones: CharSequence
        var fecha: String



        if (ValidarCampos()) {
            titulo = cTituloContenido.text.trim()
            contenido = cContenidoContenido.text.trim()
            asignado_nombre = auth.currentUser!!.displayName!!
            asignado_por = auth.currentUser!!.uid
            programador_nombre = ((cAsignarContenido).selectedItem as Programadores).nombre_completo
            programador = ((cAsignarContenido).selectedItem as Programadores).uid
            observaciones = cObservacionesContenido.text.trim()

            fecha = SimpleDateFormat("YYYY-MM-dd").format(Calendar.getInstance().time).toString()




            cantidad = (cantidad.toInt() + 1).toString()


            dbreference = database.getReference("Casos").child(key)
            dbreference.setValue(Casos(key, cantidad, asignado_nombre, asignado_por, programador_nombre, programador, titulo.toString(), contenido.toString(), observaciones.toString(), adjunto_data.count().toString(), fecha, "1"))


            val dbreference_adjuntos = database.getReference("Adjuntos").child(key)

            adjunto_data.forEach {
                val options = BitmapFactory.Options()
                options.inSampleSize = 2
                var stream = context!!.contentResolver.openInputStream(it.data.data)
                var final = ByteArrayOutputStream()
                var bitmap =  BitmapFactory.decodeStream(stream, null, options)
                bitmap.compress(Bitmap.CompressFormat.WEBP,50, final)

                var ruta = database.getReference("Casos").push().key.toString()


                streference.child("${key}/${ruta}").putBytes(final.toByteArray()).addOnSuccessListener { taskSnapshot ->
                    taskSnapshot.storage.downloadUrl.addOnSuccessListener {
                        dbreference_adjuntos.child(ruta).setValue(UrlAdjunto(it.toString()))


                    }


                }.addOnFailureListener {
                    Funciones().MensajeToast(activity!!.applicationContext, "Hubo un error en la carga")

                }
            }


            Funciones().FCMNotificaciones(programador,"${asignado_nombre} te a Asignado el Caso #${cantidad}",key)

            Funciones().MensajeToast(view!!.context, "El caso numero ${cantidad} fue creado y asignado a ${programador_nombre}")
            activity!!.title = "Tickets de Soporte"
            Funciones().CerrarFragment(view!!,"CREAR")
        }

    }

    private fun ValidarCampos(): Boolean {
        val titulo = cTituloContenido.text.trim()
        val contenido = cContenidoContenido.text.trim()
        var error = ""
        var titulo_error = "Campo(s) Obligatorio(s)"


        if (!TextUtils.isEmpty(titulo) && !TextUtils.isEmpty(contenido)) {
            return true
        } else {
            if (TextUtils.isEmpty(titulo)) {
                error = error + " - Titulo\n"
            }
            if (TextUtils.isEmpty(contenido)) {
                error = error + " - Contenido\n"
            }

            if (TextUtils.isEmpty(titulo)) {
                cTituloContenido.requestFocus()
            } else if (TextUtils.isEmpty(contenido)) {
                cContenidoContenido.requestFocus()
            }
            Funciones().MensajeAlerta(view!!.context, titulo_error, error)

            return false
        }
    }

    private fun NumeroDeCaso(firebaseDatabase: FirebaseDatabase) {
        var databaseReference = firebaseDatabase.getReference("Casos")



        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    cantidad = p0.childrenCount.toString()
                } else {
                    cantidad = "0"
                }
            }

        })
    }


    private fun ProgramadoresCombo(firebaseDatabase: FirebaseDatabase) {
        var databaseReference = firebaseDatabase.getReference("Usuarios").orderByChild("nombre_completo")
        var programadores: MutableList<Programadores> = mutableListOf()



        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    activity!!.runOnUiThread {
                        programadores.clear()
                        p0.children.mapNotNullTo(programadores) {
                            it.getValue<Programadores>(Programadores::class.java)
                        }

                        cAsignarContenido.adapter = ArrayAdapter(view!!.context, android.R.layout.simple_spinner_dropdown_item, programadores)
                        cAsignarContenido.setSelection(programadorid)


                    }
                }
            }

        })
    }

    private fun CargarAdjuntos() {
        programadorid = cAsignarContenido.selectedItemPosition
        var intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Seleccione una imagen"), 123)


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 123 && resultCode == Activity.RESULT_OK && data != null) {
            val progressbar = ProgressDialog(context)
            progressbar.setMessage("Subiendo Imagen")
            progressbar.show()
            adjunto_data.add(Adjunto(data))
            cCantidadAdjuntos.text = "${adjunto_data.count()} archivo(s) adjunto(s)"
            progressbar.dismiss()
            cAsignarContenido.setSelection(programadorid)
        }
    }

}
