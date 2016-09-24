package com.yugegong.reminder.notification;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.yugegong.reminder.ProductEditFragment;
import com.yugegong.reminder.Utils;
import com.yugegong.reminder.data.ProductContract;
import com.yugegong.reminder.data.ProductQueryHandler;

/**
 * Created by ygong on 9/15/16.
 */
public class NotificationBroadcastReceiver extends BroadcastReceiver {
    private static final String LOG_TAG = NotificationBroadcastReceiver.class.getSimpleName();

    // Dismiss action to take in the notification card
    public static final String ACTION_PRODUCT_DISMISS =
            "com.yugegong.reminder.ACTION_PRODUCT_DISMISS";
    // Set product used action to take in the notification card
    public static final String ACTION_PRODUCT_SET_USED =
            "com.yugegong.reminder.ACTION_PRODUCT_SET_USED";

    public static final int TIME_INTERVAL_IN_MILLIS = 21600000; // 6hrs
    public static ProductQueryHandler handler;

    private Context mContext;

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        Log.i(this.toString(), "Broadcast from Notification: " + action);

        mContext = context;
        if (handler != null) handler = new ProductQueryHandler(context.getContentResolver());
        int notificationId = intent.getIntExtra(AlarmBroadcastReceiver.KEY_NOTIFICATION_ID, -1);
        String productName = intent.getStringExtra(AlarmBroadcastReceiver.KEY_PRODUCT_NAME);
        long expiredTime = intent.getLongExtra(AlarmBroadcastReceiver.KEY_EXPIRED_TIME, -1L);
        Uri productUri = intent.getParcelableExtra(ProductEditFragment.PRODUCT_URI);
        Log.d(LOG_TAG, notificationId + " " + productName);

        switch (action) {
            case ACTION_PRODUCT_SET_USED:
                setProductIsUsed(productUri, 1);
                cancelNotification(notificationId);
                break;
            case ACTION_PRODUCT_DISMISS: {
                setNextAlarm(context, productUri, productName, expiredTime);
                cancelNotification(notificationId);
                break;
            }
            default:
                break;
        }
    }

    /**
     * Set a new alarm for a specified product
     * @param context The context in which the alarm will be set
     * @param productUri URI for the product
     * @param productName Name for the product
     */
    private void setNextAlarm(Context context, Uri productUri, String productName, long expiredTime) {
        AlarmService service = new AlarmService(context, productUri, productName, expiredTime);
        //Midnight tomorrow time in millisecond
        long tomorrowTime = Utils.getTodayTimeInMillis() + Utils.ONE_DAYS_IN_MILLIS;
        //Three hours later from now on in millisecond
        long threeHrsLater = Utils.getCurrentTimeInMillis() + Utils.THREE_HOURS_IN_MILLIS;
        long triggerAtMillis = expiredTime > tomorrowTime ? tomorrowTime : threeHrsLater;
        service.setAlarm(triggerAtMillis);
    }

    /**
     * Update the status of the product whether it's used by user or not
     * @param productUri the content uri of the product, which matches CODE_PRODUCT_ITEM in ProductProvider
     * @param isUsed when set to 1 means true, 0 means false
     */
    private void setProductIsUsed(Uri productUri, int isUsed) {
        Log.d(LOG_TAG, "setProductIsUsed");
        ContentValues cv = new ContentValues();
        cv.put(ProductContract.ProductEntry.COLUMN_NAME_PRODUCT_IS_USED, isUsed);
//        handler.startUpdate(1, null, productUri, cv, null, null);
        mContext.getContentResolver().update(productUri, cv, null, null);
    }


    public void cancelNotification(int notificationId) {
        NotificationManager manager =
                (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(notificationId);
    }
}
