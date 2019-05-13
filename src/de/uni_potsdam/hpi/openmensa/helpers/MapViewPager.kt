package de.uni_potsdam.hpi.openmensa.helpers

import org.osmdroid.views.MapView

import android.content.Context
import androidx.viewpager.widget.ViewPager
import android.util.AttributeSet
import android.view.View

class MapViewPager : ViewPager {
    constructor(context: Context) : super(context) {}
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    override fun canScroll(v: View, checkV: Boolean, dx: Int, x: Int, y: Int): Boolean {
        return if (v is MapView) {
            true
        } else super.canScroll(v, checkV, dx, x, y)
    }
}
