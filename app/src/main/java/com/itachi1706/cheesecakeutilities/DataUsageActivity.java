package com.itachi1706.cheesecakeutilities;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.app.usage.NetworkStats;
import android.app.usage.NetworkStatsManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.itachi1706.cheesecakeutilities.Util.CommonMethods;

import java.util.concurrent.TimeUnit;

@TargetApi(Build.VERSION_CODES.M)
public class DataUsageActivity extends AppCompatActivity implements View.OnClickListener {

    Button generalDetails, deviceDetails, userDetails;
    Button historyGeneral, historyUid;
    EditText uidValue;
    TextView results;
    NetworkStatsManager networkStatsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_usage);

        generalDetails = (Button) findViewById(R.id.btn_query_summary);
        deviceDetails = (Button) findViewById(R.id.btn_query_summary_device);
        userDetails = (Button) findViewById(R.id.btn_query_summary_user);

        historyGeneral = (Button) findViewById(R.id.btn_query_details);
        historyUid = (Button) findViewById(R.id.btn_query_details_uid);

        uidValue = (EditText) findViewById(R.id.et_query_details);
        results = (TextView) findViewById(R.id.tv_out);

        generalDetails.setOnClickListener(this);
        deviceDetails.setOnClickListener(this);
        userDetails.setOnClickListener(this);
        historyUid.setOnClickListener(this);
        historyGeneral.setOnClickListener(this);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            new AlertDialog.Builder(this).setTitle("API Level Too Low")
                    .setMessage("Your API Level (" + Build.VERSION.SDK_INT + ") is" +
                            " too low to use this utility.\n\nRequired API Level: 23")
                    .setCancelable(false)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            DataUsageActivity.this.finish();
                        }
                    }).show();
        } else {
            // Beta Note
            CommonMethods.betaInfo(this, "DataUsageActivity");

            networkStatsManager = (NetworkStatsManager) this.getSystemService(
                    Context.NETWORK_STATS_SERVICE);

            if (!checkIfPermGranted()) {
                new AlertDialog.Builder(this).setTitle("Grant Usage Stats Permission")
                        .setMessage("This utility requires you to grant the application Usage Stats access." +
                                "\nPress OK to launch settings to approve of this usage")
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
                            }
                        }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DataUsageActivity.this.finish();
                    }
                }).setCancelable(false).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.binhex_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.about:
                new AlertDialog.Builder(this)
                        .setMessage("Get Data Usage Information\n\n" +
                                "Note: BETA. Might not be within the final implementation")
                        .setCancelable(false)
                        .setPositiveButton(android.R.string.ok, null).show();
                return true;
            case R.id.exit:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void onClick(View v) {
        long end = System.currentTimeMillis();
        long start = System.currentTimeMillis() - (TimeUnit.DAYS.toMillis(1));  // 1 Month
        switch (v.getId()) {
            case R.id.btn_query_summary:
                try {
                    parseNetworkStats(networkStatsManager.querySummary(ConnectivityManager.TYPE_MOBILE, "", start, end));
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.btn_query_summary_device:
                try {
                    parseNetworkBucket(networkStatsManager.querySummaryForDevice(ConnectivityManager.TYPE_MOBILE, "", start, end));
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.btn_query_summary_user:
                try {
                    parseNetworkBucket(networkStatsManager.querySummaryForUser(ConnectivityManager.TYPE_MOBILE, "", start, end));
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.btn_query_details:
                try {
                    parseNetworkStats(networkStatsManager.queryDetails(ConnectivityManager.TYPE_MOBILE, "", start, end));
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.btn_query_details_uid: break;
        }
    }

    private boolean checkIfPermGranted() {
        AppOpsManager appOps = (AppOpsManager) this
                .getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow("android:get_usage_stats",
                android.os.Process.myUid(), this.getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    private void parseNetworkBucket(NetworkStats.Bucket bucket) {
        if (bucket == null) {
            presentOutput("NULL");
        } else {
            presentOutput(parseBucket(bucket));
        }
    }

    private void parseNetworkStats(NetworkStats stats) {
        if (stats == null) {
            presentOutput("NULL");
            return;
        }
        Log.d("Stat", "Has Stats. Processing");
        String out = "";
        NetworkStats.Bucket bucket = new NetworkStats.Bucket();
        Log.d("Stat", "Init");
        /*if (!stats.hasNextBucket()) {
            stats.close();
            presentOutput("NULL");
        }*/
        Log.d("Stat", "Start Loop");
        do {
            Log.d("Bucket", "Get Bucket");
            if (!stats.getNextBucket(bucket)) {
                Log.d("Bucket", "No Bucket");
                break;
            }
            out += parseBucket(bucket);
        } while (stats.hasNextBucket());
        Log.d("Stat", "End Loop");
        stats.close();
        presentOutput(out);
    }

    private void presentOutput(String results) {
        String concat = "UID | Start | End | RxBytes | RxPackets | State | TxBytes | TxPackets\n" + results;
        this.results.setText(concat);
    }

    private String parseBucket(NetworkStats.Bucket bucket) {
        return "UID: " + bucket.getUid() + " | Start: " + bucket.getStartTimeStamp()
                + " | End: " + bucket.getEndTimeStamp()
                + " | RxBytes: " + bucket.getRxBytes() + " | RxPackets: " + bucket.getRxPackets()
                + " | State: " + bucket.getState() + " | TxBytes: " + bucket.getTxBytes()
                + " | TxPackets: " + bucket.getTxPackets() + "\n";
    }
}
