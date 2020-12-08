package com.ulisesdiaz.integracionapifoursquare

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.gson.Gson
import com.ulisesdiaz.integracionapifoursquare.adapters.AdaptadorCustom
import com.ulisesdiaz.integracionapifoursquare.models.FoursquareModel

class SecondActivity : AppCompatActivity() {

    var fsq: Foursquare? = null
    var lista: RecyclerView? = null
    var adaptador: AdaptadorCustom? = null
    var layoutManager: RecyclerView.LayoutManager? = null

    // Variables de los permisos que se van autilizar haciendo referencia a los permisos que se van autilizar
    private val permisoFineLocation = android.Manifest.permission.ACCESS_FINE_LOCATION
    private val permisoCoarseLocation = android.Manifest.permission.ACCESS_COARSE_LOCATION

    // Variable que permite identificar el permiso mediante un nummero
    private val CODIGO_SOLICITUD_PERMISO = 100

    // Variable que permite obtener los datos de la ubicacion a traves de google play service Location
    var fusedLocationClient: FusedLocationProviderClient? = null

    // Variable que permitira hacer el traking de ubicacion
    var locationRequest: LocationRequest? = null

    // Variable que administrara las actualizaciones de la ubicacion
    var callback: LocationCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)
        lista = findViewById(R.id.recyclerLista)
        fsq = Foursquare(this)

        lista?.setHasFixedSize(true)
        layoutManager = LinearLayoutManager(this)
        lista?.layoutManager = layoutManager

        // Se instancia la variable que permitira obtener las coordenadas
        fusedLocationClient = FusedLocationProviderClient(this)

        inicializarLocationRequest()

        // Es el que responde cunado se active la funcion requestLocationUpdates
        callback = object : LocationCallback() {
            @SuppressLint("MissingPermission")
            override fun onLocationResult(locationResult: LocationResult?) {
                super.onLocationResult(locationResult)
                for (ubicacion in locationResult?.locations!!){
                    obtenerLugares(ubicacion.latitude.toString(), ubicacion.longitude.toString())

                }
            }
        }

    }

    fun obtenerLugares(lat: String, lon: String){
        val queue = Volley.newRequestQueue(this)
        val url = "https://api.foursquare.com/v2/venues/search?ll=${lat},${lon}&oauth_token=${fsq?.obtenerToken()}&v=20201208"
        val solicitud = StringRequest(Request.Method.GET, url, Response.Listener<String>{
            response ->
            Log.d("RESPONSE HTTP", response)
            val gson = Gson()
            val venues = gson.fromJson(response, FoursquareModel::class.java)
            Log.d("VENUES", venues.response?.venues?.size.toString())

            adaptador = AdaptadorCustom(venues.response?.venues!!)

            lista?.adapter = adaptador
        }, Response.ErrorListener {
            error ->
            Log.d("RESPONSE HTTP ERROR", error.message.toString())
        })
        queue.add(solicitud)
    }

    /**
     * funcion que mantiene la configuracon del servicion de tracking para obtener la obicacion
     */
    private fun inicializarLocationRequest(){
        locationRequest = LocationRequest()
        locationRequest?.interval = 600000 // Intervalo en que se va a estar actulizando la ubicacion
        locationRequest?.fastestInterval = 300000 // La velocidad mas alta en la que se va estar actualizando
        locationRequest?.priority = LocationRequest.PRIORITY_HIGH_ACCURACY // Que tanta proximidad se desa usar para obtener la ubicacion
    }

    /**
     * Ciclo de Vida onstart
     * Se activa cada vez que se inicie la aplicacion, cambie o la app pase a segundo plano
     * Condicinal donde si hay permisos obtiene la ubicion, caso contrario solicita los permisos
     */
    override fun onStart() {
        super.onStart()

        if (validarPermisosUbicacion()){
            obtenerUbicacion()
        }else{
            pedirPermisos()
        }
    }

    /**
     * Cada vez que la app entre a este estado de ciclo de vida en pausa, se para de actualizar la ubicacion
     * con el fin de ahorrar recurso del dispositvo
     */
    override fun onPause() {
        super.onPause()
        detenerActualizacionUbicacion()
    }

    /**
     * Esta funcion permite mapear si el usuario otorgo permisos, es llamada una vez que se otorgaron los permisos o se denegaron
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when(requestCode){
            CODIGO_SOLICITUD_PERMISO ->{
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    // Obtener la ubicacion
                    obtenerUbicacion()
                }else{
                    Toast.makeText(this, "No se otorgaron permisos para la ubicacion",
                        Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /**
     * Funcion que valida si el usuario ya tiene permisos o se necesita pedir
     * Se compara el permiso que deseo usar con los que se declararon en el manifest
     * Regresa verdadero si estan los permisos otorgados
     */
    private fun  validarPermisosUbicacion(): Boolean{
        val hayUbicacionPrecisa = ActivityCompat.checkSelfPermission(
            this, permisoFineLocation) == PackageManager.PERMISSION_GRANTED
        val hayUbicacionOrdinaria =  ActivityCompat.checkSelfPermission(
            this, permisoCoarseLocation) == PackageManager.PERMISSION_GRANTED

        return hayUbicacionPrecisa && hayUbicacionOrdinaria
    }

    /**
     * Permitira monitoriar las actualizaciones de la ubicacion por medio de requestLocationUpdates
     */
    @SuppressLint("MissingPermission")
    private fun  obtenerUbicacion(){
        fusedLocationClient?.requestLocationUpdates(locationRequest, callback, null)
    }

    /**
     * Si el usuario no tiene permisos o son negados entra a esta funcion para solicitarlos
     * la variable proverContexto recibe un boleano de si se otorgo el permiso o no (true o false)
     * Solo se pide el permiso de Fine Location
     */
    private fun pedirPermisos(){
        val proveerContexto = ActivityCompat.shouldShowRequestPermissionRationale(
            this , permisoFineLocation)
        if (proveerContexto){
            // Mandar mensaje con explicacion adicional
            solicitudPermiso()
        }else{
            solicitudPermiso()
        }
    }

    /**
     * Se llama a requesPermissions para ingresar todos los permisos que funcionaran en la actividad.
     * Los permisos se ingresan por medio de un arreglo
     */
    private fun solicitudPermiso(){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this, arrayOf(permisoFineLocation, permisoCoarseLocation), CODIGO_SOLICITUD_PERMISO)
        }
    }

    /**
     * Funcion para detener la actualizacion de la ubicacion por medio de un removeLocationUpdates
     */
    private fun detenerActualizacionUbicacion(){
        fusedLocationClient?.removeLocationUpdates(callback)
    }
}