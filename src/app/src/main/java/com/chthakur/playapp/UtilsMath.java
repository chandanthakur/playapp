package com.chthakur.playapp;

import android.graphics.Rect;
import android.support.v7.app.AppCompatActivity;

public class UtilsMath extends AppCompatActivity {

    public static boolean areRectSame(Rect rect1, Rect rect2) {
        if(rect1.left == rect2.left
                && rect1.top == rect2.top
                && rect1.right == rect2.right
                && rect1.bottom == rect2.bottom) {
            return true;
        }

        return false;
    }
}
