package com.simiomobile.myplace.controller;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

import com.pixplicity.easyprefs.library.Prefs;
import com.simiomobile.myplace.R;
import com.simiomobile.myplace.app.MyPlaceApplication;
import com.simiomobile.myplace.ui.activities.main.MainActivity;

/**
 * Created by Aor__Feyverly on 24/5/2560.
 */

public class NotificationController {
    private static NotificationController ourInstance = null;
    private Context mContext;
    public static NotificationController getInstance() {
        if (ourInstance == null) {
            ourInstance = new NotificationController();
            ourInstance.mContext = MyPlaceApplication.getInstance().getApplicationContext();
        }
        return ourInstance;
    }

    public void showNotification() {
        int notificationID = Prefs.getInt("NOTIFICATION",0);
        NotificationCompat.Builder mNotifyBuilder;
        android.app.NotificationManager mNotificationManager;
        mNotificationManager = (android.app.NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotifyBuilder = createNotificationBuilderBody();
        mNotifyBuilder.setContentText("Your favorite place is in nearby");
        Intent intent = new Intent(mContext, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        mNotifyBuilder.setContentIntent(pendingIntent);
        // Set Vibrate, Sound and Light
        int defaults = 0;
        defaults = defaults | Notification.DEFAULT_LIGHTS;
        defaults = defaults | Notification.DEFAULT_VIBRATE;
        defaults = defaults | Notification.DEFAULT_SOUND;
        mNotifyBuilder.setDefaults(defaults);
        // Set the content for Notification
        mNotifyBuilder.setAutoCancel(true);
        // Post a notification
        mNotificationManager.notify(notificationID, mNotifyBuilder.build());
        Prefs.putInt("NOTIFICATION",notificationID+1);
        //show launcher icon notification count
    }
    private NotificationCompat.Builder createNotificationBuilderBody() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return new NotificationCompat.Builder(mContext)
                    .setContentTitle(mContext.getResources().getString(R.string.app_name))
                    .setSmallIcon(R.mipmap.ic_launcher)
                    //     .setLargeIcon(bitmap)
                    .setColor(ContextCompat.getColor(mContext, R.color.transparent));
        } else {
            return new NotificationCompat.Builder(mContext)
                    .setContentTitle(mContext.getResources().getString(R.string.app_name))
                    .setSmallIcon(R.mipmap.ic_launcher);
        }
    }
}
