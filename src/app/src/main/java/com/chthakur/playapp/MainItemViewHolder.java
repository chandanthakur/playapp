package com.chthakur.playapp;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.HashMap;

// stores and recycles views as they are scrolled off screen
public class MainItemViewHolder extends ViewHolderBase implements View.OnClickListener {
    public static final String TAG = MainItemViewHolder.class.getSimpleName();
    public static final String ViewType = "";
    public View mParent;
    public ImageView mImageView;

    static final HashMap<Integer, Integer> rowMap = new HashMap<Integer, Integer>(){
        {
            put(1, 1);
            put(2, 2);
            put(3, 2);
            put(4, 2);
        }
    };

    public MainItemViewHolder(Context context, View parent, View itemView) {
        super(context, parent, itemView);
        mImageView = (ImageView) itemView.findViewById(R.id.image_view);
        mParent = parent;
        itemView.setOnClickListener(this);
    }

    public static ViewHolderBase createViewHolder(Context context, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.recyclerview_item, parent, false);
        MainItemViewHolder viewHolder = new MainItemViewHolder(context, parent, view);
        return viewHolder;
    }

    @Override
    public void bindViewHolder(ViewHolderBaseSchema dataSchema, int totalItems) {
        super.bindViewHolder(dataSchema, totalItems);
        MainItemViewHolder holder = this;
        ViewHolderDataSchema holderDataSchema = (ViewHolderDataSchema)dataSchema;
        Log.i(TAG, "totalItems:" + totalItems);

        holder.itemView.getLayoutParams().height = holderDataSchema.itemSize.height;
        holder.itemView.getLayoutParams().width = holderDataSchema.itemSize.width;
        Glide.with(context).load(holderDataSchema.imageUri).into(mImageView);
    }

    static public class ViewHolderDataSchema extends ViewHolderBaseSchema {
        public String getType() {
            return MainItemViewHolder.class.getSimpleName();
        }

        public String text;

        public String imageUri;

        public int color;

        public ViewHolderDataSchema() {}
    }
}
