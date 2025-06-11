package com.fruitoftek.androidfaceattendance.surfingtime;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.fruitoftek.androidfaceattendance.surfingtime.services.SurfingTimeService;

public class SurfingTimeBroadcastReceiver extends BroadcastReceiver {

    // Start the Foreground service after the device reboots
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            if (SurfingTimeService.isAvailable(context)) {
                Intent surfingTimeForeGroundServiceIntent = new Intent(context, SurfingTimeForegroundService.class);
                context.startForegroundService(surfingTimeForeGroundServiceIntent);
            }
        }
    }
}
