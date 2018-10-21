package com.commercial.askitloud.japhhi;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.PolyUtil;
import java.util.ArrayList;

import static java.lang.Integer.max;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleMap.OnMapClickListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,GoogleMap.OnInfoWindowClickListener {
    private GoogleMap mMap;
    GoogleApiClient mGoogleApiClient;
    LatLng station;
    int no;
    Location mLastLocation;
    Marker mCurrLocationMarker;
    Marker mPoliceStationMarker;
    LocationRequest mLocationRequest;
    public static String contactPerson = "Ashutosh Agrawal";
    public static String contactNumber = "9454517680";
    public ArrayList<LatLng> arrayPoints;
    DatabaseReference FireDb;
    private boolean checkClick = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        Toolbar mTopToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mTopToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            checkLocationPermission();
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Firebase Work

        FireDb = FirebaseDatabase.getInstance().getReference();
        DatabaseReference childRef = FireDb.child("latlngs");
        Log.e("tag1","in here"+childRef.toString());
        childRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int size = (int)dataSnapshot.getChildrenCount();
                Log.e("tag2","in here-"+size);
                for (int j = 1; j <= size; j++)
                {
                    arrayPoints = new ArrayList<LatLng>();
                    final DatabaseReference territoryRef = FireDb.child("latlngs/territory"+j).getRef();
                    territoryRef.addValueEventListener(new ValueEventListener() {
                        @RequiresApi(api = Build.VERSION_CODES.N)
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            int length = (int) dataSnapshot.getChildrenCount();
                            Log.e("tag3", dataSnapshot.getRef().toString() + "-in here-" + length);
                            for (int k = 1; k < length; k++) {
                                LatLng value = new LatLng((double) dataSnapshot.child("value" + k).child("x").getValue(), (double) dataSnapshot.child("value" + k).child("y").getValue());
                                arrayPoints.add(value);
                                Log.e("value::", value.latitude + "," + value.longitude);
                            }
                            Log.e("tag5", "in here");
                            Double x = (double) dataSnapshot.child("station").child("x").getValue();
                            Double y = (double) dataSnapshot.child("station").child("y").getValue();
                            Log.e("tag",x+" , "+y);
                            station = new LatLng(x,y);
                            Log.e("tag",station.latitude + "," + station.longitude);
                            LatLng myLocation = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                            Log.e("tagP",String.valueOf(arrayPoints.size()));
                            if(arrayPoints.size()>3)
                                checkTerritory(myLocation, station, arrayPoints);
                            else
                                Toast.makeText(MapsActivity.this,"Atleast 3 points required to draw a polygon",Toast.LENGTH_SHORT).show();
                        }

                            @Override
                            public void onCancelled (@NonNull DatabaseError databaseError){

                            }

                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            }
        } else {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }
        mMap.animateCamera(CameraUpdateFactory.zoomTo(30));
        mMap.setInfoWindowAdapter(new PopupAdapter(getLayoutInflater()));
        mMap.setOnInfoWindowClickListener(this);

    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {

        mLastLocation = location;

        if (mCurrLocationMarker != null)
        {
            mCurrLocationMarker.remove();
        }

        //Place current location marker
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Current Position");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
        mCurrLocationMarker = mMap.addMarker(markerOptions);



        //move map camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(30));

        //stop location updates
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    public Polygon countPolygonPoints(ArrayList<LatLng> arrayList) {
        Polygon polygon = null;
        if (arrayList.size() >= 3) {
            checkClick = true;
            PolygonOptions polygonOptions = new PolygonOptions();
            polygonOptions.addAll(arrayList);
            polygonOptions.strokeColor(Color.WHITE);
            polygonOptions.strokeWidth(4);
            polygon = mMap.addPolygon(polygonOptions);
        }
        else
            Toast.makeText(MapsActivity.this,"At least 3 points needed to draw a polygon.",Toast.LENGTH_SHORT).show();
        arrayList.clear();
        return polygon;
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Asking user if explanation is needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted. Do the
                    // contacts-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }

                } else {
                    // Permission denied, Disable the functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }

            // other 'case' lines to check for other permissions this app might request.
            // You can add here other case statements according to your requirement.
        }
    }

    public void checkTerritory(LatLng currLocation,LatLng station,ArrayList<LatLng> arrayList) {
        Log.e("TAG_A", "mssg");
        LatLng latlng = new LatLng(currLocation.latitude, currLocation.longitude);
        if (PolyUtil.containsLocation(latlng,countPolygonPoints(arrayList).getPoints(), true)) {
            //show police station of territory
            MarkerOptions markerOptions1 = new MarkerOptions();
            markerOptions1.position(station);
            markerOptions1.title("You are in territory of : ");
            markerOptions1.snippet("Admin"+"\n"+"Contact Person :"+contactPerson+"\n"+"Contact No. :"+contactNumber);
            markerOptions1.icon(BitmapDescriptorFactory.fromResource(R.drawable.police_img));
            mPoliceStationMarker = mMap.addMarker(markerOptions1);
            Toast.makeText(this.getBaseContext(), "Click on police icon to get nearest official info.", Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        if(!marker.getTitle().equals("Current Position")) {
            Intent phoneIntent = new Intent(Intent.ACTION_DIAL);
            phoneIntent.setData(Uri.parse("tel:" +contactNumber));
            startActivity(phoneIntent);
        }
    }

    @Override
    public void onMapClick(LatLng latLng) {
        mMap.setInfoWindowAdapter(null);
    }
}