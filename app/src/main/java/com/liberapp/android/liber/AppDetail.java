package com.liberapp.android.liber;

import android.app.usage.UsageStats;
import android.graphics.drawable.Drawable;

/**
 * Created by Ricardo Barbosa on 01/04/2017.
 *
 */

public class AppDetail {

    private UsageStats usageStats;
    private Drawable appIco;
    private Long totalTime;

    public UsageStats getUsageStats() {
        return usageStats;
    }

    public Drawable getAppIco() {
        return appIco;
    }

    public void setUsageStats(UsageStats usageStats) {
        this.usageStats = usageStats;
    }

    public void setAppIco(Drawable appIco) {
        this.appIco = appIco;
    }

    public Long getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(Long totalTime) {
        this.totalTime = totalTime;
    }
}
