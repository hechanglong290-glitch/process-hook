package com.rifsxd.processhook;

import android.os.StatFs;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public final class deviceProperties {

    public static final Map<String, deviceInfo> DEVICE_MAP = new HashMap<>();

    static {
        // 配置表保留
    }

    public static void initStatFsHook() {
        try {
            Class<?> statFsClass = XposedHelpers.findClass("android.os.StatFs", null);

            // 设你想虚构的超大容量（比如 128GB）
            final long FAKE_TOTAL_BYTES = 128L * 1024L * 1024L * 1024L; 
            final long FAKE_FREE_BYTES = 96L * 1024L * 1024L * 1024L;   

            // 1. 拦截 StatFs 的构造函数（无论它传的是哪个真实路径，如 /data 或 /sdcard，我们统统接管）
            XposedBridge.hookAllConstructors(statFsClass, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    // 可以在这里打印或捕获传入的路径参数，方便调试
                    if (param.args.length > 0 && param.args[0] != null) {
                        String path = param.args[0].toString();
                        XposedBridge.log("[DeviceProfile] StatFs initialized with path: " + path);
                    }
                }
            });

            // 2. 强行劫持所有容量获取方法，直接返回虚构的 128GB 结果
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

            XposedBridge.log("[DeviceProfile] Virtual Path & 128G StatFs Hooked Successfully!");
        } catch (Throwable t) {
            XposedBridge.log("[DeviceProfile] Virtual Path Hook Failed: " + t.getMessage());
        }
    }
}
