package com.chthakur.playapp;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.chthakur.playapp.Logger.ALog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Provides layout based on absolute positions, ensure all the items are under single parent child.
 * Optimized for moving views around without having to change the parent.
 */
public class CallAbsoluteGridLayout extends FrameLayout  {

    /**
     * Valid layout types
     */
    public enum LayoutType {
        BIG_SINGLE,

        GRID_FOUR,

        UNKNOWN;

        static List<LayoutType> getOrderedValidValuesList() {
            List<LayoutType> list = new ArrayList<>();
            list.add(BIG_SINGLE);
            list.add(GRID_FOUR);
            return list;
        }
    }

    public static final int MIN_THUMBNAIL_DENSITY = 4;

    public static final int THUMBNAIL_MARGIN_DP = 8;

    private int thumbnailDensity = 6;

    private static final String TAG = CallAbsoluteGridLayout.class.getSimpleName();

    private List<Rect> layoutChildRectList;

    private LayoutType currentLayout = LayoutType.BIG_SINGLE;

    private int displayWidth = 0;

    private int displayHeight = 0;

    private boolean isLandscape = false;

    private int thumbMargin;

    private static int thumbW;

    private static int thumbH;

    private static boolean forceThumbHide = false;

    private List<CallAbsoluteGridItemLayout> childFrameList = new ArrayList<>();

    private Rect thumbListBoundingRect;

    public CallAbsoluteGridLayout(Context context) {
        this(context, null, 0);
    }

