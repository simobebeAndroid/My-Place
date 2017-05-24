package com.simiomobile.myplace.network.callback;

import okhttp3.ResponseBody;
import retrofit2.Retrofit;


public interface OnNetworkCallbackListener {
    public void onSuccess(Retrofit retrofit);
    public void onBodyError(ResponseBody responseBodyError);
    public void onBodyErrorIsNull();
    public void onFailure(Throwable t);
}
