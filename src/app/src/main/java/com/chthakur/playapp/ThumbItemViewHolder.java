package com.chthakur.playapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.util.HashMap;

// stores and recycles views as they are scrolled off screen
public class ThumbItemViewHolder extends ViewHolderBase implements View.OnClickListener {
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

    public ThumbItemViewHolder(Context context, View parent, View itemView) {
        super(context, parent, itemView);
        mImageView = (ImageView) itemView.findViewById(R.id.image_view);
        mParent = parent;
        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        //if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
    }

    public static ViewHolderBase createViewHolder(Context context, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.recyclerview_item, parent, false);
        ThumbItemViewHolder viewHolder = new ThumbItemViewHolder(context, parent, view);
        return viewHolder;
    }

    @Override
    public void bindViewHolder(ViewHolderBaseSchema dataSchema, int totalItems) {
        super.bindViewHolder(dataSchema, totalItems);
        ThumbItemViewHolder holder = this;
        ViewHolderDataSchema holderDataSchema = (ViewHolderDataSchema)dataSchema;
        holder.itemView.getLayoutParams().height = holderDataSchema.itemSize.height;
        holder.itemView.getLayoutParams().width = holderDataSchema.itemSize.width;
        Glide.with(context).load(holderDataSchema.imageUri).into(mImageView);
    }

    static public class ViewHolderDataSchema extends ViewHolderBaseSchema {
        public String getType() {
            return ThumbItemViewHolder.class.getSimpleName();
        }

        public String text;

        public String imageUri;

        public int color;

        public ViewHolderDataSchema() {}
    }
}
