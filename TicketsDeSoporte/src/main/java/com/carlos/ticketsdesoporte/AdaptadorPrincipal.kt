package com.carlos.ticketsdesoporte

import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.lista_casos.view.*


class AdaptadorPrincipal(val lista_casos: MutableList<Casos>, val tipo_usuario: String) : RecyclerView.Adapter<CustomViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        return CustomViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.lista_casos, parent, false))
    }

    override fun getItemCount(): Int {
        return lista_casos.count()
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val casos =lista_casos.get(position)

        holder.view.titulo_caso_contenido.text = casos.titulo
        holder.view.fecha_caso_contenido.text = casos.fecha
        holder.view.numero_caso.text = "CASO #${casos.numero}"
        if (casos.estatus == "1") {
            holder.view.estatus_contenido.setTextColor(ContextCompat.getColor(holder.view.context, android.R.color.holo_red_dark))
            holder.view.estatus_contenido.text = "Pendiente"
        }
        if (casos.estatus == "2") {
            holder.view.estatus_contenido.setTextColor(ContextCompat.getColor(holder.view.context, android.R.color.holo_green_dark))
            holder.view.estatus_contenido.text = "Cerrado"
        }
        holder.view.asignado_a_contenido.text = casos.nombre_programador

        holder.casos = casos
        holder.tipo_usuario = tipo_usuario
    }
}

class CustomViewHolder(val view: View, var casos: Casos?=null, var tipo_usuario: String?=null) : RecyclerView.ViewHolder(view) {

    init {
        view.setOnClickListener {
            Funciones().ActivarFragmento(VerCarso(),view, casos!!,tipo_usuario, "VER")

        }
    }

}