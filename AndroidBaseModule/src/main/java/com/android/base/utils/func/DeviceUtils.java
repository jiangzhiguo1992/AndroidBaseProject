package com.android.base.utils.func;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Configuration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import com.android.base.utils.comp.ContextUtils;
import com.android.base.utils.str.StringUtils;

/**
 * Created by jiang on 2016/10/12
 * <p/>
 * describe  设备相关工具类
 */
public class DeviceUtils {

    private static DeviceUtils instance;

    private String deviceId; // GSM网络，返回IMEI；CDMA网络，返回MEID
    private String macAddress; // MAC地址
    private String model; // 设备型号
    private String manufacturer; // 设备厂商
    private String platform; // 平台(Android)
    private String osVersion; // Android版本号
    private boolean isPhone; // 是否是手机
    private boolean isTable; // 是否是手表
    private String phoneNumber; // 手机号
    private String simSerial; // sim卡序号

    /* 获取当前Device信息 */
    public static DeviceUtils get() {
        if (instance != null) return instance;
        PermUtils.requestDevice(null); // 权限请求
        instance = new DeviceUtils();
        return instance;
    }

    /**
     * 获取手机的IMIE(DeviceId)
     */
    @SuppressLint("HardwareIds")
    public String getDeviceId() {
        if (!StringUtils.isEmpty(deviceId)) return deviceId;
        String deviceId;
        if (isPhone()) {
            deviceId = ContextUtils.getTelephonyManager().getDeviceId();
        } else {
            ContentResolver contentResolver = ContextUtils.get().getContentResolver();
            deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID);
        }
        setDeviceId(deviceId);
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    @SuppressLint("HardwareIds")
    public String getPhoneNumber() {
        if (!StringUtils.isEmpty(phoneNumber)) return phoneNumber;
        setPhoneNumber(ContextUtils.getTelephonyManager().getLine1Number());
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    @SuppressLint("HardwareIds")
    public String getSimSerial() {
        if (!StringUtils.isEmpty(simSerial)) return simSerial;
        setSimSerial(ContextUtils.getTelephonyManager().getSimSerialNumber());
        return simSerial;
    }

    public void setSimSerial(String simSerial) {
        this.simSerial = simSerial;
    }

    /**
     * 物理地址
     */
    @SuppressLint("HardwareIds")
    public String getMacAddress() {
        if (!StringUtils.isEmpty(macAddress)) return macAddress;
        WifiManager wifi = (WifiManager) ContextUtils.get()
                .getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifi.getConnectionInfo();
        if (info != null) {
            setMacAddress(info.getMacAddress());
        }
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public boolean isPhone() {
        setPhone(ContextUtils.getTelephonyManager().getPhoneType()
                != TelephonyManager.PHONE_TYPE_NONE);
        return isPhone;
    }

    public void setPhone(boolean phone) {
        isPhone = phone;
    }

    public boolean isTable() {
        int screenLayout = ContextUtils.get().getResources().getConfiguration().screenLayout;
        boolean xlarge = ((screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == 4);
        boolean large = ((screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) ==
                Configuration.SCREENLAYOUT_SIZE_LARGE);
        setTable((xlarge || large));
        return isTable;
    }

    public void setTable(boolean table) {
        isTable = table;
    }

    public String getManufacturer() {
        if (!StringUtils.isEmpty(manufacturer)) return manufacturer;
        setManufacturer(Build.MANUFACTURER);
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getModel() {
        if (!StringUtils.isEmpty(model)) return model;
        setModel(Build.MODEL);
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getPlatform() {
        if (!StringUtils.isEmpty(platform)) return platform;
        setPlatform("Android");
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getOsVersion() {
        if (!StringUtils.isEmpty(osVersion)) return osVersion;
        setOsVersion(Build.VERSION.RELEASE);
        return osVersion;
    }

    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }

}
