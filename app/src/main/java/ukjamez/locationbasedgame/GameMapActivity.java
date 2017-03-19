package ukjamez.locationbasedgame;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import java.util.ArrayList;
import java.util.Random;

public class GameMapActivity extends FragmentActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener, LocationSource, OnMapReadyCallback {

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private LocationServices mLocationManager;
    private LocationSource.OnLocationChangedListener mListener;
    private boolean mDropUsed = false;

    private LatLng myCurrentPosition;
    private LatLng myLastPosition;

    private Marker mDrop;
    private Button btnPylon;
    private Button btnDrop;
    private TextView txtPylonCount;
    public TextView textWalk;
    public TextView textRun;
    public TextView textActivity;
    public ProgressBar progressWalk;
    public ProgressBar progressRun;
    public ProgressBar progressBoth;
    public TextView textT1Count;
    public TextView textT2Count;
    public TextView textT3Count;

    private ArrayList<LatLng> markersList = new ArrayList<>();
    private int noOfPylons = 0;
    private static final String PrefsFile = "PrefsFile";

    private Boolean CameraSet = false;

    private SharedPreferences _Pref ;//= getApplicationContext().getSharedPreferences(PrefsFile, MODE_PRIVATE);

    private float walkDistance = 0;
    private float runDistance = 0;
    private int walkItems = 0;
    private int runItems = 0;
    private int otherItems = 0;
    private int t1Count = 0;
    private int t2Count = 0;
    private int t3Count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_map);

        btnPylon = (Button) findViewById(R.id.buttonP);
        txtPylonCount = (TextView) findViewById(R.id.textP);
        btnDrop = (Button) findViewById(R.id.buttonD);
        textWalk = (TextView) findViewById(R.id.textWalk);
        textRun = (TextView) findViewById(R.id.textRun);
        textActivity = (TextView) findViewById(R.id.textActivity);
        progressWalk = (ProgressBar) findViewById(R.id.progressWalk);
        progressRun = (ProgressBar) findViewById(R.id.progressRun);
        progressBoth = (ProgressBar) findViewById(R.id.progressBoth);
        progressWalk.getProgressDrawable().setColorFilter(
                Color.rgb(255,127,0), android.graphics.PorterDuff.Mode.SRC_IN);
        progressWalk.setScaleY(4f);progressRun.setScaleY(4f);progressBoth.setScaleY(4f);
        progressRun.getProgressDrawable().setColorFilter(
                Color.RED, android.graphics.PorterDuff.Mode.SRC_IN);
        progressBoth.getProgressDrawable().setColorFilter(
                Color.YELLOW, android.graphics.PorterDuff.Mode.SRC_IN);
        textT1Count = (TextView) findViewById(R.id.textT1);
        textT2Count = (TextView) findViewById(R.id.textT2);
        textT3Count = (TextView) findViewById(R.id.textT3);

        _Pref = getApplicationContext().getSharedPreferences(PrefsFile, MODE_PRIVATE);

        walkDistance = _Pref.getFloat("walkDistance", 0);
        runDistance =  _Pref.getFloat("runDistance", 0);
        walkItems =  _Pref.getInt("walkItems", 2);
        runItems = _Pref.getInt("runItems", 1);
        otherItems = 5;
        progressWalk.setProgress((int) walkDistance);
        progressRun.setProgress((int) runDistance);

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

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.style_json));
        mMap.setMaxZoomPreference(17);
        mMap.setMinZoomPreference(14);
        mMap.setPadding(0,140,0,0);

        btnPylon.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //mMap.addMarker(new MarkerOptions()
                //        .position(myCurrentPosition)
                //        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET));
                mMap.addCircle(new CircleOptions()
                        .center(myCurrentPosition)
                        .radius(250)
                        .strokeColor(Color.argb(90,127,0,255))
                        .fillColor(Color.argb(50,127,0,255)));
                txtPylonCount.setText(Integer.toString(AddPylon(-1)));
                noOfPylons += 1;
                markersList.add(myCurrentPosition);

                Random random = new Random();
                //randomly add animal markers
                int tier1quantity = random.nextInt(3) + 2;
                for(int i = 0; i < tier1quantity; i++){
                    LatLng location = placeRandomMarker(215, Math.random(),myCurrentPosition);//store this
                    mMap.addMarker(new MarkerOptions()
                            .position(location).snippet("T1")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));
                }

                int tier2quantity = random.nextInt(3) + 0;
                for(int i = 0; i < tier2quantity; i++){
                    LatLng location = placeRandomMarker(215, Math.random(),myCurrentPosition);//store this
                    mMap.addMarker(new MarkerOptions()
                            .position(location).snippet("T2")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
                }

                if(noOfPylons == 3){
                    mMap.addPolygon(new PolygonOptions()
                            .addAll(markersList)
                            .strokeColor(Color.CYAN));
                    noOfPylons = 0;
                    for (LatLng marker : markersList)
                    {
                        int tier2quantity2 = random.nextInt(4) + 2;
                        for(int i = 0; i < tier2quantity2; i++){
                            LatLng location = placeRandomMarker(215, Math.random(),marker);//store this
                            mMap.addMarker(new MarkerOptions()
                                    .position(location).snippet("T2")
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
                        }
                        int tier3quantity2 = random.nextInt(3) + 0;
                        for(int i = 0; i < tier3quantity2; i++){
                            LatLng location = placeRandomMarker(215, Math.random(),marker);//store this
                            mMap.addMarker(new MarkerOptions()
                                    .position(location).snippet("T3")
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                        }
                    }
                    markersList.clear();
                }
            }
        });

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener()
        {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if(marker.getSnippet().equals("T1")){
                    marker.remove();
                    t1Count+=1;
                    textT1Count.setText(String.format("%d",t1Count));
                    return true;
                }else if(marker.getSnippet().equals("T2")){
                    marker.remove();
                    t2Count+=1;
                    textT2Count.setText(String.format("%d",t2Count));
                    return true;
                }else if(marker.getSnippet().equals("T3")){
                    marker.remove();
                    t3Count+=1;
                    textT3Count.setText(String.format("%d",t3Count));
                    return true;
                }else if(mDrop != null && marker.equals(mDrop)){
                    mDrop.remove();
                    txtPylonCount.setText(String.format("%d",AddPylon(2)));
                    return true;
                }
                return false;
            }
        });

        btnDrop.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!mDropUsed) {
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
                            .position(placeRandomMarker(500, 1,myCurrentPosition))
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET))
                            .title("Airdrop"));
                    mDrop.showInfoWindow();
                    mDropUsed = true;
                    btnDrop.setVisibility(View.INVISIBLE);

