package com.liberapp.android.liber;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class AppDetailAdapter extends RecyclerView.Adapter<AppDetailAdapter.ViewHolder> {
    private List<AppDetail> appDetailList = new ArrayList<>();
    private UsageStatsUtil usageStatsUtil;
    private Context c;
    private ItemCliked itemCliked;

    public AppDetailAdapter(Context context, ItemCliked itemCliked) {
        c = context;
        usageStatsUtil = new UsageStatsUtil(context);
        this.itemCliked = itemCliked;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_app_layout, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        final String packageName = appDetailList.get(position).getUsageStats().getPackageName();
        String appName = usageStatsUtil.convertPackageNameToName(packageName);
        viewHolder.getPackageName().setText(appName.toUpperCase());
        Long totalTimeUsed = appDetailList.get(position).getTotalTime();
        String formatHMS = UsageStatsUtil.getFormatHMS(totalTimeUsed);
        viewHolder.getTotalTimeUsed().setText(c.getString(R.string.total_text_app, formatHMS));
        viewHolder.getAppIcon().setImageDrawable(appDetailList.get(position).getAppIco());
    }

    @Override
    public int getItemCount() {
        return appDetailList.size();
    }

    public interface ItemCliked {
        void onItemClick(View view, int position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView packageName;
        private final TextView totalTimeUsed;
        private final ImageView appIcon;

        public ViewHolder(View view) {
            super(view);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (itemCliked != null) {
                        itemCliked.onItemClick(v, getAdapterPosition());
                    }
                }
            });
            packageName = (TextView) view.findViewById(R.id.text_view_app_name);
            totalTimeUsed = (TextView) view.findViewById(R.id.text_view_totaltime_use);
            appIcon = (ImageView) view.findViewById(R.id.app_icon);
        }

        public TextView getPackageName() {
            return packageName;
        }

        public TextView getTotalTimeUsed() {
            return totalTimeUsed;
        }

        public ImageView getAppIcon() {
            return appIcon;
        }

    }

    public void setAppDetailList(List<AppDetail> appDetailses) {
        appDetailList = appDetailses;
    }
}



