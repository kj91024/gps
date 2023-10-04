package utp.sanchezsanchez.gpt2;


import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import android.Manifest;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener, GoogleMap.OnMapLongClickListener {

    EditText latitud;
    EditText longitud;

    GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String api = "AIzaSyDoQm81mfWTaO97PmEJuy4omhVCfreOyv0";

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,}, 1000);
        } else {
            Toast.makeText(this, "Está super hiperactivado", Toast.LENGTH_SHORT).show();
        }

        latitud = findViewById(R.id.txtLatitud);
        longitud = findViewById(R.id.txtLongitud);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        LocationListener listener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                latitud.setText(String.valueOf(location.getLatitude()));
                longitud.setText(String.valueOf(location.getLongitude()));
                Toast.makeText(MainActivity.this, "uno", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onProviderEnabled(@NonNull String provider) {
                Toast.makeText(MainActivity.this, "dos", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onProviderDisabled(@NonNull String provider) {
                Toast.makeText(MainActivity.this, "tres", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                Toast.makeText(MainActivity.this, "cuatro", Toast.LENGTH_SHORT).show();
            }
        };
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, listener);
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1000) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Ya activaste el gps", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Metodo inicial
    public void drawPointUTP(){
        double latitude = -11.98371056141976;
        double longitude = -77.0089770872379;

        LatLng ubicacion = new LatLng(latitude, longitude);
        mMap.addMarker(new MarkerOptions().position(ubicacion).title("UTP"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(ubicacion));
    }

    public void drawPath(double latitude, double longitude){
        double fromLat = -11.98371056141976;
        double fromLong = -77.0089770872379;

        double toLat = latitude;
        double toLong = longitude;

        mMap.clear();
        LatLng ubicacion = new LatLng(fromLat, fromLong);
        mMap.addMarker(new MarkerOptions().position(ubicacion).title("UTP"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(ubicacion));

        ubicacion = new LatLng(toLat,toLong);
        mMap.addMarker(new MarkerOptions().position(ubicacion).title("Estas aquí"));

        String from = fromLong + "," + fromLat;
        String to = toLong + "," + toLat;
        Runnable foo = new Foo(from, to);
        Thread thread = new Thread(foo);
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        String result = ((Foo) foo).getValue();

        Log.i("json", result);
        try {
            JSONArray list = null;
            list = new JSONArray(result.toString());
            for (int i = 1; i < list.length(); i++) {
                JSONArray _from = list.getJSONArray(i-1);
                double _fromLat = _from.getDouble(1);
                double _fromLong = _from.getDouble(0);

                JSONArray _to = list.getJSONArray(i);
                double _toLat = _to.getDouble(1);
                double _toLong = _to.getDouble(0);

                //Log.i("json", "from: " + String.valueOf(_fromLat) + ","+ String.valueOf(_fromLong) );
                //Log.i("json", "to: " + String.valueOf(_toLat) + ","+ String.valueOf(_toLong) );

                LatLng __from = new LatLng(_fromLat, _fromLong);
                LatLng __to = new LatLng(_toLat, _toLong);
                mMap.addPolyline(new PolylineOptions().add(__from, __to).width(5).color(Color.RED));
                //mMap.moveCamera(CameraUpdateFactory.newLatLng(__to));
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onMapClick(@NonNull LatLng latLng) {
        latitud.setText(String.valueOf(latLng.latitude));
        longitud.setText(String.valueOf(latLng.longitude));
        this.drawPath(latLng.latitude, latLng.longitude);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        this.mMap.setOnMapClickListener(this);
        this.mMap.setOnMapLongClickListener(this);

        this.drawPointUTP();

        /*
        mMap.addPolyline(new PolylineOptions()
                .add(new LatLng(fromLat, fromLong), new LatLng(toLat,toLong))
                .width(5).color(Color.RED));
        */

    }
    public class Foo implements Runnable {
        private volatile String response = "vacio";
        private String from, to;

        public Foo(String _from, String _to){
            this.from = _from;
            this.to = _to;
        }
        @Override
        public void run() {
            try {
                // Log.i("asd", this.to);
                URL url = new URL("http://192.168.18.10/path.php?from=" + this.from + "&to=" + this.to);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line = "";
                String result = "";
                while (line != null) {
                    line = bufferedReader.readLine();
                    result = result + line;
                }
                httpURLConnection.disconnect();
                response = result;

                //Log.i("resultado", result);
            } catch (IOException e) {
                Log.i("resultado", e.toString());
                e.printStackTrace();
            }
        }
        public String getValue(){
            return response;
        }
    };

    @Override
    public void onMapLongClick(@NonNull LatLng latLng) {
        latitud.setText(String.valueOf(latLng.latitude));
        longitud.setText(String.valueOf(latLng.longitude));
        this.drawPath(latLng.latitude, latLng.longitude);
    }

}