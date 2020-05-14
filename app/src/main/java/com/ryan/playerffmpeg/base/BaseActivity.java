package com.ryan.playerffmpeg.base;

import android.Manifest;
import android.os.Build;
import android.os.Bundle;

import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;

import com.ryan.playerffmpeg.utils.Logger;
import com.ryan.playerffmpeg.utils.PermissionsUtils;
import com.ryan.playerffmpeg.utils.SystemUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public abstract class BaseActivity extends AppCompatActivity {

    private int REQUEST_CODE_PERMISSIONS = 99;

    protected ViewGroup mContentView;
    public ViewGroup mRootView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            final String[] PERMISSIONS = new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.RECORD_AUDIO,};

            PermissionsUtils.checkAndRequestMorePermissions(this, PERMISSIONS, REQUEST_CODE_PERMISSIONS,
                    new PermissionsUtils.PermissionRequestSuccessCallBack() {

                        @Override
                        public void onHasPermission() {
                            // 分解 onCreate 使其更符合 单一职能原则
                            setContentView(getLayoutId());
                            initApp();
                        }
                    });
        }
    }

    protected void initApp() {
        // 获取View
        initRootView();
        // 获取View
        initView();
        // 初始化变量
        initData();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    protected void initRootView() {
        // 获得 ContentView mRootView
        mContentView = this.findViewById(Window.ID_ANDROID_CONTENT);
        mRootView = (ViewGroup) mContentView.getChildAt(0);

        // 设置 沉浸式系统状态栏
        setSystemStatusBar(mRootView);
    }

    protected abstract int getLayoutId();

    protected abstract void initView();

    protected abstract void initData();

    /**
     * 系统状态栏字体颜色设置
     */
    protected void setSystemStatusBar(@NonNull View rootView) {
        // 启动页、广告页，不设置
        // setFitsSystemWindows 设置
        ViewCompat.setFitsSystemWindows(rootView, true);

        // 4.4+ 到 5.0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            WindowManager.LayoutParams localLayoutParams = getWindow().getAttributes();
            localLayoutParams.flags = (WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | localLayoutParams.flags);
        }

        // 5.0+ 到 6.0 浅色 显示
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
//            View decorView = getWindow().getDecorView();
//            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
//
//            getWindow().setStatusBarColor(Color.TRANSPARENT);
//            ViewCompat.setFitsSystemWindows(mRootView, false);
//            ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) mRootView.getLayoutParams();
//            layoutParams.topMargin = getStatusBarHeight();
//        }

        // 6.0+ 状态栏 浅色 显示
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        if (SystemUtils.SYSTEM_ROM_TYPE_MIUI.equals(SystemUtils.getRomType())) {
            // 兼容 小米
            Class<? extends Window> clazz = getWindow().getClass();
            try {
                int darkModeFlag;
                Class<?> layoutParams = Class.forName("android.view.MiuiWindowManager$LayoutParams");
                Field field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE");
                darkModeFlag = field.getInt(layoutParams);
                Method extraFlagField = clazz.getMethod("setExtraFlags", int.class, int.class);
                extraFlagField.invoke(getWindow(), darkModeFlag, darkModeFlag);
            } catch (Exception e) {
                // e.printStackTrace();
                Logger.w(this.getClass().getSimpleName() + " ,This is not Miui");
            }
            return;
        }

        if (SystemUtils.SYSTEM_ROM_TYPE_FLYME.equals(SystemUtils.getRomType())) {
            // 兼容 魅族
            try {
                WindowManager.LayoutParams lp = getWindow().getAttributes();
                Field darkFlag = WindowManager.LayoutParams.class.getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON");
                Field meizuFlags = WindowManager.LayoutParams.class.getDeclaredField("meizuFlags");
                darkFlag.setAccessible(true);
                meizuFlags.setAccessible(true);
                int bit = darkFlag.getInt(null);
                int value = meizuFlags.getInt(lp);
                value &= ~bit;
                meizuFlags.setInt(lp, value);
                getWindow().setAttributes(lp);
            } catch (Exception e) {
                // e.printStackTrace();
                Logger.w(this.getClass().getSimpleName() + " ,This is not MEIZU");
            }
        }
    }
}
