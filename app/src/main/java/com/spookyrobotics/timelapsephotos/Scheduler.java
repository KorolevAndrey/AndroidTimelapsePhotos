package com.spookyrobotics.timelapsephotos;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.SystemClock;
import android.util.Log;

import com.spookyrobotics.timelapsephotos.functional.Function;

public class Scheduler {
    private static final String PICTURE_INTENT = "com.spookyrobotics.timelapsephotos.scheduler.take_picture";
    private static final long ONE_MINUTE = 1000 * 60;
    private static final String TAG = "Scheduler";

    private static BroadcastReceiver mReceiver = null;

    private static void registerReceiver(Context context, final Function<Context, Intent> function, IntentFilter intentFilter){
        if(mReceiver != null){
            Log.e(TAG, "mReceiver already registered");
            return;
        }
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                function.call(context,intent);
            }
        };
        context.registerReceiver(mReceiver, intentFilter);
        Log.d(TAG, "Receiver registered");
    }

    public static Intent getLocalTakePictureIntent() {
        return new Intent("LOCAL:"+PICTURE_INTENT);
    }

    private static void unregisterReceiver(Context context){
        if(mReceiver == null){
            Log.e(TAG, "mReceiver not registered");
            return;
        }
        context.unregisterReceiver(mReceiver);
        mReceiver = null;

    }

    private static String getGlobalIntentAction(String packageName) {
        return packageName + PICTURE_INTENT;
    }

    public static void cancelPictureIntent(Context context) {
        unregisterReceiver(context);
        Intent intent = new Intent();
        intent.setAction(PICTURE_INTENT);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }

    public static void scheduleRepeatingPicture(Context context, String packageName, Function<Context, Intent> function) {
        Intent intent = new Intent();
        intent.setAction(getGlobalIntentAction(packageName));
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        IntentFilter intentFilter = new IntentFilter(getGlobalIntentAction(context.getPackageName()));

        registerReceiver(context, function, intentFilter);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        // Schedule first event in the past so it fires immediately
        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() - ONE_MINUTE, ONE_MINUTE, pendingIntent);
    }

    public static IntentFilter getLocalTakePictureIntentFilter() {
        return new IntentFilter(getLocalTakePictureIntent().getAction());
    }
}
