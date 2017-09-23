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
import android.view.View;
import android.widget.FrameLayout;

import com.chthakur.playapp.Logger.ALog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

import static com.chthakur.playapp.GvcGridView.getDisplayHeight;
import static com.chthakur.playapp.GvcGridView.getDisplayWidth;

public class GvcGridViewV2 extends FrameLayout {
    private static final String TAG = GvcGridViewV2.class.getSimpleName();

    RecyclerView mainGridView;

    RecyclerView thumbListView;

    GvcRecyclerViewAdapter adapter;

    private int displayWidth = 0;

    private int displayHeight = 0;

    List<ViewHolderBase.ViewHolderBaseSchema> gridData = new ArrayList<>();

    List<ViewHolderBase.ViewHolderBaseSchema> thumbListData = new ArrayList<>();

    private final int MAX_ITEMS_GRID = 4;

    private final int nColumnsGrid = 2;

    private GvcGridCustomLayout gvcGridCustomLayout;

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

    public GvcGridViewV2(Context context) {
        this(context, null, 0);
    }

    public GvcGridViewV2(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GvcGridViewV2(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.gvc_grid_view_core_v2, null);
        this.addView(view);
        displayWidth = getDisplayWidth(getContext());
        displayHeight = getDisplayHeight(getContext()) - 60;
        gvcGridCustomLayout = (GvcGridCustomLayout)findViewById(R.id.gvc_custom_layout);
    }

    public void addItem(ViewHolderBase.ViewHolderBaseSchema itemData, boolean notifyToUi) {
        gvcGridCustomLayout.addItem(itemData, notifyToUi);
    }

    public void forceNotifyChange() {
        gvcGridCustomLayout.populateDataToUI();
    }

    public void scheduleTest() {
        gvcGridCustomLayout.scheduleRun();
    }
}
