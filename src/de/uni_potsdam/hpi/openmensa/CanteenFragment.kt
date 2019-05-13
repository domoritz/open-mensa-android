package de.uni_potsdam.hpi.openmensa

import java.io.File
import java.util.ArrayList

import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.ItemizedIconOverlay
import org.osmdroid.views.overlay.OverlayItem

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.Observer

class CanteenFragment : Fragment(), OnClickListener {

    private var mapView: MapView? = null
    private var overlay: ItemizedIconOverlay<OverlayItem>? = null

    private val zoom = 18
    private var center: GeoPoint? = null

    private val mainActivity: MainActivity by lazy { activity as MainActivity }
    private val mainActivityModel: MainModel by lazy { mainActivity.model }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID
        Configuration.getInstance().osmdroidTileCache = File(
                context!!.cacheDir,
                "map tiles"
        )
        Configuration.getInstance().tileFileSystemCacheMaxBytes = (1024 * 1024 * 10).toLong()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.canteen_fragment, container, false)

        mapView = view.findViewById<View>(R.id.mapview) as MapView
        mapView!!.setTileSource(TileSourceFactory.MAPNIK)
        mapView!!.setBuiltInZoomControls(false)
        mapView!!.setMultiTouchControls(true)

        mapView!!.visibility = MapView.INVISIBLE

        val address = view.findViewById<View>(R.id.txtAddress) as TextView
        address.setOnClickListener(this)

        val title = view.findViewById<View>(R.id.txtName) as TextView
        title.setOnClickListener(this)

        mainActivityModel.currentlySelectedCanteen.observe(this, Observer {
            refresh()
        })

        return view
    }

    override fun onResume() {
        refresh()
        super.onResume()
    }

    fun refresh() {
        // TODO: refactor this
        val canteen = mainActivityModel.currentlySelectedCanteen.value?.canteen

        if (isDetached || !isAdded)
            return

        if (canteen == null)
            return

        mapView!!.visibility = MapView.VISIBLE

        val address = view!!.findViewById<View>(R.id.txtAddress) as TextView
        address.text = canteen.address

        val name = view!!.findViewById<View>(R.id.txtName) as TextView
        name.text = canteen.name

        mapView!!.controller.setZoom(zoom)
        val lat = (canteen.latitude * 1E6).toInt()
        val lon = (canteen.longitude * 1E6).toInt()

        center = GeoPoint(lat, lon)
        mapView!!.controller.setCenter(center)

        val canteenLocation = OverlayItem(canteen.name, canteen.address, center)
        val canteenMarker = this.resources.getDrawable(R.drawable.marker_blue)
        canteenLocation.setMarker(canteenMarker)

        val items = ArrayList<OverlayItem>()
        items.add(canteenLocation)

        overlay = ItemizedIconOverlay(items,
                object : ItemizedIconOverlay.OnItemGestureListener<OverlayItem> {
                    override fun onItemSingleTapUp(index: Int, item: OverlayItem): Boolean {
                        return true
                    }

                    override fun onItemLongPress(index: Int, item: OverlayItem): Boolean {
                        return true
                    }
                }, context!!)
        this.mapView!!.overlays.clear()
        this.mapView!!.overlays.add(this.overlay)
    }

    override fun onClick(v: View) {
        openMapIntent()
    }

    private fun openMapIntent() {
        val canteen = mainActivityModel.currentlySelectedCanteen.value?.canteen

        if (center == null || canteen == null)
            return

        val lat = center!!.latitudeE6 / 1E6
        val lon = center!!.longitudeE6 / 1E6
        val latlon = "$lat,$lon"
        val uri = "geo:" + latlon +
                "?z=" + zoom +
                "&q=" + latlon + "(" + canteen.name + ")"
        try {
            startActivity(Intent(android.content.Intent.ACTION_VIEW, Uri.parse(uri)))
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(MainActivity.appContext, resources.getString(R.string.nomapapp), Toast.LENGTH_LONG).show()
        }

    }
}
