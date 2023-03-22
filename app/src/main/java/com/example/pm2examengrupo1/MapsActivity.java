package com.example.pm2examengrupo1;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.Manifest;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.pm2examengrupo1.databinding.ActivityMapsBinding;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;

    private double latitud;
    private double longitud;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();
        latitud = intent.getDoubleExtra("latitud", 0);
        longitud = intent.getDoubleExtra("longitud", 0);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Obtener la ubicación actual del usuario
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Si los permisos no están concedidos, solicítelos al usuario
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 101);
            return;
        }
        Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        // Mover la cámara a las coordenadas indicadas
        LatLng ubicacion = new LatLng(latitud, longitud);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ubicacion, 16));

        // Agregar un marcador en la ubicación indicada
        mMap.addMarker(new MarkerOptions().position(ubicacion));

        // Agregar un listener al marcador para abrir Google Maps cuando se haga clic en el marcador
        Marker marker = mMap.addMarker(new MarkerOptions().position(ubicacion));
        marker.setTag(0); // Agregar un tag al marcador para identificarlo
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                // Obtener las coordenadas del marcador
                LatLng latLng = marker.getPosition();
                double latitud = latLng.latitude;
                double longitud = latLng.longitude;

                // Crear un intento explícito para abrir Google Maps y obtener las direcciones para llegar a la ubicación
                Intent intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://maps.google.com/maps?saddr=" + location.getLatitude() + "," + location.getLongitude() +
                                "&daddr=" + latitud + "," + longitud + "&mode=driving"));
                startActivity(intent);
                return false;
            }
        });
    }
}