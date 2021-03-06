package de.uni_potsdam.hpi.openmensa.ui.privacy

import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import de.uni_potsdam.hpi.openmensa.R
import de.uni_potsdam.hpi.openmensa.api.DefaultApiUrl
import de.uni_potsdam.hpi.openmensa.ui.settings.SettingsActivity
import de.uni_potsdam.hpi.openmensa.helpers.SettingsUtils
import de.uni_potsdam.hpi.openmensa.sync.CanteenSyncing

class PrivacyDialogFragment: DialogFragment() {
    companion object {
        private const val DIALOG_TAG = "PrivacyDialogFragment"
        private const val REQUEST_SETTINGS = 1

        fun showIfRequired(activity: FragmentActivity) {
            if (SettingsUtils.with(activity).sourceUrl.isBlank()) {
                if (activity.supportFragmentManager.findFragmentByTag(DIALOG_TAG) == null) {
                    PrivacyDialogFragment().show(activity.supportFragmentManager, DIALOG_TAG)
                }
            }
        }
    }

    private val serverUrl = if (DefaultApiUrl.NEEDS_UNSAFE_URL)
        DefaultApiUrl.UNSAFE_URL
    else
        DefaultApiUrl.SAFE_URL

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog = AlertDialog.Builder(context!!, theme)
            .setTitle(R.string.privacy_dialog_title)
            .setMessage(
                    getString(R.string.privacy_dialog_text, serverUrl) +
                            if (DefaultApiUrl.NEEDS_UNSAFE_URL)
                                " " + getString(R.string.privacy_dialog_warning_plaintext)
                            else
                                ""
            )
            .setPositiveButton(R.string.privacy_dialog_accept) { _, _ ->
                SettingsUtils.with(context!!).sourceUrl = serverUrl
                CanteenSyncing.runBackgroundSync(context!!)
            }
            .setNeutralButton(R.string.privacy_dialog_settings) { _, _ ->
                // configured below because it should not close the dialog
            }
            .create()
            .apply {
                setOnShowListener {
                    getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener {
                        startActivityForResult(Intent(context, SettingsActivity::class.java), REQUEST_SETTINGS)
                    }
                }
            }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_SETTINGS) {
            if (!SettingsUtils.with(context!!).sourceUrl.isBlank()) {
                dismiss()
            }
        }
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)

        activity!!.finish()
    }
}