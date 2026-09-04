package com.rifsxd.processhook;

import android.annotation.SuppressLint;
import android.view.Display;
import android.os.Build;
import android.util.Log;

import java.lang.reflect.Field;

import de.robv.android.xposed.callbacks.XC_LoadPackage;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;

@SuppressLint("DiscouragedPrivateApi")
public class processHook implements IXposedHookLoadPackage {

    private final String TAG = processHook.class.getSimpleName();

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        String packageName = loadPackageParam.packageName;

        // 无条件强制执行 64G 容量修改，确保 LSPosed 勾选的任何软件（如当贝助手）都能生效
        deviceProperties.initStatFsHook();

        // 保持原有的机型与刷新率伪装逻辑
        deviceInfo properties = deviceProperties.DEVICE_MAP.get(packageName);
        if (properties != null) {
            spoofDeviceProperties(properties);
            spoofRefreshRate(properties);
            XposedBridge.log("Spoofed " + packageName + " as " + properties.device);
        }
    }

    private void spoofDeviceProperties(deviceInfo properties) {
        setPropValue("MANUFACTURER", properties.manufacturer);
        setPropValue("BRAND", properties.brand);
        setPropValue("PRODUCT", properties.product);
        setPropValue("DEVICE", properties.device);
        setPropValue("MODEL", properties.model);
        setPropValue("HARDWARE", properties.hardware);
        setPropValue("BOARD", properties.board);
        setPropValue("BOOTLOADER", properties.bootloader);
        setPropValue("USER", properties.username);
        setPropValue("HOST", properties.hostname);
        setPropValue("FINGERPRINT", properties.fingerprint);
    }

    private void spoofRefreshRate(deviceInfo properties) {
        if (properties.refreshrate != null) {
            try {
                float spoofedRefreshRate = Float.parseFloat(properties.refreshrate);
                XposedBridge.hookAllMethods(Display.class, "getRefreshRate", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        param.setResult(spoofedRefreshRate);
                        XposedBridge.log("Spoofed refresh rate to " + spoofedRefreshRate + " Hz");
                    }
                });
            } catch (NumberFormatException e) {
                XposedBridge.log("Invalid refresh rate value: " + properties.refreshrate);
            }
        }
    }

    private void setPropValue(String key, Object value) {
        if (value != null) {
            try {
                Log.d(TAG, "Defining prop " + key + " to " + value);
                Field field = Build.class.getDeclaredField(key);
                field.setAccessible(true);
                field.set(null, value);
                field.setAccessible(false);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                XposedBridge.log("Failed to set prop: " + key + "\n" + Log.getStackTraceString(e));
            }
        }
    }
}
