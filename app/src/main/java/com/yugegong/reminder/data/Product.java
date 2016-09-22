package com.yugegong.reminder.data;

/**
 * Created by ygong on 8/4/16.
 */
public class Product {
    private String mId;
    private String mName;
    private String mDescription;
    private long mCreatedTime;
    private long mExpiredTime;
    private boolean mDone;

    public Product(String Id, String name) {
        mId = Id;
        mName = name;
        mCreatedTime = System.currentTimeMillis();
    }

    public void setCreatedTime(long timestamp) {
        mCreatedTime = timestamp;
    }

    public void setExpiredTime(long timestamp) {
        mExpiredTime = timestamp;
    }
}

