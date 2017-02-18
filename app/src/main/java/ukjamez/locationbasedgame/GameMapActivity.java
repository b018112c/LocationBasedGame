package ukjamez.locationbasedgame;

import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.ConnectionResult;
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

public class GameMapActivity extends FragmentActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener, LocationSource,OnMapReadyCallback {

    private GoogleMap mMap;
    private LatLng myCurrentPosition = new LatLng(-34, 151);
    private GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    private LocationRequest mLocationRequest;
    private LocationManager mLocationManager;
    private Marker mDrop;
    private LocationSource.OnLocationChangedListener mListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onConnected(Bundle bundle) {

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(100); // Update location every second

        mMap.setMyLocationEnabled(true);
        //UiSettings.setMyLocationButtonEnabled(false);
        mMap.setLocationSource(this);

        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            myCurrentPosition = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());

            mDrop = mMap.addMarker(new MarkerOptions()
                    .position(placeRandomMarker())
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
                    .title("Timer 15mins"));
        }
    }

    public LatLng placeRandomMarker(){
        double r = 500/111000f;
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
        return new LatLng(newY,newX);

    }

    @Override
    public void onConnectionSuspended(int i) {

    }


    @Override
    public void activate(OnLocationChangedListener listener)
    {
        mListener = listener;
    }

    @Override
    public void deactivate()
    {
        mListener = null;
    }

    @Override
    public void onLocationChanged(Location location) {
        if( mListener != null )
        {
            mListener.onLocationChanged( location );
            myCurrentPosition = new LatLng(location.getLatitude(), location.getLongitude());
            //mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            LatLngBounds bounds = new LatLngBounds(myCurrentPosition, myCurrentPosition);
            mMap.setLatLngBoundsForCameraTarget(bounds);
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

   /* @Override
    public void onPause()
    {
        if(mLocationManager != null)
        {
            mLocationManager.removeUpdates(this);
        }

        super.onPause();
    }

    @Override
    public void onResume()
    {
        super.onResume();

        if (mMap == null)
        {
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);

            if (mMap != null)
            {
                mMap.setMyLocationEnabled(true);
            }

            //This is how you register the LocationSource
            mMap.setLocationSource(this);
        }

        if(mLocationManager != null)
        {
            mMap.setMyLocationEnabled(true);
        }
    }*/


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
