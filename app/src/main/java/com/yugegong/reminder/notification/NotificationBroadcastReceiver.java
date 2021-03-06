package com.yugegong.reminder.notification;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.yugegong.reminder.ProductListFragment;
import com.yugegong.reminder.Utils;
import com.yugegong.reminder.data.Product;
import com.yugegong.reminder.data.ProductContract;
import com.yugegong.reminder.data.ProductQueryHandler;

/**
 * Receive the broadcast from the notification created in {@link AlarmBroadcastReceiver} when user
 * press an action button on the notification card.
 */
public class NotificationBroadcastReceiver extends BroadcastReceiver {
    private static final String LOG_TAG = NotificationBroadcastReceiver.class.getSimpleName();

    /**
     * Dismiss action for the notification.
     * This action will reset a new alarm for the related product and dismiss the notification card.
     */
    public static final String ACTION_PRODUCT_DISMISS =
            "com.yugegong.reminder.ACTION_PRODUCT_DISMISS";
    /**
     * This action will set the related product having already been used. Database will be updated.
     * Notification card will be dismissed afterwards.
      */
    public static final String ACTION_PRODUCT_SET_USED =
            "com.yugegong.reminder.ACTION_PRODUCT_SET_USED";

    public static final String ACTION_NOTIFICATION_DELETED =
            "com.yugegong.reminder.ACTION_NOTIFICATION_DELETED";

    private Context mContext;

    private ProductQueryHandler mQueryHandler;

    /**
     * This method is called when {@code NotificationBroadcastReceiver} is receiving an intent
     * broadcast from {@link AlarmBroadcastReceiver}. The intent can have two different actions:
     * {@link NotificationBroadcastReceiver#ACTION_PRODUCT_DISMISS} and
     * {@link NotificationBroadcastReceiver#ACTION_PRODUCT_SET_USED}.
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        Log.i(LOG_TAG, "Broadcast from Notification: " + action);

        mContext = context;
        Uri productUri = intent.getData();
        mQueryHandler = new ProductQueryHandler(context, context.getContentResolver());
        int notificationId = intent.getIntExtra(AlarmBroadcastReceiver.KEY_NOTIFICATION_ID, -1);
        String productName = intent.getStringExtra(AlarmBroadcastReceiver.KEY_PRODUCT_NAME);
        long expiredTime = intent.getLongExtra(AlarmBroadcastReceiver.KEY_EXPIRED_TIME, -1L);
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
            case ACTION_NOTIFICATION_DELETED: {
                setNextAlarm(context, productUri, productName, expiredTime);
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

        Bundle bundle =  new Bundle();
        bundle.putParcelable(ProductQueryHandler.KEY_PRODUCT_URI, productUri);
        bundle.putString(ProductQueryHandler.KEY_PRODUCT_NAME, productName);
        bundle.putLong(ProductQueryHandler.KEY_EXPIRED_TIME,expiredTime);

        String[] projection = {ProductContract.ProductEntry.COLUMN_NAME_PRODUCT_IS_USED};
        mQueryHandler.startQuery(1,
                bundle,
                productUri,
                projection,
                null, null, null);

//        AlarmService service = new AlarmService(context, productUri, productName, expiredTime);
//        service.setAlarm(triggerAtMillis);
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
        mQueryHandler.startUpdate(1, null, productUri, cv, null, null);
//        mContext.getContentResolver().update(productUri, cv, null, null);
    }

    /**
     * Cancel the notification with a specified id.
     * @param notificationId Unique id of the notification.
     */
    public void cancelNotification(int notificationId) {
        NotificationManager manager =
                (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(notificationId);
    }
}
