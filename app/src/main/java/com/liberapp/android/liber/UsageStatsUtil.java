package com.liberapp.android.liber;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Ricardo Barbosa on 09/04/2017.
 * ///
 */
@SuppressWarnings("WrongConstant")
public class UsageStatsUtil {
    private UsageStatsManager uStatsManager;
    private Context c;
    public static final String LIBER_PREFS = "LiberPreferences";
    public static final String SETTING_PREFS = "SettingsPreferences";

    public UsageStatsUtil(Context c) {
        this.c = c;
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP)
            uStatsManager = (UsageStatsManager) c.getSystemService("usagestats");
        else
            uStatsManager = (UsageStatsManager) c.getSystemService(Context.USAGE_STATS_SERVICE);
    }

    public List<UsageStats> filterApps(List<UsageStats> usageStatsList) {
        return filterByAppName(filterByTime(usageStatsList));
    }

    private boolean removeNoneApp(UsageStats usageStat) {
        boolean check = true;

        try {
            c.getPackageManager().getApplicationIcon(usageStat.getPackageName());
        } catch (PackageManager.NameNotFoundException e) {
            check = false;
        }
        return check;
    }

    private List<UsageStats> filterByTime(List<UsageStats> usageStatsList) {
        List<UsageStats> appsFiltered = new ArrayList<>();

        for (UsageStats us : usageStatsList) {
            if (us.getTotalTimeInForeground() >= TimeUnit.MINUTES.toMillis(2) && removeNoneApp(us)) {
                appsFiltered.add(us);
            }
        }
        return appsFiltered;
    }

    private List<UsageStats> filterByAppName(List<UsageStats> usageStatsList) {
        List<UsageStats> appsFiltered = new ArrayList<>();
        for (UsageStats uts : usageStatsList) {
            for (Apps app : Apps.values()) {
                if (uts.getPackageName().equals(app.getName())) {
                    appsFiltered.add(uts);
                }
            }
        }
        return appsFiltered;
    }

    public List<UsageStats> getUsageStatsList(int calendarInterval, int timeAmount, int usageStatsInterval) {
        Calendar beginTime = Calendar.getInstance();

        if (calendarInterval == Calendar.HOUR_OF_DAY) {
            beginTime.setTimeInMillis(System.currentTimeMillis());
            int check = beginTime.get(Calendar.HOUR_OF_DAY);
            if (check >= 0 && check <= 21) beginTime.add(Calendar.DAY_OF_MONTH, -1);
            beginTime.set(Calendar.HOUR_OF_DAY, 22);
            beginTime.set(Calendar.MINUTE, 10);
            beginTime.set(Calendar.SECOND, 0);

        } else {
            beginTime.add(calendarInterval, timeAmount);
        }

        Calendar endTime = Calendar.getInstance();
        endTime.setTimeInMillis(System.currentTimeMillis());


        Calendar correctlyTime = Calendar.getInstance();
        correctlyTime.setTimeInMillis(System.currentTimeMillis());


        if (usageStatsInterval == UsageStatsManager.INTERVAL_WEEKLY) {
            beginTime.add(Calendar.MINUTE, -correctlyTime.get(Calendar.MINUTE));
            beginTime.add(Calendar.SECOND, -correctlyTime.get(Calendar.SECOND));
            beginTime.add(Calendar.HOUR, 3);
            endTime.add(Calendar.HOUR, 3);
            endTime.add(Calendar.DAY_OF_MONTH, -1);
        }

        return uStatsManager.queryUsageStats(usageStatsInterval, beginTime.getTimeInMillis(), endTime.getTimeInMillis());
    }


    public List<AppDetail> getAppFiltered(int calendarInterval, int timeAmount, int usageStatsInterval) {
        List<UsageStats> queryUsageStats = getUsageStatsList(calendarInterval, timeAmount, usageStatsInterval);

        queryUsageStats = filterApps(queryUsageStats);
        List<AppDetail> appDetailListUseful = usageStatsToAppDetail(queryUsageStats);
        appDetailListUseful = sumTotalTimeInUnique(appDetailListUseful);

        SharedPreferences prefs = c.getSharedPreferences(UsageStatsUtil.LIBER_PREFS, MODE_PRIVATE);

        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);

        if (hour >= 22 && hour <= 23) {
            for (AppDetail app : appDetailListUseful) {
                Long timeUsed = app.getTotalTime() + prefs.getLong(app.getUsageStats().getPackageName(), 0);
                app.setTotalTime(timeUsed);
            }
        } else {
            for (AppDetail app : appDetailListUseful) {
                Long timeUsed = app.getTotalTime() - prefs.getLong(app.getUsageStats().getPackageName(), 0);
                app.setTotalTime(timeUsed);
            }
        }

        for (Iterator<AppDetail> app = appDetailListUseful.iterator(); app.hasNext(); ) {
            if (app.next().getTotalTime() <= 0)
                app.remove();
        }
        Collections.sort(appDetailListUseful, new UsageStatsUtil.TotalTimeUsedDesc());

        return appDetailListUseful;
    }

    public String convertPackageNameToName(String packageName) {

        final PackageManager packageManager = c.getPackageManager();
        String appName = null;
        try {
            appName = (String) packageManager.getApplicationLabel(packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return appName;
    }

    public List<AppDetail> usageStatsToAppDetail(List<UsageStats> usageStatses) {
        List<AppDetail> appDetailList = new ArrayList<>();

        for (UsageStats uts : usageStatses) {
            AppDetail app = new AppDetail();
            app.setUsageStats(uts);
            app.setTotalTime(uts.getTotalTimeInForeground());
            appDetailList.add(app);
        }
        return appDetailList;
    }

    private List<AppDetail> sumTotalTimeInUnique(List<AppDetail> appDetailses) {
        Collections.sort(appDetailses, new UsageStatsUtil.removeEquals());

        Map<String, AppDetail> map = new HashMap<>();

        for (AppDetail app : appDetailses) {
            String key = app.getUsageStats().getPackageName();
            if (!map.containsKey(key)) {
                map.put(key, app);
            } else {
                AppDetail ap = map.get(key);
                Long quant = ap.getTotalTime() + app.getTotalTime();
                ap.setTotalTime(quant);
            }
        }

        List<AppDetail> newList = new ArrayList<>();

        for (String key : map.keySet()) {
            newList.add(map.get(key));
        }

        return newList;
    }

    private enum Apps {
        INSTAGRAM("com.instagram.android"),
        WHATSAPP("com.whatsapp"),
        MESSENGER("com.facebook.orca"),
        FACEBOOK("com.facebook.katana"),
        LITEFACEBOOK("com.facebook.lite"),
        TELEGRAM("org.telegram.messenger"),
        TWITTER("com.twitter.android"),
        LINE("jp.naver.line.android"),
        VIBER("com.viber.voip"),
        SNAPCHAT("com.snapchat.android"),
        TINDER("com.tinder"),
        SKOOB("com.gaudium.skoob");
        private final String packageName;

        Apps(String name) {
            packageName = name;
        }

        public String getName() {
            return packageName;
        }

    }

    private static class TotalTimeUsedDesc implements Comparator<AppDetail> {
        @Override
        public int compare(AppDetail left, AppDetail right) {
            return Long.compare(right.getTotalTime(), left.getTotalTime());
        }
    }

    private static class removeEquals implements Comparator<AppDetail> {
        @Override
        public int compare(AppDetail right, AppDetail left) {
            return right.getUsageStats().getPackageName().compareToIgnoreCase(left.getUsageStats().getPackageName());
        }
    }

    public static String getFormatHMS(Long totalTimeUsed) {
        return String.format(Locale.getDefault(), "%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(totalTimeUsed),
                TimeUnit.MILLISECONDS.toMinutes(totalTimeUsed) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(totalTimeUsed)),
                TimeUnit.MILLISECONDS.toSeconds(totalTimeUsed) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(totalTimeUsed)));
    }
}