    public CallAbsoluteGridLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CallAbsoluteGridLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        // to start with we have device dimensions, setLayoutRect should be
        // able to refresh the view on any changes
        thumbMargin = UtilsDevice.getPixelsFromDp(context, THUMBNAIL_MARGIN_DP);
        this.isLandscape = UtilsDevice.isLandscapeOrientation(getContext());
        displayWidth = UtilsDevice.getDisplayWidth(getContext());
        displayHeight = UtilsDevice.getDisplayHeight(getContext());
        setThumbnailDensity(thumbnailDensity);
        // we want to control the z-index
        this.setChildrenDrawingOrderEnabled(true);
    }

    /**
     * This defines the outer rectangle for the wrapper parent to operate in
     * @return true if the layout was dirty and required refreshing
     */
    public boolean setLayoutRect(int left, int top, int right, int bottom) {
        int newParentWidth = right - left;
        int newParentHeight = bottom - top;
        boolean isDirty = false;
        if(displayWidth != newParentWidth || displayHeight != newParentHeight) {
            displayWidth = newParentWidth;
            displayHeight = newParentHeight;
            // we want to refresh in this case
            refreshLayout();
            isDirty = true;
        }

        return isDirty;
    }

    /**
     * Recalculate the layout rectangle list
     * @param nElements number of elements to show
     * @return list of layout rectangles
     */
    private List<Rect> updateLayoutChildRectList(int nElements) {
        return updateLayoutChildRectList(currentLayout, nElements);
    }

    /**
     * Recalculate the layout rectangles based on given layout
     * @param layoutType the layoutType impacting the rectangle list
     * @param nElements number of elements in total to add
     * @return List of layout rectangles
     */
    private List<Rect> updateLayoutChildRectList(LayoutType layoutType, int nElements) {
        int maxMainElements = (layoutType == LayoutType.BIG_SINGLE) ? 1 : 4;
        int mainElements = nElements > maxMainElements ? maxMainElements : nElements;
        layoutChildRectList = getRectangleList(displayWidth, displayHeight, mainElements, nElements, this.isLandscape);
        return layoutChildRectList;
    }

    /**
     * Refresh the layout based on the parameter layoutType
     * @param layoutType the layoutType to refresh with. This involves recalculating the rectangles
     */
    public void refreshLayout(LayoutType layoutType) {
        this.currentLayout = layoutType;
        int nElements = layoutChildRectList.size();
        // get existing child list in layout order
        layoutChildRectList = updateLayoutChildRectList(layoutType, nElements);
        applyLayoutRectToLayouts();
    }

    /**
     * Show thumbs, ensure zIndex to be higher than main frame elements
     */
    public void showThumbs() {
        ALog.i(TAG, "showThumbs");
        forceThumbHide = false;
        this.invalidate();
    }

    /**
     * Hide thumbs, ensure zIndex to be lower than main frame elements
     */
    public void hideThumbs() {
        ALog.i(TAG, "hideThumbs");
        forceThumbHide = true;
        this.invalidate();
    }

    public void setLayout(LayoutType layoutType) {
        this.currentLayout = layoutType;
    }

    public LayoutType getLayout() {
        return this.currentLayout;
    }

    public void refreshLayout() {
        this.refreshLayout(currentLayout);
    }

    public CallAbsoluteGridItemLayout addFrame(int position) {

        if(position > childFrameList.size()) {
            return null;
        }

        // get existing child list in layout order
        int newSize = childFrameList.size() + 1;
        layoutChildRectList = updateLayoutChildRectList(newSize);
        Rect rect = layoutChildRectList.get(position);
        CallAbsoluteGridItemLayout frameLayout = new CallAbsoluteGridItemLayout(getContext(), rect);
        childFrameList.add(position, frameLayout);
        this.addView(frameLayout, position);
        // Re-assign rectangles based on new layout, layout changes on adding an element
        applyLayoutRectToLayouts();
        frameLayout.setOnTouchListener(onThumbListTouchListener);
        return frameLayout;
    }

    public void removeItem(CallAbsoluteGridItemLayout frameLayout) {
        if(frameLayout.getParent() == null) {
            return;
        }

        // we need to be the parent
        this.removeView(frameLayout);
        childFrameList.remove(frameLayout);
        if(childFrameList.size() > 0) {
            // get existing child list in layout order
            layoutChildRectList = updateLayoutChildRectList(childFrameList.size());
            applyLayoutRectToLayouts();
        }
    }

    /**
     * User is expected to refresh layout in case required.
     * Default is to just set the values and let the consumer decide what to do with refresh
     * This helps performance
     * @param nThumbs, number of thumbs per screen
     */
    public void setThumbnailDensity(int nThumbs) {
        float thumbAspect = displayWidth > displayHeight ? 1/1.33f : 1.33f;
        nThumbs = getNormalizeDensityForOrientation(nThumbs, thumbAspect);
        float totalMargin = (nThumbs + 1) * thumbMargin;
        float widthForThumbs = displayWidth - totalMargin;
        thumbW = (int)((float)widthForThumbs/nThumbs);
        thumbH = (int)((float) thumbW * thumbAspect);
        thumbnailDensity = nThumbs;
    }

    /**
     * The values set for density are considering portrait orientation. We normalize the values
     * for landscape if it is landscape
     * @param nThumbs nThumbs in portrait dimension across width
     * @param thumbAspect Aspect ratio for the thumb
     * @return Number of thumbs for this particular orientation
     */
    private int getNormalizeDensityForOrientation(int nThumbs, float thumbAspect) {
        if(isLandscape) {
            nThumbs = (int)((float)nThumbs * thumbAspect * ((float)displayWidth/displayHeight));
        }

        return nThumbs;
    }

    /**
     * App the layout to all the child items.
     * May be required when layout goes dirty
     */
    private void applyLayoutRectToLayouts() {
        // rect should be in order
        Iterator<Rect> iterable = layoutChildRectList.iterator();
        for(CallAbsoluteGridItemLayout frame: childFrameList) {
            Rect rect = iterable.next();
            frame.applyRect(rect);
        }
    }

    /**
     * Swap two frames, change their corresponding rectangles essentially
     * @param frame1 first frame
     * @param frame2 second frame
     */
    public void swap(CallAbsoluteGridItemLayout frame1, CallAbsoluteGridItemLayout frame2) {
        Rect frame1Rect = new Rect(frame1.getRect());
        frame1.applyRect(frame2.getRect());
        frame2.applyRect(frame1Rect);
        Collections.swap(childFrameList, childFrameList.indexOf(frame1), childFrameList.indexOf(frame2));
    }

    /**
     * Get the least recently used frame, required for swapping the thumb with main frame
     * @return the least recently used frame
     */
    public CallAbsoluteGridItemLayout getLRUMainFrame() {
        long minTimestamp = Long.MAX_VALUE;
        CallAbsoluteGridItemLayout lruFrame = null;
        for(int kk = 0; kk < childFrameList.size(); kk++) {
            CallAbsoluteGridItemLayout frame = childFrameList.get(kk);
            if(isThumbFrame(frame)) {
                continue;
            }

            Long rectModifiedTS = frame.getRectModifiedTS();
            if(rectModifiedTS < minTimestamp) {
                minTimestamp = rectModifiedTS;
                lruFrame = frame;
            }
        }

        return lruFrame;
    }

    /**
     * Get the first frame as per layout, this can be extended to nth frame but not required  as of now
     * @return the first frame in the layout
     */
    public CallAbsoluteGridItemLayout getFirstMainFrame() {
        if(layoutChildRectList == null || layoutChildRectList.size() < 1) {
            return null;
        }

        CallAbsoluteGridItemLayout firstFrame = null;
        Rect firstFrameRect = layoutChildRectList.get(0);
        for(int kk = 0; kk < childFrameList.size(); kk++) {
            CallAbsoluteGridItemLayout frame = childFrameList.get(kk);
            if(UtilsMath.areRectSame(frame.getRect(), firstFrameRect)) {
                firstFrame = frame;
                break;
            }
        }

        return firstFrame;
    }

    /**
     * Detect if this particular child frame is thumb frame of not
     * @param childFrame frame to check for thumb or not
     * @return true if it is thumb frame
     */
    public boolean isThumbFrame(CallAbsoluteGridItemLayout childFrame) {
        Rect rect = childFrame.getRect();
        if (rect.width() == thumbW && rect.height() == thumbH) {
            return true;
        }

        return false;
    }

    /**
     * Check if the given rect is thumb size or not
     * @param rect to check for thumb size
     * @return  true if it is thumb size
     */
    public boolean isThumbSize(Rect rect) {
        if (rect.width() == thumbW && rect.height() == thumbH) {
            return true;
        }

        return false;
    }

    /**
     * Get the index of the main frame in the list
     * @param frameLayout frame to find the index for
     * @return the index in the layout list
     */
    public int getMainFrameIdx(CallAbsoluteGridItemLayout frameLayout) {
        if(layoutChildRectList == null) {
            return -1;
        }

        Rect frameRect = frameLayout.getRect();
        // Rectangles are unique and ordered
        int ii = 0;
        for(Rect rect: layoutChildRectList) {
            if(UtilsMath.areRectSame(frameRect, rect)) {
                return ii;
            }

            ii = ii + 1;
        }

        return -1;
    }

    /**
     * Get the frame given the rectangle, all rectangles are unique in the list.
     * @param frameRect Rectangle for which we want the child
     * @return the frame associated with the particular rectangle
     */
    public CallAbsoluteGridItemLayout getFrameForRect(Rect frameRect) {
        if(childFrameList == null) {
            return null;
        }

        for(CallAbsoluteGridItemLayout frame: childFrameList) {
            if(UtilsMath.areRectSame(frameRect, frame.getRect())) {
                return frame;
            }
        }

        return null;
    }

    /**
     * Return the rectangle list given the parameters
     * @param w width of the parent
     * @param h height of the parent
     * @param maxMainCount - maximum main elements in the grid
     * @param maxCount - total max elements in the grid
     * @param isLandscape - orientation of the grid
     * @return list of rectangles based on parameters given
     */
    private List<Rect> getRectangleList(int w, int h, int maxMainCount, int maxCount, boolean isLandscape) {
        HashMap<Integer, List<Rect>> rectangleMap = new HashMap<>();
        List<Rect> rectList1 = new ArrayList<>();
        rectList1.add(new Rect(0, 0, w, h));

        List<Rect> rectList2 = new ArrayList<>();
        if(!isLandscape) {
            rectList2.add(new Rect(0, 0, w, h / 2));
            rectList2.add(new Rect(0, h / 2, w, h));
        } else {
            rectList2.add(new Rect(0, 0, w/2, h));
            rectList2.add(new Rect(w/2, 0, w, h));
        }

        List<Rect> rectList3 = new ArrayList<>();
        if(!isLandscape) {
            rectList3.add(new Rect(0, 0, w, h / 2));
            rectList3.add(new Rect(0, h / 2, w / 2, h));
            rectList3.add(new Rect(w / 2, h / 2, w, h));
        } else {
            rectList3.add(new Rect(0, 0, w/2, h));
            rectList3.add(new Rect(w/2, 0, w , h/2));
            rectList3.add(new Rect(w / 2, h / 2, w, h));
        }

        List<Rect> rectList4 = new ArrayList<>();
        if(!isLandscape) {
            rectList4.add(new Rect(0, 0, w / 2, h / 2));
            rectList4.add(new Rect(w / 2, 0, w, h / 2));
            rectList4.add(new Rect(0, h / 2, w / 2, h));
            rectList4.add(new Rect(w / 2, h / 2, w, h));
        } else {
            rectList4.add(new Rect(0, 0, w/2, h/2));
            rectList4.add(new Rect(0, h/2, w/2, h));
            rectList4.add(new Rect(w/2, 0, w , h/2));
            rectList4.add(new Rect(w / 2, h / 2, w, h));
        }

        rectangleMap.put(1, rectList1);
        rectangleMap.put(2, rectList2);
        rectangleMap.put(3, rectList3);
        rectangleMap.put(4, rectList4);

        List<Rect> result = rectangleMap.get(maxMainCount);
        int thumbsNeeded =  maxCount - maxMainCount;
        if(thumbsNeeded < 1) {
            return result;
        }

        thumbListBoundingRect = getBottomThumbListBoundingRect(w, h, thumbsNeeded);
        needThumbScrolling = thumbsNeeded > thumbnailDensity ? true : false;
        maxScrollDistance = 0 - thumbListBoundingRect.left; // keep the 0 to suggest left of root frame
        List<Rect> thumbRectList = applyScrollXToThumbRectList(getThumbRectList(thumbListBoundingRect, thumbsNeeded), totalDiffX);
        result.addAll(thumbRectList);
        return result;
    }

    private List<Rect> applyScrollXToThumbRectList(List<Rect> rectList, float scrollX) {
        for(Rect rect: rectList) {
            rect.left = rect.left + (int)scrollX;
            rect.right = rect.right + (int)scrollX;
        }

        return rectList;
    }

    /**
     * @param w width of full frame
     * @param h height of full frame
     * @param count number of thumbnails to show
     * @return Bounding rect for bottom list
     */
    private Rect getBottomThumbListBoundingRect(int w, int h, int count) {
        Rect boundingRect = new Rect(w - count*(thumbW + thumbMargin), h - thumbH - 2*thumbMargin, w, h);
        return boundingRect;
    }

    /**
     * @param w width of full frame
     * @param h height of full frame
     * @param count number of thumbnails to show
     * @return Bounding rect for top list
     */
    private List<Rect> getTopThumbListBoundingRect(int w, int h, int count) {
        Rect boundingRect = new Rect(w - count*(thumbW + thumbMargin), 0, w, thumbH + 2*thumbMargin);
        return getThumbRectList(boundingRect, count);
    }

    /**
     * Generate list of rectangles within bounding Rect
     * @param boundingRect bounding rectangle for the thumbRectList
     * @param count number of thumbs
     * @return List of rectangles
     */
    private List<Rect> getThumbRectList(Rect boundingRect, int count) {
        List<Rect> rectList = new ArrayList();
        int topY = boundingRect.top + thumbMargin;
        int endW = boundingRect.right - thumbMargin;
        for(int kk = 0; kk < count; kk++) {
            Rect thumbRect = new Rect(endW - thumbW, topY, endW, topY + thumbH);
            endW = endW - thumbW - thumbMargin;
            rectList.add(thumbRect);
        }

        return rectList;
    }

    private float positionX = 0;
    private float totalDiffX = 0;
    private boolean needThumbScrolling;
    private float maxScrollDistance = 0;

    private OnTouchListener onThumbListTouchListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if(!needThumbScrolling || !isThumbFrame((CallAbsoluteGridItemLayout)v)) {
                return false;
            }

            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    positionX = event.getRawX();
                    break;
                case MotionEvent.ACTION_MOVE:
                    float diffX =  event.getRawX() - positionX;
                    totalDiffX =  totalDiffX + diffX;
                    totalDiffX = totalDiffX > maxScrollDistance ? maxScrollDistance : totalDiffX;
                    totalDiffX = totalDiffX < 0.0f ? 0.0f : totalDiffX;
                    positionX = event.getRawX();
                    if(totalDiffX > 0.00000001f) {
                        refreshLayout();
                        ALog.i("onThumbListTouchListener:onTouch:", "totalDiffX:" + totalDiffX);
                    }
                    break;
                case MotionEvent.ACTION_UP:
                default:
                    return true;
            }

            return false;
        }
    };

    /**
     * chthakur: Read => Which child index should I draw ith ?
     * Might seem suboptimal but keeping it simple helps.
     * No impact on performance in the scenario we plan to use this in
     * @param childCount total child count
     * @param ii draw index
     * @return return the frame to draw at ii index
     */
    @Override
    protected int getChildDrawingOrder (int childCount, int ii) {
        int nChild = this.getChildCount();
        List<CallAbsoluteGridItemLayout> thumbList = new ArrayList<>();
        List<CallAbsoluteGridItemLayout> mainList = new ArrayList<>();
        for(int kk = 0; kk < nChild; kk++) {
            CallAbsoluteGridItemLayout frame = (CallAbsoluteGridItemLayout)this.getChildAt(kk);
            if(isThumbFrame(frame)) {
                thumbList.add(frame);
            } else {
                mainList.add(frame);
            }
        }

        List<CallAbsoluteGridItemLayout> combinedList = new ArrayList<>();
        // if hide thumbs, draw them first
        if(forceThumbHide) {
            combinedList.addAll(thumbList);
            combinedList.addAll(mainList);
        } else {
            combinedList.addAll(mainList);
            combinedList.addAll(thumbList);
        }

        CallAbsoluteGridItemLayout frameAtIndex = combinedList.get(ii);
        int result = this.indexOfChild(frameAtIndex);
        //ALog.d(TAG, "getChildDrawingOrder: childCount(%d), order(%d), result(%d)", childCount, ii, result);
        return result;
    }
}
