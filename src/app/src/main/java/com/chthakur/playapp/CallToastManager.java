package com.chthakur.playapp;

import android.app.Activity;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.databinding.Observable;
import android.databinding.ObservableField;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.Toast;

import com.chthakur.playapp.Logger.ALog;
import com.chthakur.playapp.databinding.CallMemberToastLayoutBinding;

import java.util.LinkedList;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.subjects.PublishSubject;

public class CallToastManager {

    private static final String TAG = "M2CALL";

    private static final String LOG_PREFIX = CallToastManager.class.getSimpleName() + ":";

    private static final int TICK_INTERVAL = 1000;
    Context context;

    LinkedList<VmCallToast> toastQueue = new LinkedList<>();

    Subscription toastWorkerSubscription;

    Toast lastToast;

    public CallToastManager(Context context) {
        this.context = context;
        scheduleWorker();
    }

    public void showToast(String id, ObservableField<String> contact, ObservableField<String> title, boolean isJoining) {
        if(!isJoining && deletePairIfRequired(id)) {
            return;
        }

        VmCallToast vmCallToast = new VmCallToast(this.context, id, contact, title, isJoining);
        toastQueue.add(vmCallToast);
    }

    /**
     * lets not show the previous added one and skip adding this one as well.
     * @param id id of the member to be removed
     * @return if delete was successful
     */
    private boolean deletePairIfRequired(String id) {
        for(VmCallToast vmCallToast: this.toastQueue) {
            if(vmCallToast.id.equals(id) && vmCallToast.getIsJoining()) {

                ALog.i(TAG, LOG_PREFIX + "Pair removed:" + id);
                toastQueue.remove(vmCallToast);
                return true;
            }
        }

        return false;
    }

    public void scheduleWorker() {
        onSchedulerTick();
        toastWorkerSubscription = rx.Observable.interval(TICK_INTERVAL, TimeUnit.MILLISECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<Long>() {
            @Override
            public void call(Long aLong) {
                onSchedulerTick();
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                ALog.i(TAG, LOG_PREFIX, throwable);
            }
        });
    }

    public void onSchedulerTick() {
        VmCallToast callToast = getNextVmToastFromQueue();
        if(callToast != null && lastToast != null) {
            lastToast.cancel();
            lastToast = showToastImpl(callToast);
        } else if(callToast != null) {
            lastToast = showToastImpl(callToast);
        }
    }

    private VmCallToast getNextVmToastFromQueue() {
        while(toastQueue.size() > 0) {
            VmCallToast newToast = toastQueue.remove();
            if(newToast != null && newToast.isValid()) {
                return newToast;
            }

            ALog.i(TAG, LOG_PREFIX + newToast.getId() + ":Skipping, stale, " + newToast.howOld() + " ms");
        }

        return null;
    }

    public Toast showToastImpl(VmCallToast vmCallToast) {
        ALog.i(TAG, LOG_PREFIX + "showToastImpl:" + vmCallToast.getId());
        LayoutInflater inflater = ((Activity)context).getLayoutInflater();
        CallMemberToastLayoutBinding layoutBinding = DataBindingUtil.inflate(inflater, R.layout.call_member_toast_layout, null, false);
        layoutBinding.setVm(vmCallToast);
        Toast toast = new Toast(context);
        int deviceHeight = UtilsDevice.getDisplayHeight(context);
        toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, deviceHeight/4);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layoutBinding.getRoot());
        toast.show();
        return toast;
    }

    public void release() {
        if(toastWorkerSubscription != null) {
            toastWorkerSubscription.unsubscribe();
        }

        this.toastQueue.clear();
    }

    public static class VmCallToast {

        Context context;

        String id;

        boolean isJoining;

        Long timestamp;

        ObservableField<String> contact;

        ObservableField<String> toastTitle = new ObservableField<String>();

        public VmCallToast(Context ctx, String id, ObservableField<String> contact, ObservableField<String> contactTitle, final boolean isJoining) {
            this.id = id;
            this.contact = contact;
            this.context = ctx;
            this.isJoining = isJoining;
            this.timestamp = System.currentTimeMillis();
            setTitle(contactTitle.get(), isJoining);
        }

        public ObservableField<String> getContact() {
            return contact;
        }

        public ObservableField<String> getTitle() {
            return toastTitle;
        }

        private void setTitle(String contactName, boolean isJoining) {
            String formatString;
            if(isJoining) {
                formatString = this.context.getString(R.string.call_member_joined);
            } else {
                formatString = this.context.getString(R.string.call_member_left);
            }

            String title = String.format(Locale.getDefault(), formatString, contactName);
            toastTitle.set(title);
        }

        public String getId() {
            return id;
        }

        public boolean getIsJoining() {
            return isJoining;
        }

        public boolean isValid() {
            Long timeSinceCreate = System.currentTimeMillis() - timestamp;
            if(timeSinceCreate < 5000) {
                return true;
            }

            return false;
        }

        public long howOld() {
            Long timeSinceCreate = System.currentTimeMillis() - timestamp;
            return timeSinceCreate;
        }

        public void release() {
        }
    }
}
