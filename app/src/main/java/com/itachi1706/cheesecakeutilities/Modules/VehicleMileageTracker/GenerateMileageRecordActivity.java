package com.itachi1706.cheesecakeutilities.Modules.VehicleMileageTracker;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.LongSparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.itachi1706.cheesecakeutilities.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GenerateMileageRecordActivity extends AppCompatActivity {

    TableLayout layout;
    Spinner monthSel;
    SharedPreferences sp;
    String user_id;
    LongSparseArray<String> monthData = null;

    private static final String TAG = "GenerateMileageRec";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_mileage_record);

        layout = findViewById(R.id.veh_mileage_table);
        //monthSel = findViewById(R.id.veh_mileage_recordgen_spinner);

        sp = PreferenceManager.getDefaultSharedPreferences(this);
        user_id = sp.getString("firebase_uid", "nien");
        if (user_id.equalsIgnoreCase("nien")) {
            // Fail, return to login activity
            Toast.makeText(this, "Invalid Login Token, please re-login", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.modules_veh_mileage_generate_report, menu);
        MenuItem menuItem = menu.findItem(R.id.menuMonth);
        monthSel = (Spinner) menuItem.getActionView();

        FirebaseUtils.getFirebaseDatabase().getReference().child("users").child(user_id).child("statistics")
                .child("timeRecords").child("perMonth").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                monthData = new LongSparseArray<>();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    long key = Long.parseLong(ds.getKey());
                    Date d = new Date(key);
                    SimpleDateFormat sd = new SimpleDateFormat("MMMM yyyy", Locale.US);
                    monthData.put(key, sd.format(d));
                }
                Log.d(TAG, "Month Data Size: " + monthData.size());
                List<String> tmp = new ArrayList<>();
                for (int i = 0; i < monthData.size(); i++) {
                    tmp.add(monthData.valueAt(i));
                }

                ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getApplicationContext(), R.layout.simple_spinner_item_white, tmp);
                spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                monthSel.setAdapter(spinnerAdapter);
                monthSel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view1, int position, long id) {
                        if (position >= monthData.size()) {
                            // Error
                            Log.e(TAG, "Position #" + position + " exceeds dataset size of " + monthData.size());
                            return;
                        }
                        long date = monthData.indexOfKey(position);
                        processDate(date);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        return true;
    }

    private void processDate(long date) {
        // TODO: Process Date and generate the reports
    }
}
