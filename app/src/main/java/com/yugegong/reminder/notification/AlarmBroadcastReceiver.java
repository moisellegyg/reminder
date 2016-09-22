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
import com.yugegong.reminder.data.ProductContract;
import static com.yugegong.reminder.notification.NotificationBroadcastReceiver.*;

/**
 * Created by ygong on 8/30/16.
 */
public class AlarmBroadcastReceiver extends BroadcastReceiver {
    private static final String LOG_TAG = AlarmBroadcastReceiver.class.getSimpleName();

    public static final String KEY_PRODUCT_NAME = "product_name";
    public static final String KEY_NOTIFICATION_ID = "notification_id";

    // Group Key to group notification
    private final static String GROUP_KEY_PRODUCTS = "group_key_products";

    private NotificationManager mNotificationManager;

    private String mProductName;
    private Uri mProductUri;
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
        Log.d(LOG_TAG, mProductName);

        mProductUri = intent.getParcelableExtra(ProductEditFragment.PRODUCT_URI);
        if (mProductUri == null) return;
        mNotificationId = (int) ProductContract.ProductEntry.getIdFromUri(mProductUri);
        buildAndSendNotification();
    }

    private void buildAndSendNotification() {
        final NotificationCompat.Builder builder = getNotificationBuilder();

        createNotification(builder);

        NotificationManager notificationManager =
                (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(mNotificationId, builder.build());
    }

    private NotificationCompat.Builder getNotificationBuilder() {
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext);
        builder.setPriority(Notification.PRIORITY_HIGH)
                .setSmallIcon(R.drawable.ic_restaurant_black_48dp)
                .setContentTitle(mContext.getString(R.string.notif_title))
                .setContentText(mContext.getString(R.string.notif_product_expiring_desc, mProductName))
                .setContentIntent(createEditPendingIntent(mContext, mProductUri))
                .setGroup(GROUP_KEY_PRODUCTS);  // Android N and above
        return builder;
    }

    private void createNotification(NotificationCompat.Builder builder) {
        addDismissAction(builder);
        addSetUsedAction(builder);
    }
    private void addDismissAction(NotificationCompat.Builder builder) {
        Log.d(this.toString(), "Will show \"dismiss\" action in the Notification");
        PendingIntent dismissPendingIntent = createNotificationPendingIntent(
                mContext,
                ACTION_PRODUCT_DISMISS,
                mNotificationId,
                mProductName,
                mProductUri);
        NotificationCompat.Action action = new NotificationCompat.Action.Builder(
                R.drawable.ic_alarm_black_24dp,
                mContext.getString(R.string.action_dismiss),
                dismissPendingIntent)
                .build();
        builder.addAction(action);

    }

    private void addSetUsedAction(NotificationCompat.Builder builder) {
        Log.d(this.toString(), "Will show \"Used\" action in the Notification");
        PendingIntent setUsedPendingIntent = createNotificationPendingIntent(
                mContext,
                ACTION_PRODUCT_SET_USED,
                mNotificationId,
                mProductName,
                mProductUri);
        NotificationCompat.Action action = new NotificationCompat.Action.Builder(
                R.drawable.ic_done_black_24dp,
                mContext.getString(R.string.action_used),
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

    private static PendingIntent createNotificationPendingIntent(Context context, String action, int notificationId, String productName, Uri productUri) {
        final Intent intent = new Intent(action, null,
                context, NotificationBroadcastReceiver.class);
        Log.d("createNotification", "notificationId = " + notificationId);
        intent.putExtra(KEY_NOTIFICATION_ID, notificationId);
        intent.putExtra(KEY_PRODUCT_NAME, productName);
        intent.putExtra(ProductEditFragment.PRODUCT_URI, productUri);
        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }

}
