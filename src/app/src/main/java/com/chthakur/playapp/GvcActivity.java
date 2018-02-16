package com.chthakur.playapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

public class GvcActivity extends AppCompatActivity {
    CallLayoutWrapper gridView;

    boolean doOnce = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gvc_activity);
        gridView = (CallLayoutWrapper)findViewById(R.id.gvc_grid_root);

    }

    @Override
    public void onResume() {
        super.onResume();
        if(gridView != null && doOnce) {
            gridView.scheduleTest();
            doOnce = false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_scrolling, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
