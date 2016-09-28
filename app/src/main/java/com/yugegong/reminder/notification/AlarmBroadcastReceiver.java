package com.yugegong.reminder.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.yugegong.reminder.ProductEditActivity;
import com.yugegong.reminder.ProductEditFragment;
import com.yugegong.reminder.R;
import com.yugegong.reminder.Utils;
import com.yugegong.reminder.data.ProductContract;

import static com.yugegong.reminder.notification.NotificationBroadcastReceiver.ACTION_PRODUCT_DISMISS;
import static com.yugegong.reminder.notification.NotificationBroadcastReceiver.ACTION_PRODUCT_SET_USED;

/**
 * This broadcast receiver receive broadcast from {@link AlarmService}. It will create a notification
 * after receiving a broadcast. The created notification contains two actions:
 * {@link NotificationBroadcastReceiver#ACTION_PRODUCT_DISMISS} and
 * {@link NotificationBroadcastReceiver#ACTION_PRODUCT_SET_USED}, which would send out a broadcast
 * respectively to {@link NotificationBroadcastReceiver} when user presses action buttons.
 */
public class AlarmBroadcastReceiver extends BroadcastReceiver {
    private static final String LOG_TAG = AlarmBroadcastReceiver.class.getSimpleName();

    // keys of extras pass to intent.
    /**
     * Key to use when passing product name via intent
     */
    public static final String KEY_PRODUCT_NAME = "product_name";
    /**
     * Key to use when passing product expiration time via intent
     */
    public static final String KEY_EXPIRED_TIME = "expired_time";
    /**
     * Key to use when passing notification id via intent
     */
    public static final String KEY_NOTIFICATION_ID = "notification_id";

    // Group Key to group notification
    private final static String GROUP_KEY_PRODUCTS = "group_key_products";

    private String mProductName;
    private Uri mProductUri;
    private long mExpiredTimestamp;
    private int mNotificationId;
    private Context mContext;

    public AlarmBroadcastReceiver() {
        super();
        Log.d(LOG_TAG, "initiated");
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        if (intent == null) return;
        mProductName = intent.getStringExtra(KEY_PRODUCT_NAME);
        mExpiredTimestamp = intent.getLongExtra(KEY_EXPIRED_TIME, -1L);
        Log.d(LOG_TAG, mProductName + " expires at: " + mExpiredTimestamp);

        mProductUri = intent.getParcelableExtra(ProductEditFragment.PRODUCT_URI);
        if (mProductUri == null) return;
        mNotificationId = (int) ProductContract.ProductEntry.getIdFromUri(mProductUri);
        buildAndSendNotification();
    }

    /**
     * Build and send out a notification.
     */
    private void buildAndSendNotification() {
        final NotificationCompat.Builder builder = getNotificationBuilder();
        createNotification(builder);
        NotificationManager notificationManager =
                (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(mNotificationId, builder.build());
    }

    /**
     * Create a {@link android.support.v7.app.NotificationCompat.Builder} instance and initialize
     * it with general settings, including notification icon, title, content...
     * @return A {@link android.support.v7.app.NotificationCompat.Builder} instance
     */
    private NotificationCompat.Builder getNotificationBuilder() {
        long todayTime = Utils.getTodayTimeInMillis();
        int days = (int)((mExpiredTimestamp - todayTime) / Utils.ONE_DAYS_IN_MILLIS) + 1;
        String contentText = days == 1 ?
                mContext.getString(R.string.notif_product_expiring_today_desc, mProductName) :
                mContext.getString(R.string.notif_product_expiring_desc, mProductName, days);
        Log.d(LOG_TAG, contentText);
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext);
        builder.setPriority(Notification.PRIORITY_HIGH)
                .setSmallIcon(R.mipmap.nofication_icon)
                .setContentTitle(mContext.getString(R.string.notif_title))
                .setContentText(contentText)
                .setContentIntent(createEditPendingIntent(mContext, mProductUri))
                .setGroup(GROUP_KEY_PRODUCTS);  // Android N and above
        return builder;
    }

    private void createNotification(NotificationCompat.Builder builder) {
        // The order of calling these two methods below matters for the UI.
        addSetUsedAction(builder);
        addDismissAction(builder);
    }


    private void addDismissAction(NotificationCompat.Builder builder) {
        Log.d(LOG_TAG, "Will show \"dismiss\" action in the Notification");
        PendingIntent dismissPendingIntent = createNotificationPendingIntent(
                mContext,
                ACTION_PRODUCT_DISMISS,
                mNotificationId,
                mProductName,
                mProductUri,
                mExpiredTimestamp);
        NotificationCompat.Action action = new NotificationCompat.Action.Builder(
                R.drawable.ic_alarm_black_24dp,
                mContext.getString(R.string.notif_action_dismiss),
                dismissPendingIntent)
                .build();
        builder.addAction(action);

    }

    private void addSetUsedAction(NotificationCompat.Builder builder) {
        Log.d(LOG_TAG, "Will show \"Used\" action in the Notification");
        PendingIntent setUsedPendingIntent = createNotificationPendingIntent(
                mContext,
                ACTION_PRODUCT_SET_USED,
                mNotificationId,
                mProductName,
                mProductUri,
                mExpiredTimestamp);
        NotificationCompat.Action action = new NotificationCompat.Action.Builder(
                R.drawable.ic_done_black_24dp,
                mContext.getString(R.string.notif_action_used),
                setUsedPendingIntent)
                .build();
        builder.addAction(action);
    }

    private static PendingIntent createEditPendingIntent(Context context, Uri productUri) {
        Intent intent = new Intent(context, ProductEditActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra(ProductEditFragment.INTENT_EXTRA_DISABLE_DELETE_MENU_OPTION, false);
        intent.setData(productUri);
        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private static PendingIntent createNotificationPendingIntent(Context context, String action, int notificationId, String productName, Uri productUri, long expiredTimestamp) {
        final Intent intent = new Intent(action, null,
                context, NotificationBroadcastReceiver.class);
        Log.d("createNotification", "notificationId = " + notificationId);
        intent.putExtra(KEY_NOTIFICATION_ID, notificationId);
        intent.putExtra(KEY_PRODUCT_NAME, productName);
        intent.putExtra(KEY_EXPIRED_TIME, expiredTimestamp);
        intent.putExtra(ProductEditFragment.PRODUCT_URI, productUri);
        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }

}
