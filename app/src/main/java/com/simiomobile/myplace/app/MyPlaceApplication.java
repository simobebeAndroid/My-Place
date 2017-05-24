package com.simiomobile.myplace.app;

import android.app.Application;
import android.content.ContextWrapper;

import com.pixplicity.easyprefs.library.Prefs;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by Aor__Feyverly on 21/5/2560.
 */

public class MyPlaceApplication extends Application {
    private static MyPlaceApplication instance;

    public static MyPlaceApplication getInstance() {
        return instance;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        initialApplication();
        initialRealm();
        initialPref();
    }



    private void initialApplication() {
        instance = this;
    }
    private void initialRealm() {
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder(this)
                .name(Realm.DEFAULT_REALM_NAME)
                .schemaVersion(0)
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.setDefaultConfiguration(realmConfiguration);
    }
    private void initialPref() {
        new Prefs.Builder()
                .setContext(this)
                .setMode(ContextWrapper.MODE_PRIVATE)
                .setPrefsName(getPackageName())
                .setUseDefaultSharedPreference(true)
                .build();
    }
}
