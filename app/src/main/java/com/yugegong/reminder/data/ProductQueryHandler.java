package com.yugegong.reminder.data;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;

/**
 * Created by ygong on 9/21/16.
 */
public class ProductQueryHandler extends AsyncQueryHandler {

    public ProductQueryHandler(ContentResolver cr) {
        super(cr);
    }
}
