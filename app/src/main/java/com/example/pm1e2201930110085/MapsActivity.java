package com.example.PM2E2201930110085;

import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private double latitud, longitud;
    private String nombre;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtiene la latitud y longitud del Intent
        Intent intent = getIntent();
        latitud = intent.getDoubleExtra("latitud", 0);
        longitud = intent.getDoubleExtra("longitud", 0);
        nombre = intent.getStringExtra("nombre");

        // Obtiene el fragmento del mapa y notifica cuando esté listo para ser utilizado
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Establece la ubicación del marcador
        LatLng location = new LatLng(latitud, longitud);

        // Agrega el marcador en la ubicación especificada
        mMap.addMarker(new MarkerOptions().position(location).title(nombre));

        // Mueve la cámara del mapa a la ubicación del marcador
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15));
    }
}
