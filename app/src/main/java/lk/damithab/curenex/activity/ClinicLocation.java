package lk.damithab.curenex.activity;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.android.PolyUtil;
import com.google.maps.errors.ApiException;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.TravelMode;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import lk.damithab.curenex.BuildConfig;
import lk.damithab.curenex.R;
import lk.damithab.curenex.databinding.ActivityClinicLocationBinding;
import lk.damithab.curenex.api.DirectionApi;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ClinicLocation extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityClinicLocationBinding binding;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private LatLng currentLocation;
    private Marker markerPin;

    private Polyline polyline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityClinicLocationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        locationRequest = new LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY, 3000)
                .setMinUpdateIntervalMillis(2000)
                .build();


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);

//        MapStyleOptions styleOptions = MapStyleOptions.loadRawResourceStyle(getApplicationContext(), R.raw.map_style);
//        mMap.setMapStyle(styleOptions);

        // Add a marker in Sydney and move the camera

        enableMyLocation();


        LatLng home = new LatLng(6.9090792, 79.9408215);


        // mMap.addMarker(new MarkerOptions().position(home).title("Marker in Home"));


        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(home, 15f)); // 2-5 Country, 10 - 15 City, 18 -20 Building


        mMap.setOnMapLongClickListener(latLng -> {
            if (markerPin == null) {
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.manicon));
                markerPin = mMap.addMarker(markerOptions);
            } else {
                markerPin.setPosition(latLng);
            }


            //getDirection(currentLocation, latLng);
            getDirectionWithApi(currentLocation, latLng);

        });


    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);

            startLocationUpdate();

        } else {
            ActivityCompat.requestPermissions
                    (this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }


    @RequiresPermission(allOf = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    private void startLocationUpdate() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {

                    currentLocation = new LatLng(location.getLatitude(), location.getLongitude());

                    //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f));
                }
            }
        };

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, getMainLooper());

    }
    @Override
    protected void onPause() {
        super.onPause();

        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mMap != null) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdate();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation();
            }

        }
    }
    private final ExecutorService service = Executors.newSingleThreadExecutor();

    public void getDirectionWithApi(LatLng start, LatLng end) {

        service.execute(() -> {
            String key = BuildConfig.DIRECTIONS_API_KEY;

            String origin = start.latitude + "," + start.longitude;
            String destination = end.latitude + "," + end.longitude;

            try (GeoApiContext context = new GeoApiContext.Builder().apiKey(key).build()) {

                try {
                    DirectionsResult result = DirectionsApi.newRequest(context)
                            .mode(TravelMode.DRIVING)
                            .origin(origin)
                            .destination(destination)
                            .departureTimeNow()
                            .alternatives(true)
                            .await();

                    if (result.routes.length > 0) {
                        String encodedPath = result.routes[0].overviewPolyline.getEncodedPath();

                        List<LatLng> points = PolyUtil.decode(encodedPath);

                        runOnUiThread(() -> {
                            if (polyline == null) {
                                PolylineOptions polylineOptions = new PolylineOptions();
                                polylineOptions.width(20);
                                polylineOptions.color(R.color.purple_700);
                                polylineOptions.addAll(points);

                                polyline = mMap.addPolyline(polylineOptions);
                            } else {
                                polyline.setPoints(points);
                            }
                        });

                    }

                } catch (ApiException e) {
                    throw new RuntimeException(e);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }


            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

    }

    public void getDirection(LatLng start, LatLng end) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://maps.googleapis.com/maps/api/directions/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        DirectionApi directionApi = retrofit.create(DirectionApi.class);

        String origin = start.latitude + "," + start.longitude;
        String destination = end.latitude + "," + end.longitude;

        String key = BuildConfig.DIRECTIONS_API_KEY;


        Call<JsonObject> apiJson = directionApi.getJson(origin, destination, key);
        apiJson.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                JsonObject body = response.body();
                //Log.i(this.getClass().getName(), body.getAsJsonArray("routes").toString());


                JsonArray routes = body.getAsJsonArray("routes");

                JsonObject route = routes.get(0).getAsJsonObject();
                JsonObject overviewPolyline = route.getAsJsonObject("overview_polyline");

                List<LatLng> points = PolyUtil.decode(overviewPolyline.get("points").getAsString());

                if (polyline == null) {
                    PolylineOptions polylineOptions = new PolylineOptions();
                    polylineOptions.width(20);
                    polylineOptions.color(R.color.purple_700);
                    polylineOptions.addAll(points);

                    polyline = mMap.addPolyline(polylineOptions);
                } else {
                    polyline.setPoints(points);
                }

            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {

            }
        });


    }


}