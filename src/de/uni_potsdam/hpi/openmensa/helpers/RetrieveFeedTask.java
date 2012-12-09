package de.uni_potsdam.hpi.openmensa.helpers;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;

import de.uni_potsdam.hpi.openmensa.MainActivity;

/**
 * An abstract feed fetcher. Override the parseFromJSON and
 * onPostExecuteFinished methods in your concrete implementation.
 * 
 * @author dominik
 */
public abstract class RetrieveFeedTask extends AsyncTask<String, Integer, Integer> {
	private Exception exception;
	private ProgressDialog dialog;
	private Builder builder;
	protected Context context;
	protected Gson gson = new Gson();
	
	// show dialog while fetching
	protected Boolean visible = false;
	protected String name = "";
	
	public static final String TAG = MainActivity.TAG;
	public static final Boolean LOGV = MainActivity.LOGV;

	private final int DEFAULT_BUFFER_SIZE = 1024;

	public RetrieveFeedTask(Context context) {
		// progress dialog
		if (!visible)
			return;
		
		dialog = new ProgressDialog(context);
		dialog.setTitle("Fetching ...");
		if (name.length() > 0) {
			dialog.setMessage(String.format("Fetching the %s", name));
		}
		dialog.setIndeterminate(false);
		dialog.setMax(100);
		dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

		// error dialog that may be needed
		builder = new AlertDialog.Builder(context)
				.setNegativeButton("Okay",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
							}
						});
		;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		if (visible)
			dialog.show();
	}

	protected abstract void parseFromJSON(String jsonString);

	protected Integer doInBackground(String... urls) {
		for (String url : urls) {
			try {
				URL feed = new URL(url);
				URLConnection urlConnection = feed.openConnection();
				BufferedReader in = new BufferedReader(new InputStreamReader(
						urlConnection.getInputStream()));

				// urlConnection.connect();

				StringBuilder builder = new StringBuilder();
				long fileLength = urlConnection.getContentLength();
				long total = 0;
				int count;

				// content length is sometimes not sent
				if (visible) {
					if (fileLength < 0) {
						dialog.setIndeterminate(true);
					} else {
						dialog.setMax((int) fileLength);
					}
				}

				char buf[] = new char[DEFAULT_BUFFER_SIZE];
				while ((count = in.read(buf, 0, DEFAULT_BUFFER_SIZE)) > 0) {
					total += count;
					// publishing the progress....
					publishProgress((int) (total));
					builder.append(buf, 0, count);
				}

				handleJson(builder.toString());
			} catch (Exception ex) {
				this.exception = ex;
			}
		}
		return urls.length;
	}

	private void handleJson(String string) {
		parseFromJSON(string);
	}

	protected void onProgressUpdate(Integer... progress) {
		super.onProgressUpdate(progress);
		if (visible)
			dialog.setProgress(progress[0]);
	}

	protected void onPostExecute(Integer urlsCount) {
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
		builder.show();
	}
}
