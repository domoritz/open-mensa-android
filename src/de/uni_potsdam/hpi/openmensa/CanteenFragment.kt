package de.uni_potsdam.hpi.openmensa

import java.io.File

import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
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
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import de.uni_potsdam.hpi.openmensa.api.preferences.SettingsUtils
import de.uni_potsdam.hpi.openmensa.databinding.CanteenFragmentBinding
import de.uni_potsdam.hpi.openmensa.ui.privacy.EnableMapDialogFragment
import org.osmdroid.tileprovider.tilesource.BitmapTileSourceBase
import org.osmdroid.views.CustomZoomButtonsController
import java.io.InputStream

class CanteenFragment : Fragment() {
    companion object {
        private val nullTileSource = object: BitmapTileSourceBase("dummy", 1, 1, 1, ".void") {
            override fun getDrawable(aFilePath: String?): Drawable? = null
            override fun getDrawable(aFileInputStream: InputStream?): Drawable? = null
        }

        private val realTileSource = TileSourceFactory.MAPNIK
    }

    private val zoom = 18

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
        var lastLocation: GeoPoint? = null

        binding.addressContainer.setOnClickListener { openMapIntent() }
        binding.mapCopyright = realTileSource.copyrightNotice

        binding.mapview.setTileSource(nullTileSource)
        binding.mapview.zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
        binding.mapview.setMultiTouchControls(true)

        SettingsUtils.isMapEnabledLive(context!!).observe(this, Observer { enableMap ->
            binding.flipper.displayedChild = if (enableMap) 1 else 0

            if (enableMap) {
                binding.mapview.setTileSource(realTileSource)
            } else {
                binding.mapview.setTileSource(nullTileSource)
            }
        })

        mainActivityModel.currentlySelectedCanteen.observe(this, Observer {
            val canteen = it?.canteen

            binding.name = canteen?.name
            binding.address = canteen?.address

            binding.mapview.overlays.clear()

            if (canteen != null) {
                val center = GeoPoint(canteen.latitude, canteen.longitude)

                val canteenLocation = OverlayItem(canteen.name, canteen.address, center).apply {
                    setMarker(resources.getDrawable(R.drawable.marker_blue))
                }

                if (lastLocation != center) {
                    binding.mapview.controller.setZoom(zoom)
                    binding.mapview.controller.setCenter(center)

                    lastLocation = center
                }

                binding.mapview.overlays.add(
                        ItemizedIconOverlay(
                                // crashes if the list is not mutable when leaving
                                listOf(canteenLocation).toMutableList(),
                                object : ItemizedIconOverlay.OnItemGestureListener<OverlayItem> {
                                    override fun onItemSingleTapUp(index: Int, item: OverlayItem): Boolean {
                                        return true
                                    }

                                    override fun onItemLongPress(index: Int, item: OverlayItem): Boolean {
                                        return true
                                    }
                                }, context!!)
                )
            }
        })

        binding.enableMapButton.setOnClickListener {
            EnableMapDialogFragment().show(fragmentManager!!)
        }

        return binding.root
    }

    private fun openMapIntent() {
        mainActivityModel.currentlySelectedCanteen.value?.canteen?.let { canteen ->
            val latlon = "${canteen.latitude},${canteen.longitude}"
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
}
