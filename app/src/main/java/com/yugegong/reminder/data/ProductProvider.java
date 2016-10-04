package com.yugegong.reminder.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by ygong on 8/9/16.
 */
public class ProductProvider extends ContentProvider {
    private final static String TAG = ProductProvider.class.getSimpleName();
    // Defines a handle to the database helper object.
    private ProductDbHelper mProductDbHelper;
    // Holds the database object
    private SQLiteDatabase mDb;

    private static final int CODE_PRODUCTS = 100;
    private static final int CODE_PRODUCT_ITEM = 101;

    public static final String PRODUCT_ID_SELECTION = ProductContract.ProductEntry._ID + " = ? ";


    // Defines UriMatcher for ProductProvider
    private static UriMatcher mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        mUriMatcher.addURI(ProductContract.CONTENT_AUTHORITY, ProductContract.PATH_PRODUCT, CODE_PRODUCTS);
        mUriMatcher.addURI(ProductContract.CONTENT_AUTHORITY, ProductContract.PATH_PRODUCT + "/#", CODE_PRODUCT_ITEM);
    }

    @Override
    public boolean onCreate() {
        mProductDbHelper = new ProductDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor cursor;
        mDb = mProductDbHelper.getReadableDatabase();
        switch (mUriMatcher.match(uri)) {
            case CODE_PRODUCTS:
                cursor = mDb.query(
                        ProductContract.ProductEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case CODE_PRODUCT_ITEM:
                cursor = getProductById(uri, projection);
                break;
            default:
                cursor = null;
        }
        cursor.setNotificationUri(getContext().getContentResolver(),uri);
        return  cursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        final int code = mUriMatcher.match(uri);
        switch (code) {
            case CODE_PRODUCTS:
                return ProductContract.ProductEntry.CONTENT_DIR_TYPE;
            case CODE_PRODUCT_ITEM:
                return ProductContract.ProductEntry.CONTENT_ITEM_TYPE;
            default:
                return null;
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        long _id;
        mDb = mProductDbHelper.getWritableDatabase();
        switch (mUriMatcher.match(uri)) {
            case CODE_PRODUCTS:
                _id = mDb.insert(ProductContract.ProductEntry.TABLE_NAME, null, values);
                break;
            case CODE_PRODUCT_ITEM:
                _id = -1;
                break;
            default:
                _id = -1;
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return  ProductContract.ProductEntry.buildProductUri(_id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        Log.d(TAG, "delete " + uri.toString());
        int deletedRows = 0;
        mDb = mProductDbHelper.getWritableDatabase();
        switch (mUriMatcher.match(uri)) {
            case CODE_PRODUCTS:
                deletedRows = mDb.delete(ProductContract.ProductEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case CODE_PRODUCT_ITEM:
                deletedRows = deleteProductById(uri);
                break;
            default:
                deletedRows = 0;
        }
        if (deletedRows != 0) getContext().getContentResolver().notifyChange(uri, null);
        Log.d(TAG, "updatedRows = "  + deletedRows);
        return deletedRows;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int updatedRows = 0;
        mDb = mProductDbHelper.getWritableDatabase();
        switch (mUriMatcher.match(uri)) {
            case CODE_PRODUCTS:
                updatedRows = mDb.update(ProductContract.ProductEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            case CODE_PRODUCT_ITEM:
                updatedRows = updateProductById(uri, values);
                break;
            default:
                updatedRows = 0;
        }
        if (updatedRows != 0) getContext().getContentResolver().notifyChange(uri, null);
        Log.d(TAG, "updatedRows = "  + updatedRows);
        return updatedRows;
    }

    private Cursor getProductById(Uri uri, String[] projection){
        long _id = ProductContract.ProductEntry.getIdFromUri(uri);
        String selection = "_id = ? ";
        String[] selectionArgs = {Long.toString(_id)};
        return  mDb.query(
                ProductContract.ProductEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );
    }

    private int updateProductById(Uri uri, ContentValues values){
        long _id = ProductContract.ProductEntry.getIdFromUri(uri);
        String whereClause = "_id = ? ";
        String[] whereArgs = {Long.toString(_id)};
        return mDb.update(ProductContract.ProductEntry.TABLE_NAME,
                values,
                whereClause,
                whereArgs);
    }

    private int deleteProductById(Uri uri) {
        long _id = ProductContract.ProductEntry.getIdFromUri(uri);
        String whereClause = "_id = ? ";
        String[] whereArgs = {Long.toString(_id)};
        return mDb.delete(ProductContract.ProductEntry.TABLE_NAME, whereClause, whereArgs);
    }
}
