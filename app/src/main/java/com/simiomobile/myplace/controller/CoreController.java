package com.simiomobile.myplace.controller;

import android.content.Context;
import android.util.Log;

import com.simiomobile.myplace.app.MyPlaceApplication;
import com.simiomobile.myplace.manager.NetworkConnectionManager;
import com.simiomobile.myplace.model.PlaceSearchModel;
import com.simiomobile.myplace.realm.Place;

import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Retrofit;

/**
 * Created by Aor__Feyverly on 23/5/2560.
 */

public class CoreController extends NetworkConnectionManager {

    private static CoreController ourInstance = null;
    private Context mContext;

    private static String TAG = "CoreController";

    public CoreController() {

    }

    public static CoreController getInstance() {
        if (ourInstance == null) {
            ourInstance = new CoreController();
            ourInstance.mContext = MyPlaceApplication.getInstance().getApplicationContext();
        }
        return ourInstance;
    }


    public void callGetPlaceNearbyLocation(double latitude, double longitude, int radius, final OnPlaceNearByLocationListener listener) {

        getPlaceNearbyLocation(new OnGetPlaceNearByLocationListener() {
            @Override
            public void onSuccessWithResult(PlaceSearchModel placeSearchModel, Retrofit retrofit) {

                RealmController.with(MyPlaceApplication.getInstance()).clear();
                List<Place> places = new ArrayList<Place>();
                for (PlaceSearchModel.ResultsBean resultsBean : placeSearchModel.getResults()) {
                    Place place = new Place();
                    place.setId(resultsBean.getPlace_id());
                    place.setAddress(resultsBean.getVicinity());
                    place.setName(resultsBean.getName());
                    place.setUrlLink("");
                    //  if (p.getPlace().getWebsiteUri() != null) place.setUrlLink(p.getPlace().getWebsiteUri().toString());
                    place.setMarkerLink(resultsBean.getIcon());
                    place.setLatitude(resultsBean.getGeometry().getLocation().getLat());
                    place.setLongitude(resultsBean.getGeometry().getLocation().getLng());
                    Place placeDB = RealmController.getInstance().getPlace(place.getId());
                    if (placeDB != null) place.setFavorite(placeDB.isFavorite());
                    // Persist your data easily
                    places.add(place);
                }
                listener.onSuccessWithResult(places);
            }

            @Override
            public void onSuccess(Retrofit retrofit) {
                Log.i(TAG, "onSuccess" + retrofit.baseUrl());
            }

            @Override
            public void onBodyError(ResponseBody responseBodyError) {
                Log.e(TAG, "onBodyError" + responseBodyError.toString());
                listener.onFailure(responseBodyError.toString());
            }

            @Override
            public void onBodyErrorIsNull() {
                Log.e(TAG, "onBodyErrorIsNull");
                listener.onFailure("onBodyErrorIsNull");
            }

            @Override
            public void onFailure(Throwable t) {
                Log.e(TAG, "onFailure" + t.getMessage());
                listener.onFailure(t.getMessage());
            }
        }, latitude, longitude, radius);
    }

    /**
     * interface listener callback
     **/
    public interface OnPlaceNearByLocationListener {
        void onSuccessWithResult(List<Place> places);

        void onFailure(String error);
    }
}
