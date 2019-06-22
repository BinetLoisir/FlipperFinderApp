package com.pinmyballs.utils;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import androidx.appcompat.app.AppCompatActivity;

import com.pinmyballs.PreferencesActivity;
import com.pinmyballs.database.FlipperDatabaseHandler;

public class AsyncTaskInitDatabase extends AsyncTask<Object, Void, Boolean> {

	private AppCompatActivity mContext;
	private SharedPreferences mSettings;
	private String retourMaj = null;
	ProgressDialog mDialog = null;

	public AsyncTaskInitDatabase(AppCompatActivity context, SharedPreferences settings){
		mContext = context;
		mSettings = settings;
	}

	@Override
	protected void onPreExecute()
	{
		mDialog = ProgressDialog.show(mContext, "Initialisation", "Initialisation de l'appli. Merci de patienter :-)", true);
		super.onPreExecute();
	}

	@Override
	protected Boolean doInBackground(Object... params) {
		//GlobalService globalService = new GlobalService(mContext);
		//globalService.reinitDatabase();
		SharedPreferences.Editor editor = mSettings.edit();
        editor.putString(PreferencesActivity.KEY_PREFERENCES_DATE_LAST_UPDATE, FlipperDatabaseHandler.DATABASE_DATE_MAJ);
        editor.putString(PreferencesActivity.KEY_PREFERENCES_DATABASE_VERSION, String.valueOf(FlipperDatabaseHandler.DATABASE_VERSION));
		editor.apply();
		return true;
	}

	@Override
	protected void onPostExecute(Boolean result) {
		mDialog.dismiss();
		super.onPostExecute(result);
	}
}
