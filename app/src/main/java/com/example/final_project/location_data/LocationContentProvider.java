package com.example.final_project.location_data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class LocationContentProvider extends ContentProvider {

    public static final int LOCATIONS = 100;
    public static final int NEARBY_WITH_ID = 101;
    public static final int LOCATION_WITH_ID = 102;
    public static final int OTHER_THAN_SELECTED_ID = 103;

    private static final UriMatcher sUriMatcher = buildUriMatcher();

    public static UriMatcher buildUriMatcher(){
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        uriMatcher.addURI(LocationContract.AUTHORITY, LocationContract.PATH_LOCATION, LOCATIONS);
        uriMatcher.addURI(LocationContract.AUTHORITY, LocationContract.PATH_NEARBY + "/#", NEARBY_WITH_ID);
        uriMatcher.addURI(LocationContract.AUTHORITY, LocationContract.PATH_LOCATION + "/#", LOCATION_WITH_ID);
        uriMatcher.addURI(LocationContract.AUTHORITY, LocationContract.PATH_OTHER_THAN_SELECTED + "/#", OTHER_THAN_SELECTED_ID);

        return uriMatcher;
    }

    private LocationDBHelper dbHelper;
    @Override
    public boolean onCreate() {
        Context context = getContext();
        dbHelper = new LocationDBHelper(context);
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        final SQLiteDatabase sqLiteDatabase = dbHelper.getReadableDatabase();
        int matcher = sUriMatcher.match(uri);
        Cursor retCursor;

        switch (matcher) {
            case LOCATIONS:
                retCursor = sqLiteDatabase.query(LocationContract.LocationEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case NEARBY_WITH_ID:
                // Get id from Uri path
                String id = uri.getPathSegments().get(1);
                retCursor = sqLiteDatabase.query(LocationContract.LocationEntry.TABLE_NAME,
                        null,
                        "_id=?",
                        new String[]{id},
                        null,
                        null,
                        null);
                break;
            case OTHER_THAN_SELECTED_ID:
                String id2 = uri.getPathSegments().get(1);
                retCursor = sqLiteDatabase.query(LocationContract.LocationEntry.TABLE_NAME,
                        null,
                        "_id!=?",
                        new String[]{id2},
                        null,
                        null,
                        null);
                break;
            // Default exception
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        final SQLiteDatabase database = dbHelper.getWritableDatabase();

        int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case LOCATIONS:
                long id = database.insert(LocationContract.LocationEntry.TABLE_NAME, null, values);
                if(id > 0){
                    returnUri = ContentUris.withAppendedId(LocationContract.LocationEntry.CONTENT_URI, id);
                } else {
                    throw new SQLException("Failed to insert row into " + uri);
                }
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        final SQLiteDatabase database = dbHelper.getWritableDatabase();

        int matcher = sUriMatcher.match(uri);
        int locationsDeleted;

        switch (matcher) {
            case LOCATION_WITH_ID:
                String id = uri.getPathSegments().get(1);
                locationsDeleted = database.delete(LocationContract.LocationEntry.TABLE_NAME,
                        "_id=?",
                        new String[]{id});
                Log.d("URI", String.valueOf(uri));
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (locationsDeleted!=0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return locationsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }
}
