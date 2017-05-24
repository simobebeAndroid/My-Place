package com.simiomobile.myplace.network.api;

import com.simiomobile.myplace.model.PlaceSearchModel;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by Aor__Feyverly on 23/5/2560.
 */

public interface GooglePlaceAPI {

    @GET("api/place/nearbysearch/json?sensor=true&key=AIzaSyB2bhYUAND34BGKfE7OOTYLJkwIyAKZqDs")
    Call<PlaceSearchModel> getPlaceNearbyLocationAPI(@Query("location") String location, @Query("radius") int radius);

}
