package com.onekey.usbdebug;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    private View layoutInstructions;
    private View layoutDashboard;
    private TextView tvCommand;
    private Button btnCopy;
    private Button btnRootGrant;
    private Button btnRecheck;

    private TextView tvMasterStatus;
    private Switch switchDeveloperOptions;
    private Switch switchUsbDebug;
    private Switch switchBypassRestrictions;
    private Button btnMasterToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPermissionAndRefresh();
    }

    private void initViews() {
        layoutInstructions = findViewById(R.id.layout_instructions);
        layoutDashboard = findViewById(R.id.layout_dashboard);
        tvCommand = findViewById(R.id.tv_command);
        btnCopy = findViewById(R.id.btn_copy);
        btnRootGrant = findViewById(R.id.btn_root_grant);
        btnRecheck = findViewById(R.id.btn_recheck);

        tvMasterStatus = findViewById(R.id.tv_master_status);
        switchDeveloperOptions = findViewById(R.id.switch_developer_options);
        switchUsbDebug = findViewById(R.id.switch_usb_debug);
        switchBypassRestrictions = findViewById(R.id.switch_bypass_restrictions);
        btnMasterToggle = findViewById(R.id.btn_master_toggle);
    }

    private void setupListeners() {
        // Copy ADB command
        btnCopy.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("ADB Command", tvCommand.getText());
            if (clipboard != null) {
                clipboard.setPrimaryClip(clip);
                Toast.makeText(MainActivity.this, R.string.copied_toast, Toast.LENGTH_SHORT).show();
            }
        });

        // Recheck permission
        btnRecheck.setOnClickListener(v -> {
            if (hasSecureSettingsPermission()) {
                Toast.makeText(MainActivity.this, R.string.permission_success_toast, Toast.LENGTH_SHORT).show();
                showDashboard();
            } else {
                Toast.makeText(MainActivity.this, R.string.permission_error_toast, Toast.LENGTH_SHORT).show();
            }
        });

        // Root Granting
        btnRootGrant.setOnClickListener(v -> {
            if (grantPermissionViaRoot()) {
                if (hasSecureSettingsPermission()) {
                    Toast.makeText(MainActivity.this, R.string.root_success_toast, Toast.LENGTH_SHORT).show();
                    showDashboard();
                } else {
                    Toast.makeText(MainActivity.this, R.string.root_failed_toast, Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(MainActivity.this, R.string.root_failed_toast, Toast.LENGTH_SHORT).show();
            }
        });

        // Individual switches logic (only triggered by user touch, not programmatically)
        switchDeveloperOptions.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed()) {
                setDeveloperOptionsEnabled(isChecked);
                updateUIState();
            }
        });

        switchUsbDebug.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed()) {
                setUsbDebugEnabled(isChecked);
                updateUIState();
            }
        });

        switchBypassRestrictions.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed()) {
                setBypassRestrictionsEnabled(isChecked);
                updateUIState();
            }
        });

        // Master toggle button
        btnMasterToggle.setOnClickListener(v -> {
            boolean isDevEnabled = isDeveloperOptionsEnabled();
            boolean isUsbEnabled = isUsbDebugEnabled();
            
            // If any is off, we enable all. Otherwise, we disable all.
            boolean targetState = !(isDevEnabled && isUsbEnabled);
            
            setDeveloperOptionsEnabled(targetState);
            setUsbDebugEnabled(targetState);
            setBypassRestrictionsEnabled(targetState);
            
            // Sync UI switches and master status
            refreshSwitchesState();
            updateUIState();
            
            String message = targetState ? "已一键开启全部设置" : "已一键恢复默认设置";
            Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
        });
    }

    private void checkPermissionAndRefresh() {
        if (hasSecureSettingsPermission()) {
            showDashboard();
        } else {
            showInstructions();
        }
    }

    private void showInstructions() {
        layoutInstructions.setVisibility(View.VISIBLE);
        layoutDashboard.setVisibility(View.GONE);
    }

    private void showDashboard() {
        layoutInstructions.setVisibility(View.GONE);
        layoutDashboard.setVisibility(View.VISIBLE);
        refreshSwitchesState();
        updateUIState();
    }

    private boolean hasSecureSettingsPermission() {
        return checkSelfPermission(Manifest.permission.WRITE_SECURE_SETTINGS) == PackageManager.PERMISSION_GRANTED;
    }

    private void refreshSwitchesState() {
        switchDeveloperOptions.setChecked(isDeveloperOptionsEnabled());
        switchUsbDebug.setChecked(isUsbDebugEnabled());
        switchBypassRestrictions.setChecked(isBypassRestrictionsEnabled());
    }

    private void updateUIState() {
        boolean isDevEnabled = switchDeveloperOptions.isChecked();
        boolean isUsbEnabled = switchUsbDebug.isChecked();
        
        if (isDevEnabled && isUsbEnabled) {
            tvMasterStatus.setText("已启用 / Enabled");
            tvMasterStatus.setTextColor(0xFF38BDF8); // Cyan
            btnMasterToggle.setText(R.string.disable_all);
            btnMasterToggle.setBackgroundColor(0xFFEF4444); // Red
        } else {
            tvMasterStatus.setText("已关闭 / Disabled");
            tvMasterStatus.setTextColor(0xFFEF4444); // Red
            btnMasterToggle.setText(R.string.enable_all);
            btnMasterToggle.setBackgroundColor(0xFF38BDF8); // Cyan
        }
    }

    private boolean grantPermissionViaRoot() {
        try {
            Process process = Runtime.getRuntime().exec("su");
            java.io.OutputStream os = process.getOutputStream();
            os.write("pm grant com.onekey.usbdebug android.permission.WRITE_SECURE_SETTINGS\n".getBytes());
            os.write("exit\n".getBytes());
            os.flush();
            return process.waitFor() == 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean isDeveloperOptionsEnabled() {
        return Settings.Global.getInt(getContentResolver(), Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0) == 1;
    }

    private void setDeveloperOptionsEnabled(boolean enabled) {
        try {
            Settings.Global.putInt(getContentResolver(), Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, enabled ? 1 : 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isUsbDebugEnabled() {
        return Settings.Global.getInt(getContentResolver(), Settings.Global.ADB_ENABLED, 0) == 1;
    }

    private void setUsbDebugEnabled(boolean enabled) {
        try {
            Settings.Global.putInt(getContentResolver(), Settings.Global.ADB_ENABLED, enabled ? 1 : 0);
            Settings.Secure.putInt(getContentResolver(), "adb_enabled", enabled ? 1 : 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isBypassRestrictionsEnabled() {
        return Settings.Global.getInt(getContentResolver(), "package_verifier_enable", 1) == 0;
    }

    private void setBypassRestrictionsEnabled(boolean enabled) {
        try {
            android.content.ContentResolver resolver = getContentResolver();
            Settings.Secure.putInt(resolver, "adb_input_method_enable", enabled ? 1 : 0);
            Settings.Global.putInt(resolver, "op_permission_monitor_enabled", enabled ? 0 : 1);
            Settings.Global.putInt(resolver, "op_permission_monitor_status", enabled ? 0 : 1);
            Settings.Global.putInt(resolver, "package_verifier_enable", enabled ? 0 : 1);
            Settings.Global.putInt(resolver, "adb_install_verify", enabled ? 0 : 1);
            Settings.Secure.putInt(resolver, "adb_install_status", enabled ? 1 : 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
