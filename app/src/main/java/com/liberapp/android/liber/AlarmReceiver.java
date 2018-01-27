package com.liberapp.android.liber;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context, LiberService.class);
        i.putExtras(intent);
        context.startService(i);
    }
}
