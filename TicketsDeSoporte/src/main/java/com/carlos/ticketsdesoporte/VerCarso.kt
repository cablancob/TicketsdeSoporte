package com.carlos.ticketsdesoporte


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.content_actividad_principal.*
import kotlinx.android.synthetic.main.fragment_ver_carso.*

class VerCarso : Fragment() {

    private lateinit var caso: Casos
    private var tipo_usuario = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_ver_carso, container, false)

    }

    override fun onStart() {
        super.onStart()
        caso = arguments?.getSerializable("caso") as Casos
        tipo_usuario = arguments?.getString("url")!!
        activity?.title = "CASO # ${caso.numero}"
        vTituloContenido.text = caso.titulo
        vContenidoContenido.text = caso.contenido
        vObservacionesContenido.setText(caso.observaciones)
        vAsignadoPorContenido.text = caso.nombre_creador
        vFechaContenido.text = caso.fecha
        if (caso.estatus == "1") {
            vFechaContenido.setTextColor(ContextCompat.getColor(activity!!.applicationContext, android.R.color.holo_red_dark))
            vFechaContenido.text = "PENDIENTE"
            vCerrar.text = "Cerrar Caso"
        }
        if (caso.estatus == "2") {
            vFechaContenido.setTextColor(ContextCompat.getColor(activity!!.applicationContext, android.R.color.holo_green_dark))
            vFechaContenido.text = "CERRADO"
            vCerrar.text = "Abrir Caso"
        }

        if (tipo_usuario.equals("2") and (caso.estatus.equals("2"))) {
            vCerrar.visibility = View.GONE
        }

        if (caso.adjuntos.equals("0")) {
            vVerAdjuntos.visibility = View.GONE
        }

        vCerrar.setOnClickListener { v ->

            if (caso.estatus == "1") {
                Funciones().EstatusCaso(caso.uid, "2")
                Funciones().FCMNotificaciones(caso.creador,"${caso.nombre_programador} Cerro el Caso #${caso.numero}",caso.uid)
                Funciones().MensajeToast(v.context, "El Caso # ${caso.numero} fue cerrado exitosamente")
                activity?.title = "Tickets de Soporte"
                Funciones().CerrarFragment(v,"VER")
            } else {
                Funciones().EstatusCaso(caso.uid, "1")
                Funciones().FCMNotificaciones(caso.programador,"${caso.nombre_creador} Reabrio el Caso #${caso.numero}",caso.uid)
                Funciones().MensajeToast(v.context, "El Caso # ${caso.numero} fue Reabierto exitosamente")
                activity?.title = "Tickets de Soporte"
                Funciones().CerrarFragment(v,"VER")
            }
        }

        vBotonObservaciones.setOnClickListener { v ->
            if (caso.observaciones.trim().equals(vObservacionesContenido.text.toString().trim())) {
                Funciones().MensajeToast(v.context, "No se hizo ningun cambio en el campo de observaciones")
            } else {
                Funciones().ActualizarObservaciones(caso.uid, vObservacionesContenido.text.toString().trim())
                Funciones().MensajeToast(v.context, "El campo de observaciones fue actualizado exitosamente")
                if (tipo_usuario.equals("1")) {
                    Funciones().FCMNotificaciones(caso.programador, "${caso.nombre_creador} Agrego una Observación al Caso # ${caso.numero}",caso.uid)
                } else {
                    Funciones().FCMNotificaciones(caso.creador, "${caso.nombre_programador} Agrego una Observación al Caso # ${caso.numero}", caso.uid)
                }

            }
        }

        vVerAdjuntos.setOnClickListener { v ->

            Funciones().CerrarFragment(v, "VER")
            Funciones().ActivarFragmento(ListarAdjuntos(),v, caso, "","LISTA_ADJUNTOS")

        }


    }


}
