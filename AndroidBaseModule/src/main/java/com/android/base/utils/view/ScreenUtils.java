package com.android.base.utils.view;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

/**
 * Created by JiangZhiGuo on 2016/10/12.
 * describe 屏幕管理工具类
 */
public class ScreenUtils {

    /**
     * 无actionBar, 要在setContentView之前调用
     */
    public static void requestNoTitle(AppCompatActivity activity) {
        activity.supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
    }

    /**
     * 隐藏状态栏 , 也就是设置全屏，一定要在setContentView之前调用，否则报错
     */
    public static void hideStatusBar(Activity activity) {
        activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    /**
     * 设置屏幕为竖屏
     * <p>还有一种就是在Activity中加属性android:screenOrientation="landscape"</p>
     * <p>不设置Activity的android:configChanges时，切屏会重新调用各个生命周期，切横屏时会执行一次，切竖屏时会执行两次</p>
     * <p>设置Activity的android:configChanges="orientation"时，切屏还是会重新调用各个生命周期，切横、竖屏时只会执行一次</p>
     * <p>设置Activity的android:configChanges="orientation|keyboardHidden|screenSize"（4.0以上必须带最后一个参数）时
     * 切屏不会重新调用各个生命周期，只会执行onConfigurationChanged方法</p>
     */
    public static void requestPortrait(Activity activity) {
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    /**
     * 设置屏幕为横屏
     */
    public static void requestLandscape(Activity activity) {
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    /**
     * 获取屏幕参数集
     */
    public static DisplayMetrics getDisplay(Activity activity) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics;
    }

    /**
     * 获取屏幕的宽度px
     */
    public static int getScreenWidth(Context context) {
        return context.getResources().getDisplayMetrics().widthPixels;
    }

    /**
     * 获取屏幕的高度px
     */
    public static int getScreenHeight(Context context) {
        return context.getResources().getDisplayMetrics().heightPixels;
    }

    /**
     * 获取状态栏高度
     */
    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources()
                .getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0)
            result = context.getResources().getDimensionPixelSize(resourceId);
        return result;
    }

//    /**
//     * 获取ActionBar高度
//     */
//    public static int getActionBarHeight(Activity activity) {
//        int height = 0;
//        TypedValue tv = new TypedValue();
//        if (activity.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
//            height = TypedValue.complexToDimensionPixelSize(tv.data, activity.getResources()
//                    .getDisplayMetrics());
//        return height;
//    }

    /**
     * 获取当前屏幕截图，包含状态栏
     */
    public static Bitmap captureFullScreen(Activity activity) {
        View view = activity.getWindow().getDecorView();
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap bmp = view.getDrawingCache();
        int width = getScreenWidth(activity);
        int height = getScreenHeight(activity);
        Bitmap bp = Bitmap.createBitmap(bmp, 0, 0, width, height);
        view.destroyDrawingCache();
        return bp;
    }

    /**
     * 获取当前屏幕截图，不包含状态栏
     * <p>需要用到上面获取状态栏高度getStatusBarHeight的方法</p>
     */
    public static Bitmap captureNoStatus(Activity activity) {
        View view = activity.getWindow().getDecorView();
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap bmp = view.getDrawingCache();
        int statusBarHeight = getStatusBarHeight(activity);
        int width = getScreenWidth(activity);
        int height = getScreenHeight(activity);
        Bitmap bp = Bitmap.createBitmap(bmp, 0, statusBarHeight, width, height - statusBarHeight);
        view.destroyDrawingCache();
        return bp;
    }

    /**
     * 判断状态栏是否存在
     */
    public static boolean isStatusExists(Activity activity) {
        WindowManager.LayoutParams params = activity.getWindow().getAttributes();
        return (params.flags & WindowManager.LayoutParams.FLAG_FULLSCREEN)
                != WindowManager.LayoutParams.FLAG_FULLSCREEN;
    }

    /**
     * 判断是否锁屏
     */
    public static boolean isScreenLock(Context context) {
        KeyguardManager km = (KeyguardManager) context
                .getSystemService(Context.KEYGUARD_SERVICE);
        return km.inKeyguardRestrictedInputMode();
    }

    /**************************************沉浸式相关*********************************************/

