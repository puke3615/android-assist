package com.puke.assist.api;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

/**
 * @author puke
 * @version 2021/11/30
 */
public class ContextProvider extends ContentProvider {

    @Override
    public boolean onCreate() {
        AssistLog.i("Assist setup.");
        AppContext.setContext(getContext());
        try {
            Class.forName("com.puke.assist.core.AssistDynamicImpl");
        } catch (ClassNotFoundException ignored) {
            AssistLog.i("No AssistDynamicImpl found.");
        }
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return null;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
