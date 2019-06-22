package com.pinmyballs.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public abstract class DAOBase {

	protected SQLiteDatabase mDb = null;

	protected FlipperDatabaseHandler mHandler = null;

	public DAOBase(SQLiteDatabase pDb) {
		this.mDb = pDb;
	}

	public DAOBase(Context pContext) {
		this.mHandler = new FlipperDatabaseHandler(pContext, null);
	}

	public SQLiteDatabase open() {
		// Pas besoin de fermer la dernière base puisque getWritableDatabase s'en charge
		mDb = mHandler.getWritableDatabase();
		return mDb;
	}

	public void close() {
		mDb.close();
	}

	public SQLiteDatabase getDb() {
		return mDb;
	}
}