//                    mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener()
//                    {
//                        @Override
//                        public boolean onMarkerClick(Marker marker) {
//                            if(mDrop != null && marker.equals(mDrop)){
//                                mDrop.remove();
//                                txtPylonCount.setText(String.format("%d",AddPylon(2)));
//                                return true;
//                            }
//                            return false;
//                        }
//                    });
                }
            }
        });
        //if (myCurrentPosition != null) {}
    }

    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    private int AddPylon(int val){
        SharedPreferences pref = getApplicationContext().getSharedPreferences(PrefsFile, MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();

        int pylonCount = pref.getInt("pylonCount", 3); //,default
        int finalValue = pylonCount + val;
        editor.putInt("pylonCount", finalValue);
        editor.apply();

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
        mLocationRequest.setSmallestDisplacement(1);

        Intent activityIntent = new Intent( this, RecogniseActivity.class );
        PendingIntent pendingIntent = PendingIntent.getService( this, 0, activityIntent, PendingIntent.FLAG_UPDATE_CURRENT );
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates( mGoogleApiClient, 1000, pendingIntent );

        mMap.setMyLocationEnabled(true);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mLocationManager.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        myCurrentPosition = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
        myLastPosition = myCurrentPosition;

        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setScrollGesturesEnabled(false);
        mMap.getUiSettings().setTiltGesturesEnabled(false);

        mMap.setLocationSource(this);
    }

    public LatLng placeRandomMarker(int radius, double distance, LatLng location) {
        double r = radius / 111320f;
        double x0 = location.longitude;
        double y0 = location.latitude;

        double u = distance;
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
    public void onConnectionSuspended(int i) {    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        //buildGoogleApiClient();
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
        //if (mGoogleApiClient.isConnected() /*&& !mRequestingLocationUpdates*/) {
            //LocationUpdatesBegin();
        //}
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

            if(myCurrentPosition != null)
                myLastPosition = myCurrentPosition;
            myCurrentPosition = new LatLng(location.getLatitude(), location.getLongitude());
            LatLngBounds bounds = new LatLngBounds(myCurrentPosition, myCurrentPosition);
            mMap.setLatLngBoundsForCameraTarget(bounds);

            if(!CameraSet){
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(myCurrentPosition)
                        .zoom(15)
                        .bearing(0)
                        .tilt(30)
                        .build();
                mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                CameraSet = true;
            }else
            {
                mMap.animateCamera(CameraUpdateFactory.newLatLng(myCurrentPosition));
            }

            if(myLastPosition != null) {
                float[] distance = new float[1];
                Location.distanceBetween(myLastPosition.latitude, myLastPosition.longitude, myCurrentPosition.latitude, myCurrentPosition.longitude, distance);

                //SharedPreferences pref = getApplicationContext().getSharedPreferences(PrefsFile, MODE_PRIVATE);
                String val = _Pref.getString("currentActivity", "N/A");//use int
                String test = _Pref.getString("testConfidence", "N/A");
                if (val == "Walking") {
                    walkDistance += distance[0];
                    if ((int)walkDistance >= 500){
                        walkDistance -= 500;
                        walkItems+=1;
                        textWalk.setText(Float.toString(walkItems));
                        SharedPreferences.Editor editor = _Pref.edit();
                        editor.putInt("walkItems", walkItems);
                        editor.commit();
                    }
                    progressWalk.setProgress((int)walkDistance);
                    //progressBoth.setProgress((int)walkDistance + (int)runDistance);
                }
                else if (val == "Running") {
                    runDistance += distance[0];
                    if ((int)runDistance >= 500){
                        runDistance -= 500;
                        runItems+=1;
                        textRun.setText(Float.toString(runItems));
                        SharedPreferences.Editor editor = _Pref.edit();
                        editor.putInt("runItems", runItems);
                        editor.commit();
                    }
                    progressRun.setProgress((int)runDistance);
                    //progressBoth.setProgress((int)walkDistance + (int)runDistance);
                }
                textActivity.setText(test);
            }
            SharedPreferences.Editor editor = _Pref.edit();
            editor.putFloat("walkDistance", walkDistance);
            editor.putFloat("RunDistance", runDistance);
            editor.commit();
        }
    }

}