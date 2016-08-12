package com.yugegong.reminder.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by ygong on 8/9/16.
 */
public final class ProductContract {
    public static final String CONTENT_AUTHORITY = "com.yugegong.reminder.productprovider";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_PRODUCT = "product";



    public ProductContract(){}

    public static abstract class ProductEntry implements BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_PRODUCT)
                .build();

        // For single record
        public static final String CONTENT_DIR_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd." + CONTENT_AUTHORITY + "." + PATH_PRODUCT;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd." + CONTENT_AUTHORITY + "." + PATH_PRODUCT;

        // For multiple records
        public static final String TABLE_NAME = "product";
        public static final String COLUMN_NAME_PRODUCT_UPC = "upc";
        public static final String COLUMN_NAME_PRODUCT_NAME = "name";
        public static final String COLUMN_NAME_PRODUCT_IMG_PATH = "img_uri";
        public static final String COLUMN_NAME_PRODUCT_CREATE_DATE = "create_date";
        public static final String COLUMN_NAME_PRODUCT_EXPIRE_DATE = "expire_date";

        public static Uri buildProductUri(long _ID) {
            return CONTENT_URI.buildUpon().appendPath(Long.toString(_ID)).build();
        }
    }
}
