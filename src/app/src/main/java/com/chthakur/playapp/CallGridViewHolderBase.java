package com.chthakur.playapp;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.chthakur.playapp.Logger.ALog;

import java.util.Locale;

// stores and recycles views as they are scrolled off screen
public class CallGridViewHolderBase extends RecyclerView.ViewHolder implements View.OnClickListener {
    private static final String TAG = CallGridViewHolderBase.class.getSimpleName();

    private static final String LOGGER_PREFIX = CallGridViewHolderBase.class.getSimpleName();

    public View mParent;

    public Context context;

    private CallGridViewHolderSchema data;

    private GvcRecyclerViewAdapter.ItemClickListener onItemClickListener;

    public CallGridViewHolderBase(Context context, View parent, View itemView) {
        super(itemView);
        this.context = context;
        mParent = parent;
        itemView.setOnClickListener(this);
    }

    void setOnItemClickListener(GvcRecyclerViewAdapter.ItemClickListener clickListener) {
        onItemClickListener = clickListener;
    }

    @Override
    public void onClick(View view) {
        if(onItemClickListener != null) {
            onItemClickListener.onItemClick(view, -1);
        }
        //if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
    }

    public void bindViewHolder(CallGridViewHolderSchema schema) {
        this.data = schema;

    }

    public void refreshLayout(int w, int h) {
        CallGridViewHolderSchema holderData = data;
        ViewGroup.LayoutParams layoutParams = itemView.getLayoutParams();
        layoutParams.height = holderData.itemSize.height;
        layoutParams.width = holderData.itemSize.width;
        itemView.setLayoutParams(layoutParams);
        ALog.i(TAG, LOGGER_PREFIX + String.format(Locale.US, "refreshLayout: force size %dx%d", layoutParams.width, layoutParams.height));
    }

    public void destroyView() {
        ALog.i(TAG, LOGGER_PREFIX + ":destroyView");
    }

    public CallGridViewHolderSchema getData() {
        return this.data;
    }
}
