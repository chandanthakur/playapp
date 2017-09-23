package com.chthakur.playapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.chthakur.playapp.Logger.ALog;

import java.util.Locale;

// stores and recycles views as they are scrolled off screen
public class CallRemoteMemberViewHolder extends ViewHolderBase implements View.OnClickListener {

    private static final String TAG = "M2CALL";

    private CallRemoteMemberView mCallRemoteMemberView;
    private static final String LOGGER_PREFIX = CallRemoteMemberViewHolder.class.getSimpleName() + ":";

    public CallRemoteMemberViewHolder(Context context, View parent, View itemView) {
        super(context, parent, itemView);
        mCallRemoteMemberView = (CallRemoteMemberView) itemView.findViewById(R.id.call_member_view);
        itemView.setOnClickListener(this);
    }

    public static ViewHolderBase createViewHolder(Context context, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.call_remote_member_view_holder, parent, false);
        CallRemoteMemberViewHolder viewHolder = new CallRemoteMemberViewHolder(context, parent, view);
        return viewHolder;
    }

    @Override
    public void bindViewHolder(ViewHolderBaseSchema dataSchema, int totalItems) {
        super.bindViewHolder(dataSchema, totalItems);
        CallRemoteMemberViewHolder holder = this;
        ViewHolderDataSchema holderData = (ViewHolderDataSchema)dataSchema;
        ViewGroup.LayoutParams layoutParams = holder.itemView.getLayoutParams();
        layoutParams.height = dataSchema.itemSize.height;
        layoutParams.width = dataSchema.itemSize.width;
        itemView.setLayoutParams(layoutParams);
        ALog.i(TAG, LOGGER_PREFIX + String.format(Locale.US, "bindViewHolder: force size %dx%d", layoutParams.width, layoutParams.height));
        mCallRemoteMemberView.setVideoId(holderData.videoId);
        //mCallRemoteMemberView.setVm(holderData.vmCallMember);
    }

    static public class ViewHolderDataSchema extends ViewHolderBaseSchema {
        public String getType() {
            return CallRemoteMemberViewHolder.class.getSimpleName();
        }

        public ViewHolderDataSchema() {}
    }
}
