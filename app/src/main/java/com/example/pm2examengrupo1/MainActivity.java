package com.example.pm2examengrupo1;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.pm2examengrupo1.RestApiMethods.Methods;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    static final int REQUEST_IMAGE = 101;
    static final int PETICION_ACCESS_CAM = 201;
    static final int LOCATION_PERMISSION_REQUEST_CODE = 301;

    GoogleMap googleMap;
    MapView mapView;
    String currentPhotoPath;
    ImageView imageView;
    Button btnTFoto,btnSalvar,btnLista;
    EditText edtNombre, edtTelefono, edtlatTextView, edtlongTextView;

    private FusedLocationProviderClient fusedLocationClient;

    String POSTMethod;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ControlsSet();
        permisosGPS();

        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        btnTFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                permisos();
            }
        });

        btnSalvar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {ConsumeCreateApi();}
        });

        btnLista.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ActivityListView.class);
                startActivity(intent);
            }
        });
    }
    private void Limpiar(){
        // Limpia los campos de la interfaz
        edtNombre.setText("");
        edtTelefono.setText("");
        edtlatTextView.setText("");
        edtlongTextView.setText("");
        imageView.setImageResource(0);
    }

    private void ControlsSet() {
        imageView = findViewById(R.id.imageView);
        btnTFoto = findViewById(R.id.btnTFoto);
        btnSalvar = findViewById(R.id.btnSalvar);
        btnLista = findViewById(R.id.btnLista);
        edtNombre = findViewById(R.id.edtNombre);
        edtTelefono = findViewById(R.id.edtTelefono);
        edtlatTextView = findViewById(R.id.edtlatTextView);
        edtlongTextView = findViewById(R.id.edtlongTextView);
        mapView = findViewById(R.id.mapView);
    }
    private void permisosGPS() {
        // Verificar si el permiso ACCESS_FINE_LOCATION ha sido otorgado
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            // Verificar si el GPS está habilitado
            checkGPSStatus();
        }
    }

    private void permisos() {
        //Metodo para obtener los permisos requeridos de la aplicacion
        if(ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PETICION_ACCESS_CAM);
        }else{
            dispatchTakePictureIntent();
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Asegúrese de que haya una actividad de cámara para manejar la intención
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Crea el Archivo donde debe ir la foto
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                ex.toString();
            }
            // Continuar solo si el archivo se creó correctamente
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.pm2examengrupo1.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents

        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Si el código de solicitud es REQUEST_IMAGE y el resultado es RESULT_OK
        if(requestCode == REQUEST_IMAGE && resultCode == RESULT_OK)
        {
            //Si la ruta de la imagen es válida
            if (currentPhotoPath != null) {
                try {
                    //Crear un objeto File a partir de la ruta de la imagen
                    File foto = new File(currentPhotoPath);
                    //Decodificar la imagen en un objeto Bitmap
                    Bitmap bitmap = BitmapFactory.decodeFile(foto.getAbsolutePath());
                    //Establecer la imagen en el ImageView
                    imageView.setImageBitmap(bitmap);

                    //Si se ha concedido el permiso de ubicación fina
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        //Obtener la última ubicación conocida del usuario
                        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                            //Si la ubicación no es nula
                            if (location != null) {
                                //Crear un objeto LatLng a partir de la ubicación
                                LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                                //Agregar un marcador al mapa de Google en la ubicación actual
                                googleMap.addMarker(new MarkerOptions().position(currentLatLng).title("Current Location"));
                                //Mover el mapa de Google a la ubicación actual
                                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f));
                                //Establecer la latitud en el TextView correspondiente
                                edtlatTextView.setText("" + location.getLatitude());
                                //Establecer la longitud en el TextView correspondiente
                                edtlongTextView.setText("" + location.getLongitude());
                            }
                        });
                    } else {
                        //Solicitar al usuario que conceda el permiso de ubicación fina
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
                    }
                }
                //Capturar cualquier excepción que pueda surgir durante el procesamiento de la imagen
                catch (Exception ex)
                {
                    ex.toString();
                }
            }
        }
    }
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showCurrentLocation();
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        //Metodo para obtener los permisos requeridos de la aplicacion
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            //showCurrentLocation();
        }
    }

    private void showCurrentLocation() {
        // Comprueba si se ha concedido el permiso de ubicación fina
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Utiliza fusedLocationClient para obtener la última ubicación conocida del dispositivo
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                // Si se encuentra una ubicación, crea un objeto LatLng y muestra un marcador en el mapa en esa ubicación
                if (location != null) {
                    LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                    googleMap.addMarker(new MarkerOptions().position(currentLatLng).title("Current Location"));
                    // Mueve la cámara a la ubicación actual del dispositivo y establece el nivel de zoom en 15f
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f));
                    // Muestra las coordenadas de latitud y longitud en los EditText correspondientes
                    edtlatTextView.setText("" + location.getLatitude());
                    edtlongTextView.setText("" + location.getLongitude());
                }
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume(); // reanuda el objeto mapView
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause(); // pausa el objeto mapView
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy(); // destruye el objeto mapView
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory(); // notifica al objeto mapView de que hay poca memoria disponible
    }
    private void ConsumeCreateApi() {
        // Obtener los datos ingresados en los campos de texto
        String nombre = edtNombre.getText().toString().trim();
        String telefono = edtTelefono.getText().toString().trim();
        String latitud = edtlatTextView.getText().toString().trim();
        String longitud = edtlongTextView.getText().toString().trim();

        // Verificar que la latitud y longitud no estén vacías
        if (TextUtils.isEmpty(latitud) || TextUtils.isEmpty(longitud)) {
            // Mostrar una alerta para indicar al usuario que la latitud y longitud no pueden estar vacías
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Debe describir la ubicacion");
            builder.setMessage("La latitud y longitud no pueden estar vacías.");
            builder.setPositiveButton("OK", null);
            builder.show();
            return;
        }

        // Verificar si se ha tomado la foto
        if (currentPhotoPath == null) {
            // Mostrar una alerta para indicar al usuario que la foto no puede estar vacía
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("La foto no puede estar vacía")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // Cerrar el diálogo
                        }
                    });
            builder.create().show();
            return;
        }

        // Crear el objeto JSON con los datos del alumno
        HashMap<String, String> parametros = new HashMap<>();
        parametros.put("nombre",edtNombre.getText().toString());
        parametros.put("telefono",edtTelefono.getText().toString());
        parametros.put("foto",ImageToBase64(currentPhotoPath));
        parametros.put("latitud",edtlatTextView.getText().toString());
        parametros.put("longitud",edtlongTextView.getText().toString());

        // Establecer el método HTTP POST y la URL de la API de creación
        POSTMethod = Methods.ApiCreate;
        // Convertir el objeto HashMap a un objeto JSON
        JSONObject JsonAlumn = new JSONObject(parametros);

        // Crear una nueva petición de Volley
        RequestQueue peticion = Volley.newRequestQueue(getApplicationContext());
        JsonObjectRequest jsonObjectRequest =new JsonObjectRequest(Request.Method.POST, POSTMethod, JsonAlumn, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    // Convertir la respuesta del servidor en un objeto JSONArray
                    JSONArray jsonArray =new JSONArray(response);
                    for (int i =0; i<= jsonArray.length();i++){
                        // Obtener cada objeto JSONObject del JSONArray
                        JSONObject asg = jsonArray.getJSONObject(i);
                    }
                }catch (Exception e){

                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Manejar errores de la petición de Volley
            }
        });
        // Agregar la petición de Volley a la cola de solicitudes
        peticion.add(jsonObjectRequest);
        // Limpiar los campos de texto
        Limpiar();
    }

    public static String ImageToBase64(String path) {
        // Se inicializan todas las variables necesarias como nulas
        Bitmap bmp = null;
        ByteArrayOutputStream bos = null;
        byte[] bt = null;
        String Image64String = null;

        try
        {
            // Se decodifica la imagen en la ruta especificada y se almacena en bmp
            bmp = BitmapFactory.decodeFile(path);
            // Se crea un objeto ByteArrayOutputStream para almacenar los bytes de la imagen
            bos = new ByteArrayOutputStream();
            // Se comprime la imagen en formato JPEG con una tasa de compresión del 50% y se almacena en bos
            bmp.compress(Bitmap.CompressFormat.JPEG,50,bos);
            // Se convierte el objeto ByteArrayOutputStream en un arreglo de bytes
            bt = bos.toByteArray();
            // Se convierte el arreglo de bytes a una cadena de caracteres en formato base64 y se almacena en Image64String
            Image64String =  android.util.Base64.encodeToString(bt, android.util.Base64.DEFAULT);
        }
        catch (Exception ex)
        {
            // Se imprime la pila de excepciones si ocurre algún error
            ex.printStackTrace();
        }
        // Se devuelve la cadena de caracteres en formato base64
        return Image64String;
    }

    private void checkGPSStatus() {
        // Obtiene una instancia del servicio de ubicación del sistema
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Verifica si el proveedor de ubicación GPS está habilitado en el dispositivo
        boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        // Si el GPS no está habilitado, muestra un diálogo para permitir que el usuario lo habilite
        if (!isGPSEnabled) {
            // Crea un diálogo de alerta
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            // Configura el mensaje del diálogo
            builder.setMessage("El GPS no está habilitado. ¿Desea habilitarlo ahora?")

                    // El diálogo no puede ser cancelado al presionar afuera de él
                    .setCancelable(false)

                    // Agrega un botón "Sí" al diálogo
                    .setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // Inicia la actividad de configuración de ubicación para permitir que el usuario habilite el GPS
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(intent);
                        }
                    })

                    // Agrega un botón "No" al diálogo
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // Cierra el diálogo sin hacer nada
                            dialog.cancel();
                        }
                    });

            // Crea el diálogo de alerta
            AlertDialog alert = builder.create();

            // Muestra el diálogo de alerta
            alert.show();
        }
    }
}