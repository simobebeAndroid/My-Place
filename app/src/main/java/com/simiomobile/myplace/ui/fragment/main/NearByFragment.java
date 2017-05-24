package com.simiomobile.myplace.ui.fragment.main;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.awareness.snapshot.LocationResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.inthecheesefactory.thecheeselibrary.fragment.support.v4.app.StatedFragment;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.single.PermissionListener;
import com.simiomobile.myplace.R;
import com.simiomobile.myplace.adapter.PlacesAdapter;
import com.simiomobile.myplace.controller.BusController;
import com.simiomobile.myplace.controller.CoreController;
import com.simiomobile.myplace.controller.RealmController;
import com.simiomobile.myplace.model.FavoriteEventBus;
import com.simiomobile.myplace.realm.Place;
import com.simiomobile.myplace.service.FenceLocationService;
import com.simiomobile.myplace.ui.activities.location.SelectLocationActivity;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

public class NearByFragment extends StatedFragment implements PermissionListener {


    private PlacesAdapter mAdapter;
    private RecyclerView rvCard;
    private ImageView ivMap;
    private ProgressBar pbLoading;

    private List<Place> mPlaceList;

    private GoogleApiClient mGoogleApiClient;


    private double mLatitude, mLongitude;
    private static int REQUEST_CODE = 1001;

    public NearByFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment NearByFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static NearByFragment newInstance() {
        NearByFragment fragment = new NearByFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_near_by, container, false);
        // Inflate the layout for this fragment
        initInstant(view);
        initialData();
        initialListener();
        setCheckPermissions();
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        BusController.getInstance().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        BusController.getInstance().unregister(this);
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected())
            mGoogleApiClient.disconnect();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Add your code here
        if (requestCode == REQUEST_CODE && data != null) {
            Bundle bundle = data.getBundleExtra("result");
            mLatitude = bundle.getDouble("LATITUDE");
            mLongitude = bundle.getDouble("LONGITUDE");
            getPlace(mLatitude, mLongitude);
        }
    }

    private void initInstant(View view) {
        rvCard = (RecyclerView) view
                .findViewById(R.id.app_rv_place);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rvCard.setLayoutManager(layoutManager);
        ivMap = (ImageView) view.findViewById(R.id.app_iv_map);
        pbLoading = (ProgressBar) view.findViewById(R.id.app_pb_loading);
    }

    private void initialData() {
        mPlaceList = new ArrayList<>();
        mAdapter = new PlacesAdapter(getContext(), mPlaceList);
        rvCard.setAdapter(mAdapter);
    }

    private void initialListener() {
        mAdapter.setOnItemClickListener(new PlacesAdapter.OnItemClickListener() {
            @Override
            public void onPlaceClick(Place place) {

            }

            @Override
            public void onFavoriteClick(Place place, int position) {
                place.setFavorite(!place.isFavorite());
                mPlaceList.set(position, place);
                mAdapter.notifyDataSetChanged();
                RealmController.getInstance().savePlace(place);
                BusController.getInstance().postOnMain(new FavoriteEventBus(true, place));

            }
        });
        ivMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), SelectLocationActivity.class);
                Bundle bundle = new Bundle();
                bundle.putDouble("LATITUDE", mLatitude);
                bundle.putDouble("LONGITUDE", mLongitude);
                intent.putExtras(bundle);
                getActivity().startActivityForResult(intent, REQUEST_CODE);
            }
        });
    }

    public void setRealmAdapter(List<Place> places) {
        mPlaceList.clear();
        mPlaceList.addAll(places);
        mAdapter.notifyDataSetChanged();
    }

    private void setCheckPermissions() {
        Dexter.withActivity(getActivity())
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(this).withErrorListener(new PermissionRequestErrorListener() {
            @Override
            public void onError(DexterError error) {
                Log.e("Dexter", "There was an error: " + error.toString());
            }
        }).check();
    }

    private void initialService() {
        pbLoading.setVisibility(View.VISIBLE);
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addApi(Awareness.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(@Nullable Bundle bundle) {
                        // The broadcast receiver that will receive intents when a fence is triggered.
                        getLocation();
                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                        pbLoading.setVisibility(View.GONE);
                    }
                })
                .build();
        mGoogleApiClient.connect();
    }

    private void getLocation() {
        if (ContextCompat.checkSelfPermission(
                getActivity(),
                android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            Awareness.SnapshotApi.getLocation(mGoogleApiClient)
                    .setResultCallback(new ResultCallback<LocationResult>() {
                        @Override
                        public void onResult(@NonNull LocationResult locationResult) {
                            if (!locationResult.getStatus().isSuccess()) {
                                Log.e("Location", "Could not get location.");
                                return;
                            }
                            Location location = locationResult.getLocation();
                            mLatitude = location.getLatitude();
                            mLongitude = location.getLongitude();
                            getPlace(mLatitude, mLongitude);
                        }
                    });
        }

    }

    private void getPlace(double latitude, double longitude) {
        if (ContextCompat.checkSelfPermission(
                getActivity(),
                android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            pbLoading.setVisibility(View.VISIBLE);
            CoreController.getInstance().callGetPlaceNearbyLocation(latitude, longitude, 250, new CoreController.OnPlaceNearByLocationListener() {
                @Override
                public void onSuccessWithResult(List<Place> places) {
                    setRealmAdapter(places);
                    pbLoading.setVisibility(View.GONE);
                }

                @Override
                public void onFailure(String error) {

                    pbLoading.setVisibility(View.GONE);
                }
            });

        }

    }

    /**
     * receiver of list place  changed
     **/
    @Subscribe
    public void onListPlaceChange(FavoriteEventBus result) {
        if (result != null && result.isRefresh()) {
            mAdapter.updateItem(result.getPlace());
        }
    }

    @Override
    public void onPermissionGranted(PermissionGrantedResponse response) {
        initialService();
        Intent trackingService = new Intent(getContext(),
                FenceLocationService.class);
        getActivity().startService(trackingService);
    }

    @Override
    public void onPermissionDenied(PermissionDeniedResponse response) {
        pbLoading.setVisibility(View.GONE);
    }

    @Override
    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
        pbLoading.setVisibility(View.GONE);
    }
}
