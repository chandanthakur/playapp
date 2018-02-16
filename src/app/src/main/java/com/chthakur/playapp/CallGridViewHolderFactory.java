package com.chthakur.playapp;

import android.content.Context;
import android.view.ViewGroup;

import java.util.HashMap;

public class CallGridViewHolderFactory {
    final static int LocalCameraViewHolderViewType = 502;
    final static int RemoteMemberViewHolderViewType = 503;

    private static HashMap<String, Integer> mViewTypeMap = new HashMap<String, Integer>() {
        {
            put(CallMemberLocalGridViewHolder.class.getSimpleName(), LocalCameraViewHolderViewType);
            put(CallMemberRemoteGridViewHolder.class.getSimpleName(), RemoteMemberViewHolderViewType);
        }};

    static public CallGridViewHolderBase create(Context context, ViewGroup parent, int viewType) {
        CallGridViewHolderBase viewHolder = null;
        switch (viewType) {
            case LocalCameraViewHolderViewType:
                viewHolder = CallMemberLocalGridViewHolder.createViewHolder(context, parent);
                break;
            case RemoteMemberViewHolderViewType:
                viewHolder = CallMemberRemoteGridViewHolder.createViewHolder(context, parent);
                break;
            default:
                break;
        }

        return viewHolder;
    }

    static public int getViewType(String viewTypeStr){
        if(mViewTypeMap.containsKey(viewTypeStr)) {
            return mViewTypeMap.get(viewTypeStr);
        }

        return -1;
    }
}
