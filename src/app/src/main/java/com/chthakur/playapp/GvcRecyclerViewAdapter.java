package com.chthakur.playapp;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GvcRecyclerViewAdapter extends RecyclerView.Adapter<CallGridViewHolderBase> {

    private List<CallGridViewHolderSchema> mData = new ArrayList<>();
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private Context context;

    // data is passed into the constructor
    public GvcRecyclerViewAdapter(Context context, List<CallGridViewHolderSchema> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        this.context = context;
    }

    @Override
    public int getItemViewType(int position){
        // return type based on view in data
        CallGridViewHolderSchema holderData = (CallGridViewHolderSchema)mData.get(position);
        int viewType = ViewHolderFactory.getViewType(holderData.getType());
        return viewType;
    }

    // inflates the cell layout from xml when needed
    @Override
    public CallGridViewHolderBase onCreateViewHolder(ViewGroup parent, int viewType) {
        CallGridViewHolderBase viewHolder = ViewHolderFactory.create(context, parent, viewType);
        return viewHolder;
    }

    // binds the data to the textview in each cell
    @Override
    public void onBindViewHolder(CallGridViewHolderBase holder, int position) {
        CallGridViewHolderSchema dataSchema = mData.get(position);
        holder.setOnItemClickListener(this.mClickListener);
        holder.bindViewHolder(dataSchema);
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

    public boolean addAtIndex(int position, CallGridViewHolderSchema schema) {
        if(position < mData.size()) {
            mData.add(position, schema);
            //notifyItemInserted(position);
            notifyDataSetChanged();
        }

        return true;
    }

    // convenience method for getting data at click position
    public CallGridViewHolderSchema getItem(int id) {
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
                put(MainItemCallGridViewHolder.class.getSimpleName(), MainItemViewHolderViewType);
                put(CallMemberLocalGridViewHolder.class.getSimpleName(), LocalCameraViewHolderViewType);
                put(CallMemberRemoteGridViewHolder.class.getSimpleName(), RemoteCameraViewHolderViewType);
                put(ThumbItemCallGridViewHolder.class.getSimpleName(), ThumbItemViewHolderViewType);
            }};

        static public CallGridViewHolderBase create(Context context, ViewGroup parent, int viewType) {
            CallGridViewHolderBase viewHolder = null;
            switch (viewType) {
                case MainItemViewHolderViewType:
                    viewHolder = MainItemCallGridViewHolder.createViewHolder(context, parent);
                    break;
                case ThumbItemViewHolderViewType:
                    viewHolder = ThumbItemCallGridViewHolder.createViewHolder(context, parent);
                    break;
                case LocalCameraViewHolderViewType:
                    viewHolder = CallMemberLocalGridViewHolder.createViewHolder(context, parent);
                    break;
                case RemoteCameraViewHolderViewType:
                    viewHolder = CallMemberRemoteGridViewHolder.createViewHolder(context, parent);
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
