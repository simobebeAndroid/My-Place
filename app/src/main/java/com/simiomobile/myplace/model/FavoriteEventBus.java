package com.simiomobile.myplace.model;

import com.simiomobile.myplace.realm.Place;

public class FavoriteEventBus {
    private boolean isRefresh;

    private Place place;

    public FavoriteEventBus(boolean isRefresh, Place place) {
        this.isRefresh = isRefresh;
        this.place = place;
    }

    public boolean isRefresh() {
        return isRefresh;
    }

    public void setRefresh(boolean refresh) {
        isRefresh = refresh;
    }

    public Place getPlace() {
        return place;
    }

    public void setPlace(Place place) {
        this.place = place;
    }
}

