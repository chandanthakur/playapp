package com.chthakur.playapp;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.chthakur.playapp.Logger.ALog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

import static com.chthakur.playapp.GvcGridView.getDisplayHeight;
import static com.chthakur.playapp.GvcGridView.getDisplayWidth;

public class GvcGridCustomLayout extends FrameLayout {
    private static final String TAG = GvcGridCustomLayout.class.getSimpleName();


    GvcAbsoluteGridLayout gvcAbsoluteGridLayout;

    private int displayWidth = 0;

    private int displayHeight = 0;

    private static boolean isLandscape = false;

    private HashMap<ViewHolderBase.ViewHolderBaseSchema, FrameLayout> dataToFrameMap = new HashMap<>();

    private HashMap<FrameLayout, ViewHolderBase> frameToViewHolderMap = new HashMap<>();

    private List<FrameLayout> mainGridChildren = new ArrayList<>();

    private LinearLayout thumbListRoot;

    List<ViewHolderBase.ViewHolderBaseSchema> gridData = new ArrayList<>();

    List<ViewHolderBase.ViewHolderBaseSchema> thumbListData = new ArrayList<>();

    private final int MAX_ITEMS_SUPPORTED = 9;

    private final int MAX_ITEMS_GRID = 4;

    private final int nColumnsGrid = 2;

    static final HashMap<Integer, Integer> spanMap = new HashMap<Integer, Integer>(){
        {
            put(10, 2);
            put(20, 2);
            put(21, 2);
            put(30, 2);
            put(31, 1);
            put(32, 1);
            put(40, 1);
            put(41, 1);
            put(42, 1);
            put(43, 1);
        }
    };

    static final HashMap<Integer, Integer> rowMap = new HashMap<Integer, Integer>(){
        {
            put(1, 1);
            put(2, 2);
            put(3, 2);
            put(4, 2);
        }
    };

    public GvcGridCustomLayout(Context context) {
        this(context, null, 0);
    }

