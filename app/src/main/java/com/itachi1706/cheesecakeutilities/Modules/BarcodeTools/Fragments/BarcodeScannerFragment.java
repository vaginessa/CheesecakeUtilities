package com.itachi1706.cheesecakeutilities.Modules.BarcodeTools.Fragments;

import android.app.admin.DevicePolicyManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode;
import com.google.gson.Gson;
import com.itachi1706.cheesecakeutilities.Modules.BarcodeTools.BarcodeCaptureActivity;
import com.itachi1706.cheesecakeutilities.Modules.BarcodeTools.BarcodeHelper;
import com.itachi1706.cheesecakeutilities.R;
import com.itachi1706.cheesecakeutilities.Util.LogHelper;

import static android.content.Context.DEVICE_POLICY_SERVICE;

/**
 * Created by Kenneth on 24/12/2017.
 * for com.itachi1706.cheesecakeutilities.Modules.BarcodeTools.Fragments in CheesecakeUtilities
 */

public class BarcodeScannerFragment extends Fragment {

    // use a compound button so either checkbox or switch widgets work.
    private CompoundButton useFlash;
    private Button scan;
    private TextView statusMessage;
    private TextView barcodeValue;

    private static final int RC_BARCODE_CAPTURE = 9001;
    private static final String TAG = "BarcodeScanner";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(R.layout.fragment_barcode_scanner, container, false);

        statusMessage = v.findViewById(R.id.status_message);
        barcodeValue = v.findViewById(R.id.barcode_value);
        barcodeValue.setMovementMethod(new ScrollingMovementMethod());
        useFlash = v.findViewById(R.id.use_flash);
        scan = v.findViewById(R.id.read_barcode);
        scan.setOnClickListener(view -> {
            // launch barcode activity.
            Intent intent = new Intent(getActivity(), BarcodeCaptureActivity.class);
            intent.putExtra(BarcodeCaptureActivity.USE_FLASH, useFlash.isChecked());
            startActivityForResult(intent, RC_BARCODE_CAPTURE);
        });
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Check for camera source
        //noinspection ConstantConditions
        DevicePolicyManager devicePolicyManager = (DevicePolicyManager) getActivity().getSystemService(DEVICE_POLICY_SERVICE);
        if (!this.getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA) ||
                (devicePolicyManager != null && devicePolicyManager.getCameraDisabled(null))) {
            // Disables stuff
            scan.setEnabled(false);
            statusMessage.setText(R.string.no_camera_hardware);
        } else {
            // Enable stuff
            scan.setEnabled(true);
            statusMessage.setText(getString(R.string.barcode_header));
        }
    }

    // Returning barcode activity
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_BARCODE_CAPTURE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    Gson gson = new Gson();
                    String json = data.getStringExtra(BarcodeCaptureActivity.BARCODE_OBJECT);
                    FirebaseVisionBarcode barcode = gson.fromJson(json, FirebaseVisionBarcode.class);
                    statusMessage.setText(R.string.barcode_success);
                    StringBuilder result = new StringBuilder();
                    result.append("Format: ").append(BarcodeHelper.getFormatName(barcode.getFormat())).append("\n");
                    result.append("Type: ").append(BarcodeHelper.getValueFormat(barcode.getValueType())).append("\n\n");
                    result.append("Content: ").append(barcode.getDisplayValue()).append("\n\n");
                    result.append("Raw Value: ").append(barcode.getRawValue()).append("\n");
                    result.append(BarcodeHelper.handleSpecialBarcodes(barcode)).append("\n");
                    barcodeValue.setText(result);
                    barcodeValue.setClickable(true);
                    barcodeValue.setOnClickListener(v -> {
                        ClipboardManager clipboard = (ClipboardManager) v.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                        if (clipboard != null) {
                            ClipData clip = ClipData.newPlainText("barcode", barcode.getDisplayValue());
                            clipboard.setPrimaryClip(clip);
                            Toast.makeText(v.getContext(), "Barcode copied to clipboard", Toast.LENGTH_LONG).show();
                        }
                    });
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        barcodeValue.setOnLongClickListener(v1 -> {
                            ClipData clip = ClipData.newPlainText("barcode", barcode.getDisplayValue());
                            View.DragShadowBuilder dragShadowBuilder = new View.DragShadowBuilder(v1);
                            v1.startDragAndDrop(clip, dragShadowBuilder, true, View.DRAG_FLAG_GLOBAL | View.DRAG_FLAG_GLOBAL_URI_READ |
                                    View.DRAG_FLAG_GLOBAL_PERSISTABLE_URI_PERMISSION);
                            return true;
                        });
                    }
                    LogHelper.d(TAG, "Barcode read: " + barcode.getDisplayValue());
                } else {
                    statusMessage.setText(R.string.barcode_failure);
                    barcodeValue.setClickable(false);
                    LogHelper.d(TAG, "No barcode captured, intent data is null");
                }
            } else {
                statusMessage.setText(String.format(getString(R.string.barcode_error),
                        CommonStatusCodes.getStatusCodeString(resultCode)));
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
