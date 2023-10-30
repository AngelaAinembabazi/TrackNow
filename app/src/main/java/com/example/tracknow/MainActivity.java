package com.example.tracknow;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private EditText ipAddressEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        ipAddressEditText = findViewById(R.id.ipAddressEditText);
        Button trackLocationButton = findViewById(R.id.trackLocationButton);

        trackLocationButton.setOnClickListener(v -> trackLocation());
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
    }

    private void trackLocation() {
        String ipAddress = ipAddressEditText.getText().toString();

        if (ipAddress.isEmpty()) {
            Toast.makeText(this, "Please enter an IP address", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            try {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url("https://ipapi.co/" + ipAddress + "/json/")
                        .build();
                Response response = client.newCall(request).execute();
                assert response.body() != null;
                String jsonData = response.body().string();

                double latitude = Double.parseDouble(jsonData.substring(jsonData.indexOf("\"latitude\":") + 12, jsonData.indexOf(",")));
                double longitude = Double.parseDouble(jsonData.substring(jsonData.indexOf("\"longitude\":") + 13, jsonData.indexOf("}")));

                LatLng location = new LatLng(latitude, longitude);

                runOnUiThread(() -> {
                    mMap.clear();
                    mMap.addMarker(new MarkerOptions().position(location).title("IP Location"));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 10));
                });

            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
