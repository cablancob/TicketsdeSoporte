package com.carlos.ticketsdesoporte

import android.app.ProgressDialog
import android.graphics.drawable.Drawable
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import kotlinx.android.synthetic.main.lista_adjuntos.view.*


class AdaptadorAdjuntos(val lista_adjuntos: MutableList<UrlAdjunto>, var casos: Casos) : RecyclerView.Adapter<AdjuntosViewHolder>() {
    override fun onBindViewHolder(holder: AdjuntosViewHolder, position: Int) {
        val adjunto = lista_adjuntos.get(position)

        val progressDialog = ProgressDialog(holder.view.context)
        progressDialog.setMessage("Cargando Imagen(es)")
        progressDialog.show()

        Glide.with(holder.view).load(adjunto.url).error(Glide.with(holder.view).load(R.mipmap.ic_no_contact)).listener(object : RequestListener<Drawable>{
            override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                return false
            }

            override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                progressDialog.dismiss()
                return false
            }

        }).into(holder.view.aVerAdjunto)

        holder.adjunto = adjunto
        holder.casos = casos

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdjuntosViewHolder {
        return AdjuntosViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.lista_adjuntos, parent, false))
    }

    override fun getItemCount(): Int {
        return lista_adjuntos.count()
    }

}

class AdjuntosViewHolder(val view: View, var adjunto: UrlAdjunto?= null, var casos: Casos?= null) : RecyclerView.ViewHolder(view) {

    init {
        view.setOnClickListener {
            Funciones().ActivarFragmento(VerAdjunto(), view, casos,adjunto!!.url,"ADJUNTO")
        }
    }

}