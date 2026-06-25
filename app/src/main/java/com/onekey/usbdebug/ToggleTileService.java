package com.onekey.usbdebug;

import android.Manifest;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.widget.Toast;

public class ToggleTileService extends TileService {

    @Override
    public void onStartListening() {
        super.onStartListening();
        updateTileState();
    }

    @Override
    public void onClick() {
        super.onClick();
        if (!hasSecureSettingsPermission()) {
            Toast.makeText(this, "请先打开“一键调试”应用授予 ADB 权限", Toast.LENGTH_LONG).show();
            return;
        }

        // Toggle state
        boolean enabled = !(isUsbDebugEnabled() && isDeveloperOptionsEnabled());
        setDeveloperOptionsEnabled(enabled);
        setUsbDebugEnabled(enabled);
        setBypassRestrictionsEnabled(enabled);

        // Show Toast
        String message = enabled ? "调试及定制系统限制已解除" : "调试及定制系统限制已恢复";
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

        // Update the tile UI
        updateTileState();
    }

    private void updateTileState() {
        Tile tile = getQsTile();
        if (tile == null) return;
        
        boolean enabled = isUsbDebugEnabled() && isDeveloperOptionsEnabled();
        
        tile.setState(enabled ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
        tile.setLabel(enabled ? "调试：已开启" : "调试：已关闭");
        tile.updateTile();
    }

    private boolean hasSecureSettingsPermission() {
        return checkSelfPermission(Manifest.permission.WRITE_SECURE_SETTINGS) == PackageManager.PERMISSION_GRANTED;
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

    private void setBypassRestrictionsEnabled(boolean enabled) {
        try {
            android.content.ContentResolver resolver = getContentResolver();
            // Xiaomi Secure Input (USB debug security settings)
            Settings.Secure.putInt(resolver, "adb_input_method_enable", enabled ? 1 : 0);
            
            // OPPO Permission Monitor
            Settings.Global.putInt(resolver, "op_permission_monitor_enabled", enabled ? 0 : 1);
            Settings.Global.putInt(resolver, "op_permission_monitor_status", enabled ? 0 : 1);
            
            // Package verification and install warning bypass
            Settings.Global.putInt(resolver, "package_verifier_enable", enabled ? 0 : 1);
            Settings.Global.putInt(resolver, "adb_install_verify", enabled ? 0 : 1);
            Settings.Secure.putInt(resolver, "adb_install_status", enabled ? 1 : 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
