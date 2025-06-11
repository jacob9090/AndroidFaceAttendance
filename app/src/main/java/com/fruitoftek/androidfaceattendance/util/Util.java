package com.fruitoftek.androidfaceattendance.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.os.StatFs;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.preference.PreferenceManager;
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat;
import org.apache.commons.lang3.StringUtils;
import java.io.File;
import java.net.NetworkInterface;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import com.fruitoftek.androidfaceattendance.R;
import com.fruitoftek.androidfaceattendance.data.model.Users;
import com.fruitoftek.androidfaceattendance.detection.env.Logger;

public class Util {
    private static final Logger LOGGER = new Logger();
    private static String TAG = "Util";

    public static String SurfingDateTimeFormat = "yyyy-MM-dd HH:mm:ss";
    public static SimpleDateFormat SurfingSimpleDateFormat = new SimpleDateFormat(SurfingDateTimeFormat);

    public static String generateNewDeviceSerialNumber() {
        String uuid = UUID.randomUUID().toString();
        String timeLow = uuid.split("-")[0];
        String modelName = android.os.Build.MODEL;
        modelName = modelName.trim()
                .replace(" ", "")
                .replaceAll("[^\\.A-Za-z0-9]", "").toUpperCase();
        StringBuilder deviceSnSb = new StringBuilder();
        deviceSnSb = deviceSnSb
                .append("SA")
                .append(timeLow)
                .append(modelName);
        String deviceSn = StringUtils.substring(deviceSnSb.toString(), 0, 17);
        return deviceSn;
    }

