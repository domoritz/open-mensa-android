package de.uni_potsdam.hpi.openmensa;

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

/**
 * An abstract feed fetcher. Override the parseFromJSON and
 * onPostExecuteFinished methods.
 * 
 * @author dominik
 */
abstract class RetrieveFeedTask extends AsyncTask<String, Integer, Integer> {
	private Exception exception;
	private ProgressDialog dialog;
	private Builder builder;
	protected Context context;
	protected Gson gson = new Gson();
	
	public static final String TAG = MainActivity.TAG;
	public static final Boolean LOGV = MainActivity.LOGV;

	private final int DEFAULT_BUFFER_SIZE = 1024;

	public RetrieveFeedTask(Context context) {
		// progress dialog
		dialog = new ProgressDialog(context);
		dialog.setMessage("Fetching");
		dialog.setIndeterminate(false);
		dialog.setMax(100);
		dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

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
		dialog.show();
	}

	protected abstract void parseFromJSON(String string);

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
				if (fileLength < 0) {
					dialog.setIndeterminate(true);
				}

				char buf[] = new char[DEFAULT_BUFFER_SIZE];
				while ((count = in.read(buf, 0, DEFAULT_BUFFER_SIZE)) > 0) {
					total += count;
					// publishing the progress....
					publishProgress((int) (total * 100 / fileLength));
					builder.append(buf, 0, count);
				}

				parseFromJSON(builder.toString());
			} catch (Exception ex) {
				this.exception = ex;
			}
		}
		return urls.length;
	}

	protected void onProgressUpdate(Integer... progress) {
		super.onProgressUpdate(progress);
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

		if (dialog.isShowing()) {
			dialog.dismiss();
		}
	}

	protected abstract void onPostExecuteFinished();

	public void showErrorMessage(Exception ex) {
		builder.setTitle(ex.getClass().getName());
		builder.setMessage(ex.toString());
		builder.show();
	}
}
