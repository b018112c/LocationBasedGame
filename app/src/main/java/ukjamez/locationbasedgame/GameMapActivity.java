package ukjamez.locationbasedgame;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.CountDownTimer;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.server.converter.StringToIntConverter;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

import java.util.ArrayList;
import java.util.List;

public class GameMapActivity extends FragmentActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener, LocationSource, OnMapReadyCallback {

    private GoogleMap mMap;
    private LatLng myCurrentPosition = new LatLng(-34, 151);
    private GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    private LocationRequest mLocationRequest;
    private LocationServices mLocationManager;
    private Marker mDrop;
    private LocationSource.OnLocationChangedListener mListener;
    private boolean mDropPlaced = false;
    private Button btnPylon;
    private TextView txtPylonCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_map);

        btnPylon = (Button) findViewById(R.id.buttonP);
        txtPylonCount = (TextView) findViewById(R.id.textP);

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .addApi(ActivityRecognition.API)
                    .build();
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        btnPylon.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                mMap.addMarker(new MarkerOptions()
                        .position(myCurrentPosition)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                        .title("Pylon"));
                txtPylonCount.setText(Integer.toString(AddPylon(-1)));
                noOfPylons += 1;
                markersList.add(myCurrentPosition);

                if(noOfPylons == 3){
                mMap.addPolygon(new PolygonOptions()
                        .addAll(markersList)
                        .strokeColor(Color.CYAN));
                    noOfPylons = 0;
                    markersList.clear();
                }
            }
        });

    }

    private ArrayList<LatLng> markersList = new ArrayList<LatLng>();
    private int noOfPylons = 0;
    private static final String PrefsFile = "PrefsFile";

    private int AddPylon(int val){
        SharedPreferences pref = getApplicationContext().getSharedPreferences(PrefsFile, MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();

        int pylonCount = pref.getInt("pylonCount", 3); //,default
        int finalValue = pylonCount + val;
        editor.putInt("pylonCount", finalValue);
        editor.commit();

        return finalValue;
    }


    @Override
    public void onConnected(Bundle bundle) {

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        txtPylonCount.setText(String.format("%d",AddPylon(0)));
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(1000); // Update location every second
        //mLocationRequest.setSmallestDisplacement(1);

        Intent intent = new Intent( this, RecogniseActivity.class );
        PendingIntent pendingIntent = PendingIntent.getService( this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT );
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates( mGoogleApiClient, 2000, pendingIntent );

        mMap.setMyLocationEnabled(true);

        LocationUpdatesBegin();

        //UiSettings.setMyLocationButtonEnabled(false);
        mMap.setLocationSource(this);

        if (mLastLocation != null) {
            myCurrentPosition = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());

            if (!mDropPlaced) {
                new CountDownTimer(900000, 1000) {

                    public void onTick(long millisUntilFinished) {

                        int seconds = (int) (millisUntilFinished / 1000) % 60 ;
                        int minutes = (int) ((millisUntilFinished / (1000*60)) % 60);
                        mDrop.setTitle(String.valueOf(minutes +":" + String.format("%02d",seconds)));
                        mDrop.showInfoWindow();
                    }

                    public void onFinish() {
                        mDrop.remove();
                    }

                }.start();
                mDrop = mMap.addMarker(new MarkerOptions()
                        .position(placeRandomMarker())
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
                        .title("Timer 15mins"));
                mDrop.showInfoWindow();
                mDropPlaced = true;

                mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener()
                {

                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        if(mDrop != null && marker.equals(mDrop)){
                            mDrop.remove();
                            txtPylonCount.setText(String.format("%d",AddPylon(2)));
                            return true;
                        }
                        return false;
                    }

                });
            }
        }
    }

    public LatLng placeRandomMarker() {
        double r = 500 / 111000f;
        double x0 = myCurrentPosition.longitude;
        double y0 = myCurrentPosition.latitude;
        double u = Math.random();
        double v = Math.random();
        double w = r * Math.sqrt(u);
        double t = 2 * Math.PI * v;
        double x = w * Math.cos(t);
        double y1 = w * Math.sin(t);
        double x1 = x / Math.cos(y0);

        double newY = y0 + y1;
        double newX = x0 + x1;
        return new LatLng(newY, newX);

    }

    @Override
    public void onConnectionSuspended(int i) {

    }


    @Override
    public void activate(OnLocationChangedListener listener) {
        mListener = listener;
    }

    @Override
    public void deactivate() {
        mListener = null;
    }

    @Override
    public void onLocationChanged(Location location) {
        if (mListener != null) {
            mListener.onLocationChanged(location);
            myCurrentPosition = new LatLng(location.getLatitude(), location.getLongitude());
            LatLngBounds bounds = new LatLngBounds(myCurrentPosition, myCurrentPosition);
            mMap.setLatLngBoundsForCameraTarget(bounds);
            //mMap.animateCamera(CameraUpdateFactory.newLatLng(myCurrentPosition));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(myCurrentPosition));

        }
    }

    //@Override
    // public void OnTick(){

    // }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        //buildGoogleApiClient();
    }

    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onPause() {
        if (mLocationManager != null) {
            mLocationManager.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected() /*&& !mRequestingLocationUpdates*/) {
            LocationUpdatesBegin();
        }
    }

    private void LocationUpdatesBegin() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mLocationManager.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.style_json));

        CameraPosition cameraPosition = new CameraPosition.Builder()
        .target(myCurrentPosition)
                .zoom(15)
                .bearing(0)
                .tilt(30)
                .build();

        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }
}
