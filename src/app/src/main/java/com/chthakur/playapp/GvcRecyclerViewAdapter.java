package com.chthakur.playapp;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.zip.Inflater;

public class GvcRecyclerViewAdapter extends RecyclerView.Adapter<ViewHolderBase> {

    private List<ViewHolderBase.ViewHolderBaseSchema> mData = new ArrayList<>();
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private Context context;

    // data is passed into the constructor
    public GvcRecyclerViewAdapter(Context context, List<ViewHolderBase.ViewHolderBaseSchema> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        this.context = context;
    }

    @Override
    public int getItemViewType(int position){
        // return type based on view in data
        ViewHolderBase.ViewHolderBaseSchema holderData = (ViewHolderBase.ViewHolderBaseSchema)mData.get(position);
        int viewType = ViewHolderFactory.getViewType(holderData.getType());
        return viewType;
    }

    // inflates the cell layout from xml when needed
    @Override
    public ViewHolderBase onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewHolderBase viewHolder = ViewHolderFactory.create(context, parent, viewType);
        return viewHolder;
    }

    // binds the data to the textview in each cell
    @Override
    public void onBindViewHolder(ViewHolderBase holder, int position) {
        ViewHolderBase.ViewHolderBaseSchema dataSchema = mData.get(position);
        holder.setOnItemClickListener(this.mClickListener);
        holder.bindViewHolder(dataSchema, mData.size());
    }

    // total number of cells
    @Override
    public int getItemCount() {
        return mData.size();
    }

    public boolean removeAtIndex(int position) {
        if(position < mData.size()) {
            mData.remove(position);
            //notifyItemRemoved(position);
            notifyDataSetChanged();
        }

        return true;
    }

    public boolean addAtIndex(int position, ViewHolderBase.ViewHolderBaseSchema schema) {
        if(position < mData.size()) {
            mData.add(position, schema);
            //notifyItemInserted(position);
            notifyDataSetChanged();
        }

        return true;
    }

    // convenience method for getting data at click position
    public ViewHolderBase.ViewHolderBaseSchema getItem(int id) {
        return mData.get(id);
    }

    // allows clicks events to be caught
    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

    static class ViewHolderFactory {
        public final static int MainItemViewHolderViewType = 500;
        public final static int ThumbItemViewHolderViewType = 501;
        public final static int LocalCameraViewHolderViewType = 502;
        public final static int RemoteCameraViewHolderViewType = 503;

        private static HashMap<String, Integer> mViewTypeMap = new HashMap<String, Integer>() {
            {
                put(MainItemViewHolder.class.getSimpleName(), MainItemViewHolderViewType);
                put(CallLocalMemberViewHolder.class.getSimpleName(), LocalCameraViewHolderViewType);
                put(CallRemoteMemberViewHolder.class.getSimpleName(), RemoteCameraViewHolderViewType);
                put(ThumbItemViewHolder.class.getSimpleName(), ThumbItemViewHolderViewType);
            }};

        static public ViewHolderBase create(Context context, ViewGroup parent, int viewType) {
            ViewHolderBase viewHolder = null;
            switch (viewType) {
                case MainItemViewHolderViewType:
                    viewHolder = MainItemViewHolder.createViewHolder(context, parent);
                    break;
                case ThumbItemViewHolderViewType:
                    viewHolder = ThumbItemViewHolder.createViewHolder(context, parent);
                    break;
                case LocalCameraViewHolderViewType:
                    viewHolder = CallLocalMemberViewHolder.createViewHolder(context, parent);
                    break;
                case RemoteCameraViewHolderViewType:
                    viewHolder = CallRemoteMemberViewHolder.createViewHolder(context, parent);
                    break;
            }

            return viewHolder;
        }

        static public int getViewType(String viewTypeStr){
            // return type based on view in data
            if(mViewTypeMap.containsKey(viewTypeStr)) {
                return mViewTypeMap.get(viewTypeStr);
            }

            return -1;
        }
    }
}
