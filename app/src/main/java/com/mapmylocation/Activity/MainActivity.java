package com.mapmylocation.Activity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.mapmylocation.R;
import com.mapmylocation.Service.BackgroundService;
import com.mapmylocation.utils.Util;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    public static final int LOCATION_UPDATE_MIN_DISTANCE = 10;
    public static final int LOCATION_UPDATE_MIN_TIME = 5000;
    private GoogleMap mMap;
    private LocationManager mLocationManager;

    private ImageView mGpsNavgation;
    private Chronometer mChronometer;

    private ToggleButton mToggleGps;
    SupportMapFragment mapFragment;
    private boolean mPermissionDenied = false;
    private int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private Location location = null;
    private PolylineOptions mOptions;

    private ArrayList<LatLng> points;
    private float MAP_ZOOM_LEVEL = 18f;

    /**
     * Code used in requesting runtime permissions.
     */
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;


    private boolean mAlreadyStartedService = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        getLatLongFromService();
        mChronometer.start();

        if (mapFragment != null) {
            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap map) {
                    loadMap(map);
                    initMap();
                    drawMarker();

                }
            });
        } else {
            Toast.makeText(this, "Error - Map Fragment was null!!", Toast.LENGTH_SHORT).show();
        }


        mGpsNavgation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pointCurrentLocation();
            }
        });

        mToggleGps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mToggleGps.isChecked()) {
                    startLocationService();
                    mChronometer.setBase(SystemClock.elapsedRealtime());
                    mChronometer.start();
                    drawMarker();
                } else {
                    Intent intent = new Intent(MainActivity.this, BackgroundService.class);
                    stopService(intent);
                    mAlreadyStartedService = false;
                    drawMarker();
                    mChronometer.stop();
                }
            }
        });


    }

    private void getLatLongFromService() {
        LocalBroadcastManager.getInstance(this).registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        String latitude = intent.getStringExtra(BackgroundService.EXTRA_LATITUDE);
                        String longitude = intent.getStringExtra(BackgroundService.EXTRA_LONGITUDE);

                        if (latitude != null && longitude != null) {
                            //mMsgView.setText(getString(R.string.msg_location_service_started) + "\n Latitude : " + latitude + "\n Longitude: " + longitude);
                            LatLng latLng = new LatLng(Double.valueOf(latitude), Double.valueOf(longitude));

                            points.add(latLng); //added

                        }
                    }
                }, new IntentFilter(BackgroundService.ACTION_LOCATION_BROADCAST)
        );
    }

    protected void loadMap(GoogleMap googleMap) {
        mMap = googleMap;

    }

    private void initViews() {
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mToggleGps = (ToggleButton) findViewById(R.id.toggle_gps);
        points = new ArrayList<LatLng>();
        mChronometer = (Chronometer) findViewById(R.id.chronometer);
        mGpsNavgation = (ImageView) findViewById(R.id.navigation);
    }


    private void initMap() {
        int googlePlayStatus = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (googlePlayStatus != ConnectionResult.SUCCESS) {
            GooglePlayServicesUtil.getErrorDialog(googlePlayStatus, this, -1).show();
            finish();
        } else {
            if (mMap != null) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.

                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            LOCATION_PERMISSION_REQUEST_CODE);
                    return;
                }
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
                mMap.getUiSettings().setAllGesturesEnabled(true);

            }
        }
    }


    private void drawMarker() {
        if (mMap != null) {
            mMap.clear();
            mMap.getUiSettings().setRotateGesturesEnabled(true);
            if (points != null && points.size() > 0) {
                int locationsCount = points.size();
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(points.get(locationsCount - 1), MAP_ZOOM_LEVEL));
                // LatLng gps = new LatLng(location.getLatitude(), location.getLongitude());
                mOptions = new PolylineOptions();
                mOptions.addAll(points);
                mOptions.width(10);
                mOptions.color(ContextCompat.getColor(this, R.color.textColorPrimaryInverse));
                mMap.addPolyline(mOptions);
                if (points.size() > 0) {
                    mMap.addMarker(new MarkerOptions()
                            .position(points.get(points.size() - 1))
                            .title("End Position"))
                            .setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                } else {
                    mMap.addMarker(new MarkerOptions()
                            .position(points.get(0))
                            .title("Current Position"))
                            .setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_start_map_marker));
                }

                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(points.get(0), 12));
            } else {
                pointCurrentLocation();
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (Util.isPermissionGranted(permissions, grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            drawMarker();
        } else {
            // Display the missing permission error dialog when the fragments resume.
            mPermissionDenied = true;
        }
    }

    private void pointCurrentLocation() {

        if (points == null || points.size() == 0) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (location != null) {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), MAP_ZOOM_LEVEL));
                mMap.addMarker(new MarkerOptions()
                        .title("Started running")
                        .position(new LatLng(location.getLatitude(), location.getLongitude()))
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_start_map_marker))
                );
            }
        } else {
            int size = points.size();
            if (size > 0) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(points.get(size - 1), MAP_ZOOM_LEVEL));
                mMap.setLatLngBoundsForCameraTarget(null);

            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        CheckGooglePlayServices();
    }

    @Override
    protected void onDestroy() {
        //Stop location sharing service to app server.........

        stopService(new Intent(this, BackgroundService.class));
        mAlreadyStartedService = false;
        super.onDestroy();
    }

    /**
     * Step 1: Check Google Play services
     */
    private void CheckGooglePlayServices() {

        //Check whether this user has installed Google play service which is being used by Location updates.
        if (isGooglePlayServicesAvailable()) {

            //Passing null to indicate that it is executing for the first time.
            checkInternetConnection(null);

        } else {
            Toast.makeText(getApplicationContext(), R.string.no_google_playservice_available, Toast.LENGTH_LONG).show();
        }
    }


    /**
     * Step 2: Check for internet connection
     */
    private Boolean checkInternetConnection(DialogInterface dialog) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        if (activeNetworkInfo == null || !activeNetworkInfo.isConnected()) {
            promptInternetConnect();
            return false;
        }


        if (dialog != null) {
            dialog.dismiss();
        }

        //Yes there is active internet connection. Next check Location is granted by user or not.

        if (checkPermissions()) { //Yes permissions are granted by the user. Go to the next step.
            startLocationService();
        } else {  //No user has not granted the permissions yet. Request now.
            requestPermissions();
        }
        return true;
    }

    /**
     * Show A Dialog with button to refresh the internet state.
     */
    private void promptInternetConnect() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(R.string.title_alert_no_intenet);
        builder.setMessage(R.string.msg_alert_no_internet);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Step 3: Start the Location Monitor Service
     */
    private void startLocationService() {

        //And it will be keep running until you close the entire application from task manager.
        //This method will executed only once.

        if (!mAlreadyStartedService) {


            //Start location sharing service to app server.........
            Intent intent = new Intent(this, BackgroundService.class);
            startService(intent);

            mAlreadyStartedService = true;
            //Ends................................................
        }
    }

    /**
     * Return the availability of GooglePlayServices
     */
    public boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int status = googleApiAvailability.isGooglePlayServicesAvailable(this);
        if (status != ConnectionResult.SUCCESS) {
            if (googleApiAvailability.isUserResolvableError(status)) {
                googleApiAvailability.getErrorDialog(this, status, 2404).show();
            }
            return false;
        }
        return true;
    }


    /**
     * Return the current state of the permissions needed.
     */
    private boolean checkPermissions() {
        int permissionState1 = ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION);

        int permissionState2 = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);

        return permissionState1 == PackageManager.PERMISSION_GRANTED && permissionState2 == PackageManager.PERMISSION_GRANTED;

    }

    /**
     * Start permissions requests.
     */
    private void requestPermissions() {

        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION);

        boolean shouldProvideRationale2 =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION);


        // Provide an additional rationale to the img_user. This would happen if the img_user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale || shouldProvideRationale2) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.");
            showSnackbar(R.string.permission_rationale,
                    android.R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                                    REQUEST_PERMISSIONS_REQUEST_CODE);
                        }
                    });
        } else {
            Log.i(TAG, "Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the img_user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }


    /**
     * Shows a {@link Snackbar}.
     *
     * @param mainTextStringId The id for the string resource for the Snackbar text.
     * @param actionStringId   The text of the action item.
     * @param listener         The listener associated with the Snackbar action.
     */
    private void showSnackbar(final int mainTextStringId, final int actionStringId,
                              View.OnClickListener listener) {
        Snackbar.make(
                findViewById(android.R.id.content),
                getString(mainTextStringId),
                Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(actionStringId), listener).show();
    }

}
