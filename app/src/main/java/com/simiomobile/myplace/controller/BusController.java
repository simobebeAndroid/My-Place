package com.simiomobile.myplace.controller;

import android.os.Handler;
import android.os.Looper;

import com.squareup.otto.Bus;

/**
 * Created by Aor__Feyverly on 22/5/2560.
 */

public final class BusController extends Bus {

    private static BusController instance;
    private final long TIME = 1000L;

    public static BusController getInstance() {
        if (instance == null)
            instance = new BusController();
        return instance;
    }

    private Handler mainThread = new Handler(Looper.getMainLooper());

    public void postOnMain(final Object event) {
        mainThread.postDelayed(new Runnable() {
            @Override
            public void run() {
                BusController.getInstance().post(event);
            }
        },TIME);
    }
}
