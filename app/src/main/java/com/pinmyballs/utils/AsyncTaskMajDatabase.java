package com.pinmyballs.utils;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.pinmyballs.PreferencesActivity;
import com.pinmyballs.R;
import com.pinmyballs.database.FlipperDatabaseHandler;
import com.pinmyballs.service.GlobalService;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AsyncTaskMajDatabase extends AsyncTask<Object, Void, Boolean> {

	private AppCompatActivity mContext;
	private SharedPreferences mSettings;
	private String retourMaj = null;
	ProgressDialog mDialog = null;

	public AsyncTaskMajDatabase (AppCompatActivity context, SharedPreferences settings){
		mContext = context;
		mSettings = settings;
	}

	@Override
	protected void onPreExecute()
	{
		mDialog = new ProgressDialog(mContext);
		mDialog.setMessage(mContext.getResources().getString(R.string.dialogMajDBMEssage));
		mDialog.setTitle(mContext.getResources().getString(R.string.dialogMajDBTitle));
		mDialog.setCancelable(false);
		mDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Annuler", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				cancel(true);
				dialog.dismiss();
			}
		});
		mDialog.setIndeterminate(true);
		mDialog.show();

		super.onPreExecute();
	}

	@Override
	protected Boolean doInBackground(Object... params) {
		GlobalService globalService = new GlobalService();
		Editor editor = mSettings.edit();
		try {
			String dateDerniereMajString;
            dateDerniereMajString = mSettings.getString(PreferencesActivity.KEY_PREFERENCES_DATE_LAST_UPDATE, FlipperDatabaseHandler.DATABASE_DATE_MAJ);
			retourMaj = globalService.majBaseAvecNouveaute(mContext, dateDerniereMajString);

			if (retourMaj != null){
				// La màj s'est bien passée, on mémorise la date de mise à jour dans les Préférences.
				DateFormat df = new SimpleDateFormat("yyyy/MM/dd", Locale.FRANCE);
				Date today = Calendar.getInstance().getTime();
				String dateDuJour = df.format(today);
                editor.putString(PreferencesActivity.KEY_PREFERENCES_DATE_LAST_UPDATE, dateDuJour);
                editor.putString(PreferencesActivity.KEY_PREFERENCES_DATABASE_VERSION, String.valueOf(FlipperDatabaseHandler.DATABASE_VERSION));
				editor.apply();
			}
		} catch (InterruptedException ie){
			String a = "a";
		} catch (RuntimeException re){
			String a = "a";
		} catch (Exception e) {
			// Erreur trappée. On efface la base, elle sera réinitialisée au prochain appel, et on
			// set la date de mise à jour à la valeur par défaut.
            //EasyTracker.getTracker().sendEvent("ui_error", "MAJ_DB_ERROR", "PreferencesActivity", 0L);
			mContext.deleteDatabase(FlipperDatabaseHandler.FLIPPER_BASE_NAME);
            editor.putString(PreferencesActivity.KEY_PREFERENCES_DATE_LAST_UPDATE, FlipperDatabaseHandler.DATABASE_DATE_MAJ);
			editor.commit();
			return false;
		}
		return true;
	}

	@Override
	protected void onPostExecute(Boolean result) {


		mDialog.dismiss();

		// S'il y a eu une exception, on affiche le message d'erreur et on se casse
		if (!result){
			Toast toast = Toast.makeText(mContext, mContext.getResources().getString(R.string.toastMajEchec), Toast.LENGTH_SHORT);
			toast.show();
			return;
		}

		// Sinon on affiche la popup de màj où le message disant que la base est à jour.
		if (retourMaj != null){
			new AlertDialog.Builder(mContext).setTitle(mContext.getResources().getString(R.string.titrePopupRecapMaj))
				.setMessage(retourMaj).setPositiveButton("Cool !", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
					}
				}).show();
		}else{
			Toast toast = Toast.makeText(mContext, mContext.getResources().getString(R.string.toastMajPasNecessaire), Toast.LENGTH_SHORT);
			toast.show();
		}
		super.onPostExecute(result);
	}
}
