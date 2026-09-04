package com.rifsxd.processhook;

import android.os.StatFs;
import java.util.HashMap;
import java.util.Map;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public final class deviceProperties {

    public static final Map<String, deviceInfo> DEVICE_MAP = new HashMap<>();

    static {
        // 你原本的机型配置表可以继续写在这里
        // 比如：DEVICE_MAP.put("com.example.app", new deviceInfo(...));
    }

    // 必须是 public static void，允许 processHook 外部调用
    public static void initStatFsHook() {
        try {
            Class<?> statFsClass = XposedHelpers.findClass("android.os.StatFs", null);

            final long FAKE_TOTAL_BYTES = 64L * 1024L * 1024L * 1024L; // 64GB 总容量
            final long FAKE_FREE_BYTES = 48L * 1024L * 1024L * 1024L;   // 48GB 剩余可用空间

            XposedHelpers.findAndHookMethod(statFsClass, "getTotalBytes", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    param.setResult(FAKE_TOTAL_BYTES);
                }
            });

            XposedHelpers.findAndHookMethod(statFsClass, "getBlockCountLong", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    StatFs thiz = (StatFs) param.thisObject;
                    long blockSize = thiz.getBlockSizeLong();
                    if (blockSize > 0) {
                        param.setResult(FAKE_TOTAL_BYTES / blockSize);
                    }
                }
            });

            XposedHelpers.findAndHookMethod(statFsClass, "getFreeBytes", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    param.setResult(FAKE_FREE_BYTES);
                }
            });

            XposedHelpers.findAndHookMethod(statFsClass, "getAvailableBytes", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    param.setResult(FAKE_FREE_BYTES);
                }
            });

            XposedHelpers.findAndHookMethod(statFsClass, "getFreeBlocksLong", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    StatFs thiz = (StatFs) param.thisObject;
                    long blockSize = thiz.getBlockSizeLong();
                    if (blockSize > 0) {
                        param.setResult(FAKE_FREE_BYTES / blockSize);
                    }
                }
            });

            XposedHelpers.findAndHookMethod(statFsClass, "getAvailableBlocksLong", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    StatFs thiz = (StatFs) param.thisObject;
                    long blockSize = thiz.getBlockSizeLong();
                    if (blockSize > 0) {
                        param.setResult(FAKE_FREE_BYTES / blockSize);
                    }
                }
            });

            XposedBridge.log("[DeviceProfile] Full-Spectrum StatFs ROM Spoofing Hooked Successfully!");
        } catch (Throwable t) {
            XposedBridge.log("[DeviceProfile] StatFs Hook Failed: " + t.getMessage());
        }
    }
}
