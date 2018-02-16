package com.chthakur.playapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.chthakur.playapp.Logger.ALog;

import java.util.Locale;

// stores and recycles views as they are scrolled off screen
public class CallMemberRemoteGridViewHolder extends CallGridViewHolderBase implements View.OnClickListener {

    private static final String TAG = "M2CALL";

    private CallRemoteMemberView mCallRemoteMemberView;
    private static final String LOGGER_PREFIX = CallMemberRemoteGridViewHolder.class.getSimpleName() + ":";

    public CallMemberRemoteGridViewHolder(Context context, View parent, View itemView) {
        super(context, parent, itemView);
        mCallRemoteMemberView = (CallRemoteMemberView) itemView.findViewById(R.id.call_member_view);
        itemView.setOnClickListener(this);
    }

    public static CallGridViewHolderBase createViewHolder(Context context, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.call_remote_member_view_holder, parent, false);
        CallMemberRemoteGridViewHolder viewHolder = new CallMemberRemoteGridViewHolder(context, parent, view);
        return viewHolder;
    }

    @Override
    public void bindViewHolder(CallGridViewHolderSchema dataSchema) {
        super.bindViewHolder(dataSchema);
        CallMemberRemoteGridViewHolder holder = this;
        CallGridViewHolderDataSchema holderData = (CallGridViewHolderDataSchema)dataSchema;
        ViewGroup.LayoutParams layoutParams = holder.itemView.getLayoutParams();
        layoutParams.height = dataSchema.itemSize.height;
        layoutParams.width = dataSchema.itemSize.width;
        itemView.setLayoutParams(layoutParams);
        ALog.i(TAG, LOGGER_PREFIX + String.format(Locale.US, "bindViewHolder: force size %dx%d", layoutParams.width, layoutParams.height));
        mCallRemoteMemberView.setVideoId(holderData.videoId);
        //mCallRemoteMemberView.setVm(holderData.vmCallMember);
    }

    static public class CallGridViewHolderDataSchema extends CallGridViewHolderSchema {
        public String getType() {
            return CallMemberRemoteGridViewHolder.class.getSimpleName();
        }

        public CallGridViewHolderDataSchema() {}
    }
}
