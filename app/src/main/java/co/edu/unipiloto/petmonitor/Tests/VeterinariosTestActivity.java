package co.edu.unipiloto.petmonitor.Tests;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import co.edu.unipiloto.petmonitor.CasosdeUso.Veterinario;
import co.edu.unipiloto.petmonitor.CasosdeUso.VeterinarioAdapter;
import co.edu.unipiloto.petmonitor.R;

public class VeterinariosTestActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private RecyclerView recyclerViewVeterinarios;
    private VeterinarioAdapter veterinarioAdapter;
    private List<Veterinario> listaVeterinarios = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_veterinarios); // Usa el mismo layout que la actividad original

        recyclerViewVeterinarios = findViewById(R.id.recyclerViewVeterinarios);
        recyclerViewVeterinarios.setLayoutManager(new LinearLayoutManager(this));

        veterinarioAdapter = new VeterinarioAdapter(listaVeterinarios, veterinario -> {
            double lat = veterinario.getLatitud();
            double lng = veterinario.getLongitud();

            LatLng ubicacion = new LatLng(lat, lng);
            mMap.addMarker(new MarkerOptions().position(ubicacion).title(veterinario.getNombre()));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(ubicacion, 15));
        });

        recyclerViewVeterinarios.setAdapter(veterinarioAdapter);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Coordenadas de prueba (ej. Bogotá, Colombia)
        double lat = 4.7110;
        double lng = -74.0721;

        buscarVeterinarios(lat, lng);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(4.7110, -74.0721), 12));
    }

    private void buscarVeterinarios(double lat, double lng) {
        String url = "https://overpass-api.de/api/interpreter?data=[out:json];node[amenity=veterinary](around:5000," + lat + "," + lng + ");out;";

        new Thread(() -> {
            try {
                URL apiUrl = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();
                connection.setRequestMethod("GET");

                InputStream inputStream = new BufferedInputStream(connection.getInputStream());
                String response = new BufferedReader(new InputStreamReader(inputStream)).lines().collect(Collectors.joining("\n"));

                JSONObject jsonResponse = new JSONObject(response);
                JSONArray elements = jsonResponse.getJSONArray("elements");

                listaVeterinarios.clear();

                for (int i = 0; i < elements.length(); i++) {
                    JSONObject element = elements.getJSONObject(i);
                    JSONObject tags = element.optJSONObject("tags");
                    if (tags == null) continue;

                    String nombre = tags.has("name") ? tags.getString("name") : "Nombre no disponible";
                    String direccion = tags.has("addr:full") ? tags.getString("addr:full") :
                            tags.has("addr:street") ? tags.getString("addr:street") : "Dirección no disponible";

                    double latitudVet = element.getDouble("lat");
                    double longitudVet = element.getDouble("lon");

                    String placeId = element.getString("id");

                    listaVeterinarios.add(new Veterinario(nombre, direccion, 0.0, placeId, latitudVet, longitudVet));
                }

                runOnUiThread(() -> veterinarioAdapter.notifyDataSetChanged());

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Error al obtener datos", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
}

