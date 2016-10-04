package com.yugegong.reminder.data;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.yugegong.reminder.Utils;
import com.yugegong.reminder.notification.AlarmService;

/**
 * Created by ygong on 9/21/16.
 */
public class ProductQueryHandler extends AsyncQueryHandler {
    private static final String LOG_TAG = ProductQueryHandler.class.getSimpleName();

    public static final String KEY_PRODUCT_URI = "uri";
    public static final String KEY_PRODUCT_NAME =  "name";
    public static final String KEY_EXPIRED_TIME ="expired_time";

    private Context mContext;

    public ProductQueryHandler(Context context, ContentResolver cr) {
        super(cr);
        mContext = context;
    }

    @Override
    protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
        if (cursor == null) return;
        int isUsed = -1;
        if (cursor.moveToFirst()) {
            int columnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_NAME_PRODUCT_IS_USED);
            if (columnIndex != -1) {
                isUsed = cursor.getInt(columnIndex);
            }
            Log.d(LOG_TAG, "onQueryComplete " + columnIndex + " " + isUsed);
        }
        cursor.close();
        if (isUsed != 0) return;
        Bundle bundle =  (Bundle) cookie;
        Uri productUri = bundle.getParcelable(KEY_PRODUCT_URI);
        String productName = bundle.getString(KEY_PRODUCT_NAME);
        Long expiredTime = bundle.getLong(KEY_EXPIRED_TIME);

        //Midnight tomorrow time in millisecond
        long tomorrowTime = Utils.getTodayTimeInMillis() + Utils.ONE_DAYS_IN_MILLIS;
        //Three hours later from now on in millisecond
        long threeHrsLater = Utils.getCurrentTimeInMillis() + Utils.THREE_HOURS_IN_MILLIS;
        long triggerAtMillis = expiredTime > tomorrowTime ? tomorrowTime : threeHrsLater;

        AlarmService service = new AlarmService(mContext, productUri, productName, expiredTime);
        service.setAlarm(triggerAtMillis);
    }
}
