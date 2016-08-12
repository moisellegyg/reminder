package com.yugegong.reminder.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by ygong on 8/9/16.
 */
public class ProductDbHelper extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Product.db";

    private static final String TEXT_TYPE = " TEXT NOT NULL";
    private static final String NULLABLE_TEXT_TYPE = " TEXT";
    private static final String INTEGER_TYPE = " INTEGER NOT NULL";
    private static final String COMMA_SEP = ",";

    private static final String SQL_CREATE_PRODUCTS_TABLE =
            "CREATE TABLE " + ProductContract.ProductEntry.TABLE_NAME + " (" +
                    ProductContract.ProductEntry._ID + " INTEGER PRIMARY KEY," +
                    ProductContract.ProductEntry.COLUMN_NAME_PRODUCT_UPC + NULLABLE_TEXT_TYPE + COMMA_SEP +
                    ProductContract.ProductEntry.COLUMN_NAME_PRODUCT_NAME + TEXT_TYPE + COMMA_SEP +
                    ProductContract.ProductEntry.COLUMN_NAME_PRODUCT_CREATE_DATE + INTEGER_TYPE + COMMA_SEP +
                    ProductContract.ProductEntry.COLUMN_NAME_PRODUCT_EXPIRE_DATE + INTEGER_TYPE + COMMA_SEP +
                    ProductContract.ProductEntry.COLUMN_NAME_PRODUCT_IMG_PATH + NULLABLE_TEXT_TYPE + " )";

    private static final String SQL_DELETE_PRODUCTS_TABLE =
            "DROP TABLE IF EXISTS " + ProductContract.ProductEntry.TABLE_NAME;

    public ProductDbHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_PRODUCTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_PRODUCTS_TABLE);
        onCreate(db);
    }
}
