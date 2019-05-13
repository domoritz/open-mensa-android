package de.uni_potsdam.hpi.openmensa.helpers;

import org.osmdroid.views.MapView;

import android.content.Context;
import androidx.viewpager.widget.ViewPager;
import android.util.AttributeSet;
import android.view.View;

public class CustomViewPager extends ViewPager {

    public CustomViewPager(Context context) {
        super(context);
    }

    public CustomViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected boolean canScroll(View v, boolean checkV, int dx, int x, int y) {
        if(v instanceof MapView){
            return true;
        }
        return super.canScroll(v, checkV, dx, x, y);
    }

}
