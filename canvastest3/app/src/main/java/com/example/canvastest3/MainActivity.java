package com.example.canvastest3;

import android.app.ActionBar.OnNavigationListener;
import android.app.Activity;
import android.app.ActionBar;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ArrayAdapter;

public class MainActivity extends Activity
        implements OnNavigationListener {

    CanvasTest3View view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        view = new CanvasTest3View(this);
        setContentView(view);

        getActionBar().setDisplayShowTitleEnabled(false);
        getActionBar().setNavigationMode(
                ActionBar.NAVIGATION_MODE_LIST);
        ArrayAdapter<CharSequence> adapter
                = ArrayAdapter.createFromResource(
                this,
                R.array.color_array,
                R.layout.actionbar_spinner);
        adapter.setDropDownViewResource(
                R.layout.actionbar_spinner_dropdown);
        getActionBar().setListNavigationCallbacks(adapter, this);
    }

    @Override
    public boolean onNavigationItemSelected(
            int itemPosition, long itemId) {
        int color = Color.WHITE;
        switch(itemPosition){
            case 0:
                color = Color.BLUE;
                break;
            case 1:
                color = Color.GREEN;
                break;
            case 2:
                color = Color.RED;
                break;
        }
        view.setColor(color);
        return true;
    }


}