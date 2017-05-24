package com.simiomobile.myplace.controller;

import android.app.Activity;
import android.app.Application;
import android.support.v4.app.Fragment;

import com.simiomobile.myplace.realm.Place;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by Aor__Feyverly on 21/5/2560.
 */

public class RealmController {

    private static RealmController instance;
    private final Realm realm;

    public RealmController(Application application) {
        realm = Realm.getDefaultInstance();
    }

    public static RealmController with(Fragment fragment) {

        if (instance == null) {
            instance = new RealmController(fragment.getActivity().getApplication());
        }
        return instance;
    }

    public static RealmController with(Activity activity) {

        if (instance == null) {
            instance = new RealmController(activity.getApplication());
        }
        return instance;
    }

    public static RealmController with(Application application) {
        if (instance == null) {
            instance = new RealmController(application);
        }
        return instance;
    }

    public static RealmController getInstance() {
        return instance;
    }

    public Realm getRealm() {
        return realm;
    }

    public void refresh() {
        realm.refresh();
    }

    public synchronized void clearAll() {
        realm.beginTransaction();
        realm.clear(Place.class);
        realm.commitTransaction();
    }

    public synchronized void clear(){
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                RealmResults<Place> result = realm.where(Place.class).equalTo("isFavorite",false).findAll();
                result.clear();
            }
        });
    }

    public synchronized void savePlace(Place place){
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(place);
        realm.commitTransaction();
    }

    public synchronized RealmResults<Place> getPlaces() {

        return realm.where(Place.class).findAll();
    }

    public synchronized Place getPlace(String id) {
        return realm.where(Place.class).equalTo("id", id).findFirst();
    }

    public synchronized boolean hasPlaces() {

        return !realm.allObjects(Place.class).isEmpty();
    }

    public synchronized RealmResults<Place> getFavoritePlaces() {

        return realm.where(Place.class)
                .equalTo("isFavorite", true)
                .findAll();

    }
}
