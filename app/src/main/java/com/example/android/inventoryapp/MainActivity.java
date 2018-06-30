package com.example.android.inventoryapp;

import android.app.LoaderManager;
import android.content.*;
import android.database.Cursor;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.android.inventoryapp.data.DatabaseContract;
import com.example.android.inventoryapp.data.DatabaseContract.ProductTable;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int PRODUCT_LOADER = 0;
    ProductCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setup fab
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        // Find the ListView which will be populated with the item data.
        ListView productListView = findViewById(R.id.list);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = findViewById(R.id.empty_view);
        productListView.setEmptyView(emptyView);

        // Setup an Adapter to create a list item for each row of item data in the Cursor.
        // There is no item data yet (until the loader finishes) so pass in null for the Cursor.
        mCursorAdapter = new ProductCursorAdapter(this, null);
        productListView.setAdapter(mCursorAdapter);

        productListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                Uri currentProductUri = ContentUris.withAppendedId(DatabaseContract.ProductTable.CONTENT_URI, id);
                intent.setData(currentProductUri);
                startActivity(intent);
            }
        });

        getLoaderManager().initLoader(PRODUCT_LOADER, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_insert_dummy_data:
                insertProduct();
                return true;
            case R.id.action_delete_all_entries:
                deleteAllProducts();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void insertProduct() {
        ContentValues values = new ContentValues();
        values.put(ProductTable.COLUMN_NAME_PRODUCT_NAME, "Samsung 7");
        values.put(ProductTable.COLUMN_NAME_PRICE, 9900);
        values.put(ProductTable.COLUMN_NAME_QUANTITY, 10);
        values.put(ProductTable.COLUMN_NAME_SUPLIER_NAME, "Samsung Inc.");
        values.put(ProductTable.COLUMN_NAME_SUPLIER_PHONE_NUMBER, "+123 123456");

        Uri newUri = getContentResolver().insert(DatabaseContract.ProductTable.CONTENT_URI, values);
    }

    private void deleteAllProducts() {
        int rowsCountDeleted = getContentResolver().delete(DatabaseContract.ProductTable.CONTENT_URI, null, null);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Define a projection that specifies which columns from the database you will actually use after this query.
        String[] projection = {
                ProductTable._ID,
                ProductTable.COLUMN_NAME_PRODUCT_NAME,
                ProductTable.COLUMN_NAME_PRICE,
                ProductTable.COLUMN_NAME_QUANTITY,
                ProductTable.COLUMN_NAME_SUPLIER_NAME,
                ProductTable.COLUMN_NAME_SUPLIER_PHONE_NUMBER,
        };

        // This loader will execute the ContentProvider's query method on a background thread.
        return new CursorLoader(this,       // Parent activity context.
                DatabaseContract.ProductTable.CONTENT_URI, // Provider content URI to query.
                projection,                         // Columns to include in the resulting Cursor.
                null,                      // No selection clause.
                null,                   // No selection arguments.
                null);                     // Default sort order.
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Update with this new cursor containing updated data.
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Callback called when the data needs to be deleted
        mCursorAdapter.swapCursor(null);
    }
}
