package com.liberapp.android.liber;

import android.app.IntentService;
import android.app.usage.UsageStatsManager;
import android.content.Intent;
import android.content.SharedPreferences;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class LiberService extends IntentService {


    public LiberService() {
        super("LiberService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            List<AppDetail> apps = new UsageStatsUtil(this).
                    getAppFiltered(Calendar.HOUR_OF_DAY, 0, UsageStatsManager.INTERVAL_DAILY);

            int notification = intent.getIntExtra("notification", 1);
            if (notification == 0) {
                SharedPreferences prefs = getSharedPreferences(UsageStatsUtil.SETTING_PREFS, MODE_PRIVATE);
                Long totaltime = 0L;
                for (AppDetail app : apps) {
                    totaltime += app.getTotalTime();
                }

                long timeUsed = TimeUnit.MILLISECONDS.toHours(totaltime);

                if (timeUsed == 1 && prefs.getBoolean("Notification1Gone", true)) {
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean("Notification1Gone", false);
                    editor.putBoolean("Notification2Gone", true);
                    editor.apply();

                    Long total = totaltime;
                    totaltime = TimeUnit.MILLISECONDS.toMinutes(totaltime);
                    Long totalAllDay = 120L;
                    int percent = (int) ((totaltime * 100f) / totalAllDay);
                    LiberNotification.notify(this, "Otimização de tempo", percent,
                            UsageStatsUtil.getFormatHMS(total));
                } else if (timeUsed == 2 && prefs.getBoolean("Notification2Gone", true)) {
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean("Notification1Gone", true);
                    editor.putBoolean("Notification2Gone", false);
                    editor.apply();

                    Long total = totaltime;
                    totaltime = TimeUnit.MILLISECONDS.toMinutes(totaltime);
                    Long totalAllDay = 120L;
                    int percent = (int) ((totaltime * 100f) / totalAllDay);
                    LiberNotification.notify(this, "Otimização de tempo", percent,
                            UsageStatsUtil.getFormatHMS(total));
                }
            } else {
                SharedPreferences prefs = getSharedPreferences(UsageStatsUtil.LIBER_PREFS, MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();

                Calendar c = Calendar.getInstance();
                long timeNow = c.get(Calendar.HOUR_OF_DAY);

                if (timeNow > 22 || timeNow < 1) {
                    for (AppDetail app : apps) {
                        editor.putLong(app.getUsageStats().getPackageName(), app.getTotalTime());
                    }
                } else if (timeNow > 20 || timeNow < 23) {
                    for (AppDetail app : apps) {
                        Long timeUsed = app.getTotalTime() - prefs.getLong(app.getUsageStats().getPackageName(), 0);
                        app.setTotalTime(timeUsed);
                        editor.putLong(app.getUsageStats().getPackageName(), app.getTotalTime());
                    }
                }
                editor.apply();
            }
        }
    }
}
