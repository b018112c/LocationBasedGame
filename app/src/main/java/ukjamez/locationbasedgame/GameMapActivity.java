package ukjamez.locationbasedgame;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.location.Location;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;


public class GameMapActivity extends FragmentActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener, LocationSource, OnMapReadyCallback {

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private LocationServices mLocationManager;
    private LocationSource.OnLocationChangedListener mListener;

    private LatLng myCurrentPosition;
    private LatLng myLastPosition;

    private Marker mDrop;
    private FloatingActionButton btnPylon;
    private FloatingActionButton btnDrop;
    private TextView txtPylonCount;
    public TextView textWalk;
    public TextView textRun;

    public ProgressBar progressWalk;
    public ProgressBar progressRun;
    public ProgressBar progressBoth;
    public TextView textT1Count;
    public TextView textT2Count;
    public TextView textT3Count;

    private ArrayList<Circle> connectedDomesList = new ArrayList<>();
    private ArrayList<Circle> unconnectedDomesList = new ArrayList<>();
    private ArrayList<Polygon> connectedArray = new ArrayList<>();

    private static final String PrefsFile = "PrefsFile";

    private ArrayList<Marker> tier1List = new ArrayList<>();
    private ArrayList<Marker> tier2List = new ArrayList<>();
    private ArrayList<Marker> tier3List = new ArrayList<>();

    private Boolean CameraSet = false;

    private SharedPreferences _Pref;

    private float walkDistance = 0;
    private float runDistance = 0;
    private float combinedDistance = 0;
    private int walkItems = 0;
    private int runItems = 0;
    private int otherItems = 0;

    private int t1Count = 0;
    private int t2Count = 0;
    private int t3Count = 0;

    private static final int collectionRadius = 75;

    private CountDownTimer dropTimer;

