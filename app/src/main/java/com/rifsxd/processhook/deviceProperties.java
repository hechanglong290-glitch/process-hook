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
            final long FAKE_TOTAL_BYTES = 64L * 1024L * 1024L * 1024L; // 64GB
            final long FAKE_FREE_BYTES = 48L * 1024L * 1024L * 1024L;   // 48GB

            // 1. 劫持 Java 层文件空间查询 (File.getTotalSpace 等)
            XposedHelpers.findAndHookMethod(File.class, "getTotalSpace", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    File file = (File) param.thisObject;
                    String path = file.getAbsolutePath();
                    if (path.contains("data") || path.contains("sdcard") || path.equals("/") || path.contains("mnt")) {
                        param.setResult(FAKE_TOTAL_BYTES);
                    }
                }
            });

            XposedHelpers.findAndHookMethod(File.class, "getFreeSpace", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    File file = (File) param.thisObject;
                    String path = file.getAbsolutePath();
                    if (path.contains("data") || path.contains("sdcard") || path.equals("/") || path.contains("mnt")) {
                        param.setResult(FAKE_FREE_BYTES);
                    }
                }
            });

            XposedHelpers.findAndHookMethod(File.class, "getUsableSpace", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    File file = (File) param.thisObject;
                    String path = file.getAbsolutePath();
                    if (path.contains("data") || path.contains("sdcard") || path.equals("/") || path.contains("mnt")) {
                        param.setResult(FAKE_FREE_BYTES);
                    }
                }
            });

            // 2. 劫持标准 StatFs（现代字节 + 老式块计算全家桶）
            Class<?> statFsClass = XposedHelpers.findClass("android.os.StatFs", null);

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

            // 3. 劫持系统服务 StorageStatsService（针对系统设置、偏好设置页面）
            try {
                Class<?> storageStatsServiceClass = XposedHelpers.findClass("com.android.server.storage.StorageStatsService", null);
                XposedBridge.hookAllMethods(storageStatsServiceClass, "getTotalBytes", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        param.setResult(FAKE_TOTAL_BYTES);
                    }
                });
                XposedBridge.hookAllMethods(storageStatsServiceClass, "getFreeBytes", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        param.setResult(FAKE_FREE_BYTES);
                    }
                });
                XposedBridge.log("[DeviceProfile] StorageStatsService Hooked Successfully!");
            } catch (Throwable t) {
                // 适配不同安卓大版本的系统服务类名变动
            }

            XposedBridge.log("[DeviceProfile] Full Spectrum Storage Spoofing Initialized Successfully!");
        } catch (Throwable t) {
            XposedBridge.log("[DeviceProfile] Full Hook Failed: " + t.getMessage());
        }
    }
}
