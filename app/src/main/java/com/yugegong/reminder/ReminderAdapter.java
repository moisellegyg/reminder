package com.yugegong.reminder;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by ygong on 8/3/16.
 */
public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder>{
    private final static String TAG = ReminderAdapter.class.getSimpleName();

    private String[] mGoods;

    public ReminderAdapter(String[] data) {
        mGoods = data;
    }

    public static class ReminderViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final ImageView mImageView;
        public final TextView mTextView;
        public ReminderViewHolder(View view){
            super(view);
            mImageView = (ImageView) view.findViewById(R.id.product_image);
            mTextView = (TextView) view.findViewById(R.id.product_name);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Toast.makeText(v.getContext(), mTextView.getText(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public ReminderViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (parent instanceof RecyclerView) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_reminder, parent, false);
            view.setFocusable(true);
            return new ReminderViewHolder(view);
        } else throw new RuntimeException("ReminderAdapter is not bound to RecyclerView.");
    }

    @Override
    public void onBindViewHolder(ReminderViewHolder holder, int position) {
        holder.mTextView.setText(mGoods[position]);
    }

    @Override
    public int getItemCount() {
        return mGoods.length;
    }


}
