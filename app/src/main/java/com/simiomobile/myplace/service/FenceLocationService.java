package com.simiomobile.myplace.service;

import android.Manifest;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.awareness.fence.AwarenessFence;
import com.google.android.gms.awareness.fence.FenceState;
import com.google.android.gms.awareness.fence.FenceUpdateRequest;
import com.google.android.gms.awareness.fence.LocationFence;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.ResultCallbacks;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.simiomobile.myplace.controller.BusController;
import com.simiomobile.myplace.controller.RealmController;
import com.simiomobile.myplace.controller.NotificationController;
import com.simiomobile.myplace.model.FavoriteEventBus;
import com.simiomobile.myplace.realm.Place;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Aor__Feyverly on 23/5/2560.
 */

public class FenceLocationService extends Service implements
        LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener  {

    public static final String FENCE_RECEIVER_ACTION ="com.simiomobile.myplace.LocationFenceReceiver.FENCE_RECEIVER_ACTION";


    private static final String TAG = "FenceBroadcastReceiver";
    private static final String IN_LOCATION_FENCE_KEY = "IN_LOCATION_FENCE_KEY";

    public static final int STATUS_IN = 0;
    public static final int STATUS_OUT = 1;

    private GoogleApiClient mGoogleApiClient;
    private PendingIntent mPendingIntent;
    private Context mContext;

    private static int mRadiusNearby = 100;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                .addApi(Awareness.API)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();
        BusController.getInstance().register(this);

        IntentFilter mNetworkStateFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mNetworkStateReceiver, mNetworkStateFilter);
    }

    @Override
    public void onDestroy() {
        BusController.getInstance().unregister(this);
        if (mGoogleApiClient != null) mGoogleApiClient.disconnect();
        unregisterFences();
        unregisterReceiver(mLocationFenceReceiver);
        unregisterReceiver(mNetworkStateReceiver);
        super.onDestroy();
    }

    private List<AwarenessFence> creates(List<Place> places) {
        List<AwarenessFence> awarenessFences = new ArrayList<>();
        for (Place place : places) {
            awarenessFences.add(create(place.getLatitude(), place.getLongitude()));
        }
        return awarenessFences;
    }

    private AwarenessFence create(double lat, double lon) {
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }
        return LocationFence.in(lat, lon,mRadiusNearby, 0L);
    }

    private void registerFences() {
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            return;
        } else {
            List<AwarenessFence> fences = creates(RealmController.with(getApplication()).getFavoritePlaces());
            if (fences.size() == 0) return;
            AwarenessFence awarenessFence = AwarenessFence.or(fences);
            Awareness.FenceApi.updateFences(
                    mGoogleApiClient,
                    new FenceUpdateRequest.Builder()
                            .addFence(IN_LOCATION_FENCE_KEY, awarenessFence, mPendingIntent)
                            .build())
                    .setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(@NonNull Status status) {
                            if (status.isSuccess()) {

                            } else {

                            }
                        }
                    });
        }
    }

    private void unregisterFences() {
        Awareness.FenceApi.updateFences(
                mGoogleApiClient,
                new FenceUpdateRequest.Builder()
                        .removeFence(IN_LOCATION_FENCE_KEY)
                        .build()).setResultCallback(new ResultCallbacks<Status>() {
            @Override
            public void onSuccess(@NonNull Status status) {

            }

            @Override
            public void onFailure(@NonNull Status status) {

            }
        });
    }


    private final BroadcastReceiver mLocationFenceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            FenceState fenceState = FenceState.extract(intent);

            if (TextUtils.equals(fenceState.getFenceKey(), IN_LOCATION_FENCE_KEY)) {
                switch (fenceState.getCurrentState()) {
                    case FenceState.TRUE:
                        setHeadphoneState(STATUS_IN);
                        break;
                    case FenceState.FALSE:
                        setHeadphoneState(STATUS_OUT);
                        break;
                    case FenceState.UNKNOWN:
                        Log.i("Fences", "Oops, your headphone status is unknown!");
                        break;
                }
            }
        }

    };

    private final BroadcastReceiver mNetworkStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager connectivityManager = (ConnectivityManager)
                    context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();

            boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

            if (isConnected) {
                if (!mGoogleApiClient.isConnected()) {
                    mGoogleApiClient.reconnect();
                }

            }
        }
    };



    /**
     * receiver of list place  changed
     **/
    @Subscribe
    public void onListPlaceChange(FavoriteEventBus result) {
        Log.i("Fences", "onListPlaceChange FavoriteEventBus");
        if (result != null && result.isRefresh()) {
            registerFences();
        }
    }

    private void setHeadphoneState(int status) {
        switch (status) {
            case STATUS_IN:
                NotificationController.getInstance().showNotification();
                break;
            case STATUS_OUT:

                break;
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Intent intent = new Intent(FENCE_RECEIVER_ACTION);
        mPendingIntent = PendingIntent.getBroadcast(mContext, 1, intent, 0);
        registerFences();
        registerReceiver(mLocationFenceReceiver, new IntentFilter(FENCE_RECEIVER_ACTION));
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {

    }
}