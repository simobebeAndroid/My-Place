package com.simiomobile.myplace.manager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.simiomobile.myplace.model.PlaceSearchModel;
import com.simiomobile.myplace.network.api.GooglePlaceAPI;
import com.simiomobile.myplace.network.callback.OnNetworkCallbackListener;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Aor__Feyverly on 23/5/2560.
 */

public class NetworkConnectionManager {

    private OnGetPlaceNearByLocationListener onGetPlaceNearByLocationListener;
    private Gson gson;
    protected NetworkConnectionManager() {
        gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
                .create();
    }

    protected void getPlaceNearbyLocation(final OnGetPlaceNearByLocationListener listener, double latitude, double longitude, int radius)
    {
        onGetPlaceNearByLocationListener = listener;
        String location = String.valueOf(latitude)+","+String.valueOf(longitude);
        String url = "https://maps.googleapis.com/maps/";
        final Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        GooglePlaceAPI googlePlaceAPI = retrofit.create(GooglePlaceAPI.class);
        Call<PlaceSearchModel> call = googlePlaceAPI.getPlaceNearbyLocationAPI(location,radius);
        call.enqueue(new Callback<PlaceSearchModel>() {
            @Override
            public void onResponse(Call<PlaceSearchModel> call, Response<PlaceSearchModel> response) {
                try {
                    PlaceSearchModel placeSearchModel = response.body();
                    if (placeSearchModel == null) {
                        //404 or the response cannot be converted to User.
                        ResponseBody responseBody = response.errorBody();
                        if (responseBody != null) {
                            onGetPlaceNearByLocationListener.onBodyError(responseBody);
                        } else {
                            onGetPlaceNearByLocationListener.onBodyErrorIsNull();
                        }
                    } else {
                        onGetPlaceNearByLocationListener.onSuccessWithResult(placeSearchModel,retrofit);
                        onGetPlaceNearByLocationListener.onSuccess(retrofit);
                    }

                } catch (Exception e) {

                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<PlaceSearchModel> call, Throwable t) {
                onGetPlaceNearByLocationListener.onFailure(t);
            }
        });
    }

    /** interface listener service **/
    public interface OnGetPlaceNearByLocationListener extends OnNetworkCallbackListener {
        void onSuccessWithResult(PlaceSearchModel placeSearchModel, Retrofit retrofit );
    }
}
