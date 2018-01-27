package com.liberapp.android.liber;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;

public class LiberNotification {

    private static final String NOTIFICATION_TAG = "Liber";

    public static void notify(final Context context, final String exampleString, int percent, String totalTime) {

        final Resources res = context.getResources();
        final Bitmap picture = BitmapFactory.decodeResource(res, R.drawable.ic_notification_liber);
        final String title = res.getString(
                R.string.liber_notification_title_template, exampleString);
        final String text = res.getString(
                R.string.liber_notification_message, percent, totalTime);

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setDefaults(Notification.DEFAULT_ALL)
                .setSmallIcon(R.drawable.ic_stat_liber)
                .setContentTitle(title)
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setLargeIcon(picture)
                .setTicker(exampleString)
                .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                .setContentIntent(
                        PendingIntent.getActivity(
                                context, 0,
                                new Intent(context.getApplicationContext(), LiberActivity.class),
                                PendingIntent.FLAG_UPDATE_CURRENT))
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(text)
                        .setBigContentTitle(title)
                        .setSummaryText(context.getString(R.string.use_notification)))
                .setAutoCancel(true);

        notify(context, builder.build());
    }

    private static void notify(final Context context, final Notification notification) {
        final NotificationManager nm = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);

        nm.notify(NOTIFICATION_TAG.hashCode(), notification);

    }
}
