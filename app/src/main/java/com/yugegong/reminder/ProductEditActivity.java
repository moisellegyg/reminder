package com.yugegong.reminder;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

/**
 * Created by ygong on 8/4/16.
 */
public class ProductEditActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_edit);

    }


    public void cancelEdit(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);

    }

    public void saveEdit(View view) {
        Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
    }

}
