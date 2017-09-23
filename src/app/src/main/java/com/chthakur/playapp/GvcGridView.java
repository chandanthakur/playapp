package com.chthakur.playapp;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.chthakur.playapp.Logger.ALog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

public class GvcGridView extends FrameLayout implements GvcRecyclerViewAdapter.ItemClickListener {
    private static final String TAG = GvcGridView.class.getSimpleName();

    RecyclerView mainGridView;

    RecyclerView thumbListView;

    GvcRecyclerViewAdapter adapter;

    private int displayWidth = 0;

    private int displayHeight = 0;

    List<ViewHolderBase.ViewHolderBaseSchema> gridData = new ArrayList<>();

    List<ViewHolderBase.ViewHolderBaseSchema> thumbListData = new ArrayList<>();

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

    public GvcGridView(Context context) {
        this(context, null, 0);
    }

    public GvcGridView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GvcGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.gvc_grid_view_core_v2, null);
        setUpMainRecyclerView(context, view);
        setUpThumbRecyclerView(context, view);
        this.addView(view);
        displayWidth = getDisplayWidth(getContext());
        displayHeight = getDisplayHeight(getContext()) - 60;
        this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ALog.i(TAG, "onClick");
            }
        });
    }

    private void setUpMainRecyclerView(Context context, View rootLayout) {
        mainGridView = (RecyclerView)rootLayout.findViewById(R.id.grid_main_view);
        GridLayoutManager layoutManager = new GridLayoutManager(context, nColumnsGrid);
        mainGridView.setLayoutManager(layoutManager);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                GvcRecyclerViewAdapter adapter = (GvcRecyclerViewAdapter)mainGridView.getAdapter();
                int nItems = adapter.getItemCount();
                int spanKey = nItems * 10 + position;
                if(spanMap.containsKey(spanKey)) {
                    return spanMap.get(spanKey);
                } else {
                    return 1;
                }
            }
        });


        //mainGridView.getRecycledViewPool().setMaxRecycledViews(GvcRecyclerViewAdapter.ViewHolderFactory.MainItemViewHolderViewType, 0);
        adapter = new GvcRecyclerViewAdapter(context, gridData);
        adapter.setClickListener(this);
        mainGridView.setAdapter(adapter);
        //scheduleUpdateSimulation(gridData.size());
    }

    private void setUpThumbRecyclerView(Context context, View rootLayout) {
        thumbListView = (RecyclerView)rootLayout.findViewById(R.id.grid_thumb_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, true);
        thumbListView.setLayoutManager(layoutManager);

        //mainGridView.getRecycledViewPool().setMaxRecycledViews(GvcRecyclerViewAdapter.ViewHolderFactory.ThumbItemViewHolderViewType, 0);
        adapter = new GvcRecyclerViewAdapter(context, thumbListData);
        adapter.setClickListener(this);
        thumbListView.setAdapter(adapter);
        //scheduleUpdateSimulation(gridData.size());
        //scheduleToggleSimulation();
    }

    public void addItem(ViewHolderBase.ViewHolderBaseSchema itemData) {
        addItem(itemData, true);
    }

    public void addItem(ViewHolderBase.ViewHolderBaseSchema itemData, boolean notifyChange) {
        if(itemData == null) {
            return;
        }

        ALog.i(TAG, "add new grid item:" + itemData.getType());
        int position = gridData.size();
        if(gridData.size() < MAX_ITEMS_GRID) {
            addMainItem(itemData, notifyChange);
        } else {
            addThumbItem(itemData, notifyChange);
        }
    }

    public void forceNotifyChange() {
        repopulateItemSizeForMainDataList(gridData);
        GvcRecyclerViewAdapter mainAdapter = (GvcRecyclerViewAdapter)mainGridView.getAdapter();
        mainAdapter.notifyDataSetChanged();
        GvcRecyclerViewAdapter thumbAdapter = (GvcRecyclerViewAdapter)thumbListView.getAdapter();
        thumbAdapter.notifyDataSetChanged();
    }

    public void addMainItem(ViewHolderBase.ViewHolderBaseSchema itemData) {
        addMainItem(itemData, true);
    }

    public void addMainItem(ViewHolderBase.ViewHolderBaseSchema itemData, boolean notifyChange) {
        if(itemData == null) {
            return;
        }

        ALog.i(TAG, "add new grid item:" + itemData.getType());
        int position = gridData.size();
        GvcRecyclerViewAdapter adapter = (GvcRecyclerViewAdapter)mainGridView.getAdapter();
        itemData.backgroundColor = getAudioGradiant(position);
        gridData.add(itemData);
        repopulateItemSizeForMainDataList(gridData);
        if(notifyChange) {
            adapter.notifyDataSetChanged();
        }
    }

    public void addThumbItem(ViewHolderBase.ViewHolderBaseSchema itemData) {
       addThumbItem(itemData, true);
    }

    public void addThumbItem(ViewHolderBase.ViewHolderBaseSchema itemData, boolean notifyChange) {
        if(itemData == null) {
            return;
        }

        ALog.i(TAG, "add new grid item:" + itemData.getType());
        int position = gridData.size();
        GvcRecyclerViewAdapter adapter = (GvcRecyclerViewAdapter)thumbListView.getAdapter();
        itemData.backgroundColor = getAudioGradiant(position);
        itemData.itemSize = getThumbItemSize();
        thumbListData.add(itemData);
        if(notifyChange) {
            adapter.notifyDataSetChanged();
        }
    }


    static public class ItemSize {

        public ItemSize(int width, int height, boolean isThumb) {
            this.width = width;
            this.height = height;
            this.isThumb = isThumb;
        }

        public int width;

        public int height;

        public boolean isThumb;
    }

    private void repopulateItemSizeForMainDataList(List<ViewHolderBase.ViewHolderBaseSchema> dataList) {
        int maxItems = dataList.size();
        int index = 0;
        for(ViewHolderBase.ViewHolderBaseSchema data: dataList) {
            data.itemSize = getMainItemSizeFromIndex(maxItems, index++);
        }
    }

    public ItemSize getMainItemSizeFromIndex(int totalItems, int index) {
        int parentHeight = displayHeight;
        int parentWidth = displayWidth;
        int rows = -1;
        ItemSize size = new ItemSize(parentWidth, parentHeight, false);
        if(rowMap.containsKey(totalItems)) {
            rows = rowMap.get(totalItems);
            size.height = parentHeight/rows;
        }

        int spanKey = totalItems * 10 + index;
        float spanUnitWidth = parentWidth/nColumnsGrid;
        if(spanMap.containsKey(spanKey)) {
          size.width = (int)spanUnitWidth*spanMap.get(spanKey);
        }

        return size;
    }

    public ItemSize getThumbItemSize() {
        int parentWidth = displayWidth;
        int width = parentWidth/5;
        int height = (int)((float)width*1.5); // go for 2:3 by default for thumbs
        return new ItemSize(width, height, true);
    }

    boolean isBigSingleItemMode = false;

    public void toggleGrid() {
        if(isBigSingleItemMode) {
            toggleToBigSingleItemMode();
        } else {
            toggleToDefaultMode();
        }

        isBigSingleItemMode = !isBigSingleItemMode;
    }

    public void toggleToBigSingleItemMode() {
        while(gridData.size() > 1) {
            ViewHolderBase.ViewHolderBaseSchema element = gridData.remove(gridData.size() - 1);
            element.itemSize = getThumbItemSize();
            thumbListData.add(0, element);
        }

        repopulateItemSizeForMainDataList(gridData);
        GvcRecyclerViewAdapter mainGridViewAdapter = (GvcRecyclerViewAdapter)mainGridView.getAdapter();
        GvcRecyclerViewAdapter thumbGridViewAdapter = (GvcRecyclerViewAdapter)thumbListView.getAdapter();

        mainGridViewAdapter.notifyDataSetChanged();
        thumbGridViewAdapter.notifyDataSetChanged();
    }

    public void toggleToDefaultMode() {
        while(thumbListData.size() > 0) {
            if(gridData.size() < MAX_ITEMS_GRID) {
                ViewHolderBase.ViewHolderBaseSchema element = thumbListData.remove(0);
                gridData.add(element);
            } else {
                break;
            }
        }

        repopulateItemSizeForMainDataList(gridData);
        GvcRecyclerViewAdapter mainGridViewAdapter = (GvcRecyclerViewAdapter)mainGridView.getAdapter();
        GvcRecyclerViewAdapter thumbGridViewAdapter = (GvcRecyclerViewAdapter)thumbListView.getAdapter();
        mainGridViewAdapter.notifyDataSetChanged();
        thumbGridViewAdapter.notifyDataSetChanged();
    }

    public void removeItem(String id) {
        ALog.i(TAG, "remove grid item:" + id);
        if(!removeItemFromMainGrid(id)) {
            removeItemFromThumbGrid(id);
        }
    }

    private boolean removeItemFromMainGrid(String id) {
        GvcRecyclerViewAdapter adapter = (GvcRecyclerViewAdapter)mainGridView.getAdapter();
        boolean isRemoved = removeItemFromMainGrid(adapter, gridData, id);
        return isRemoved;
    }

    private boolean removeItemFromThumbGrid(String id) {
        GvcRecyclerViewAdapter adapter = (GvcRecyclerViewAdapter)thumbListView.getAdapter();
        boolean isRemoved = removeItemFromMainGrid(adapter, thumbListData, id);
        return isRemoved;
    }

    private boolean removeItemFromMainGrid(GvcRecyclerViewAdapter adapter, List<ViewHolderBase.ViewHolderBaseSchema> dataList, String id) {
        if(dataList != null && dataList.size() < 1) {
            return false;
        }

        int foundIndex = getIndexForId(adapter, dataList, id);
        if(foundIndex != -1) {
            dataList.remove(foundIndex);
            adapter.notifyDataSetChanged();
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
        GvcRecyclerViewAdapter adapter = (GvcRecyclerViewAdapter)mainGridView.getAdapter();
        int index = getIndexForId(adapter, gridData, id);
        return index;
    }

    private int getIndexForIdFromThumbGrid(String id) {
        GvcRecyclerViewAdapter adapter = (GvcRecyclerViewAdapter)thumbListView.getAdapter();
        int index = getIndexForId(adapter, thumbListData, id);
        return index;
    }

    private int getIndexForId(GvcRecyclerViewAdapter adapter, List<ViewHolderBase.ViewHolderBaseSchema> dataList, String id) {
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

    private void scheduleUpdateSimulation(final int maxItems) {
        Observable.interval(5, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<Long>() {
            @Override
            public void call(Long aLong) {
                GvcRecyclerViewAdapter adapter = (GvcRecyclerViewAdapter)mainGridView.getAdapter();
                int nItems = adapter.getItemCount();
                int randomNum = (int)(Math.random() * ( nItems - 0 ));
                if((randomNum%2 == 0 && nItems > 1 || nItems == maxItems)) {
                    Log.i(TAG, "item removed: " + randomNum);
                    adapter.removeAtIndex(randomNum);
                } else {
                    Log.i(TAG, "item added: " + randomNum);
                    adapter.addAtIndex(randomNum, getRandData(randomNum));
                }
            }
        });
    }

    private void scheduleToggleSimulation() {
        Observable.interval(5, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<Long>() {
            @Override
            public void call(Long aLong) {
                toggleGrid();
            }
        });
    }

    static public ViewHolderBase.ViewHolderBaseSchema getRandData(int kk) {
        List<String> imageList = new ArrayList<>();
        imageList.add("https://cdn.pixabay.com/photo/2017/05/16/10/10/shark-2317422_960_720.png");
        imageList.add("https://cdn.pixabay.com/photo/2013/07/18/10/58/kitten-163627_960_720.jpg");
        imageList.add("https://cdn.pixabay.com/photo/2013/07/13/09/51/sheep-156163_960_720.png");
        imageList.add("https://cdn.pixabay.com/photo/2013/07/13/01/17/housefly-155460_960_720.png");
        imageList.add("https://cdn.pixabay.com/photo/2015/02/03/02/14/keyboard-621830_960_720.jpg");
        imageList.add("https://cdn.pixabay.com/photo/2014/04/03/00/35/owl-308773_960_720.png");
        imageList.add("https://cdn.pixabay.com/photo/2014/04/03/10/32/turtle-310825_960_720.png");
        imageList.add("https://cdn.pixabay.com/photo/2014/04/03/00/35/zebra-308769_960_720.png");
        imageList.add("https://cdn.pixabay.com/photo/2013/07/12/16/30/earthworm-151033_960_720.png");
        imageList.add("https://cdn.pixabay.com/photo/2012/04/18/00/30/apple-36282_960_720.png");
        MainItemViewHolder.ViewHolderDataSchema element = null;
        element = new MainItemViewHolder.ViewHolderDataSchema();
        element.text = "dataElement:" + kk;
        element.imageUri = kk < imageList.size() ? imageList.get(kk) : imageList.get(kk%imageList.size());
        return element;
    }

    public static Random random = new Random();
    static public ViewHolderBase.ViewHolderBaseSchema getCallLocalMemberData(int kk) {
        ViewHolderBase.ViewHolderBaseSchema element = new CallLocalMemberViewHolder.ViewHolderDataSchema();
        element.id = String.valueOf(random.nextInt());
        element.videoId = kk;
        return element;
    }

    static public ViewHolderBase.ViewHolderBaseSchema getRemoteCallMemberData(int kk) {
        ViewHolderBase.ViewHolderBaseSchema element = new CallRemoteMemberViewHolder.ViewHolderDataSchema();
        element.id = String.valueOf(random.nextInt());
        element.videoId = kk;
        return element;
    }

    static public int getDisplayHeight(Context context){
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager()
                .getDefaultDisplay()
                .getMetrics(displayMetrics);
        return displayMetrics.heightPixels;
    }

    static public int getDisplayWidth(Context context){
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager()
                .getDefaultDisplay()
                .getMetrics(displayMetrics);
        return displayMetrics.widthPixels;
    }

    @Override
    public void onItemClick(View view, int position) {
        toggleGrid();
        Log.i("TAG", "You clicked number " + ", which is at cell position " + position);
    }

    public void addGridElement(View element) {

    }

    private int getAudioGradiant(int index) {
        if(index == 0) {
            return R.drawable.call_gradient_variation1;
        } else if(index == 1) {
            return R.drawable.call_gradient_variation2;
        } else if(index == 2) {
            return R.drawable.call_gradient_variation3;
        } else if(index == 3) {
            return R.drawable.call_gradient_variation4;
        } else {
            return R.drawable.call_audio_gradient;
        }
    }


}
