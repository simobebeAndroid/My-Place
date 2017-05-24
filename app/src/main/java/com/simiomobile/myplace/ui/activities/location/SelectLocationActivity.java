package com.simiomobile.myplace.ui.activities.location;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.simiomobile.myplace.R;
import com.simiomobile.myplace.databinding.ActivitySelectLocationBinding;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class SelectLocationActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnCameraMoveListener,GoogleApiClient.OnConnectionFailedListener {private GoogleMap mMap;

    private ActivitySelectLocationBinding binding;

    private GoogleApiClient mGoogleApiClient;
    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final int GOOGLE_API_CLIENT_ID = 0;

    private double mLatitude, mLongitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Select Location");
        binding = DataBindingUtil.setContentView(this, R.layout.activity_select_location);

        mLatitude = getIntent().getExtras().getDouble("LATITUDE");
        mLongitude = getIntent().getExtras().getDouble("LONGITUDE");
        initialUI();
        initialListener();
    }

    private void initialListener() {
        binding.appLlSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent returnIntent = new Intent();
                Bundle bundle = new Bundle();
                bundle.putDouble("LATITUDE",mLatitude);
                bundle.putDouble("LONGITUDE",mLongitude);
                returnIntent.putExtra("result",bundle);
                setResult(RESULT_OK,returnIntent);
                finish();
            }
        });
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // this takes the user 'back', as if they pressed the left-facing triangle icon on the main android toolbar.
                // if this doesn't work as desired, another possibility is to call `finish()` here.
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    @Override
    protected void onStart() {
        super.onStart();
        if( mGoogleApiClient != null )
            mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        if( mGoogleApiClient != null && mGoogleApiClient.isConnected() ) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void initialUI() {
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.app_fm_map);
        mapFragment.getMapAsync(this);

        mGoogleApiClient = new GoogleApiClient.Builder(SelectLocationActivity.this)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, GOOGLE_API_CLIENT_ID, this)
                .build();
        binding.appTvName.setVisibility(View.GONE);
        binding.appTvAddress.setVisibility(View.GONE);
        binding.appPbLocation.setVisibility(View.VISIBLE);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        mMap = googleMap;
        mMap.setOnCameraMoveListener(this);
        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                Log.d("TAG", mMap.getCameraPosition().target.toString());
            }
        });
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.setMyLocationEnabled(true);
        mMap.setMapType(com.google.android.gms.maps.GoogleMap.MAP_TYPE_TERRAIN);
        LatLng current = new LatLng(mLatitude, mLongitude);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(current, 17.0f));
    }


    @Override
    public void onCameraMove() {
        binding.appTvName.setVisibility(View.GONE);
        binding.appTvAddress.setVisibility(View.GONE);
        binding.appPbLocation.setVisibility(View.VISIBLE);

        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                Log.d("TAG", mMap.getCameraPosition().target.toString());
                try {

                    binding.appTvName.setVisibility(View.VISIBLE);
                    binding.appTvAddress.setVisibility(View.VISIBLE);
                    binding.appPbLocation.setVisibility(View.GONE);

                    getAddress(mMap.getCameraPosition().target.latitude,mMap.getCameraPosition().target.longitude);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });


    }

    private void getAddress(double latitude,double longitude) throws IOException {
        mLatitude = latitude;
        mLongitude = longitude;
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());

        addresses = geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
        if (addresses.size() == 0)return;
        String result = "" ;//= addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
        if (addresses.size() > 0) {
            Address address = addresses.get(0);
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                sb.append(address.getAddressLine(i)).append("\n");
            }
            sb.append(address.getLocality()).append("\n");
            sb.append(address.getPostalCode()).append("\n");
            sb.append(address.getCountryName());
            result = sb.toString();
        }

        String city = addresses.get(0).getLocality();
        String state = addresses.get(0).getAdminArea();
        String country = addresses.get(0).getCountryName();
        String postalCode = addresses.get(0).getPostalCode();
        String knownName = addresses.get(0).getFeatureName();

        binding.appTvName.setText("Select Location");
        binding.appTvAddress.setText(result);

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e("TAG", "Google Places API connection failed with error code: "
                + connectionResult.getErrorCode());
        Toast.makeText(this,
                "Google Places API connection failed with error code:" +
                        connectionResult.getErrorCode(),
                Toast.LENGTH_LONG).show();
    }
}
