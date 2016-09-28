package com.yugegong.reminder.notification;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.yugegong.reminder.ProductEditFragment;

/**
 * This service is used to set up an alarm to send out a notification for a specified product.
 * It creates a broadcast which will bereceived by {@link AlarmBroadcastReceiver}.
 */
public class AlarmService {
    private static final String LOG_TAG = AlarmService.class.getSimpleName();

    private PendingIntent mSendIntent;
    private AlarmManager mAlarmManager;

    /**
     * Public constructor.
     * @param context The context that this service is being started in.
     * @param productUri The uri of the product that is being set up an alarm for.
     * @param productName The name of the product.
     * @param expiredTimestamp The expiration time of the product in millisecond.
     */
    public AlarmService(Context context, Uri productUri, String productName, long expiredTimestamp) {
        Log.d(LOG_TAG, productUri.toString());
        if (context == null) return;
        mAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmBroadcastReceiver.class);
        intent.putExtra(ProductEditFragment.PRODUCT_URI, productUri);
        intent.putExtra(AlarmBroadcastReceiver.KEY_PRODUCT_NAME, productName);
        intent.putExtra(AlarmBroadcastReceiver.KEY_EXPIRED_TIME, expiredTimestamp);
        mSendIntent = PendingIntent.getBroadcast(context, 0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * Set up an alarm at a specific time.
     * @param triggerAtMillis Time in millisecond to trigger the alarm
     */
    public void setAlarm(long triggerAtMillis) {
        if (mAlarmManager == null) return;
        Log.d(LOG_TAG, "setAlarm");
        mAlarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, mSendIntent);
    }
}
