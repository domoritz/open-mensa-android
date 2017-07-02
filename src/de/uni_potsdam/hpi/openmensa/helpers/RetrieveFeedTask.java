package de.uni_potsdam.hpi.openmensa.helpers;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import android.app.Activity;
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
public abstract class RetrieveFeedTask extends AsyncTask<String, Integer, Void> {
	protected Exception exception;
	protected ProgressDialog dialog;
	private Builder builder;
	protected Context context;
	protected Gson gson = new Gson();
	
	protected static Set<String> currentlyRequestingFrom = new HashSet<String>();
	
	// provided by the header "X-Total-Pages"
	protected Integer totalPages = null;
	
	// show dialog while fetching
	protected Boolean visible = false;
	protected String name = "";
	
	public static final String TAG = MainActivity.TAG;
	public static final Boolean LOGV = MainActivity.LOGV;

	private final int DEFAULT_BUFFER_SIZE = 1024;

    private Activity mActivity;

	public RetrieveFeedTask(Context context, Activity activity) {
        mActivity = activity;
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
		builder = new AlertDialog.Builder(context)
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

	protected abstract void parseFromJSON(String jsonString);

	protected Void doInBackground(String... urls) {
		if (urls.length != 1)
			throw new IllegalArgumentException("You need to provide exactly one.");


		String url = urls[0];
		if (currentlyRequestingFrom.contains(url)) {
			Log.d(TAG, String.format("We are already fetching data from %s. Let's skip it.", url));
		}
		currentlyRequestingFrom.add(url);
		Log.d(TAG, String.format("Fetching from %s", url));
		
		try {
			URL feed = new URL(url);
			HttpURLConnection urlConnection = (HttpURLConnection)feed.openConnection();
			BufferedReader in = new BufferedReader(new InputStreamReader(
					urlConnection.getInputStream()));

			long fileLength = urlConnection.getContentLength();
			long total = 0;
			int count;
			
			totalPages = urlConnection.getHeaderFieldInt("X-Total-Pages", -1);
			if (totalPages < 0) {
				totalPages = null;
			}

			// content length is sometimes not sent
			if (visible) {
				if (fileLength < 0) {
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dialog.setIndeterminate(true);
                        }
                    });

				} else {
					dialog.setMax((int) fileLength);
				}
			}
			
			StringBuilder builder = new StringBuilder();

			char buf[] = new char[DEFAULT_BUFFER_SIZE];
			while ((count = in.read(buf, 0, DEFAULT_BUFFER_SIZE)) > 0) {
				total += count;
				// publishing the progress....
				publishProgress((int) (total));
				builder.append(buf, 0, count);
			}
			
			parseFromJSON(builder.toString());
		} catch (Exception ex) {
			this.exception = ex;
		} finally {
			currentlyRequestingFrom.remove(url);
		}
		return null;
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

	protected void showErrorMessage(Exception ex) {
		builder.setTitle(ex.getClass().getName());
		builder.setMessage(ex.toString());
		if(!((Activity)MainActivity.getAppContext()).isFinishing()) {
			builder.show();
		}
	}
	
	public Boolean noPending() {
		return currentlyRequestingFrom.isEmpty();
	}
}
