package com.onekey.usbdebug;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.widget.Toast;

public class ToggleReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!"com.onekey.usbdebug.action.TOGGLE".equals(intent.getAction())) return;

        if (!hasSecureSettingsPermission(context)) {
            Toast.makeText(context, "一键调试：未被授予 ADB 安全设置写入权限，操作失败", Toast.LENGTH_LONG).show() ;
            return;
        }

        android.content.ContentResolver contentResolver = context.getContentResolver();
        boolean isDevEnabled = Settings.Global.getInt(contentResolver, Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0) == 1;
        boolean isUsbEnabled = Settings.Global.getInt(contentResolver, Settings.Global.ADB_ENABLED, 0) == 1;

        // Determine target state
        boolean targetState;
        if (intent.hasExtra("enable")) {
            targetState = intent.getBooleanExtra("enable", false);
        } else {
            targetState = !(isDevEnabled && isUsbEnabled);
        }

        try {
            // Set Developer Options
            Settings.Global.putInt(contentResolver, Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, targetState ? 1 : 0);

            // Set USB Debugging
            Settings.Global.putInt(contentResolver, Settings.Global.ADB_ENABLED, targetState ? 1 : 0);
            Settings.Secure.putInt(contentResolver, "adb_enabled", targetState ? 1 : 0);

            // Set Bypass Custom Systems restrictions
            Settings.Secure.putInt(contentResolver, "adb_input_method_enable", targetState ? 1 : 0);
            Settings.Global.putInt(contentResolver, "op_permission_monitor_enabled", targetState ? 0 : 1);
            Settings.Global.putInt(contentResolver, "op_permission_monitor_status", targetState ? 0 : 1);
            Settings.Global.putInt(contentResolver, "package_verifier_enable", targetState ? 0 : 1);
            Settings.Global.putInt(contentResolver, "adb_install_verify", targetState ? 0 : 1);
            Settings.Secure.putInt(contentResolver, "adb_install_status", targetState ? 1 : 0);

            String statusMsg = targetState ? "一键开启全部调试设置" : "一键恢复默认设置";
            Toast.makeText(context, "一键调试：" + statusMsg, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "一键调试：执行失败, " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private boolean hasSecureSettingsPermission(Context context) {
        return context.checkSelfPermission(Manifest.permission.WRITE_SECURE_SETTINGS) == PackageManager.PERMISSION_GRANTED;
    }
}
