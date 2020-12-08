package com.ulisesdiaz.integracionapifoursquare

import android.content.Intent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.foursquare.android.nativeoauth.FoursquareOAuth

class Foursquare(var activity: AppCompatActivity) {

    //
    public val CODIGO_CONEXION = 200
    // Sirve para que una vez se tnga la conexion, se mande una solicitud de identificacion de token e identificarlo con el codigo 201
    private val CODIGO_INTERCAMBIO_TOKEN = 201

    // Sirven para autentificarse en el ApI de foursquare
    private val CLIENT_ID = "GJ34F312QSUQXNKFZDLVOYHBDEORLTRIBKKK0P5P4XLNOMC4"
    private val CLIENT_SECRET =  "UMNYVNUSGIQZOQXW3RB1KL5YKKWRJXCGRZLOJ0YGC2ENVCL5"
    private val SETTINGS = "settings"

    init {

    }

    fun validarActivityResult(requestCode: Int, resultCode: Int, data: Intent?){
        when(requestCode){
            CODIGO_CONEXION ->{conexionCompleta(resultCode, data)}
            CODIGO_INTERCAMBIO_TOKEN ->{intercambioTokenCompleta(resultCode, data)}
        }
    }

    fun conexionCompleta(resultCode: Int, data: Intent?){
        val codigoRespuesta = FoursquareOAuth.getAuthCodeFromResult(resultCode, data)
        val exception = codigoRespuesta.exception

        if (exception == null){
            // autenticacion fue correcta
            val codigo = codigoRespuesta.code
            realizarIntercambioToken(codigo)
        }else{
            mensaje("No se pudo realizar la autenticacion, intentalo mas tarde ...")
        }
    }

    private fun realizarIntercambioToken(codigo: String){
        val intent = FoursquareOAuth.getTokenExchangeIntent(activity.applicationContext, CLIENT_ID, CLIENT_SECRET, codigo)
        activity.startActivityForResult(intent, CODIGO_INTERCAMBIO_TOKEN)
    }

    private fun intercambioTokenCompleta(resultCode: Int, data: Intent?){
        val respuestaToken = FoursquareOAuth.getTokenFromResult(resultCode, data)
        val exception = respuestaToken.exception

        if (exception == null){
            val accessToken = respuestaToken.accessToken
            guardarToken(accessToken)
            mensaje("Token: ${accessToken}")
            navegarSiguienteActividad()
        }else
            mensaje("no se pudo obtener el token")
    }



    fun iniciarSesion(){
        val intent = FoursquareOAuth.getConnectIntent(activity.applicationContext, CLIENT_ID)
        // Validar que el usuario tiene la aplicacion
        if (FoursquareOAuth.isPlayStoreIntent(intent)){
            // Mostrar mensaje que no tiene la app
            mensaje("No tienes la app instalada de Foursquare...")
            activity.startActivity(intent)
        }else{
            // Mapea una respuesta del intent
            activity.startActivityForResult(intent, CODIGO_CONEXION)
        }
    }


    fun mensaje(mensaje: String){
        Toast.makeText(activity.applicationContext, mensaje, Toast.LENGTH_SHORT).show()
    }

    fun hayToken(): Boolean{
        if (obtenerToken() == ""){
            return false
        }else{
            return true

        }
    }


    private fun guardarToken(token: String){
        val settings = activity.getSharedPreferences(SETTINGS,0)
        val editor = settings.edit()
        editor.putString("accessToken", token)
        editor.commit()
    }

     fun obtenerToken():String{
        val settings = activity.getSharedPreferences(SETTINGS, 0)
        val token = settings.getString("accessToken", "")
        return  token!!
    }

    fun navegarSiguienteActividad(){
        activity.startActivity(Intent(activity, SecondActivity::class.java))
        activity.finish()
    }
}