package com.carlos.ticketsdesoporte

import android.app.ProgressDialog
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ProgressBar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.content_actividad_principal.*
import kotlinx.android.synthetic.main.fragment_listar_adjuntos.*


class ListarAdjuntos : Fragment() {

    private lateinit var database: FirebaseDatabase
    private lateinit var caso: Casos
    private var lista_adjuntos: MutableList<UrlAdjunto> = mutableListOf()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_listar_adjuntos, container, false)

    }

    override fun onStart() {
        super.onStart()
        database = FirebaseDatabase.getInstance()

        caso = arguments?.getSerializable("caso") as Casos
        activity?.title = "Total de Adjuntos: ${caso.adjuntos}"


        aRecycler.layoutManager = LinearLayoutManager(activity)
        aRecycler.layoutAnimation = AnimationUtils.loadLayoutAnimation(aRecycler.context,R.anim.animacion_recycle_layout)

        MostrarAdjuntos(database,caso.uid)

        


    }


    private fun MostrarAdjuntos(firebaseDatabase: FirebaseDatabase, uid : String) {

        firebaseDatabase.getReference("Adjuntos").child(uid).addValueEventListener(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    lista_adjuntos.clear()
                    p0.children.mapNotNullTo(lista_adjuntos) {
                        it.getValue<UrlAdjunto>(UrlAdjunto::class.java)
                    }
                    aRecycler.adapter = AdaptadorAdjuntos(lista_adjuntos, caso)
                    aRecycler.scheduleLayoutAnimation()

                }

            }

        })
    }

}
