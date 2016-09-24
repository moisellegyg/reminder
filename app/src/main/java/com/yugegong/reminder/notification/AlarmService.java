package com.yugegong.reminder.notification;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.yugegong.reminder.ProductEditFragment;

/**
 * Created by ygong on 8/30/16.
 */
public class AlarmService {
    private static final String LOG_TAG = AlarmService.class.getSimpleName();

//    private Context mContext;
    private PendingIntent mSendIntent;
    private AlarmManager mAlarmManager;

    public AlarmService(Context context, Uri productUri, String productName, long expiredTimestamp) {
        Log.d(LOG_TAG, productUri.toString());
//        mContext = context;
        mAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent editIntent = new Intent(context, AlarmBroadcastReceiver.class);
        editIntent.putExtra(ProductEditFragment.PRODUCT_URI, productUri);
        editIntent.putExtra(AlarmBroadcastReceiver.KEY_PRODUCT_NAME, productName);
        editIntent.putExtra(AlarmBroadcastReceiver.KEY_EXPIRED_TIME, expiredTimestamp);
        mSendIntent = PendingIntent.getBroadcast(context, 0,
                editIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public void setAlarm(long triggerAtMillis) {
//        mAlarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, timestamp, AlarmManager.INTERVAL_HOUR, mSendIntent);
        if (mAlarmManager == null) return;
        Log.d(LOG_TAG, "setAlarm");
        mAlarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, mSendIntent);
    }
}
