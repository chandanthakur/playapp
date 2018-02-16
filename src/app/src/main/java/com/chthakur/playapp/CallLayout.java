package com.chthakur.playapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;

import com.chthakur.playapp.Logger.ALog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

public class CallLayout extends FrameLayout implements View.OnTouchListener{
    private static final String TAG = CallLayout.class.getSimpleName();

    private CallAbsoluteGridLayout callAbsoluteGridLayout;
    private CallAbsoluteGridLayout.LayoutType currentLayout = CallAbsoluteGridLayout.LayoutType.BIG_SINGLE;
    private List<CallAbsoluteGridLayout.LayoutType> layoutTypeList;
    private final Map<CallGridViewHolderSchema, FrameLayout> dataToFrameMap = new HashMap<>();
    private final Map<FrameLayout, CallGridViewHolderBase> frameToViewHolderMap = new HashMap<>();
    private final List<OnTouchListener> onTouchListenerList = new ArrayList<>();
    private final List<CallGridViewHolderSchema> coreDataList = new ArrayList<>();

    public CallLayout(Context context) {
        this(context, null, 0);
    }

    public CallLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CallLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.gvc_grid_custom_layout, this);
        callAbsoluteGridLayout = (CallAbsoluteGridLayout)view.findViewById(R.id.gvc_grid_wrap);
        callAbsoluteGridLayout.setLayout(currentLayout);
        layoutTypeList = CallAbsoluteGridLayout.LayoutType.getOrderedValidValuesList();
    }

    /**
     * Respond to changes in height, control refresh to both core grid layout + to all the call members
     * Much needed in devices such as samsung galaxy s8+, the view parent size varies according to the bottom
     * navigation
     * @param changed if the layout is dirty
     */
    @Override
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        boolean isDirty = callAbsoluteGridLayout.setLayoutRect(left, top, right, bottom);
        // All the required changes are made to the frames, we need to pull those and push to
        // individual views
        if(isDirty) {
            syncFrameDimensionsToItemData();
        }
    }

    /**
     * Exposing listening to touch events for this view
     * @param touchListener registered touch listener
     */
    @Override
    public void setOnTouchListener(OnTouchListener touchListener) {
        onTouchListenerList.add(touchListener);
    }

    /**
     * Listen to touch events and send them to all the registered touch listeners
     */
    @SuppressLint("ClickableViewAccessibility") // Lint is confused since we are forwarding the events to other views
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        for(OnTouchListener touchListener: onTouchListenerList) {
            touchListener.onTouch(v, event);
        }

        return true;
    }

    /**
     * Add a item to the grid
     * @param itemData data schema for the item to be added
     */
    public void addItem(CallGridViewHolderSchema itemData) {
        addItem(itemData, coreDataList.size());
    }

    /**
     * Add item at a particular position in the grid
     * @param itemData data schema for the item to be added
     * @param position position where we need to add the item
     */
    public void addItem(CallGridViewHolderSchema itemData, int position) {
        if (itemData == null || position > coreDataList.size()) {
            return;
        }

        CallAbsoluteGridItemLayout frameChildLayout = callAbsoluteGridLayout.addFrame(position);
        if(frameChildLayout == null) {
            return;
        }

        coreDataList.add(position, itemData);
        syncFrameDimensionsToItemData();
        addDataItemToUI(frameChildLayout, itemData);
        Rect rect = frameChildLayout.getRect();
        ALog.i(TAG, "addItem:" + itemData.id + ":" + rect.width() + "x" + rect.height());
        frameChildLayout.setOnClickListener(onFrameClickListener);
        frameChildLayout.setOnTouchListener(onFrameTouchListener);
        frameChildLayout.setOnDoubleTapListener(onDoubleTapListener);
    }

    /**
     * Click handler for the frame which holds the particular view item in the grid
     */
    private OnClickListener onFrameClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            CallAbsoluteGridItemLayout frame = (CallAbsoluteGridItemLayout)v;
            if(callAbsoluteGridLayout.isThumbFrame(frame)) {
                onThumbClick(frame);
            }
        }
    };

    private CallAbsoluteGridItemLayout.OnDoubleTapListener onDoubleTapListener = new CallAbsoluteGridItemLayout.OnDoubleTapListener() {
        @Override
        public void onDoubleTap(View view, MotionEvent motionEvent) {
            CallAbsoluteGridItemLayout frame = (CallAbsoluteGridItemLayout)view;
            if(!callAbsoluteGridLayout.isThumbFrame(frame)) {
                onMainFrameClick(frame);
            }
        }
    };

    /**
     * Touch listener for the frame which holds the particular view item in the grid
     */
    private OnTouchListener onFrameTouchListener = new OnTouchListener() {
        @Override
        @SuppressLint("ClickableViewAccessibility") // Lint is confused since we are forwarding the events to parent view.
        public boolean onTouch(View v, MotionEvent event) {
            CallAbsoluteGridItemLayout frame = (CallAbsoluteGridItemLayout)v;
            if(!callAbsoluteGridLayout.isThumbFrame(frame)) {
                CallLayout.this.onTouch(v, event);
            }

            return false;
        }
    };

    /**
     * Switch to next available layout.
     * @return the layout to which the grid was switched to
     */
    private CallAbsoluteGridLayout.LayoutType getNextLayoutType() {
        int index = layoutTypeList.indexOf(currentLayout);
        index = index + 1;
        index = index >= layoutTypeList.size() ? 0 : index;
        return layoutTypeList.get(index);
    }


    /**
     * Sync frame data to core data list
     */
    private void syncFrameDimensionsToItemData() {
        syncFrameDimensionsToItemData(this.coreDataList);
    }

    /**
     * Dimensions are first reflected to Grid frame items in {@link CallAbsoluteGridLayout} and then those
     * need to be reflected back to item data. Individual items are then refreshed giving them oppurtunity
     * to refresh their layout if it is dimensions dependent
     */
    private void syncFrameDimensionsToItemData(List<CallGridViewHolderSchema> dataList) {
        for(CallGridViewHolderSchema schema: dataList) {
            if(dataToFrameMap.containsKey(schema)) {
                CallAbsoluteGridItemLayout frameLayout = (CallAbsoluteGridItemLayout) dataToFrameMap.get(schema);
                Rect rect = frameLayout.getRect();
                boolean isThumb = callAbsoluteGridLayout.isThumbSize(rect);
                schema.itemSize = new ItemSize(rect.width(), rect.height(), isThumb);
                CallGridViewHolderBase callGridViewHolderBase = frameToViewHolderMap.get(frameLayout);
                callGridViewHolderBase.refreshLayout(schema.itemSize.width, schema.itemSize.height);
            }
        }
    }

    /**
     * Remove item given the item id
     * @param id id of the item to be removed
     */
    public boolean removeItem(String id) {
        ALog.i(TAG, "remove grid item:" + id);
        CallGridViewHolderSchema data = getDataForId(id);
        if(data == null) {
            return false;
        }

        CallAbsoluteGridItemLayout frame = getFrameForId(id);
        CallGridViewHolderBase callGridViewHolderBase = frameToViewHolderMap.get(frame);
        callGridViewHolderBase.destroyView();
        this.coreDataList.remove(data);
        frameToViewHolderMap.remove(frame);
        dataToFrameMap.remove(data);
        callAbsoluteGridLayout.removeItem(frame);
        syncFrameDimensionsToItemData();
        return true;
    }

    /**
     * We maintain a map from data to frame, we extract the item data from item id and then using itemdata
     * we get the frame from the map
     * @param id id data to which this frame belongs
     * @return the Frame corresponding to this particular id/data
     */
    public CallAbsoluteGridItemLayout getFrameForId(String id) {
        CallGridViewHolderSchema viewData = getDataForId(id);
        if(viewData == null) {
            return null;
        }

        CallAbsoluteGridItemLayout frame = (CallAbsoluteGridItemLayout) dataToFrameMap.get(viewData);
        return frame;
    }

    /**
     * Get the data from the given id, we maintain the internal item data list, search the items
     * and return the data fro the given Id
     * @param id id of the item for which we need to return the data
     * @return Data for this particular id
     */
    public CallGridViewHolderSchema getDataForId(String id) {
        int index = getIndexForIdFromCoreList(coreDataList, id);
        if(index == -1) {
            return null;
        }

        CallGridViewHolderSchema viewData = coreDataList.get(index);
        return viewData;
    }

    /**
     * Return index of the item given the item Id
     * @param list list to search the data item
     * @param id identity of the item to search
     * @return The found index or -1
     */
    public int getIndexForIdFromCoreList(List<CallGridViewHolderSchema> list, String id) {
        int index = 0;
        for(CallGridViewHolderSchema schema: coreDataList) {
            if(schema.id.equals(id)) {
                return index;
            }

            index++;
        }

        return -1;
    }

    /**
     * Check if a particular item with id exists in our data list
     * @param id id of the item to find
     * @return if or not the item exists
     */
    public boolean hasItemWithId(String id) {
        int index = getIndexForId(id);
        return index != -1;
    }

    /**
     * Index in data list given the id
     * @param id id of the item to find index for
     * @return index of the item found
     */
    public int getIndexForId(String id) {
        int index = getIndexForIdFromCoreList(this.coreDataList, id);
        return index;
    }

    /**
     * Show thumbnails, delegate to {@link CallAbsoluteGridLayout}
     */
    public void showThumbs() {
        callAbsoluteGridLayout.showThumbs();
    }


    /**
     * Hide thumbnails, delegate to {@link CallAbsoluteGridLayout}
     */
    public void hideThumbs() {
        callAbsoluteGridLayout.hideThumbs();
    }

    /**
     * Define the behavior on thumb-click. We need to swap the thumb with one of the main items
     * based on LRU scheme
     * @param thumbFrame the clicked thumb frame
     */
    private void onThumbClick(CallAbsoluteGridItemLayout thumbFrame) {
        if(!frameToViewHolderMap.containsKey(thumbFrame)) {
            return;
        }

        CallAbsoluteGridItemLayout mainFrame = callAbsoluteGridLayout.getLRUMainFrame();
        if(mainFrame == null) {
            return;
        }

        callAbsoluteGridLayout.swap(mainFrame, thumbFrame);
        CallGridViewHolderSchema thumbFrameData = frameToViewHolderMap.get(thumbFrame).getData();
        CallGridViewHolderSchema mainFrameData = frameToViewHolderMap.get(mainFrame).getData();
        swapInCoreDataList(thumbFrameData, mainFrameData);
        List<CallGridViewHolderSchema> syncList = new ArrayList<>();
        syncList.add(thumbFrameData);
        syncList.add(mainFrameData);
        syncFrameDimensionsToItemData(syncList);
    }

    private void onMainFrameClick(CallAbsoluteGridItemLayout mainFrame) {
        if(!frameToViewHolderMap.containsKey(mainFrame)) {
            return;
        }

        // ignore if size is too less for action here
        if(coreDataList.size() <= 1) {
            return;
        }

        if(currentLayout == CallAbsoluteGridLayout.LayoutType.BIG_SINGLE) {
            toggleGrid();
            return;
        }

        // we move the clicked frame to first position and then toggle
        CallAbsoluteGridItemLayout firstFrame = callAbsoluteGridLayout.getFirstMainFrame();
        if(firstFrame == mainFrame) {
            toggleGrid();
            return;
        }

        // Toggle grid will re-layout and sync to core data
        CallGridViewHolderSchema mainFrameData = frameToViewHolderMap.get(mainFrame).getData();
        CallGridViewHolderSchema firstFrameData = frameToViewHolderMap.get(firstFrame).getData();
        callAbsoluteGridLayout.swap(firstFrame, mainFrame);
        swapInCoreDataList(firstFrameData, mainFrameData);
        toggleGrid();
    }

    /**
     * Swap two data items in core data list
     * @param data1 first item data to swap
     * @param data2 second data item to swap
     */
    private void swapInCoreDataList(CallGridViewHolderSchema data1, CallGridViewHolderSchema data2) {
        int indexData1 = coreDataList.indexOf(data1);
        int indexData2 = coreDataList.indexOf(data2);
        if(indexData1 == -1 || indexData2 == -1) {
            return;
        }

        Collections.swap(coreDataList, indexData1, indexData2);
    }

    /**
     * Add data item to the UI, this includes attaching the item to the parent frame designated by {@link CallAbsoluteGridLayout}
     * @param parentRoot the parent frame
     * @param itemData itemData to construct the inner view holder item
     */
    private void addDataItemToUI(CallAbsoluteGridItemLayout parentRoot, CallGridViewHolderSchema itemData) {
        int viewType = CallGridViewHolderFactory.getViewType(itemData.getType());
        CallGridViewHolderBase viewHolder = CallGridViewHolderFactory.create(getContext(), parentRoot, viewType);
        Rect rect = parentRoot.getRect();
        itemData.itemSize = new ItemSize(rect.width(), rect.height(), callAbsoluteGridLayout.isThumbFrame(parentRoot));
        viewHolder.bindViewHolder(itemData);
        frameToViewHolderMap.put(parentRoot, viewHolder);
        dataToFrameMap.put(itemData, parentRoot);
        parentRoot.addView(viewHolder.itemView);
    }

    /**
     * Set the default layout for the grid
     * @param value value to set for default Layout
     */
    public void enableDefaultLayout(CallAbsoluteGridLayout.LayoutType value) {
        currentLayout = value;
        // see if force refreshLayout is needed. Used in init as of now
        callAbsoluteGridLayout.setLayout(currentLayout);
    }

    /**
     * Check if default mode is Grid
     * @return true if it is Grid mode
     */
    public boolean isGridModeByDefault() {
        return currentLayout == CallAbsoluteGridLayout.LayoutType.GRID_FOUR;
    }

    /**
     * Set thumbnail density per width on screen. Defines how many thumbnails to accommodate on screen
     * @param value the value to set
     */
    public void setThumbnailDensity(int value) {
        callAbsoluteGridLayout.setThumbnailDensity(value);
    }

    /**
     * Toggle grid layout based on values in the layout
     * @return the layout after toggle completes
     */
    public CallAbsoluteGridLayout.LayoutType toggleGrid() {
        currentLayout = getNextLayoutType();
        callAbsoluteGridLayout.refreshLayout(currentLayout);
        syncFrameDimensionsToItemData();
        return currentLayout;
    }


    /**
     * Release all the items by calling destroy method.
     * Release any of events/subscriptions
     */
    public void release() {
        for(CallGridViewHolderSchema schema: this.coreDataList) {
            CallAbsoluteGridItemLayout frame = (CallAbsoluteGridItemLayout)dataToFrameMap.get(schema);
            CallGridViewHolderBase callGridViewHolderBase = frameToViewHolderMap.get(frame);
            callGridViewHolderBase.destroyView();
        }
    }

    /**
     * Item size for the item
     */
    static public class ItemSize {
        public ItemSize(int width, int height, boolean isThumb) {
            this.width = width;
            this.height = height;
            this.isThumb = isThumb;
        }

        public int width;

        public int height;

        boolean isThumb;
    }

    static boolean once = true;
    private void scheduleUpdateSimulation(final List<CallGridViewHolderSchema> dataList) {
        Observable.interval(5000, TimeUnit.MILLISECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<Long>() {
            @Override
            public void call(Long aLong) {
                int eventIdx = randInt(0, 3);
                if (eventIdx == 0) { // REMOVE
                    if (!simulateRemove(dataList)) {
                        simulateAdd(dataList);
                    }
                } else if (eventIdx == 1) { // ADD
                    simulateAdd(dataList);
                } else if (eventIdx == 2) { //TOGGLE GRID
                    toggleGrid();
                } else if(eventIdx == 3) {

                }
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                Log.i(TAG, "scheduleUpdateSimulation:Error:",throwable);
            }
        });
    }

    private boolean simulateRemove(final List<CallGridViewHolderSchema> dataList) {
        if(dataList.size() > 0) {
            int idx = randInt(0, dataList.size() - 1);
            CallGridViewHolderSchema data = dataList.remove(idx);
            CallLayout.this.removeItem(data.id);
            return true;
        }

        return false;
    }

    private void simulateAdd(final List<CallGridViewHolderSchema> dataList) {
        int idx = randInt(2500, 5000);
        CallGridViewHolderSchema baseSchema;
        if(idx < 3000) {
            baseSchema = getCallLocalMemberData(idx);
        } else {
            baseSchema = getRemoteCallMemberData(idx);
        }

        dataList.add(baseSchema);
        this.addItem(baseSchema);
    }

    public static Random random = new Random();
    static public CallGridViewHolderSchema getCallLocalMemberData(int kk) {
        CallGridViewHolderSchema element = new CallMemberLocalGridViewHolder.CallGridViewHolderDataSchema();
        element.id = String.valueOf(random.nextInt());
        element.videoId = kk;
        return element;
    }

    static public CallGridViewHolderSchema getRemoteCallMemberData(int kk) {
        CallGridViewHolderSchema element = new CallMemberRemoteGridViewHolder.CallGridViewHolderDataSchema();
        element.id = String.valueOf(random.nextInt());
        element.videoId = kk;
        return element;
    }

    public void scheduleRun() {
        List<CallGridViewHolderSchema> dataList = new ArrayList<>();
        for (int kk = 0; kk < 12 ; kk++) {
            dataList.add(getRemoteCallMemberData(2500 + kk));
        }

        dataList.add(getCallLocalMemberData(9854));
        for(CallGridViewHolderSchema data: dataList) {
            this.addItem(data);
        }

        //scheduleUpdateSimulation(dataList);
    }

    public static int randInt(int min, int max) {

        Random rand = new Random();

        // nextInt is normally exclusive of the top value,
        // so add 1 to make it inclusive
        int randomNum = rand.nextInt((max - min) + 1) + min;

        return randomNum;
    }

    
}
