package com.android.base.utils.file;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.text.format.Formatter;

import com.android.base.utils.func.ConvertUtils;
import com.android.base.utils.media.GlideUtils;
import com.android.base.utils.sys.AppUtils;
import com.android.base.utils.sys.ContextUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by gg on 2017/4/9.
 * 外存管理类
 */
public class CacheUtils {

    /**
     * 获取具有缓存的文件夹
     */
    public static List<String> getCacheFiles() {
        String resDir = AppUtils.get().getResDir();
        String filesDir = AppUtils.get().getFilesDir("");
        String cacheDir = AppUtils.get().getCacheDir();
        File internalFilesDir = ContextUtils.get().getFilesDir();
        File internalCacheDir = ContextUtils.get().getCacheDir();
        File cacheFile = GlideUtils.getCacheFile();

        List<String> filesList = new ArrayList<>();
        filesList.add(resDir);
        filesList.add(filesDir);
        filesList.add(cacheDir);
        filesList.add(internalFilesDir.getAbsolutePath());
        filesList.add(internalCacheDir.getAbsolutePath());
        filesList.add(cacheFile.getAbsolutePath());
        return filesList;
    }

    /**
     * 获取具有缓存的文件大小
     */
    public static long getCacheLength() {
        File resDir = new File(AppUtils.get().getResDir());
        File filesDir = new File(AppUtils.get().getFilesDir(""));
        File cacheDir = new File(AppUtils.get().getCacheDir());
        File internalFilesDir = ContextUtils.get().getFilesDir();
        File internalCacheDir = ContextUtils.get().getCacheDir();
        File cacheFile = GlideUtils.getCacheFile();
        return resDir.length() + filesDir.length() + cacheDir.length()
                + internalFilesDir.length() + internalCacheDir.length()
                + cacheFile.length();
    }

    /**
     * 获取具有缓存的文件大小
     */
    public static String getCacheSize() {
        long cacheLength = getCacheLength();
        return ConvertUtils.byte2FitSize(cacheLength);
    }

    /**
     * 清除所有资源
     */
    public static void clearResource() {
        String resDir = AppUtils.get().getResDir();
        FileUtils.deleteFilesAndDirInDir(resDir);
    }

    /**
     * 清除缓存
     */
    public static void clearCache() {
        String filesDir = AppUtils.get().getFilesDir("");
        String cacheDir = AppUtils.get().getCacheDir();
        File externalFilesDir = new File(filesDir);
        File externalCacheDir = new File(cacheDir);
        File internalFilesDir = ContextUtils.get().getFilesDir();
        File internalCacheDir = ContextUtils.get().getCacheDir();

        FileUtils.deleteFilesAndDirInDir(externalFilesDir);
        FileUtils.deleteFilesAndDirInDir(externalCacheDir);
        FileUtils.deleteFilesAndDirInDir(internalFilesDir);
        FileUtils.deleteFilesAndDirInDir(internalCacheDir);
        GlideUtils.clearCache();
    }

    /**
     * 外存总共空间
     */
    public static String getExternalTotal() {
        long totalSpace = Environment.getExternalStorageDirectory().getTotalSpace();
        return FileUtils.getFileSize(totalSpace);
    }

    /**
     * 外存使用空间
     */
    public static String getExternalUsable() {
        long usableSpace = Environment.getExternalStorageDirectory().getUsableSpace();
        return FileUtils.getFileSize(usableSpace);
    }

    /**
     * 外存剩余空间
     */
    public static String getExternalFree() {
        long freeSpace = Environment.getExternalStorageDirectory().getFreeSpace();
        return FileUtils.getFileSize(freeSpace);
    }

    /**
     * **********************************内存**********************************
     * 清理内存
     */
    public static void clearMemory() {
        GlideUtils.clearMemory();
        System.gc();
        // 也可以清理掉一些优先级低的进程
    }

    /**
     * 获取手机内存信息
     */
    private static ActivityManager.MemoryInfo getMemoryInfo() {
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        ContextUtils.getActivityManager().getMemoryInfo(memoryInfo);
        return memoryInfo;
    }

    /**
     * 获取总共运存
     */
    public static String getTotalMem(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            return Formatter.formatFileSize(context, getMemoryInfo().totalMem);
        }
        return "";
    }

    /**
     * 获取可用运存
     */
    public static String getAvailMem(Context context) {
        return Formatter.formatFileSize(context, getMemoryInfo().availMem);
    }

    /**
     * 系统内存不足的阀值，即临界值
     */
    public static String getThreshold(Context context) {
        return Formatter.formatFileSize(context, getMemoryInfo().threshold);
    }

    /**
     * 如果当前可用内存 <= threshold，该值为真
     */
    public static boolean isLowMemory(Context context) {
        return getMemoryInfo().availMem <= getMemoryInfo().threshold;
    }

}