    public GvcGridCustomLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GvcGridCustomLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.gvc_grid_custom_layout, null);

        gvcAbsoluteGridLayout = (GvcAbsoluteGridLayout)view.findViewById(R.id.gvc_grid_wrap);

        mainGridChildren.add((FrameLayout) view.findViewById(R.id.gvc_grid_row1_col1));
        mainGridChildren.add((FrameLayout) view.findViewById(R.id.gvc_grid_row2_col1));
        mainGridChildren.add((FrameLayout) view.findViewById(R.id.gvc_grid_row2_col2));
        mainGridChildren.add((FrameLayout) view.findViewById(R.id.gvc_grid_row1_col2));

        thumbListRoot = (LinearLayout)view.findViewById(R.id.gvc_thumb_row);
        isLandscape = UtilsDevice.isLandscapeOrientation(getContext());
        this.addView(view);
        displayWidth = getDisplayWidth(getContext());
        displayHeight = getDisplayHeight(getContext());

        if(isLandscape) {
            displayWidth -= 60;
        } else {
            displayHeight -= 60;
        }

        for(int kk = 0; kk < mainGridChildren.size(); kk++) {
            mainGridChildren.get(kk).setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP) {
                        toggleGrid();
                    }

                    return false;
                }
            });
        }
    }

    public void addItem(ViewHolderBase.ViewHolderBaseSchema itemData) {
        addItem(itemData, true);
    }

    public void addItem(ViewHolderBase.ViewHolderBaseSchema itemData, boolean notifyChange) {
        if (itemData == null) {
            return;
        }

        ALog.i(TAG, "add new grid item:" + itemData.getType());
        if (isSlotAvailableInMainList()) {
            addMainItem(itemData, notifyChange);
        } else {
            addThumbItem(itemData, notifyChange);
        }
    }

    public void addMainItem(ViewHolderBase.ViewHolderBaseSchema itemData) {
        addMainItem(itemData, true);
    }

    public void addMainItem(ViewHolderBase.ViewHolderBaseSchema itemData, boolean notifyChange) {
        if(itemData == null) {
            return;
        }

        ALog.i(TAG, "add new grid item:" + itemData.getType());
        gridData.add(itemData);
        repopulateItemSizeForMainDataList(gridData);
        if(notifyChange) {
            FrameLayout frameLayout = getMainLayoutSlot();
            populateMainDataItemToUI(frameLayout, itemData);
        }
    }

    boolean isSlotAvailableInMainList() {
        int nElement = gridData.size();
        if(isBigSingleItemMode) {
            return nElement < 1;
        } else {
            return nElement < MAX_ITEMS_GRID;
        }
    }

    public void removeItem(String id) {
        ALog.i(TAG, "remove grid item:" + id);
        if(!removeItemFromMainGrid(id)) {
            removeItemFromThumbGrid(id);
        }
    }

    private boolean removeItemFromMainGrid(String id) {
        boolean isRemoved = removeItemFromMainGrid(gridData, id);
        return isRemoved;
    }

    private boolean removeItemFromThumbGrid(String id) {
        boolean isRemoved = removeItemFromThumbList(thumbListData, id);
        return isRemoved;
    }

    private boolean removeItemFromMainGrid(List<ViewHolderBase.ViewHolderBaseSchema> dataList, String id) {
        if(dataList != null && dataList.size() < 1) {
            return false;
        }

        int foundIndex = getIndexForId(dataList, id);
        if(foundIndex == -1) {
            return false;
        }

        // we need to kind of shift
        List<ViewHolderBase.ViewHolderBaseSchema> shiftList = new ArrayList<>();
        for(int kk = foundIndex; kk < dataList.size(); kk++) {
            shiftList.add(dataList.get(kk));
        }

        dataList.remove(foundIndex);
        if(thumbListData.size() > 0) {
            ViewHolderBase.ViewHolderBaseSchema thumbFirstItem = thumbListData.get(0);
            shiftList.add(thumbFirstItem);
            gridData.add(thumbFirstItem);
            thumbListData.remove(thumbFirstItem);
        }

        repopulateItemSizeForMainDataList(gridData);
        for(int kk =0; kk < foundIndex; kk++) {
            rebindMainFrame(gridData.get(kk));
        }

        if(shiftList.size() > 1) {
            purgeMainElement(getFrameFromData(shiftList.get(0)));
            List<FrameLayout> framesToShift = new ArrayList<>();
            for(int kk = 0; kk < shiftList.size(); kk++) {
                framesToShift.add(getFrameFromData(shiftList.get(kk)));
            }

            FrameLayout prevFrame = framesToShift.get(0);
            for(int kk = 1; kk < framesToShift.size(); kk++) {
                FrameLayout currentFrame = framesToShift.get(kk);
                ALog.i(TAG, "prevFrame:" + getIdFromFrame(prevFrame) + ", currFrame:" + getIdFromFrame(currentFrame));
                if(isMainFrame(currentFrame)) {
                    swapToMainFromMain(prevFrame, currentFrame);
                } else {
                    // this got to be the last one if it is thumb

                    swapToMainFromThumbList(prevFrame, currentFrame);
                    break;
                }

                prevFrame = currentFrame;
            }
        } else {
            purgeMainElement(getFrameFromData(shiftList.get(0)));
        }

        return foundIndex != -1;
    }

    private boolean isThumbData(ViewHolderBase.ViewHolderBaseSchema baseSchema) {
        if(thumbListData.indexOf(baseSchema) != -1) {
            return true;
        }

        return false;
    }

    private boolean removeItemFromThumbList(List<ViewHolderBase.ViewHolderBaseSchema> dataList, String id) {
        if(dataList != null && dataList.size() < 1) {
            return false;
        }

        int foundIndex = getIndexForId(dataList, id);
        if(foundIndex != -1) {
            ViewHolderBase.ViewHolderBaseSchema dataItem = dataList.get(foundIndex);
            FrameLayout frameLayout = getFrameFromData(dataItem);
            purgeThumbElement(frameLayout);
            dataList.remove(foundIndex);
        }

        return foundIndex != -1;
    }

    public boolean hasItemWithId(String id) {
        int index = getIndexForId(id);
        return index != -1;
    }

    public int getIndexForId(String id) {
        int index = getIndexForIdFromMainGrid(id);
        if(index == -1) {
            index = getIndexForIdFromThumbGrid(id);
        }

        return index;
    }

    private int getIndexForIdFromMainGrid(String id) {
        int index = getIndexForId(gridData, id);
        return index;
    }

    private int getIndexForIdFromThumbGrid(String id) {
        int index = getIndexForId(thumbListData, id);
        return index;
    }

    private int getIndexForId(List<ViewHolderBase.ViewHolderBaseSchema> dataList, String id) {
        if(dataList != null && dataList.size() < 1) {
            return -1;
        }

        int foundIndex = -1;
        int index = 0;
        for(ViewHolderBase.ViewHolderBaseSchema viewHolderBaseSchema: dataList) {
            if(viewHolderBaseSchema.id == id) {
                foundIndex = index;
            }

            index = index + 1;
        }

        return foundIndex;
    }

    public void forceNotifyChange() {
        populateDataToUI();
    }

    public FrameLayout getMainLayoutSlot() {
        for(FrameLayout frameLayout: mainGridChildren) {
            if(!frameToViewHolderMap.containsKey(frameLayout)) {
                return frameLayout;
            }
        }

        return null;
    }

    private class ThumbFrameLayout extends FrameLayout {
        public ThumbFrameLayout(Context context) {
            super(context);
        }

        @Override
        public boolean onInterceptTouchEvent(MotionEvent ev) {
            return true;
        }

        @Override
        public boolean onTouchEvent(MotionEvent motionEvent) {
            switch (motionEvent.getAction() & MotionEvent.ACTION_MASK)
            {
                case MotionEvent.ACTION_UP:
                    ALog.d(TAG, "onThumbClick");
                    onThumbClick(ThumbFrameLayout.this);
                    break;
                default:
                    break;
            }

            return true;
        }
    }

    private void onThumbClick(ThumbFrameLayout thumbFrame) {
        if(!frameToViewHolderMap.containsKey(thumbFrame)) {
           return;
        }

        FrameLayout mainFrame = getMainFrameForSwapOnThumbClick();
        if(mainFrame == null) {
            return;
        }

        swapMainWithThumbFrame(mainFrame, thumbFrame);
    }

    private void swapMainWithThumbFrame(FrameLayout mainFrame, FrameLayout thumbFrame) {
        if(mainFrame.getChildCount() < 1 || thumbFrame.getChildCount() < 1) {
            return;
        }

        View mainFrameRootChild = mainFrame.getChildAt(0);
        View thumbFrameRootChild = thumbFrame.getChildAt(0);
        ViewHolderBase mainFrameVH = frameToViewHolderMap.get(mainFrame);
        ViewHolderBase thumbFrameVH = frameToViewHolderMap.get(thumbFrame);
        GvcGridView.ItemSize mainFrameItemSize = mainFrameVH.getData().itemSize;
        GvcGridView.ItemSize thumbFrameItemSize = thumbFrameVH.getData().itemSize;
        // swap child first
        mainFrame.removeView(mainFrameRootChild);
        thumbFrame.removeView(thumbFrameRootChild);
        mainFrame.addView(thumbFrameRootChild);
        thumbFrame.addView(mainFrameRootChild);

        dataToFrameMap.put(mainFrameVH.getData(), thumbFrame);
        dataToFrameMap.put(thumbFrameVH.getData(), mainFrame);

        frameToViewHolderMap.put(mainFrame, thumbFrameVH);
        frameToViewHolderMap.put(thumbFrame, mainFrameVH);

        // checks not required, assume consistent data, UI thread
        int mainGridIdx = gridData.indexOf(mainFrameVH.getData());
        int thumbListIdx = thumbListData.indexOf(thumbFrameVH.getData());
        ViewHolderBase.ViewHolderBaseSchema mainGridItem = gridData.remove(mainGridIdx);
        ViewHolderBase.ViewHolderBaseSchema thumbListItem = thumbListData.remove(thumbListIdx);
        mainGridItem.timestamp = System.currentTimeMillis(); // swap timestamp
        thumbListItem.timestamp = System.currentTimeMillis(); // swap timestamp
        mainGridItem.itemSize = thumbFrameItemSize;
        thumbListItem.itemSize = mainFrameItemSize;
        gridData.add(mainGridIdx, thumbListItem);
        thumbListData.add(thumbListIdx, mainGridItem);

        mainFrameVH.refreshLayout(mainFrameItemSize.width, mainFrameItemSize.height);
        thumbFrameVH.refreshLayout(thumbFrameItemSize.width, thumbFrameItemSize.height);
    }

    private FrameLayout getMainFrameForSwapOnThumbClick() {
        if(gridData.size() < 1) {
            return null;
        }

        long minTS = Long.MAX_VALUE;
        int idx = 0;
        for(int kk = 0; kk < gridData.size(); kk++) {
            ViewHolderBase.ViewHolderBaseSchema itemData = gridData.get(kk);
            if(itemData.timestamp < minTS) {
                idx = kk;
                minTS = itemData.timestamp;
            }
        }

        return getFrameFromData(gridData.get(idx));
    }

    public FrameLayout getThumbLayoutSlot() {
        FrameLayout view = new ThumbFrameLayout(getContext());
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(6,6,6,6);
        view.setLayoutParams(layoutParams);
        view.setBackgroundColor(Color.parseColor("#ffffff"));
        return view;
    }

    private boolean isMainFrame(FrameLayout frameLayout) {
        if(mainGridChildren.indexOf(frameLayout) == -1) {
            return  false;
        }

        return true;
    }

    private void populateMainDataItemToUI(FrameLayout frameLayout, ViewHolderBase.ViewHolderBaseSchema dataItem) {
        // size has changed, so we need to re-bind everything again.
        rebindMainFrameElements(getListExcept(gridData, dataItem));
        addDataItemToUI(frameLayout, dataItem);
    }

    private void populateThumbDataItemToUI(FrameLayout frameLayout, ViewHolderBase.ViewHolderBaseSchema dataItem) {
        // size has changed, so we need to re-bind everything again.
        this.thumbListRoot.addView(frameLayout);
        addDataItemToUI(frameLayout, dataItem);
    }

    private void rebindThumbFrameElements(List<ViewHolderBase.ViewHolderBaseSchema> dataList) {
        for(ViewHolderBase.ViewHolderBaseSchema data: dataList) {
            if(!dataToFrameMap.containsKey(data)) {
                FrameLayout frameLayout = getThumbLayoutSlot();
                populateThumbDataItemToUI(frameLayout, data);
            }
        }
    }

    public void populateDataToUI() {
        repopulateItemSizeForMainDataList(gridData);
        // already binded ones
        rebindMainFrameElements(gridData);
        for(ViewHolderBase.ViewHolderBaseSchema data: gridData) {
            if(!dataToFrameMap.containsKey(data)) {
                populateMainDataItemToUI(getMainLayoutSlot(), data);
            }
        }

        repopulateItemSizeForThumbDataList(thumbListData);
        rebindThumbFrameElements(thumbListData);
    }

    private void addDataItemToUI(FrameLayout parentRoot, ViewHolderBase.ViewHolderBaseSchema itemData) {
        parentRoot.getLayoutParams().height = itemData.itemSize.height;
        parentRoot.getLayoutParams().width = itemData.itemSize.width;
        parentRoot.setVisibility(VISIBLE);

        int viewType = GvcRecyclerViewAdapter.ViewHolderFactory.getViewType(itemData.getType());
        ViewHolderBase viewHolder = GvcRecyclerViewAdapter.ViewHolderFactory.create(getContext(), parentRoot, viewType);
        viewHolder.bindViewHolder(itemData, gridData.size());
        frameToViewHolderMap.put(parentRoot, viewHolder);
        dataToFrameMap.put(itemData, parentRoot);
        viewHolder.itemView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleGrid();
                ALog.i(TAG, "onClick");
            }
        });

        parentRoot.addView(viewHolder.itemView);
    }

    public void addThumbItem(ViewHolderBase.ViewHolderBaseSchema itemData) {
        addThumbItem(itemData, true);
    }

    public void addThumbItem(ViewHolderBase.ViewHolderBaseSchema itemData, boolean notifyChange) {
        if(itemData == null) {
            return;
        }

        ALog.i(TAG, "add new grid item:" + itemData.getType());
        itemData.itemSize = getThumbItemSize();
        thumbListData.add(itemData);
        if(notifyChange) {
            populateThumbDataItemToUI(getThumbLayoutSlot(), itemData);
        }
    }

    private void repopulateItemSizeForMainDataList(List<ViewHolderBase.ViewHolderBaseSchema> dataList) {
        int maxItems = dataList.size();
        int index = 0;
        for(ViewHolderBase.ViewHolderBaseSchema data: dataList) {
            data.itemSize = getMainItemSizeFromIndex(maxItems, index++);
        }
    }

    private void repopulateItemSizeForThumbDataList(List<ViewHolderBase.ViewHolderBaseSchema> dataList) {
        for(ViewHolderBase.ViewHolderBaseSchema data: dataList) {
            data.itemSize = getThumbItemSize();
        }
    }

    public GvcGridView.ItemSize getThumbItemSize() {
        int parentHeight = displayHeight;
        int parentWidth = displayWidth;
        int width = parentWidth / 5;
        int height = (int) ((float) width * ((float)parentHeight/parentWidth)); // go for 2:3 by default for thumbs
        return new GvcGridView.ItemSize(width, height, true);
    }

    public GvcGridView.ItemSize getMainItemSizeFromIndex(int totalItems, int index) {
        int parentHeight = displayHeight;
        int parentWidth = displayWidth;
        if (!rowMap.containsKey(totalItems)) {
            return new GvcGridView.ItemSize(parentWidth, parentHeight, false);
        }

        GvcGridView.ItemSize size = new GvcGridView.ItemSize(parentWidth, parentHeight, false);
        int rows = rowMap.get(totalItems);
        if(isLandscape) {
            size.width = parentWidth / rows;
        } else {
            size.height = parentHeight / rows;
        }

        int spanKey = totalItems * 10 + index;
        float spanUnitSize = parentWidth / nColumnsGrid;
        if(isLandscape) {
            spanUnitSize = parentHeight / nColumnsGrid;
        }

        if (spanMap.containsKey(spanKey)) {
            if(isLandscape) {
                size.height = (int) spanUnitSize * spanMap.get(spanKey);
            } else {
                size.width = (int) spanUnitSize * spanMap.get(spanKey);
            }
        }

        return size;
    }

    private boolean isBigSingleItemMode = false;
    public void toggleGrid() {
        if(isBigSingleItemMode) {
            toggleToDefaultMode();
        } else {
            toggleToBigSingleItemMode();
        }

        isBigSingleItemMode = !isBigSingleItemMode;
    }

    public void toggleToBigSingleItemMode() {
        List<Pair<FrameLayout, FrameLayout>> swapNodes = new ArrayList<>();
        while(gridData.size() > 1) {
            ViewHolderBase.ViewHolderBaseSchema element = gridData.remove(gridData.size() - 1);
            element.itemSize = getThumbItemSize();
            thumbListData.add(0, element);
            swapNodes.add(new Pair(getThumbLayoutSlot(), getFrameFromData(element)));
        }

        repopulateItemSizeForMainDataList(gridData);
        rebindMainFrameElements(gridData);
        for(Pair<FrameLayout, FrameLayout> swapNode: swapNodes) {
            swapToThumbFromMainGrid(swapNode.first, swapNode.second);
        }
    }

    public void toggleToDefaultMode() {
        List<ViewHolderBase.ViewHolderBaseSchema> gridDataOrig = getListCopy(gridData);
        List<Pair<FrameLayout, FrameLayout>> swapNodes = new ArrayList<>();
        while(thumbListData.size() > 0) {
            if(gridData.size() < MAX_ITEMS_GRID) {
                ViewHolderBase.ViewHolderBaseSchema element = thumbListData.remove(0);
                FrameLayout thumbFrame = getFrameFromData(element);
                FrameLayout mainFrame = getMainFrameFromIdx(gridData.size());
                gridData.add(element);
                swapNodes.add(new Pair(mainFrame, thumbFrame));
            } else {
                break;
            }
        }

        // once we know the elements, we first need to rebind older ones
        repopulateItemSizeForMainDataList(gridData);
        rebindMainFrameElements(gridDataOrig);
        for(Pair<FrameLayout, FrameLayout> swapNode: swapNodes) {
            swapToMainFromThumbList(swapNode.first, swapNode.second);
        }
    }

    List<ViewHolderBase.ViewHolderBaseSchema> getListCopy(List<ViewHolderBase.ViewHolderBaseSchema> dataList) {
        List<ViewHolderBase.ViewHolderBaseSchema> dataListCopy = new ArrayList<>();
        for(ViewHolderBase.ViewHolderBaseSchema schema: dataList) {
            dataListCopy.add(schema);
        }

        return dataListCopy;
    }


    List<ViewHolderBase.ViewHolderBaseSchema> getListExcept(List<ViewHolderBase.ViewHolderBaseSchema> dataList, ViewHolderBase.ViewHolderBaseSchema except) {
        List<ViewHolderBase.ViewHolderBaseSchema> dataListCopy = new ArrayList<>();
        for(ViewHolderBase.ViewHolderBaseSchema schema: dataList) {
            if(!schema.equals(except)) {
                dataListCopy.add(schema);
            }
        }

        return dataListCopy;
    }

    FrameLayout getFrameFromData(ViewHolderBase.ViewHolderBaseSchema itemData){
        if(!this.dataToFrameMap.containsKey(itemData)) {
            return null;
        }

        return dataToFrameMap.get(itemData);
    }

    FrameLayout getMainFrameFromIdx(int index){
        return this.mainGridChildren.get(index);
    }

    void rebindMainFrameElements(List<ViewHolderBase.ViewHolderBaseSchema> elementList) {
        for(ViewHolderBase.ViewHolderBaseSchema itemData: elementList) {
            // only data that is binded before
            rebindMainFrame(itemData);
        }
    }

    void rebindMainFrame(ViewHolderBase.ViewHolderBaseSchema itemData) {
        if(!dataToFrameMap.containsKey(itemData)) {
            return;
        }

        FrameLayout frameLayout = getFrameFromData(itemData);
        ViewHolderBase viewHolder = frameToViewHolderMap.get(frameLayout);
        frameLayout.getLayoutParams().width = itemData.itemSize.width;
        frameLayout.getLayoutParams().height = itemData.itemSize.height;
        //viewHolder.bindViewHolder(itemData, 0);
        viewHolder.refreshLayout(itemData.itemSize.width, itemData.itemSize.height);
    }

    void purgeMainElement(FrameLayout mainFrame) {
        ALog.i(TAG, "purgeMainElement: " + getIdFromFrame(mainFrame));
        if(mainFrame.getChildCount() > 0) {
            ViewHolderBase viewHolder = frameToViewHolderMap.get(mainFrame);
            View child = mainFrame.getChildAt(0);
            mainFrame.removeView(child);
            viewHolder.destroyView();
            frameToViewHolderMap.remove(mainFrame);
            mainFrame.setVisibility(GONE);
            dataToFrameMap.remove(viewHolder);
        }
    }

    void purgeThumbElement(FrameLayout thumbFrame) {
        ALog.i(TAG, "purgeThumbElement: " + getIdFromFrame(thumbFrame));
        ViewHolderBase viewHolder = frameToViewHolderMap.get(thumbFrame);
        View child = thumbFrame.getChildAt(0);
        thumbFrame.removeView(child);
        viewHolder.destroyView();
        frameToViewHolderMap.remove(thumbFrame);
        thumbListRoot.removeView(thumbFrame);
        ViewHolderBase.ViewHolderBaseSchema baseSchema = viewHolder.getData();
        dataToFrameMap.remove(baseSchema);
    }

    void swapToThumbFromMainGrid(FrameLayout thumbFrame, FrameLayout mainFrame) {
        ALog.i(TAG, "swapToThumbFromMainGrid: replace:" + getIdFromFrame(thumbFrame) + ", with:" + getIdFromFrame(mainFrame));
        ViewHolderBase viewHolder = frameToViewHolderMap.get(mainFrame);
        View child = mainFrame.getChildAt(0);
        mainFrame.removeView(child);
        thumbFrame.addView(child);;
        thumbListRoot.addView(thumbFrame, 0);
        frameToViewHolderMap.put(thumbFrame, viewHolder);
        frameToViewHolderMap.remove(mainFrame);
        ViewHolderBase.ViewHolderBaseSchema baseSchema = viewHolder.getData();
        thumbFrame.getLayoutParams().width = baseSchema.itemSize.width;
        thumbFrame.getLayoutParams().height = baseSchema.itemSize.height;
        mainFrame.setVisibility(GONE);
        //viewHolder.bindViewHolder(baseSchema, 0);
        viewHolder.refreshLayout(baseSchema.itemSize.width, baseSchema.itemSize.height);
        dataToFrameMap.put(baseSchema, thumbFrame);
    }

    void swapToMainFromThumbList(FrameLayout mainFrame, FrameLayout thumbFrame) {
        ALog.i(TAG, "swapToMainFromThumbList: replace:" + getIdFromFrame(mainFrame) + ", with:" + getIdFromFrame(thumbFrame));
        ViewHolderBase viewHolder = frameToViewHolderMap.get(thumbFrame);
        // need to have element
        View child = thumbFrame.getChildAt(0);
        thumbFrame.removeView(child);
        mainFrame.addView(child);;
        frameToViewHolderMap.put(mainFrame, viewHolder);
        frameToViewHolderMap.remove(thumbFrame);
        thumbListRoot.removeView(thumbFrame);
        ViewHolderBase.ViewHolderBaseSchema baseSchema = viewHolder.getData();
        mainFrame.getLayoutParams().width = baseSchema.itemSize.width;
        mainFrame.getLayoutParams().height = baseSchema.itemSize.height;
        mainFrame.setVisibility(VISIBLE);
        //viewHolder.bindViewHolder(baseSchema, 0);
        viewHolder.refreshLayout(baseSchema.itemSize.width, baseSchema.itemSize.height);
        dataToFrameMap.put(baseSchema, mainFrame);
    }

    void swapToMainFromMain(FrameLayout mainFrameDest, FrameLayout mainFrameSrc) {
        ALog.i(TAG, "swapToMainFromMain: replace:" + getIdFromFrame(mainFrameDest) + ", with:" + getIdFromFrame(mainFrameSrc));
        ViewHolderBase viewHolder = frameToViewHolderMap.get(mainFrameSrc);
        // need to have element
        View child = mainFrameSrc.getChildAt(0);
        mainFrameSrc.removeView(child);
        purgeMainElement(mainFrameDest);
        mainFrameDest.addView(child);
        frameToViewHolderMap.put(mainFrameDest, viewHolder);
        frameToViewHolderMap.remove(mainFrameSrc);
        ViewHolderBase.ViewHolderBaseSchema baseSchema = viewHolder.getData();
        mainFrameDest.getLayoutParams().width = baseSchema.itemSize.width;
        mainFrameDest.getLayoutParams().height = baseSchema.itemSize.height;
        mainFrameDest.setVisibility(VISIBLE);
        mainFrameSrc.setVisibility(GONE);
        //viewHolder.bindViewHolder(baseSchema, 0);
        viewHolder.refreshLayout(baseSchema.itemSize.width, baseSchema.itemSize.height);
        dataToFrameMap.put(baseSchema, mainFrameDest);
    }

    static boolean once = true;
    private void scheduleUpdateSimulation(final List<ViewHolderBase.ViewHolderBaseSchema> dataList) {
        Observable.interval(200, TimeUnit.MILLISECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<Long>() {
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

    private boolean simulateRemove(final List<ViewHolderBase.ViewHolderBaseSchema> dataList) {
        if(dataList.size() > 0) {
            int idx = randInt(0, dataList.size() - 1);
            ViewHolderBase.ViewHolderBaseSchema data = dataList.remove(idx);
            GvcGridCustomLayout.this.removeItem(data.id);
            return true;
        }

        return false;
    }

    private void simulateAdd(final List<ViewHolderBase.ViewHolderBaseSchema> dataList) {
        int idx = randInt(2500, 5000);
        ViewHolderBase.ViewHolderBaseSchema baseSchema;
        if(idx < 3000) {
            baseSchema = GvcGridView.getCallLocalMemberData(idx);
        } else {
            baseSchema = GvcGridView.getRemoteCallMemberData(idx);
        }

        dataList.add(baseSchema);
        this.addItem(baseSchema);
    }

    public void scheduleRun() {
        List<ViewHolderBase.ViewHolderBaseSchema> dataList = new ArrayList<>();
        for (int kk = 0; kk < 1; kk++) {
            dataList.add(GvcGridView.getRemoteCallMemberData(2500 + kk));
        }

        dataList.add(GvcGridView.getCallLocalMemberData(9854));
        for(ViewHolderBase.ViewHolderBaseSchema data: dataList) {
            this.addItem(data, false);
        }

        //this.forceNotifyChange();
        //scheduleUpdateSimulation(dataList);
    }

    public static int randInt(int min, int max) {

        Random rand = new Random();

        // nextInt is normally exclusive of the top value,
        // so add 1 to make it inclusive
        int randomNum = rand.nextInt((max - min) + 1) + min;

        return randomNum;
    }

    public int getIdFromFrame(FrameLayout frameLayout) {
        if(frameToViewHolderMap.containsKey(frameLayout)) {
            ViewHolderBase viewHolder = frameToViewHolderMap.get(frameLayout);
            return viewHolder.getData().videoId;
        }

        return -1;
    }

    public void release() {
        for(ViewHolderBase.ViewHolderBaseSchema itemData: gridData) {
            purgeMainElement(getFrameFromData(itemData));
        }

        for(ViewHolderBase.ViewHolderBaseSchema itemData: thumbListData) {
            purgeThumbElement(getFrameFromData(itemData));
        }
    }
}