    /**
     * @return The value of the SurfingTime Delay Setting. Delay in seconds
     */
    public static int getDelaySetting(Application application) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(application);
        String delayStr = sharedPreferences.getString("surfingTimeDelay", "");
        int delay = 120;
        if (StringUtils.isNotEmpty(delayStr) && StringUtils.isNumeric(delayStr)) {
            delay = Integer.parseInt(delayStr);
        }
        return delay;
    }

    /**
     * @return The model name of the Android Device without any special characters on it and trimmed to 20 spaces
     */
    public static String getDeviceModelForInfo() {
        String modelName = android.os.Build.MODEL;
        modelName = StringUtils.substring(modelName, 0, 20);
        return modelName;
    }

    /**
     * @return The timezone offset number formatted for command INFO
     */
    public static String getTimezoneForInfo() {
        try {
            Date now = new Date();
            // int timeZoneOffset = TimeZone.getDefault().getRawOffset() / 1000 / 60 / 60;
            int timeZoneOffsetWithDayLightSaving = TimeZone.getDefault().getOffset(now.getTime()) / 1000 / 60 / 60;
            return String.valueOf(timeZoneOffsetWithDayLightSaving);
        } catch (Exception ex) {
            LOGGER.e(TAG, ex, "Error processing TimeZone Offset Hours, defaulting to Central Time");
            return "-6";
        }
    }

    /**
     * @return The name of the Android Device without any special characters on it and trimmed to 50 spaces
     */
    public static String getDeviceNameForInfo(Application application) {
        String deviceName = Settings.Global.getString(application.getContentResolver(), Settings.Global.DEVICE_NAME);
        deviceName = deviceName.replaceAll("[^\\.A-Za-z0-9\\s]", "").trim();
        deviceName = StringUtils.substring(deviceName, 0, 50);
        return deviceName;
    }

    public static String getMacAddress(Context context) {
        // After Android 6.0 getting the MAC Address is now not possible
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wInfo = wifiManager.getConnectionInfo();
        String macAddress = wInfo.getMacAddress();
        return macAddress;
    }

    public static String getMacAddress() {
        try {// This one Never actually worked
            List<NetworkInterface> networkInterfaceList = Collections.list(NetworkInterface.getNetworkInterfaces());
            String stringMac = "";
            for (NetworkInterface networkInterface : networkInterfaceList) {
                if (networkInterface.getName().equalsIgnoreCase("wlan0")) {
                    for (int i = 0 ; i < networkInterface.getHardwareAddress().length; i++) {
                        String stringMacByte = Integer.toHexString(networkInterface.getHardwareAddress()[i]& 0xFF);
                        if(stringMacByte.length() == 1) {
                            stringMacByte = "0" +stringMacByte;
                        }
                        stringMac = stringMac + stringMacByte.toUpperCase() + ":";
                    }
                    break;
                }
            }
            return stringMac;
        } catch (Exception e) {
            LOGGER.e(TAG, e, "Error getting MAC Address from Device");
        }
        return  "0";
    }

    public static boolean externalMemoryAvailable() {
        return android.os.Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED);
    }

    /**
     * @return Available Internal Memory number of Bytes
     */
    public static long getAvailableInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSizeLong();
        long availableBlocks = stat.getAvailableBlocksLong();
        return availableBlocks * blockSize;
    }

    public static String getAvailableInternalMemorySizeFormatted() {
        return formatSize(getAvailableInternalMemorySize());
    }

    /**
     * @return Total Internal Memory number of Bytes
     */
    public static long getTotalInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSizeLong();
        long totalBlocks = stat.getBlockCountLong();
        return totalBlocks * blockSize;
    }

    public static String getTotalInternalMemorySizeFormatted() {
        return formatSize(getTotalInternalMemorySize());
    }

    /**
     * @return Available External Memory number of Bytes
     */
    public static long getAvailableExternalMemorySize() {
        if (externalMemoryAvailable()) {
            File path = Environment.getExternalStorageDirectory();
            StatFs stat = new StatFs(path.getPath());
            long blockSize = stat.getBlockSizeLong();
            long availableBlocks = stat.getAvailableBlocksLong();
            return availableBlocks * blockSize;
        } else {
            return -1;
        }
    }

    public static String getAvailableExternalMemorySizeFormatted() {
        return formatSize(getAvailableExternalMemorySize());
    }

    /**
     * @return Total External Memory number of Bytes
     */
    public static long getTotalExternalMemorySize() {
        if (externalMemoryAvailable()) {
            File path = Environment.getExternalStorageDirectory();
            StatFs stat = new StatFs(path.getPath());
            long blockSize = stat.getBlockSizeLong();
            long totalBlocks = stat.getBlockCountLong();
            return totalBlocks * blockSize;
        } else {
            return -1;
        }
    }

    public static String getTotalExternalMemorySizeFormatted() {
        return formatSize(getTotalExternalMemorySize());
    }

    public static String formatSize(long size) {
        String suffix = null;

        if (size >= 1024) {
            suffix = "KB";
            size /= 1024;
            if (size >= 1024) {
                suffix = "MB";
                size /= 1024;
            }
        }

        StringBuilder resultBuffer = new StringBuilder(Long.toString(size));

        int commaOffset = resultBuffer.length() - 3;
        while (commaOffset > 0) {
            resultBuffer.insert(commaOffset, ',');
            commaOffset -= 3;
        }

        if (suffix != null) resultBuffer.append(suffix);
        return resultBuffer.toString();
    }

    /**
     * Get a string representing the Date and Time NOW
     * in format "yyyy-MM-dd HH:mm:ss"
     */
    public static String getDateTimeNow() {
        return getFormatterDateTime(new Date());
    }

    /**
     * Format Date to "yyyy-MM-dd HH:mm:ss"
     */
    public static String getFormatterDateTime(Date date) {
        return SurfingSimpleDateFormat.format(date);
    }

    /**
     * Get Date from String with format "yyyy-MM-dd HH:mm:ss"
     */
    public static Date parseSurfingFormattedDateString(String dateStr) throws ParseException {
        return SurfingSimpleDateFormat.parse(dateStr);
    }

    /**
     * Returns a resized version of the Bitmap passed in
     */
    public static Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;

        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
        return resizedBitmap;
    }

    public static void showAttendanceRecordOkAlertDialog(Users user, Context context, Activity activity) {
        String userIdName = String.format("%d-%s", user.user, user.name);
        Bitmap userProfilePic = user.getPhoto();
        activity.runOnUiThread(() -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            LayoutInflater inflaterAlertDialog = activity.getLayoutInflater();
            View dialogLayout = inflaterAlertDialog.inflate(R.layout.face_attendance_record_ok, null);
            TextView textViewName = dialogLayout.findViewById(R.id.textView_att_record_ok_name);
            textViewName.setText(userIdName);
            if (userProfilePic != null) {
                ImageView imageViewUserProfilePic = dialogLayout.findViewById(R.id.imageView_att_record_ok);
                imageViewUserProfilePic.setImageBitmap(userProfilePic);
            }
            ImageView imageView_ok = dialogLayout.findViewById(R.id.imageView_ok);
            Drawable drawableOk = imageView_ok.getDrawable();

            builder.setView(dialogLayout);
            AlertDialog alertDialog = builder.create();
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            alertDialog.show();

            // Animate Check
            if (drawableOk instanceof AnimatedVectorDrawableCompat) {
                AnimatedVectorDrawableCompat avdc = (AnimatedVectorDrawableCompat) drawableOk;
                avdc.start();
            } else if (drawableOk instanceof AnimatedVectorDrawable) {
                AnimatedVectorDrawable avd = (AnimatedVectorDrawable) drawableOk;
                avd.start();
            }

            // Close the Alert after 2 seconds
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    alertDialog.dismiss();
                    timer.cancel();
                }
            }, 2000);
        });
    }

    public static String getVerifyTypeString(int verifyType, Application application) {
        if (verifyType == VerifyType.PASSWORD.getType()) {
            return application.getString(R.string.verifyType_Password);
        } else if (verifyType == VerifyType.FACE.getType()) {
            return application.getString(R.string.verifyType_Face);
        }
        return "";
    }

}
