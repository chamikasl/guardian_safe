package icbt.team1.gs.Driver.Fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import icbt.team1.gs.R;

public class HomeFragment extends Fragment {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private FirebaseAuth auth;
    private DatabaseReference databaseReference;
    private String driverId;
    private boolean isLocationUpdatesActive = false;
    private MapView mapView;
    private GoogleMap googleMap;
    private Button zoomInButton;
    private Button zoomOutButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.driver_fragment_home, container, false);

        zoomInButton = rootView.findViewById(R.id.zoomInButton);
        zoomOutButton = rootView.findViewById(R.id.zoomOutButton);

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();

        // Get the driver's UID from Firebase Auth
        driverId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "";

        // Initialize FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // Initialize location request parameters
        locationRequest = new LocationRequest();
        locationRequest.setInterval(10000); // Update interval in milliseconds
        locationRequest.setFastestInterval(5000); // Fastest update interval
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        // Initialize location callback
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    // Handle the location here
                    handleLocation(location);
                }
            }
        };

        // Initialize the mapView and set up Google Map
        mapView = rootView.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);

        mapView.getMapAsync(map -> {
            googleMap = map;

            // Set initial zoom level
            float initialZoomLevel = 15.0f; // Adjust as needed
            googleMap.moveCamera(CameraUpdateFactory.zoomTo(initialZoomLevel));

            // Set click listeners for zoom buttons
            zoomInButton.setOnClickListener(view -> {
                // Zoom in by 1 level
                if (googleMap != null) {
                    googleMap.animateCamera(CameraUpdateFactory.zoomIn());
                }
            });

            zoomOutButton.setOnClickListener(view -> {
                // Zoom out by 1 level
                if (googleMap != null) {
                    googleMap.animateCamera(CameraUpdateFactory.zoomOut());
                }
            });
        });

        // Set click listeners for buttons
        setButtonClickListeners(rootView);

        return rootView;
    }

    // Helper method to set click listeners for buttons
    private void setButtonClickListeners(View rootView) {

        // Open QR scanner button
        ImageButton openQRScannerButton = rootView.findViewById(R.id.open_qr_scanner);
        Button startRide = rootView.findViewById(R.id.startRideButton);
        Button endRide = rootView.findViewById(R.id.endRideButton);

        openQRScannerButton.setOnClickListener(view -> openTimeSelectorFragment());

        endRide.setOnClickListener(view -> {
            startRide.setVisibility(View.VISIBLE);
            zoomOutButton.setVisibility(View.GONE);
            zoomInButton.setVisibility(View.GONE);
            openQRScannerButton.setVisibility(View.GONE);
            endRide.setVisibility(View.GONE);
            mapView.setVisibility(View.GONE);
            // Stop location updates when "end ride" button is clicked
            stopLocationUpdates();
            Toast.makeText(requireContext(), "Location sharing stopped.", Toast.LENGTH_SHORT).show();
        });

        startRide.setOnClickListener(view -> {
            startRide.setVisibility(View.GONE);
            zoomOutButton.setVisibility(View.VISIBLE);
            zoomInButton.setVisibility(View.VISIBLE);
            openQRScannerButton.setVisibility(View.VISIBLE);
            endRide.setVisibility(View.VISIBLE);
            // Check and request location permission before starting location updates
            if (checkLocationPermission()) {
                startLocationUpdates();
                Toast.makeText(requireContext(), "Location sharing started.", Toast.LENGTH_SHORT).show();
            }
        });


    }

    private void openTimeSelectorFragment() {
        // Create and show the TimeSelectorFragment
        TimeSelectorFragment timeSelectorFragment = new TimeSelectorFragment();
        timeSelectorFragment.show(getFragmentManager(), "time_selector_dialog");
    }

    private void handleLocation(Location location) {
        // Handle the location here
        String locationUrl = "https://maps.google.com/?q=" + location.getLatitude() + "," + location.getLongitude();
        //Toast.makeText(requireContext(), "Current Location URL: " + locationUrl, Toast.LENGTH_LONG).show();
        saveLocationLink(locationUrl);

        if (googleMap != null) {
            LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
            googleMap.clear();
            googleMap.addMarker(new MarkerOptions().position(currentLocation).title("Current Location"));
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));
            googleMap.animateCamera(CameraUpdateFactory.zoomTo(15.0f)); // Adjust zoom level
            mapView.setVisibility(View.VISIBLE); // Show the map
            zoomInButton.setVisibility(View.VISIBLE);
            zoomOutButton.setVisibility(View.VISIBLE);
        }
    }

    private void saveLocationLink(String locationUrl) {
        if (!driverId.isEmpty()) {
            String driverLocationPath = "drivers/" + driverId + "/locationLink";
            databaseReference = FirebaseDatabase.getInstance().getReference();
            databaseReference.child(driverLocationPath).setValue(locationUrl)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                // Location link saved successfully
                            } else {
                                // Error saving location link
                                Toast.makeText(requireContext(), "Error saving location link", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    private boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Request location permission if not granted
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, start location updates if not already active
                if (!isLocationUpdatesActive) {
                    startLocationUpdates();
                }
            } else {
                Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startLocationUpdates() {
        if (checkLocationPermission()) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
            isLocationUpdatesActive = true;
        }
    }

    private void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
        isLocationUpdatesActive = false;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }
}
