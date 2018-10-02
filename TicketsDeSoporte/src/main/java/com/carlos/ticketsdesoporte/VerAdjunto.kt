package com.carlos.ticketsdesoporte

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.app_bar_actividad_principal.*

import kotlinx.android.synthetic.main.fragment_ver_adjunto.*


class VerAdjunto : Fragment() {

    private var Url = ""
    private lateinit var caso : Casos


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_ver_adjunto, container, false)
    }

    override fun onStart() {
        super.onStart()
        activity?.toolbar?.visibility = View.GONE
        Url = arguments?.getString("url")!!
        caso = arguments?.getSerializable("caso") as Casos
        Glide.with(context!!).load(Url).error(Glide.with(context!!).load(R.mipmap.ic_no_contact)) .into(vAdjunto)


    }



}
