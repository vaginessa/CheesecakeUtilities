package com.itachi1706.cheesecakeutilities.Modules.ListApplications.RecyclerAdapters;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.itachi1706.cheesecakeutilities.Modules.ListApplications.ListApplicationsDetailActivity;
import com.itachi1706.cheesecakeutilities.Modules.ListApplications.Objects.AppsItem;
import com.itachi1706.cheesecakeutilities.R;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by itachi1706 on 2/20/2016.
 * For com.itachi1706.cheesecakeutilities.Modules.ListApplications.RecyclerAdapters in Cheesecake Utilities.
 */
public class AppsAdapter extends RecyclerView.Adapter<AppsAdapter.AppsViewHolder> {
    private List<AppsItem> appsList;

    private static final int SORT_NAME = 0;
    public static final int SORT_API = 1;

    public AppsAdapter(List<AppsItem> strings)
    {
        this.appsList = strings;
    }

    public AppsAdapter(AppsItem[] strings)
    {
        this.appsList = Arrays.asList(strings);
    }

    public void sort() {
        sort(SORT_NAME);
    }

    public void sort(int type) {
        switch (type) {
            case SORT_API: sortByApiVersion(); break;
            case SORT_NAME:
            default: sortByName(); break;
        }
    }

    private void sortByName() {
        Collections.sort(appsList, new Comparator<AppsItem>() {
            @Override
            public int compare(AppsItem o1, AppsItem o2) {
                return o1.getAppName().compareTo(o2.getAppName());
            }
        });
    }

    private void sortByApiVersion() {
        Collections.sort(appsList, new Comparator<AppsItem>() {
            @Override
            public int compare(AppsItem o1, AppsItem o2) {
                return AppsAdapter.compare(o2.getApiVersion(), o1.getApiVersion());
            }
        });
    }

    private static int compare(int x, int y) {
        return (x < y) ? -1 : ((x == y) ? 0 : 1);
    }

    @Override
    public int getItemCount()
    {
        return appsList.size();
    }

    @Override
    public void onBindViewHolder(AppsViewHolder appsViewHolder, int i)
    {
        AppsItem s = appsList.get(i);
        appsViewHolder.appName.setText(s.getAppName());
        appsViewHolder.appApiVersion.setText(s.getApiVersion() + "");
        appsViewHolder.appPackageName.setText(s.getPackageName());
        appsViewHolder.appIcon.setImageDrawable(s.getIcon());
        appsViewHolder.appVersion.setText("Version: " + s.getVersion());
    }

    @Override
    public AppsViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.recyclerview_applist_apps, viewGroup, false);

        return new AppsViewHolder(itemView);
    }


    class AppsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView appName, appPackageName, appApiVersion, appVersion;
        ImageView appIcon;

        AppsViewHolder(View v)
        {
            super(v);
            appName = (TextView) v.findViewById(R.id.tvAppName);
            appPackageName = (TextView) v.findViewById(R.id.tvPackageName);
            appApiVersion = (TextView) v.findViewById(R.id.tvAPI);
            appIcon = (ImageView) v.findViewById(R.id.iv_icon);
            appVersion = (TextView) v.findViewById(R.id.tvVersion);
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Intent intent1 = new Intent(v.getContext(), ListApplicationsDetailActivity.class);
            intent1.putExtra("packageName", appPackageName.getText().toString());
            v.getContext().startActivity(intent1);
        }
    }
}