    private Circle myLocationRadius;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_map);

        btnPylon = (FloatingActionButton) findViewById(R.id.buttonP);
        txtPylonCount = (TextView) findViewById(R.id.textP);
        btnDrop = (FloatingActionButton) findViewById(R.id.buttonD);
        textWalk = (TextView) findViewById(R.id.textWalk);
        progressWalk = (ProgressBar) findViewById(R.id.progressWalk);
        progressWalk.getProgressDrawable().setColorFilter(Color.rgb(255, 127, 0), android.graphics.PorterDuff.Mode.SRC_IN);
        progressWalk.setScaleY(4f);

        textRun = (TextView) findViewById(R.id.textRun);
        progressRun = (ProgressBar) findViewById(R.id.progressRun);
        progressRun.getProgressDrawable().setColorFilter(Color.RED, android.graphics.PorterDuff.Mode.SRC_IN);
        progressRun.setScaleY(4f);
        progressBoth = (ProgressBar) findViewById(R.id.progressBoth);
        progressBoth.getProgressDrawable().setColorFilter(Color.BLUE, android.graphics.PorterDuff.Mode.SRC_IN);
        progressBoth.setScaleY(4f);
        textT1Count = (TextView) findViewById(R.id.textT1);
        textT2Count = (TextView) findViewById(R.id.textT2);
        textT3Count = (TextView) findViewById(R.id.textT3);


        _Pref = getApplicationContext().getSharedPreferences(PrefsFile, MODE_PRIVATE);

        walkDistance = _Pref.getFloat("walkDistance", 0);
        runDistance = _Pref.getFloat("runDistance", 0);
        combinedDistance = _Pref.getFloat("combinedDistance", 0);
        walkItems = _Pref.getInt("walkItems", 2);
        textWalk.setText(Integer.toString(walkItems));
        runItems = _Pref.getInt("runItems", 1);
        textRun.setText(Integer.toString(runItems));
        otherItems = 5;

        progressWalk.setProgress((int) walkDistance);
        progressRun.setProgress((int) runDistance);
        progressBoth.setProgress((int) combinedDistance);

        t1Count = _Pref.getInt("tier1Count", 0);
        textT1Count.setText(Integer.toString(t1Count));
        t2Count = _Pref.getInt("tier2Count", 0);
        textT2Count.setText(Integer.toString(t2Count));
        t3Count = _Pref.getInt("tier3Count", 0);
        textT3Count.setText(Integer.toString(t3Count));

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

    //###############Saving and loading Data Methods################

    private void saveLocations() {

        SharedPreferences pref = getApplicationContext().getSharedPreferences(PrefsFile, MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        String allConnectedDomes = "";
        for (Circle dome : connectedDomesList) {
            if (allConnectedDomes != "") allConnectedDomes += ";";
            allConnectedDomes += (dome.getCenter().latitude + "," + dome.getCenter().longitude);
        }
        editor.putString("allConnectedDomes", allConnectedDomes);
        String allUnconnectedDomes = "";
        for (Circle dome : unconnectedDomesList) {
            if (allUnconnectedDomes != "") allUnconnectedDomes += ";";
            allUnconnectedDomes += (dome.getCenter().latitude + "," + dome.getCenter().longitude);
        }
        editor.putString("allUnconnectedDomes", allUnconnectedDomes);
        editor.commit();
    }

    private void loadLocations() {

        String allConnectedDomes = _Pref.getString("allConnectedDomes", "");
        String[] splitConnectedDomes = allConnectedDomes.split(";");
        for (String splitDome : splitConnectedDomes) {
            if (splitDome.contains(",")) {
                String[] splitLine = splitDome.split(",");
                LatLng dl = (new LatLng(Double.parseDouble(splitLine[0]), Double.parseDouble(splitLine[1])));

                Circle circle = mMap.addCircle(new CircleOptions()
                        .center(dl)
                        .radius(250)
                        .strokeColor(Color.argb(90, 127, 0, 255))
                        .fillColor(Color.argb(50, 127, 0, 255)));
                connectedDomesList.add(circle);
            }
        }
        String allUnconnectedDomes = _Pref.getString("allUnconnectedDomes", "");
        String[] splitUnconnectedDomes = allUnconnectedDomes.split(";");
        for (String splitDome : splitUnconnectedDomes) {
            if (splitDome.contains(",")) {
                String[] splitLine = splitDome.split(",");
                LatLng dl = (new LatLng(Double.parseDouble(splitLine[0]), Double.parseDouble(splitLine[1])));

                Circle circle = mMap.addCircle(new CircleOptions()
                        .center(dl)
                        .radius(250)
                        .strokeColor(Color.argb(90, 127, 0, 255))
                        .fillColor(Color.argb(50, 127, 0, 255)));
                unconnectedDomesList.add(circle);
            }
        }

        for (int i = 0; i < connectedDomesList.size(); i += 3) {
            ArrayList<LatLng> polygons = new ArrayList<>();
            polygons.add(connectedDomesList.get(i).getCenter());
            polygons.add(connectedDomesList.get(i + 1).getCenter());
            polygons.add(connectedDomesList.get(i + 2).getCenter());
            connectedArray.add(mMap.addPolygon(new PolygonOptions() //add polygon
                    .addAll(polygons)
                    .strokeColor(Color.CYAN)));
        }
    }

    private void saveCollectibles() {

        SharedPreferences pref = getApplicationContext().getSharedPreferences(PrefsFile, MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        String[] allTiers = new String[3];
        allTiers[0] = "";
        allTiers[1] = "";
        allTiers[2] = "";
        for (Marker tier : tier1List) {
            if (allTiers[0] != "") allTiers[0] += ";";
            allTiers[0] += (tier.getPosition().latitude + "," + tier.getPosition().longitude);
        }
        for (Marker tier : tier2List) {
            if (allTiers[1] != "") allTiers[1] += ";";
            allTiers[1] += (tier.getPosition().latitude + "," + tier.getPosition().longitude);
        }
        for (Marker tier : tier3List) {
            if (allTiers[2] != "") allTiers[2] += ";";
            allTiers[2] += (tier.getPosition().latitude + "," + tier.getPosition().longitude);
        }
        editor.putString("allTier1", allTiers[0]);
        editor.putString("allTier2", allTiers[1]);
        editor.putString("allTier3", allTiers[2]);
        editor.commit();
    }

    private void loadCollectibles() {
        String[] allTiers = new String[3];
        allTiers[0] = _Pref.getString("allTier1", "");
        allTiers[1] = _Pref.getString("allTier2", "");
        allTiers[2] = _Pref.getString("allTier3", "");
        for (int i = 0; i <= 2; i++) {
            String[] splitToLocation = allTiers[i].split(";");
            for (String splitToLatLng : splitToLocation) {
                if (splitToLatLng.contains(",")) {
                    String[] splitLine = splitToLatLng.split(",");
                    LatLng cl = (new LatLng(Double.parseDouble(splitLine[0]), Double.parseDouble(splitLine[1])));
                    if (i == 0) {
                        tier1List.add(mMap.addMarker(new MarkerOptions()
                                .position(cl).snippet("T1")
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.fox1))));
                    } else if (i == 1) {
                        tier2List.add(mMap.addMarker(new MarkerOptions()
                                .position(cl).snippet("T2")
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.deer))));
                    } else if (i == 2) {
                        tier3List.add(mMap.addMarker(new MarkerOptions()
                                .position(cl).snippet("T3")
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.tier3))));
                    }
                }
            }
        }
    }

