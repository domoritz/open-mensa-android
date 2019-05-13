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
import android.graphics.drawable.Drawable
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
import de.uni_potsdam.hpi.openmensa.api.preferences.SettingsUtils
import de.uni_potsdam.hpi.openmensa.databinding.CanteenFragmentBinding
import de.uni_potsdam.hpi.openmensa.ui.privacy.EnableMapDialogFragment
import org.osmdroid.tileprovider.tilesource.BitmapTileSourceBase
import java.io.InputStream

class CanteenFragment : Fragment(), OnClickListener {
    companion object {
        private val nullTileSource = object: BitmapTileSourceBase("dummy", 1, 1, 1, ".void") {
            override fun getDrawable(aFilePath: String?): Drawable? = null
            override fun getDrawable(aFileInputStream: InputStream?): Drawable? = null
        }
    }

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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = CanteenFragmentBinding.inflate(inflater, container, false)
        val view = binding.root// TODO: remove
        mapView = binding.mapview

        mapView!!.setTileSource(nullTileSource)
        SettingsUtils.isMapEnabledLive(context!!).observe(this, Observer { enableMap ->
            binding.flipper.displayedChild = if (enableMap) 1 else 0

            if (enableMap) {
                mapView!!.setTileSource(TileSourceFactory.MAPNIK)
            } else {
                mapView!!.setTileSource(nullTileSource)
            }
        })

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

        binding.enableMapButton.setOnClickListener {
            EnableMapDialogFragment().show(fragmentManager!!)
        }

        return binding.root
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

        // mapView!!.visibility = MapView.VISIBLE

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
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(uri)))
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context!!, resources.getString(R.string.nomapapp), Toast.LENGTH_LONG).show()
        }

    }
}