    /**
     * 着色模式: 为status着色 ContextCompat.getColor(id)
     * Status底部为白色,所以这个不能全屏模式,下同
     */
    public static void setStatusColor(Activity activity, int statusColor) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = activity.getWindow();
            // 清除Status透明的状态
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            // 添加Status可以着色的状态
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            // 开始着色Status
            window.setStatusBarColor(statusColor);
        } else {
            // TODO: 2017/3/27  4.0-4.4
        }
    }

    /**
     * 着色模式: 为navigation着色 ContextCompat.getColor(id)
     */
    public static void setNavigationColor(Activity activity, int statusColor) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = activity.getWindow();
            // 清除Navigation透明的状态
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            // 添加Navigation可以着色的状态
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            // 开始着色Navigation
            window.setNavigationBarColor(statusColor);
        } else {
            // TODO: 2017/3/27  4.0-4.4
        }
    }

    /**
     * 全屏模式：这里只负责status的透明 ,并且最顶部view要设置 fitsSystemWindows="true"
     * 动态显示 view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
     * 动态显示 遮挡top布局 view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
     * 动态隐藏 view.setSystemUiVisibility(View.INVISIBLE);
     */
    public static void setStatusTrans(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = activity.getWindow();
            // 清除Status和navigation透明的状态
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            // 让DecorView填充Status和Navigation，这样他们的底色就不是白色，而是我们的Layout的背景色
            // setSystemUiVisibility就是用来操作Status的方法
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            // 添加Status可以着色的状态
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            // 开始着色Status
            window.setStatusBarColor(Color.TRANSPARENT);
        } else {
            // TODO: 2017/3/27  4.0-4.4
        }
    }

    /**
     * 全屏模式：这里只负责Navigation的透明
     */
    public static void setNavigationTrans(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = activity.getWindow();
            // 清除Status和navigation透明的状态
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            // 让DecorView填充Status和Navigation，这样他们的底色就不是白色，而是我们的Layout的背景色
            // setSystemUiVisibility就是用来操作Status的方法
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
            // 添加Status可以着色的状态
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            // 开始着色Navigation
            window.setNavigationBarColor(Color.TRANSPARENT);
        } else {
            // TODO: 2017/3/27  4.0-4.4
        }
    }

    /**
     * 获取activity的xml布局
     */
    public static View getMainLayout(Activity activity) {
        return activity.getWindow().findViewById(Window.ID_ANDROID_CONTENT);
    }

    /**
     * 获取状态栏＋标题栏高度,如果没有ActionBar，那么只有状态栏的高度
     */
    public static int getTopBarHeight(Activity activity) {
        return getMainLayout(activity).getBottom();
    }

    /**
     * 获取actionBar高度
     */
    public static int getActionBarHeight(Activity activity) {
        Rect frame = new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
        return frame.top;
    }

    /**
     * 获取状态栏高度, 和getStatusHeight()效果一样
     */
    public static int getStatusBarHeight(Activity activity) {
        return getTopBarHeight(activity) - getActionBarHeight(activity);
    }

    /**
     * 动态显示Status
     */
    public static void showStatus(View view) {
        view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
    }

    /**
     * 动态显示Status , 会遮挡top布局
     */
    public static void fitStatus(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
    }

    /**
     * 动态隐藏Status
     */
    public static void disStatus(View view) {
        view.setSystemUiVisibility(View.INVISIBLE);
    }

    /**
     * 唤醒屏幕并解锁
     * <uses-permission android:name="android.permission.WAKE_LOCK" />
     * <uses-permission android:name="android.permission.DISABLE_KEYGUARD" />
     *
     * @param context
     */
    public static void wakeUpAndUnlock(Context context) {
        KeyguardManager km = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        KeyguardManager.KeyguardLock kl = km.newKeyguardLock("unLock");
        //解锁
        kl.disableKeyguard();
        //获取电源管理器对象
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        //获取PowerManager.WakeLock对象,后面的参数|表示同时传入两个值,最后的是LogCat里用的Tag
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_DIM_WAKE_LOCK, "bright");
        //点亮屏幕
        wl.acquire();
        //释放
        wl.release();
    }

    /**
     * 判断当前手机是否处于锁屏(睡眠)状态
     *
     * @param context
     * @return
     */
    public static boolean isSleeping(Context context) {
        KeyguardManager kgMgr = (KeyguardManager) context
                .getSystemService(Context.KEYGUARD_SERVICE);

        return kgMgr.inKeyguardRestrictedInputMode();
    }

    /**
     * 主动回到Home，后台运行
     *
     * @param context
     */
    public static void goHome(Context context) {
        Intent mHomeIntent = new Intent(Intent.ACTION_MAIN);
        mHomeIntent.addCategory(Intent.CATEGORY_HOME);
        mHomeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        context.startActivity(mHomeIntent);
    }
}
