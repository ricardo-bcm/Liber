package com.liberapp.android.liber;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.klinker.android.sliding.SlidingActivity;

import java.util.List;

public class ListAppActivity extends SlidingActivity implements AppDetailAdapter.ItemCliked {
    private AppDetailAdapter itemAdapter;
    private RecyclerView recyclerView;
    private List<AppDetail> appDetailList;

    @Override
    public void init(Bundle savedInstanceState) {

        disableHeader();

        int intervalUsedApps = getIntent().getIntExtra("intervalUsedApp", 0);
        int dateUsedApps = getIntent().getIntExtra("dateUsedApp", 0);
        int quantTimeUsedApps = getIntent().getIntExtra("quantTimeUsedApp", 0);

        setContent(R.layout.activity_list_app);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerview_item_app);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        recyclerView.scrollToPosition(0);
        itemAdapter = new AppDetailAdapter(this, this);
        recyclerView.setAdapter(itemAdapter);

        appDetailList = new UsageStatsUtil(this).
                getAppFiltered(dateUsedApps, quantTimeUsedApps, intervalUsedApps);

        int sizeHeight = 0;

        for (int i = 0; i < appDetailList.size(); i++) {

            sizeHeight += (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 90, getResources().getDisplayMetrics());
        }

        LinearLayout layout = (LinearLayout) findViewById(R.id.content_layout);
        ViewGroup.LayoutParams params = layout.getLayoutParams();
        params.height = sizeHeight;
        layout.setLayoutParams(params);

        updateAppsList(appDetailList);

        Intent intent = getIntent();
        if (intent.getBooleanExtra(LiberActivity.ARG_USE_EXPANSION, false)) {
            expandFromPoints(
                    intent.getIntExtra(LiberActivity.ARG_EXPANSION_LEFT_OFFSET, 0),
                    intent.getIntExtra(LiberActivity.ARG_EXPANSION_TOP_OFFSET, 0),
                    intent.getIntExtra(LiberActivity.ARG_EXPANSION_VIEW_WIDTH, 0),
                    intent.getIntExtra(LiberActivity.ARG_EXPANSION_VIEW_HEIGHT, 0)
            );
        }
    }

    private void updateAppsList(List<AppDetail> appDetailList) {

        for (AppDetail app : appDetailList) {
            try {
                Drawable appIcon = getPackageManager()
                        .getApplicationIcon(app.getUsageStats().getPackageName());
                app.setAppIco(appIcon);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        itemAdapter.setAppDetailList(appDetailList);
        itemAdapter.notifyDataSetChanged();
        recyclerView.scrollToPosition(0);
    }

    @Override
    public void onItemClick(View view, int position) {
        SharedPreferences prefs = getSharedPreferences(UsageStatsUtil.SETTING_PREFS, MODE_PRIVATE);
        if (prefs.getBoolean("dialogNoteFistTime", true)) {

            final int p = position;
            final SharedPreferences pref = prefs;

            AlertDialog.Builder builder = new AlertDialog.Builder(ListAppActivity.this);
            builder.setTitle(R.string.title_dialog_info);
            builder.setMessage(R.string.desactive_notification_dialog);
            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putBoolean("dialogNoteFistTime", false);
                    editor.apply();

                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(Uri.parse("package:" + appDetailList.get(p).getUsageStats().getPackageName()));
                    startActivity(intent);
                }
            });
            builder.show();
        } else {
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.parse("package:" + appDetailList.get(position).getUsageStats().getPackageName()));
            startActivity(intent);
        }
    }
}
