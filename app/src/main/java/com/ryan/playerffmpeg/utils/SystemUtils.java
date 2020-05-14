package com.ryan.playerffmpeg.utils;

import android.os.Environment;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;
import java.util.Random;


public class SystemUtils {

    public static final String SYSTEM_ROM_TYPE_EMUI = "Emui";
    public static final String SYSTEM_ROM_TYPE_MIUI = "Miui";
    public static final String SYSTEM_ROM_TYPE_FLYME = "Flyme";
    public static final String SYSTEM_ROM_TYPE_ANDROID = "Android";

    //EMUI标识
    private static final String KEY_EMUI_VERSION_CODE = "ro.build.version.emui";
    private static final String KEY_EMUI_API_LEVEL = "ro.build.hw_emui_api_level";
    private static final String KEY_EMUI_CONFIG_HW_SYS_VERSION = "ro.confg.hw_systemversion";

    //MIUI标识
    private static final String KEY_MIUI_VERSION_CODE = "ro.miui.ui.version.code";
    private static final String KEY_MIUI_VERSION_NAME = "ro.miui.ui.version.name";
    private static final String KEY_MIUI_INTERNAL_STORAGE = "ro.miui.internal.storage";

    //Flyme标识
    private static final String KEY_FLYME_ID_FALG_KEY = "ro.build.display.id";
    private static final String KEY_FLYME_ID_FALG_VALUE_KEYWORD = "Flyme";
    private static final String KEY_FLYME_ICON_FALG = "persist.sys.use.flyme.icon";
    private static final String KEY_FLYME_SETUP_FALG = "ro.meizu.setupwizard.flyme";
    private static final String KEY_FLYME_PUBLISH_FALG = "ro.flyme.published";

    private static final String IMEI_FILE_NAME = "ROKID_IMEI";

    private static Properties prop = null;

    private static String sImei = null;

    public static String currentTimeMillisStr() {
        return String.valueOf(System.currentTimeMillis());
    }

    public static long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    public static int randomInt() {
        return new Random(Integer.MAX_VALUE).nextInt();
    }

    /**
     * return ROM_TYPE ROM类型的枚举
     * description获取ROM类型: MIUI_ROM, FLYME_ROM, EMUI_ROM, OTHER_ROM
     */

    public static String getRomType() {
        // 华为
        if (containsKey(KEY_EMUI_VERSION_CODE)
                || containsKey(KEY_EMUI_API_LEVEL)
                || containsKey(KEY_EMUI_CONFIG_HW_SYS_VERSION)) {
            return SYSTEM_ROM_TYPE_EMUI;
        }
        // 小米
        if (containsKey(KEY_MIUI_VERSION_CODE)
                || containsKey(KEY_MIUI_VERSION_NAME)
                || containsKey(KEY_MIUI_INTERNAL_STORAGE)) {
            return SYSTEM_ROM_TYPE_MIUI;
        }
        // 魅族
        if (containsKey(KEY_FLYME_ICON_FALG)
                || containsKey(KEY_FLYME_SETUP_FALG)
                || containsKey(KEY_FLYME_PUBLISH_FALG)) {
            return SYSTEM_ROM_TYPE_FLYME;
        }
        // 魅族
        String romName = getProperty(KEY_FLYME_ID_FALG_KEY);
        if (!TextUtils.isEmpty(romName) && romName.contains(KEY_FLYME_ID_FALG_VALUE_KEYWORD)) {
            return SYSTEM_ROM_TYPE_FLYME;
        }
        // 其他Android系统
        return SYSTEM_ROM_TYPE_ANDROID;
    }

    private static String getProperty(String key) {
        try {
            if (prop == null) {
                prop = new Properties();
                prop.load(new FileInputStream(new File(Environment.getRootDirectory(), "build.prop")));
            }

            return prop.getProperty(key);
        } catch (Exception e) {
            return "";
        }
    }

    private static boolean containsKey(String key) {
        try {
            if (prop == null) {
                prop = new Properties();
                prop.load(new FileInputStream(new File(Environment.getRootDirectory(), "build.prop")));
            }
            return prop.getProperty(key) != null;
        } catch (Exception e) {
            return isHaveInGetProp(key);
        }
    }

    private static boolean isHaveInGetProp(String name) {
        String line;
        BufferedReader input = null;
        try {
            Process p = Runtime.getRuntime().exec("getprop " + name);
            input = new BufferedReader(new InputStreamReader(p.getInputStream()), 1024);
            line = input.readLine();
            input.close();
        } catch (IOException ex) {
            Logger.e("Unable to read prop " + name);
            return false;
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return !TextUtils.isEmpty(line);
    }

}
