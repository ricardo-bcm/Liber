package com.liberapp.android.liber;

import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.AppOpsManager;
import android.app.PendingIntent;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.github.lzyzsd.circleprogress.CircleProgress;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.nightonke.boommenu.BoomButtons.ButtonPlaceEnum;
import com.nightonke.boommenu.BoomButtons.HamButton;
import com.nightonke.boommenu.BoomButtons.OnBMClickListener;
import com.nightonke.boommenu.BoomMenuButton;
import com.nightonke.boommenu.ButtonEnum;
import com.nightonke.boommenu.Piece.PiecePlaceEnum;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class LiberActivity extends AppCompatActivity {
    public static final String ARG_USE_EXPANSION = "arg_use_expansion";
    public static final String ARG_EXPANSION_LEFT_OFFSET = "arg_left_offset";
    public static final String ARG_EXPANSION_TOP_OFFSET = "arg_top_offset";
    public static final String ARG_EXPANSION_VIEW_WIDTH = "arg_view_width";
    public static final String ARG_EXPANSION_VIEW_HEIGHT = "arg_view_height";
    private PieChart pieChart;
    private ArrayList<PieEntry> pieEntries;
    private PieDataSet pieDataSet;
    private PieData pieData;
    private UsageStatsUtil usageStatsUtil;
    private int usageStatsInterval;
    private int calendarInterval;
    private int timeAmount;
    private Long actualTimeInMillis;
    private Calendar calendar = Calendar.getInstance();
    private TextView phraseTxt;
    private TextView totalTimeTxt;
    private CircleProgress circleProgress;
    private int percent;
    private Button showListBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_liber);

        middleNightAlarm();
        beginNightAlarm();
        notificationAlarm();

        //ATRIBUINDO AOS ID's
        circleProgress = (CircleProgress) findViewById(R.id.circle_progress);
        totalTimeTxt = (TextView) findViewById(R.id.total_time_text);
        phraseTxt = (TextView) findViewById(R.id.phrase_textview);
        showListBtn = (Button) findViewById(R.id.show_list_apps);
        Button circlebutton = (Button) findViewById(R.id.button2);

        //SETTING INITIATE VALUES
        usageStatsInterval = UsageStatsManager.INTERVAL_DAILY;
        calendarInterval = Calendar.HOUR_OF_DAY;
        actualTimeInMillis = System.currentTimeMillis();
        calendar = Calendar.getInstance();
        calendar.setTimeInMillis(actualTimeInMillis);
        timeAmount = -calendar.get(Calendar.HOUR_OF_DAY);
        usageStatsUtil = new UsageStatsUtil(this);

        //CHART
        pieChart = (PieChart) findViewById(R.id.pie_chart);
        pieEntries = new ArrayList<>();
        addValuesToPieEntry();

        pieDataSet = new PieDataSet(pieEntries, "");
        pieDataSet.setColors(addColorsToPieChart());
        pieDataSet.setSliceSpace(5f);
        pieDataSet.setValueTextColor(R.color.colorPrimaryDark);
        pieDataSet.setValueTextSize(20);

        pieData = new PieData(pieDataSet);
        pieData.setValueFormatter(new PercentFormatter());

        pieChart.setData(pieData);
        pieChart.animateY(1500);
        pieChart.getLegend().setEnabled(false);
        pieChart.getDescription().setEnabled(false);
        pieChart.setEntryLabelTextSize(15);
        pieChart.setEntryLabelColor(R.color.colorPrimaryDark);
        pieChart.setTransparentCircleRadius(0f);
        pieChart.setHoleRadius(40f);
        pieChart.setUsePercentValues(true);

        pieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                if (e != null) {
                    long o = (long) e.getY();
                    String tempoDeUso = UsageStatsUtil.getFormatHMS(o);
                    Toast.makeText(LiberActivity.this, getString(R.string.piechart_toast_info) + tempoDeUso, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNothingSelected() {
            }
        });
        pieChart.invalidate();
        //END CHART
        ObjectAnimator animator = ObjectAnimator.ofInt(circleProgress, "progress", 0, percent);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.setDuration(1500);
        animator.start();
        //HAMBUTTON
        BoomMenuButton bmb = (BoomMenuButton) findViewById(R.id.bmb);
        assert bmb != null;
        bmb.setButtonEnum(ButtonEnum.Ham);
        bmb.setPiecePlaceEnum(PiecePlaceEnum.HAM_3);
        bmb.setButtonPlaceEnum(ButtonPlaceEnum.HAM_3);
        bmb.setShadowEffect(false);

        HamButton.Builder hamButton1 = new HamButton.Builder()
                .normalText(getString(R.string.btn_day))
                .textSize(20)
                .pieceColor(Color.WHITE)
                .normalColorRes(R.color.colorPrimary)
                .normalTextColor(Color.WHITE)
                .listener(new OnBMClickListener() {
                    @Override
                    public void onBoomButtonClick(int index) {
                        usageStatsInterval = UsageStatsManager.INTERVAL_DAILY;
                        calendarInterval = Calendar.HOUR_OF_DAY;
                        calendar.setTimeInMillis(actualTimeInMillis);
                        timeAmount = -calendar.get(Calendar.HOUR_OF_DAY);
                        showListBtn.setText(R.string.btn_day);
                        pieEntries.clear();
                        addValuesToPieEntry();
                        pieDataSet = new PieDataSet(pieEntries, "");
                        pieData = new PieData(pieDataSet);
                        pieChart.notifyDataSetChanged();
                        pieChart.invalidate();
                        pieChart.animateXY(1000, 500);
                    }
                });

        HamButton.Builder hamButton2 = new HamButton.Builder()
                .normalText(getString(R.string.btn_weekly))
                .textSize(20)
                .pieceColor(Color.WHITE)
                .normalColorRes(R.color.colorPrimary)
                .normalTextColor(Color.WHITE)
                .listener(new OnBMClickListener() {
                    @Override
                    public void onBoomButtonClick(int index) {
                        usageStatsInterval = UsageStatsManager.INTERVAL_DAILY;
                        calendarInterval = Calendar.DAY_OF_MONTH;
                        showListBtn.setText(R.string.btn_weekly);
                        timeAmount = -7;
                        pieEntries.clear();
                        addValuesToPieEntry();
                        pieDataSet = new PieDataSet(pieEntries, "");
                        pieData = new PieData(pieDataSet);
                        pieChart.notifyDataSetChanged();
                        pieChart.invalidate();
                        pieChart.animateXY(1000, 500);
                    }
                });

        HamButton.Builder hamButton3 = new HamButton.Builder()
                .normalText(getString(R.string.btn_montly))
                .textSize(20)
                .pieceColor(Color.WHITE)
                .normalColorRes(R.color.colorPrimary)
                .normalTextColor(Color.WHITE)
                .listener(new OnBMClickListener() {
                    @Override
                    public void onBoomButtonClick(int index) {
                        usageStatsInterval = UsageStatsManager.INTERVAL_MONTHLY;
                        calendarInterval = Calendar.MONTH;
                        timeAmount = -1;
                        showListBtn.setText(R.string.btn_montly);
                        pieEntries.clear();
                        addValuesToPieEntry();
                        pieDataSet = new PieDataSet(pieEntries, "");
                        pieData = new PieData(pieDataSet);
                        pieChart.notifyDataSetChanged();
                        pieChart.invalidate();
                        pieChart.animateXY(1000, 500);
                    }
                });

        bmb.addBuilder(hamButton1);
        bmb.addBuilder(hamButton2);
        bmb.addBuilder(hamButton3);
        //END HAMBUTTON

        showListBtn.setText(R.string.btn_day);
        showListBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNormalExample();
            }
        });
        circlebutton.setBackgroundColor(Color.TRANSPARENT);
        circlebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(LiberActivity.this);
                builder.setTitle(R.string.title_dialog_info);
                builder.setMessage(R.string.cirlce_progress_dialog);
                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                builder.show();
            }
        });
    }

    private void middleNightAlarm() {
        boolean isActive = (PendingIntent.getBroadcast(this, 0, new Intent("STORAGE_APPTIMES"), PendingIntent.FLAG_NO_CREATE) == null);
        if (isActive) {
            AlarmManager alarmManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent("STORAGE_APPTIMES");
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, 21);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 0);
            Calendar nowCal = Calendar.getInstance();
            nowCal.setTimeInMillis(System.currentTimeMillis());
            long alarmTime = cal.getTimeInMillis();
            long currentTime = nowCal.getTimeInMillis();

            if (alarmTime < currentTime) {
                cal.add(Calendar.DAY_OF_MONTH, 1);
                alarmTime = cal.getTimeInMillis();
            }
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
                    alarmTime,
                    AlarmManager.INTERVAL_DAY,
                    pendingIntent);
        }
    }

    private void beginNightAlarm() {
        boolean isActive = (PendingIntent.getBroadcast(this, 1, new Intent("STORAGE_APPTIMES"), PendingIntent.FLAG_NO_CREATE) == null);
        if (isActive) {
            AlarmManager alarmManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent("STORAGE_APPTIMES");
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 1, intent, 0);
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            Calendar nowCal = Calendar.getInstance();
            nowCal.setTimeInMillis(System.currentTimeMillis());
            long alarmTime = cal.getTimeInMillis();
            long currentTime = nowCal.getTimeInMillis();

            if (alarmTime < currentTime) {
                cal.add(Calendar.DAY_OF_MONTH, 1);
                alarmTime = cal.getTimeInMillis();
            }
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
                    alarmTime,
                    AlarmManager.INTERVAL_DAY,
                    pendingIntent);
        }
    }

    private void notificationAlarm() {
        boolean isActive = (PendingIntent.getBroadcast(this, 2, new Intent("STORAGE_APPTIMES"), PendingIntent.FLAG_NO_CREATE) == null);
        if (isActive) {
            AlarmManager alarmManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent("STORAGE_APPTIMES");
            intent.putExtra("notification", 0);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 2, intent, 0);
            Calendar nowCal = Calendar.getInstance();
            nowCal.setTimeInMillis(System.currentTimeMillis());
            alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP,
                    nowCal.getTimeInMillis() + 300000,
                    300000,
                    pendingIntent);
        }
    }

    public ArrayList<Integer> addColorsToPieChart() {
        ArrayList<Integer> colors = new ArrayList<>();
        for (int c : ColorTemplate.VORDIPLOM_COLORS)
            colors.add(c);
        for (int c : ColorTemplate.JOYFUL_COLORS)
            colors.add(c);
        for (int c : ColorTemplate.COLORFUL_COLORS)
            colors.add(c);
        for (int c : ColorTemplate.LIBERTY_COLORS)
            colors.add(c);
        for (int c : ColorTemplate.PASTEL_COLORS)
            colors.add(c);
        for (int c : ColorTemplate.MATERIAL_COLORS)
            colors.add(c);
        colors.add(ColorTemplate.getHoloBlue());
        return colors;
    }

    public void addValuesToPieEntry() {

        List<AppDetail> appDetailListUsefull = usageStatsUtil.
                getAppFiltered(calendarInterval, timeAmount, usageStatsInterval);

        int i = 0;
        String appName;
        Long totalTimeUsed;
        Long otherTotalTime = 0L;
        Long timeAllApps = 0L;
        for (AppDetail app : appDetailListUsefull) {
            timeAllApps += app.getTotalTime();
            if (i < 5) {
                appName = usageStatsUtil.convertPackageNameToName(app.getUsageStats().getPackageName());
                totalTimeUsed = app.getTotalTime();

                pieEntries.add(new PieEntry(totalTimeUsed.floatValue(), appName));
            } else otherTotalTime += app.getTotalTime();

            if (i == appDetailListUsefull.size() - 1 && otherTotalTime > 0L) {
                pieEntries.add(new PieEntry(otherTotalTime, getString(R.string.others_label_piechart)));
                break;
            }
            i++;
        }
        totalTimeTxt.setText(UsageStatsUtil.getFormatHMS(timeAllApps));

        if (calendarInterval == Calendar.HOUR_OF_DAY) {
            timeAllApps = TimeUnit.MILLISECONDS.toMinutes(timeAllApps);
            Long totalAllDay = 120L;
            percent = (int) ((timeAllApps * 100f) / totalAllDay);
        }

        if (percent >= 0 && percent <= 49) {
            circleProgress.setFinishedColor(ColorTemplate.rgb("#7DA7D9"));
            phraseTxt.setText(getString(R.string.phrase_0));
            pieChart.invalidate();
        } else if (percent >= 50 && percent <= 99) {
            circleProgress.setFinishedColor(ColorTemplate.rgb("#ABABAB"));
            phraseTxt.setText(getString(R.string.phrase_over_50));
            pieChart.invalidate();
        } else if (percent >= 100) {
            percent = 100;
            circleProgress.setFinishedColor(ColorTemplate.rgb("#FF6F69"));
            phraseTxt.setText(getString(R.string.phrase_over_100));
            pieChart.invalidate();
        }
    }

    private void showNormalExample() {
        startActivity(addExpansionArgs(new Intent(this, ListAppActivity.class)));
    }

    public Intent addExpansionArgs(Intent intent) {
        intent.putExtra(ARG_USE_EXPANSION, true);
        View expansionView = findViewById(R.id.expansion_view);
        int location[] = new int[2];
        expansionView.getLocationInWindow(location);
        intent.putExtra(ARG_EXPANSION_LEFT_OFFSET, location[0]);
        intent.putExtra(ARG_EXPANSION_TOP_OFFSET, location[1]);
        intent.putExtra(ARG_EXPANSION_VIEW_WIDTH, expansionView.getWidth());
        intent.putExtra(ARG_EXPANSION_VIEW_HEIGHT, expansionView.getHeight());
        intent.putExtra("intervalUsedApp", usageStatsInterval);
        intent.putExtra("dateUsedApp", calendarInterval);
        intent.putExtra("quantTimeUsedApp", timeAmount);
        return intent;
    }

    @TargetApi(Build.VERSION_CODES.M)
    private boolean checkPermission() {
        boolean granted;
        AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), getPackageName());
        if (mode == AppOpsManager.MODE_DEFAULT) {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP)
                granted = (checkCallingOrSelfPermission("android:get_usage_stats") == PackageManager.PERMISSION_GRANTED);
            else
                granted = (checkCallingOrSelfPermission(android.Manifest.permission.PACKAGE_USAGE_STATS) == PackageManager.PERMISSION_GRANTED);
        } else {
            granted = (mode == AppOpsManager.MODE_ALLOWED);
        }
        return granted;
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences prefs = getSharedPreferences("SETTINGS", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        boolean checkRefresh = prefs.getBoolean("refresh", true);
        editor.apply();

        if (checkRefresh && checkPermission()) {
            phraseTxt.setText("");
            editor.putBoolean("refresh", false);
            editor.commit();
            Intent i = getIntent();
            finish();
            startActivity(i);
        }
        if (!checkPermission()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.dialog_attention_title);
            builder.setMessage(R.string.permission_dialog);
            builder.setPositiveButton(R.string.confirm_button_dialog, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
                }
            });
            builder.setNegativeButton(R.string.negative_button_dialog, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialogFinish();
                }
            });
            builder.show();
        }
    }

    private void dialogFinish() {
        AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
        builder2.setTitle(R.string.dialog_attention_title);
        builder2.setMessage(R.string.dialog_info_negative_permission);
        builder2.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        builder2.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.sobre) {
            startActivity(new Intent(this, AboutActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