//##############################Map Related Methods################################

    public void AddDropTime(long newTimer) {
        dropTimer = new CountDownTimer(newTimer, 1000) {
            public void onTick(long millisUntilFinished) {

                int seconds = (int) (millisUntilFinished / 1000) % 60;
                int minutes = (int) ((millisUntilFinished / (1000 * 60)) % 60);
                mDrop.setTitle(String.valueOf(minutes + ":" + String.format("%02d", seconds)));
                mDrop.showInfoWindow();
            }

            public void onFinish() {
                OnDropRemoved();
            }

        }.start();
    }


    private float FindDistance(LatLng firstLoc, LatLng secondLoc){
        float[] gap = new float[1];
        Location.distanceBetween(firstLoc.latitude, firstLoc.longitude, secondLoc.latitude, secondLoc.longitude, gap);
        return gap[0];
    }


    private ArrayList<Marker> CollectibleCheckInCircle(ArrayList<Marker> collectibleList){
        ArrayList<Marker> keepList = new ArrayList<>(collectibleList);
        for (Marker collectible: collectibleList) {
            boolean keep = false;
            for (Circle connectedDome: connectedDomesList) {
                float gap = FindDistance(collectible.getPosition(),connectedDome.getCenter());
                if(gap <= connectedDome.getRadius()){
                    keep = true;
                    break;//no need to keep checking
                }
            }
            if(!keep){
                collectible.remove();
                keepList.remove(collectible);
            }
        }
        return keepList;
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.style_json));
        mMap.setMaxZoomPreference(17);
        mMap.setMinZoomPreference(14);
        mMap.setPadding(0, 140, 0, 0); //set map camera

        loadLocations();
        loadCollectibles(); //load map elements
        if (_Pref.getBoolean("dropUsed", false)) { //if drop hasn't been used
            Calendar c = Calendar.getInstance();
            long currentTime = c.getTimeInMillis();
            long dropEnd = _Pref.getLong("dropStart", 0) + 900000;
            long msTime = dropEnd - currentTime; //find time left till drop expires
            if (msTime > 1) {
                AddDropTime(msTime); //create the drop countdown timer
                String[] splitLoc = _Pref.getString("dropLocation", "0,0").split(",");
                LatLng dl = (new LatLng(Double.parseDouble(splitLoc[0]), Double.parseDouble(splitLoc[1])));
                mDrop = mMap.addMarker(new MarkerOptions()
                        .position(dl)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.airport))
                        .title("Airdrop"));
                mDrop.showInfoWindow();
                btnDrop.setVisibility(View.INVISIBLE); //add drop marker top map, hide drop button
            }else{
                btnDrop.setVisibility(View.VISIBLE);
                SharedPreferences.Editor editor = _Pref.edit();
                editor.putBoolean("dropUsed", false);
                editor.commit();
            }
        }

        btnPylon.setOnClickListener(new View.OnClickListener() { //create pylon placement button listener
            public void onClick(View v) {
                LatLng circleLocation = myCurrentPosition;
                ArrayList<Circle> allCircles = new ArrayList<>(unconnectedDomesList);
                allCircles.addAll(connectedDomesList);
                boolean withinDome = false;
                for (Circle withinCircle : allCircles) {
                    float gap = FindDistance(circleLocation, withinCircle.getCenter());
                    if (gap < withinCircle.getRadius()) {
                        withinDome = true;
                    }
                }
                if (_Pref.getInt("pylonCount", 3) > 0 && !withinDome) { //if player has remaining pylons

                    Circle tempCircle = mMap.addCircle(new CircleOptions()
                            .center(circleLocation)
                            .radius(250)
                            .strokeColor(Color.argb(90, 127, 0, 255))
                            .fillColor(Color.argb(50, 127, 0, 255)));

                    //connectedDomesList.add(tempCircle);
                    if (connectedArray.size() == 2) { //check if number of polygons is 3, otherwise remove 2 oldest markers and connecting triangle
                        connectedArray.get(0).remove();
                        connectedArray.remove(0);
                        connectedDomesList.get(2).remove();
                        connectedDomesList.get(1).remove();
                        connectedDomesList.get(0).remove();
                        connectedDomesList.remove(2);
                        connectedDomesList.remove(1);
                        connectedDomesList.remove(0);
                        //check all tier1/2/3 markers to see if each are within one of the
                        tier1List = CollectibleCheckInCircle(tier1List);
                        tier2List = CollectibleCheckInCircle(tier2List);
                        tier3List = CollectibleCheckInCircle(tier3List);
                    }

                    unconnectedDomesList.add(tempCircle);

                    txtPylonCount.setText(Integer.toString(AddPylon(-1)));

                    Random random = new Random();
                    //randomly add animal markers
                    int tier1quantity = random.nextInt(3) + 2;
                    for (int i = 0; i < tier1quantity; i++) {
                        LatLng location = placeRandomMarker(215, Math.random(), circleLocation);
                        tier1List.add(mMap.addMarker(new MarkerOptions()
                                .position(location).snippet("T1")
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.fox1))));
                    }

                    int tier2quantity = random.nextInt(2) + 0;
                    for (int i = 0; i < tier2quantity; i++) {
                        LatLng location = placeRandomMarker(215, Math.random(), circleLocation);
                        tier2List.add(mMap.addMarker(new MarkerOptions()
                                .position(location).snippet("T2")
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.deer))));
                    }

                    if (unconnectedDomesList.size() == 3) { //if 3 unconnected domes are present
                        ArrayList<LatLng> unconnectedLocations = new ArrayList<>();
                        for (Circle circle : unconnectedDomesList) {
                            unconnectedLocations.add(circle.getCenter());
                        }
                        connectedArray.add(mMap.addPolygon(new PolygonOptions() //add polygon
                                .addAll(unconnectedLocations)
                                .strokeColor(Color.CYAN)));

                        for (Circle marker : unconnectedDomesList) {
                            int tier2quantity2 = random.nextInt(4) + 2;
                            for (int i = 0; i < tier2quantity2; i++) {
                                LatLng location = placeRandomMarker(215, Math.random(), marker.getCenter());
                                tier2List.add(mMap.addMarker(new MarkerOptions()
                                        .position(location).snippet("T2")
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.deer))));
                            }
                            int tier3quantity2 = random.nextInt(3) + 0;
                            for (int i = 0; i < tier3quantity2; i++) {
                                LatLng location = placeRandomMarker(215, Math.random(), marker.getCenter());
                                tier3List.add(mMap.addMarker(new MarkerOptions()
                                        .position(location).snippet("T3")
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.tier3))));
                            }

                            connectedDomesList.add(marker); //add unconnected to connected in a 3
                        }
                        unconnectedDomesList.clear();//reset unconnected
                    }

                    saveCollectibles();
                    saveLocations(); //store changes
                }
            }
        });

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                float pickupDist = FindDistance(marker.getPosition(), myCurrentPosition);
                if (pickupDist < collectionRadius) {

                    if (mDrop != null && marker.equals(mDrop)) {
                        OnDropRemoved();
                        dropTimer.cancel();
                        txtPylonCount.setText(String.format("%d", AddPylon(2)));
                        return true;
                    } else if (marker.getSnippet().equals("T1") && otherItems > 0) {
                        tier1List.remove(marker);
                        marker.remove();
                        t1Count += 1;
                        textT1Count.setText(String.format("%d", t1Count));
                        SharedPreferences.Editor editor = _Pref.edit();
                        editor.putInt("tier1Count", t1Count);
                        editor.commit();
                        saveCollectibles();
                        return true;
                    } else if (marker.getSnippet().equals("T2") && walkItems > 0) {
                        tier2List.remove(marker);
                        marker.remove();
                        walkItems -= 1;
                        t2Count += 1;
                        textWalk.setText(String.format("%d", walkItems));
                        textT2Count.setText(String.format("%d", t2Count));
                        SharedPreferences.Editor editor = _Pref.edit();
                        editor.putInt("walkItems", walkItems);
                        editor.putInt("tier2Count", t2Count);
                        editor.commit();
                        saveCollectibles();
                        return true;
                    } else if (marker.getSnippet().equals("T3") && runItems > 0) {
                        tier3List.remove(marker);
                        marker.remove();
                        runItems -= 1;
                        t3Count += 1;
                        textRun.setText(String.format("%d", runItems));
                        textT3Count.setText(String.format("%d", t3Count));
                        SharedPreferences.Editor editor = _Pref.edit();
                        editor.putInt("runItems", runItems);
                        editor.putInt("tier3Count", t3Count);
                        editor.commit();
                        saveCollectibles();
                        return true;
                    }
                }
                return false;
            }
        });

        btnDrop.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!_Pref.getBoolean("dropUsed", false)) {
                    Calendar c = Calendar.getInstance();
                    long currentTime = c.getTimeInMillis();
                    SharedPreferences.Editor editor = _Pref.edit();
                    editor.putLong("dropStart", currentTime);
                    editor.commit();
                    AddDropTime(900000);
                    mDrop = mMap.addMarker(new MarkerOptions()
                            .position(placeRandomMarker(500, 1, myCurrentPosition))
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.airport))
                            .title("Airdrop"));
                    mDrop.showInfoWindow();
                    btnDrop.setVisibility(View.INVISIBLE);
                    editor.putString("dropLocation", mDrop.getPosition().latitude + "," + mDrop.getPosition().longitude);
                    editor.putBoolean("dropUsed", true);
                    editor.commit();
                }
            }
        });
    }


    private void OnDropRemoved() {
        mDrop.remove();
        btnDrop.setVisibility(View.VISIBLE);
        SharedPreferences.Editor editor = _Pref.edit();
        editor.putBoolean("dropUsed", false);
        editor.commit();
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
        super.onPause();
        if (mLocationManager != null) {
            mLocationManager.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }


    @Override
    public void onResume() {
        super.onResume();
    }

    //#################Location Related Methods###########################

    private int AddPylon(int val) {
        SharedPreferences pref = getApplicationContext().getSharedPreferences(PrefsFile, MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();

        int pylonCount = pref.getInt("pylonCount", 3);
        int finalValue = pylonCount + val;
        editor.putInt("pylonCount", finalValue);
        editor.apply();

        return finalValue;
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
    public void onConnected(Bundle bundle) {

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        txtPylonCount.setText(String.format("%d", AddPylon(0)));
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(1000); // Update location every second
        mLocationRequest.setSmallestDisplacement(1);

        Intent activityIntent = new Intent(this, RecogniseActivity.class);
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, activityIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(mGoogleApiClient, 1000, pendingIntent);

        mMap.setMyLocationEnabled(true);


        mLocationManager.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        myCurrentPosition = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
        myLastPosition = myCurrentPosition;

        if (myLocationRadius ==null){
        myLocationRadius = mMap.addCircle(new CircleOptions()
                .center(myCurrentPosition)
                .radius(75)
                .strokeWidth(1.5f)
                .strokeColor(Color.argb(255, 0, 127, 255))
                .fillColor(Color.argb(100, 0, 240, 255)));}

        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setScrollGesturesEnabled(false);
        mMap.getUiSettings().setTiltGesturesEnabled(false);

        mMap.setLocationSource(this);

    }


    @Override
    public void onConnectionSuspended(int i) {
        if (mLocationManager != null) {
            mLocationManager.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
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

            if (myCurrentPosition != null)
                myLastPosition = myCurrentPosition;
            myCurrentPosition = new LatLng(location.getLatitude(), location.getLongitude());
            LatLngBounds bounds = new LatLngBounds(myCurrentPosition, myCurrentPosition);
            mMap.setLatLngBoundsForCameraTarget(bounds);
            myLocationRadius.setCenter(myCurrentPosition);

            if (!CameraSet) {
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(myCurrentPosition)
                        .zoom(15)
                        .bearing(0)
                        .tilt(30)
                        .build();
                mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                CameraSet = true;
            } else {
                mMap.animateCamera(CameraUpdateFactory.newLatLng(myCurrentPosition));
            }

            if (myLastPosition != null) {
                float distance = FindDistance(myLastPosition, myCurrentPosition);

                String val = _Pref.getString("currentActivity", "N/A");
                if (val == "Walking") {
                    walkDistance += distance;
                    combinedDistance += distance;
                    progressWalk.setProgress((int) walkDistance);
                    progressBoth.setProgress((int) combinedDistance);
                } else if (val == "Running") {
                    runDistance += distance;
                    combinedDistance += distance;
                    progressRun.setProgress((int) runDistance);
                    progressBoth.setProgress((int) combinedDistance);
                }

                if ((int) walkDistance >= 500) {
                    walkDistance -= 500;
                    walkItems += 2;
                    textWalk.setText(Integer.toString(walkItems));
                    SharedPreferences.Editor editor = _Pref.edit();
                    editor.putInt("walkItems", walkItems);
                    editor.commit();
                }
                if ((int) runDistance >= 500) {
                    runDistance -= 500;
                    runItems += 2;
                    textRun.setText(Integer.toString(runItems));
                    SharedPreferences.Editor editor = _Pref.edit();
                    editor.putInt("runItems", runItems);
                    editor.commit();
                }
                if ((int) combinedDistance >= 2500) {
                    combinedDistance -= 2500;
                    walkItems += 5;
                    runItems += 3;
                    textRun.setText(Integer.toString(runItems));
                    textWalk.setText(Integer.toString(walkItems));
                    SharedPreferences.Editor editor = _Pref.edit();
                    editor.putInt("runItems", runItems);
                    editor.putInt("walkItems", walkItems);
                    editor.commit();
                }

                SharedPreferences.Editor editor = _Pref.edit();
                editor.putFloat("walkDistance", walkDistance);
                editor.putFloat("runDistance", runDistance);
                editor.putFloat("combinedDistance", combinedDistance);
                editor.commit();
            }
        }
    }
}