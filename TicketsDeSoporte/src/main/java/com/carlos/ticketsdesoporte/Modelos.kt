package com.carlos.ticketsdesoporte

import android.content.Intent
import java.io.Serializable

class Usuarios(val uid: String = "", val nombre_completo: String = "", val correo: String = "",  val tipo_usuario: String = "")

class Casos(val uid: String = "",val numero: String = "",val nombre_creador: String = "",  val creador: String = "",val nombre_programador: String = "" , val programador: String = "", val titulo: String = "", val contenido: String = "", val observaciones: String = "", val adjuntos: String = "", val fecha: String = "", val estatus: String = "") :  Serializable


class Programadores(val uid: String = "", val nombre_completo: String = "", val correo: String = "", val tipo_usuario: String = "") {
    override fun toString(): String {
        return "${nombre_completo} - (${correo})"
    }
}

class Adjunto(val data: Intent = Intent())

class UrlAdjunto(val url: String = "")

class json(var to: String = "",val collapse_key: String = "type_a", var data: data = data())

class data(var caso_uid: String = "", var body: String = "")

class FCMUsuario(val nombre_completo: String = "", val fcm_key: String = "")