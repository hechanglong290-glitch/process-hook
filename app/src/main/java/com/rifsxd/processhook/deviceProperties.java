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
        // 配置表保留
    }

    public static void initStatFsHook() {
        try {
            Class<?> statFsClass = XposedHelpers.findClass("android.os.StatFs", null);

            // 设为你想要的 64GB
            final long FAKE_TOTAL_BYTES = 64L * 1024L * 1024L * 1024L; 
            final long FAKE_FREE_BYTES = 48L * 1024L * 1024L * 1024L;   

            // 1. 拦截构造函数，打印日志确认命中
            XposedBridge.hookAllConstructors(statFsClass, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    if (param.args.length > 0 && param.args[0] != null) {
                        XposedBridge.log("[DeviceProfile] StatFs initialized with path: " + param.args[0]);
                    }
                }
            });

            // 2. 现代 API 劫持
            XposedHelpers.findAndHookMethod(statFsClass, "getTotalBytes", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    param.setResult(FAKE_TOTAL_BYTES);
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

            // 3. 【核心救命稻草】老式 API 劫持（适配当贝助手等工具的块计算逻辑）
            // 强行让块大小为 4KB (4096字节)，总块数 = 64GB / 4096
            final long FAKE_BLOCK_SIZE = 4096L;
            final long FAKE_BLOCK_COUNT = FAKE_TOTAL_BYTES / FAKE_BLOCK_SIZE;
            final long FAKE_FREE_BLOCKS = FAKE_FREE_BYTES / FAKE_BLOCK_SIZE;

            XposedHelpers.findAndHookMethod(statFsClass, "getBlockSize", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    param.setResult((int) FAKE_BLOCK_SIZE);
                }
            });

            XposedHelpers.findAndHookMethod(statFsClass, "getBlockSizeLong", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    param.setResult(FAKE_BLOCK_SIZE);
                }
            });

            XposedHelpers.findAndHookMethod(statFsClass, "getBlockCount", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    param.setResult((int) FAKE_BLOCK_COUNT);
                }
            });

            XposedHelpers.findAndHookMethod(statFsClass, "getBlockCountLong", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    param.setResult(FAKE_BLOCK_COUNT);
                }
            });

            XposedHelpers.findAndHookMethod(statFsClass, "getFreeBlocks", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    param.setResult((int) FAKE_FREE_BLOCKS);
                }
            });

            XposedHelpers.findAndHookMethod(statFsClass, "getFreeBlocksLong", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    param.setResult(FAKE_FREE_BLOCKS);
                }
            });

            XposedHelpers.findAndHookMethod(statFsClass, "getAvailableBlocks", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    param.setResult((int) FAKE_FREE_BLOCKS);
                }
            });

            XposedHelpers.findAndHookMethod(statFsClass, "getAvailableBlocksLong", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    param.setResult(FAKE_FREE_BLOCKS);
                }
            });

            XposedBridge.log("[DeviceProfile] Complete Block & Bytes StatFs Hooked Successfully!");
        } catch (Throwable t) {
            XposedBridge.log("[DeviceProfile] Hook Failed: " + t.getMessage());
        }
    }
}
