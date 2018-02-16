package com.chthakur.playapp;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.chthakur.playapp.Logger.ALog;
import java.util.Locale;

// stores and recycles views as they are scrolled off screen
public class CallMemberLocalGridViewHolder extends CallGridViewHolderBase implements View.OnClickListener {

    private static final String TAG = "M2CALL";

    private static final String LOGGER_PREFIX = CallMemberLocalGridViewHolder.class.getSimpleName() + ":";

    private CallLocalMemberView mCallLocalMemberView;


    public CallMemberLocalGridViewHolder(Context context, View parent, View itemView) {
        super(context, parent, itemView);
        mCallLocalMemberView = (CallLocalMemberView) itemView.findViewById(R.id.call_member_view);

        itemView.setOnClickListener(this);
    }

    public static CallGridViewHolderBase createViewHolder(Context context, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.call_local_member_view_holder, parent, false);
        CallMemberLocalGridViewHolder viewHolder = new CallMemberLocalGridViewHolder(context, parent, view);
        return viewHolder;
    }

    @Override
    public void bindViewHolder(CallGridViewHolderSchema dataSchema) {
        super.bindViewHolder(dataSchema);
        CallMemberLocalGridViewHolder holder = this;
        CallGridViewHolderDataSchema holderData = (CallGridViewHolderDataSchema)dataSchema;
        ViewGroup.LayoutParams layoutParams = holder.itemView.getLayoutParams();
        layoutParams.height = dataSchema.itemSize.height;
        layoutParams.width = dataSchema.itemSize.width;
        itemView.setLayoutParams(layoutParams);
        mCallLocalMemberView.setVideoId(holderData.videoId);
        ALog.i(TAG, LOGGER_PREFIX + String.format(Locale.US, "bindViewHolder: force size %dx%d", layoutParams.width, layoutParams.height));
    }

    @Override
    public void refreshLayout(int w, int h) {
        super.refreshLayout(w, h);
    }

    @Override
    public void destroyView() {
        mCallLocalMemberView.destroyDrawingCache();
        super.destroyView();
    }

    static public int getDisplayWidth(Context context){
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager()
                .getDefaultDisplay()
                .getMetrics(displayMetrics);
        return displayMetrics.widthPixels;
    }

    static public class CallGridViewHolderDataSchema extends CallGridViewHolderSchema {
        public String getType() {
            return CallMemberLocalGridViewHolder.class.getSimpleName();
        }

        public CallGridViewHolderDataSchema() {}
    }
}
