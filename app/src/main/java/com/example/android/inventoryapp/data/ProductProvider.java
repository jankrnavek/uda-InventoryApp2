package com.example.android.inventoryapp.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;
import com.example.android.inventoryapp.data.DatabaseContract.ProductTable;

public class ProductProvider extends ContentProvider {

    public static final String LOG_TAG = ProductProvider.class.getSimpleName();

    private static final int PRODUCTS = 100;
    private static final int PRODUCT_ID = 101;


    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer to run the first time anything is called from this class
    static {
        sUriMatcher.addURI(DatabaseContract.CONTENT_AUTHORITY, DatabaseContract.PATH_PRODUCTS, PRODUCTS);
        sUriMatcher.addURI(DatabaseContract.CONTENT_AUTHORITY, DatabaseContract.PATH_PRODUCTS + "/#", PRODUCT_ID);
    }

    // Database helper object
    private DatabaseHelper mDbHelper;

    @Override
    public boolean onCreate() {
        mDbHelper = new DatabaseHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase database = mDbHelper.getReadableDatabase();
        Cursor cursor;

        int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                cursor = database.query(ProductTable.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case PRODUCT_ID:
                selection = ProductTable._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(ProductTable.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        // Set notification URI on the Cursor to know what content URI the Cursor was created for.
        // If the data at this URI changes, then the Cursor needs to be updated.
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return insertItem(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Insert product into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertItem(Uri uri, ContentValues values) {
        // Check that the product name is not null
        String name = values.getAsString(ProductTable.COLUMN_NAME_PRODUCT_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Product requires a name");
        }

        // Check that the price is not null
        Integer price = values.getAsInteger(ProductTable.COLUMN_NAME_PRICE);
        if (price == null) {
            throw new IllegalArgumentException("Product requires price");
        }

        // Check that the quantity is not null
        Integer quantity = values.getAsInteger(ProductTable.COLUMN_NAME_QUANTITY);
        if (quantity == null) {
            throw new IllegalArgumentException("There needs to be at least one product left");
        }

        // Check that there is supplier name
        String sName = values.getAsString(ProductTable.COLUMN_NAME_SUPLIER_NAME);
        if (sName == null) {
            throw new IllegalArgumentException("Supplier name required");
        }

        // Check that there is supplier phone number
        String sPhone = values.getAsString(ProductTable.COLUMN_NAME_SUPLIER_PHONE_NUMBER);
        if (sPhone == null) {
            throw new IllegalArgumentException("Supplier phone required");
        }

        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Insert the new product with the given values
        long id = database.insert(ProductTable.TABLE_NAME, null, values);
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        // Notify all listeners that the data has changed for the item content URI
        getContext().getContentResolver().notifyChange(uri, null);

        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection,
                      String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return updateItem(uri, contentValues, selection, selectionArgs);
            case PRODUCT_ID:
                // For the PRODUCT_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = ProductTable._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateItem(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    /**
     * Update items in the database with the given content values. Apply the changes to the rows
     * specified in the selection and selection arguments (which could be 0 or 1 or more cakes).
     * Return the number of rows that were successfully updated.
     */
    private int updateItem(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        if (values.containsKey(ProductTable.COLUMN_NAME_PRODUCT_NAME)) {
            String name = values.getAsString(ProductTable.COLUMN_NAME_PRODUCT_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Product requires name");
            }
        }

        if (values.containsKey(ProductTable.COLUMN_NAME_PRICE)) {
            Integer name = values.getAsInteger(ProductTable.COLUMN_NAME_PRICE);
            if (name == null) {
                throw new IllegalArgumentException("Product requires price");
            }
        }

        if (values.containsKey(ProductTable.COLUMN_NAME_QUANTITY)) {
            Integer name = values.getAsInteger(ProductTable.COLUMN_NAME_QUANTITY);
            if (name == null) {
                throw new IllegalArgumentException("Quantity is required");
            }
        }

        if (values.containsKey(ProductTable.COLUMN_NAME_SUPLIER_NAME)) {
            String name = values.getAsString(ProductTable.COLUMN_NAME_SUPLIER_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Supplier name required");
            }
        }

        if (values.containsKey(ProductTable.COLUMN_NAME_SUPLIER_PHONE_NUMBER)) {
            String name = values.getAsString(ProductTable.COLUMN_NAME_SUPLIER_PHONE_NUMBER);
            if (name == null) {
                throw new IllegalArgumentException("Supplier phone required");
            }
        }

        // In case of nothing to update, just don't...
        if (values.size() == 0) {
            return 0;
        }

        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        int rowsUpdated = database.update(ProductTable.TABLE_NAME, values, selection, selectionArgs);

        // If 1 or more rows were updated, then notify all listeners that the data at the  given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows updated
        return rowsUpdated;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Get writable database.
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        int rowsCountDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                rowsCountDeleted = database.delete(ProductTable.TABLE_NAME, selection, selectionArgs);
                break;
            case PRODUCT_ID:
                selection = ProductTable._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsCountDeleted = database.delete(ProductTable.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        // If 1 or more rows were deleted, then notify all listeners that the data at the given URI has changed
        if (rowsCountDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsCountDeleted;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return ProductTable.CONTENT_LIST_TYPE;
            case PRODUCT_ID:
                return ProductTable.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }
}
