package de.uni_potsdam.hpi.openmensa.helpers;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import de.uni_potsdam.hpi.openmensa.MainActivity;

public abstract class RetrieveAsyncTask extends AsyncTask<String, Integer, Void> {
	protected Exception exception;
	protected ProgressDialog dialog;
	private Builder builder;
	protected Context context;

	// show dialog while fetching
	protected Boolean visible = false;
	protected String name = "";

	public static final String TAG = MainActivity.Companion.getTAG();
	public static final Boolean LOGV = MainActivity.Companion.getLOGV();

	private final int DEFAULT_BUFFER_SIZE = 1024;

	private Activity mActivity;

	public RetrieveAsyncTask(Context context, Activity activity) {
		mActivity = activity;
		this.context = context;

		// progress dialog
		dialog = new ProgressDialog(context);
		dialog.setTitle("Fetching ...");
		if (name.length() > 0) {
			dialog.setMessage(String.format("Fetching the %s", name));
		}
		dialog.setIndeterminate(false);
		dialog.setMax(100);
		dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

		// error dialog that may be needed
		builder = new Builder(context)
			.setNegativeButton("Okay",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.cancel();
						}
					});
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		if (visible)
			dialog.show();
	}

	protected void onProgressUpdate(Integer... progress) {
		super.onProgressUpdate(progress);
		if (visible)
			dialog.setProgress(progress[0]);
	}

	protected void onPostExecute(Void v) {
		if (this.exception != null) {
			Log.w(TAG, "Exception: " + exception.getMessage());
			if (LOGV) {
				Log.d(TAG, Log.getStackTraceString(exception));
			}
			showErrorMessage(this.exception);
		} else {
			onPostExecuteFinished();
		}

		if (visible){
			if (dialog.isShowing()) {
				dialog.dismiss();
			}
		}
	}

	protected abstract void onPostExecuteFinished();

	private void showErrorMessage(Exception ex) {
		builder.setTitle(ex.getClass().getName());
		builder.setMessage(ex.toString());
		if(!((Activity)MainActivity.Companion.getAppContext()).isFinishing()) {
			builder.show();
		}
	}
}
