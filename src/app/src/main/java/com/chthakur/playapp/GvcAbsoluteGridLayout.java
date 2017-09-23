package com.chthakur.playapp;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
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

public class GvcAbsoluteGridLayout extends FrameLayout  {

    private static final String TAG = GvcAbsoluteGridLayout.class.getSimpleName();

    List<Rect> layoutChildRectList;

    boolean isBigSingleLayout = false;

    List<AbsoluteFrameChildLayout> outerFrameList = new ArrayList<>();

    public GvcAbsoluteGridLayout(Context context) {
        this(context, null, 0);
    }

    public GvcAbsoluteGridLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GvcAbsoluteGridLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.gvc_absolute_grid_layout, null);
        this.addView(view);
    }

    private List<Rect> updateLayoutChildRectList(int nElements) {
        if(isBigSingleLayout) {
            layoutChildRectList = getRectangleList(UtilsDevice.getDisplayWidth(getContext()), UtilsDevice.getDisplayHeight(getContext()), 1, nElements - 1);
        } else {
            layoutChildRectList = getRectangleList(UtilsDevice.getDisplayWidth(getContext()), UtilsDevice.getDisplayHeight(getContext()), 4, nElements - 4);
        }

        return layoutChildRectList;
    }

    private void toggleLayout() {
        isBigSingleLayout = !isBigSingleLayout;
    }

    private void refreshLayout(int nElements) {

    }

    private void addItem(ViewGroup item) {
        int newSize = outerFrameList.size() + 1;
        layoutChildRectList = updateLayoutChildRectList(newSize);
        AbsoluteFrameChildLayout frameLayout = new AbsoluteFrameChildLayout(getContext(), layoutChildRectList.get(newSize - 1));
        frameLayout.addView(item);
        outerFrameList.add(frameLayout);
    }

    private void removeItem(ViewGroup item) {

    }

    HashMap<Integer, List<Rect>> rectangleMap = new HashMap<>();
    public List<Rect> getRectangleList(int w, int h, int maxMainCount, int maxCount) {
        List<Rect> rectList1 = new ArrayList<>();
        rectList1.add(new Rect(0, 0, w, h));

        List<Rect> rectList2 = new ArrayList<>();
        rectList2.add(new Rect(0, 0, w, h/2));
        rectList2.add(new Rect(0, h/2, w, h));

        List<Rect> rectList3 = new ArrayList<>();
        rectList3.add(new Rect(0, 0, w, h/2));
        rectList3.add(new Rect(0, h/2, w/2, h));
        rectList3.add(new Rect(w/2, h/2, w, h));

        List<Rect> rectList4 = new ArrayList<>();
        rectList4.add(new Rect(0, 0, w/2, h/2));
        rectList4.add(new Rect(w/2, 0, w, h/2));
        rectList4.add(new Rect(0, h/2, w/2, h));
        rectList4.add(new Rect(w/2, h/2, w, h));

        rectangleMap.put(1, rectList1);
        rectangleMap.put(2, rectList2);
        rectangleMap.put(3, rectList3);
        rectangleMap.put(4, rectList4);

        List<Rect> result = rectangleMap.get(maxMainCount);
        result.addAll(getThumbRectList(w, h, maxCount - maxMainCount));
        return result;
    }

    public List<Rect> getThumbRectList(int w, int h, int count) {
        int thumbW = w/5;
        int thumbH = (thumbW*3)/2;
        int endH = h;
        int endW = w;
        List<Rect> rectList = new ArrayList();
        for(int kk = 0; kk < count; kk++) {
            Rect thumbRect = new Rect(endW - thumbW, endH - thumbH, endW, h);
            endW = endW - thumbW;
            rectList.add(thumbRect);
        }

        return rectList;
    }

    static int index = 0;
    public class AbsoluteFrameChildLayout extends FrameLayout {
        public AbsoluteFrameChildLayout(Context context, Rect rect) {
            this(context, null, 0);
            ViewGroup.LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT);
            this.setLayoutParams(layoutParams);
            this.setLayoutParams(new ViewGroup.LayoutParams(rect.width(), rect.height()));
            setRandomColorBG();
            this.setX(rect.left);
            this.setY(rect.top);
        }

        @TargetApi(23)
        void setRandomColorBG() {
            int val = index%4;
            if(val == 0) {
                this.setBackgroundColor(getContext().getColor(R.color.colorGreen));
            }  else if (val == 1) {
                this.setBackgroundColor(getContext().getColor(R.color.colorAccent));
            } else if(val == 2) {
                this.setBackgroundColor(getContext().getColor(R.color.colorRed));
            } else if(val == 3) {
                this.setBackgroundColor(getContext().getColor(R.color.colorPrimary));
            }

            index = index + 1;
        }

        public AbsoluteFrameChildLayout(Context context, AttributeSet attrs) {
            this(context, attrs, 0);
        }

        public AbsoluteFrameChildLayout(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
        }
    }

    public static int randInt(int min, int max) {

        Random rand = new Random();

        // nextInt is normally exclusive of the top value,
        // so add 1 to make it inclusive
        int randomNum = rand.nextInt((max - min) + 1) + min;

        return randomNum;
    }
}
