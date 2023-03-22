package com.example.pm2examengrupo1;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;


import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.pm2examengrupo1.RestApiMethods.Alumno;
import com.example.pm2examengrupo1.RestApiMethods.Methods;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class ActivityActualizar extends AppCompatActivity  {

    List<Alumno> alumnoList;
    ArrayList<String> arrayAlumn;
    Button btnActualizar,btnLista,btnTFoto;

    static final int REQUEST_IMAGE = 101;
    static final int PETICION_ACCESS_CAM = 201;
    static final int LOCATION_PERMISSION_REQUEST_CODE = 301;
    GoogleMap googleMap;
    MapView mapView;
    String currentPhotoPath;
    ImageView imageView;
    EditText edtNombre, edtTelefono;
    TextView latTextView, longTextView;
    private FusedLocationProviderClient fusedLocationClient;

    private Location currentLocation;
    String currentPath;
    String id; // declare the global variable here
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_actualizar);

        ControlsSet();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mapView.onCreate(savedInstanceState);
        Intent intentActual = getIntent();

        int id = intentActual.getIntExtra("id", 0); // El segundo parámetro (0) es el valor por defecto en caso de que no se encuentre la clave.

        ConsumirApi(id);

        alumnoList = new ArrayList<>();
        arrayAlumn = new ArrayList<String>();

        btnActualizar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ConsumirUpdateApi(id);
                Intent intent = new Intent(getApplicationContext(), ActivityListView.class);
                startActivity(intent);
            }
        });

        btnTFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(ActivityActualizar.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
                } else {
                    permisos();
                }

            }
        });

        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap map) {
                googleMap = map;
                googleMap.getUiSettings().setZoomControlsEnabled(true);
            }
        });
    }
    private void getLastLocation() {
        // Verificar si se tienen los permisos de localización
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Si no se tienen los permisos, se solicitan
            ActivityCompat.requestPermissions(ActivityActualizar.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        // Se obtiene la última ubicación conocida
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Si se obtiene una ubicación válida
                        if (location != null) {
                            // Se guarda la ubicación actual
                            currentLocation = location;
                            // Se actualiza la interfaz de usuario con la latitud y longitud actuales
                            latTextView.setText(String.valueOf(currentLocation.getLatitude()));
                            longTextView.setText(String.valueOf(currentLocation.getLongitude()));
                            // Se crea un objeto LatLng a partir de la ubicación actual
                            LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                            // Se mueve la cámara del mapa a la ubicación actual
                            googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                            // Se anima la cámara del mapa a un nivel de zoom de 17.0f
                            googleMap.animateCamera(CameraUpdateFactory.zoomTo(17.0f));
                            // Se crea un marcador en el mapa en la ubicación actual
                            MarkerOptions markerOptions = new MarkerOptions()
                                    .position(latLng)
                                    .title("My Location")
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                            googleMap.addMarker(markerOptions);
                        }
                    }
                });
    }

    private void ControlsSet() {
        imageView = findViewById(R.id.imageView);
        btnTFoto = findViewById(R.id.btnTFoto);
        btnActualizar = findViewById(R.id.btnActualizar);
        btnLista = findViewById(R.id.btnLista);
        edtNombre = findViewById(R.id.edtNombre);
        edtTelefono = findViewById(R.id.edtTelefono);
        latTextView = findViewById(R.id.latTextView);
        longTextView = findViewById(R.id.longTextView);
        mapView = findViewById(R.id.mapView);

    }

    private void ConsumirUpdateApi(int id){
        // Crear un HashMap para almacenar los parámetros de la solicitud
        HashMap<String, String> parametros = new HashMap<>();
        // Agregar los parámetros necesarios para la solicitud PUT
        parametros.put("id", Integer.toString(id));
        parametros.put("nombre",edtNombre.getText().toString());
        parametros.put("telefono",edtTelefono.getText().toString());

        String fotoBase64 = "";
        if(currentPhotoPath == null){ // Si no se toma una nueva foto
            // Obtener el objeto Bitmap de la foto actual
            Bitmap bmp = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
            // Comprimir el objeto Bitmap a JPEG y guardarlo en un objeto ByteArrayOutputStream
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.JPEG,50,bos);
            byte[] bt = bos.toByteArray();
            // Convertir el objeto Bitmap a base64
            fotoBase64 =  android.util.Base64.encodeToString(bt, android.util.Base64.DEFAULT);
        }
        else{
            // Convertir la imagen en base64 utilizando el método ImageToBase64
            fotoBase64 = ImageToBase64(currentPhotoPath);
        }
        // Agregar la foto en base64 a los parámetros
        parametros.put("foto", fotoBase64);
        // Agregar la latitud y longitud a los parámetros
        parametros.put("latitud",latTextView.getText().toString());
        parametros.put("longitud",longTextView.getText().toString());

        // Crear un objeto JSON con los parámetros
        JSONObject JsonAlumn = new JSONObject(parametros);

        // Crear una nueva cola de solicitudes Volley
        RequestQueue peticion = Volley.newRequestQueue(getApplicationContext());

        // Crear una solicitud PUT con el método PUT, la URL de la API y el objeto JSON creado anteriormente
        JsonObjectRequest jsonObjectRequest =new JsonObjectRequest(Request.Method.PUT, Methods.UpdateApi + "/" + id , JsonAlumn, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    // Convertir la respuesta en un JSONArray
                    JSONArray jsonArray =new JSONArray(response);
                    // Recorrer el JSONArray y obtener los objetos JSONObject correspondientes
                    for (int i =0; i<= jsonArray.length();i++){
                        JSONObject asg = jsonArray.getJSONObject(i);
                    }
                }catch (Exception e){

                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        // Agregar la solicitud a la cola de solicitudes Volley
        peticion.add(jsonObjectRequest);
    }

    public static String ImageToBase64(String path) {
        Bitmap bmp = null;
        ByteArrayOutputStream bos = null;
        byte[] bt = null;
        String Image64String = null;

        try
        {
            bmp = BitmapFactory.decodeFile(path);
            bos = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.JPEG,50,bos);
            bt = bos.toByteArray();
            Image64String =  android.util.Base64.encodeToString(bt, android.util.Base64.DEFAULT);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return Image64String;
    }

    private void ConsumirApi(int id) {
        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, Methods.GettApi + "?id=" + id, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONArray jsonArray = new JSONArray(response);
                    JSONObject rowAlumn = jsonArray.getJSONObject(0);

                    Alumno alumn = new Alumno(rowAlumn.getInt("id"),
                            rowAlumn.getString("nombre"),
                            rowAlumn.getString("telefono"),
                            rowAlumn.getString("foto"),
                            rowAlumn.getDouble("latitud"),
                            rowAlumn.getDouble("longitud")
                    );

                    edtNombre.setText(alumn.getNombre());
                    edtTelefono.setText(alumn.getTelefono());
                    latTextView.setText(String.valueOf(alumn.getLatitud()));
                    longTextView.setText(String.valueOf(alumn.getLongitud()));

                    // Obtener el valor de la cadena Base64 del objeto Alumno
                    String fotoBase64 = alumn.getFoto();
                    // Decodificar la cadena Base64 a un array de bytes
                    byte[] fotoBytes = Base64.decode(fotoBase64, Base64.DEFAULT);
                    // Crear un objeto Bitmap a partir de los bytes decodificados
                    Bitmap fotoBitmap = BitmapFactory.decodeByteArray(fotoBytes, 0, fotoBytes.length);
                    // Establecer el objeto Bitmap en el ImageView
                    imageView.setImageBitmap(fotoBitmap);
                    mapView.getMapAsync(new OnMapReadyCallback() {
                        @Override
                        public void onMapReady(GoogleMap googleMap) {
                            // Obtener las coordenadas del objeto Alumno
                            double latitud = alumn.getLatitud();
                            double longitud = alumn.getLongitud();

                            // Crear un objeto LatLng con las coordenadas
                            LatLng coordenadas = new LatLng(latitud, longitud);

                            // Añadir un marcador en las coordenadas
                            googleMap.addMarker(new MarkerOptions().position(coordenadas));

                            // Mover la cámara a las coordenadas
                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(coordenadas, 15));
                        }
                    });

                } catch (JSONException ex) {
                    ex.toString();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // handle error
            }
        });

        queue.add(stringRequest);
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
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                ex.toString();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.pm2examengrupo1.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE);
                getLastLocation();
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

        if(requestCode == REQUEST_IMAGE && resultCode == RESULT_OK)
        {
            if (currentPhotoPath != null) {
                try {
                    File foto = new File(currentPhotoPath);
                    Bitmap bitmap = BitmapFactory.decodeFile(foto.getAbsolutePath());
                    imageView.setImageBitmap(bitmap);
                }
                catch (Exception ex)
                {
                    ex.toString();
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}