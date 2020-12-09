# IntegracionApiFoursquare
Ejemplo de como se realiza una solicitud http a la api de Foursquare utilizando la ubicación actual del dispositivo, esto una vez que el usuario se autentique para obtener el  Token y loguearse para hacer uso de los recursos de la API de Foursquare, cuando se hace la solicitud http se procede a interpretar la respuesta con la librería gson para finalmente mapearlo en un RecyclerView, como punto adicional se implementa la librería Picasso para el gestionar las imágenes que regresa la solicitud a la Api de Foursquare.

Nota: se debe de crear un usuario Foursquare Developer para posterimento integrar la clave sha-1 del proyecto android al sistema de Foursquare, despues integrar la credenciales CLIENTE_ID y CLIENT_SECRET al proyecto Android.

Verificar que se cuentan con los permisos de internet en el manifest.

Añadir la actividad al manifiesto
```xml
<activity
    android:name="com.foursquare.android.nativeoauth.TokenExchangeActivity"
    android:theme="@android:style/Theme.Dialog"/>
```

Añadir la librería de Foursquare 
```
implementation 'com.foursquare:foursquare-android-oauth:1.1.1'
```
