package com.example.android.inventoryapp;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.*;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import android.widget.TextView;
import android.widget.Toast;
import com.example.android.inventoryapp.data.DatabaseContract.ProductTable;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private EditText mNameTextEdit;
    private EditText mPriceTextEdit;
    private EditText mSupplierNameTextEdit;
    private EditText mSuppliearPhoneTextEdit;

    private Button mContactSupplierBtn;

    private TextView mQuantityTextView;
    private Button mRemoveBtn;
    private Button mAddBtn;

    private boolean mProductHasChanged = false;
    private Uri mCurrentProductUri;
    int productQuantity;


    private static final int PRODUCT_EDIT_LOADER = 0;

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mProductHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        Intent intent = getIntent();
        mCurrentProductUri = intent.getData();

        mContactSupplierBtn = findViewById(R.id.btn_call_supplier);


        if (mCurrentProductUri == null) {
            setTitle("Add new product");

            // Invalidate the options menu, so the "Delete" menu option can be hidden. (It doesn't make sense to delete an item that hasn't been created yet.)
            invalidateOptionsMenu();
            mContactSupplierBtn.setVisibility(View.GONE);
        } else {
            setTitle("Edit product");

            getLoaderManager().initLoader(PRODUCT_EDIT_LOADER, null, this);

            mContactSupplierBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel: " + mSuppliearPhoneTextEdit.getText()));
                    startActivity(intent);
                }
            });
        }

        mNameTextEdit = (EditText) findViewById(R.id.edit_product_name);
        mPriceTextEdit = (EditText) findViewById(R.id.edit_price);
        mSupplierNameTextEdit = (EditText) findViewById(R.id.edit_supplier_name);
        mSuppliearPhoneTextEdit = (EditText) findViewById(R.id.edit_supplier_phone_number);

        mQuantityTextView = (TextView) findViewById(R.id.edit_quantity);
        mAddBtn = findViewById(R.id.btn_add_product);
        mRemoveBtn = findViewById(R.id.btn_remove_product);

        mAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                productQuantity = Integer.valueOf(mQuantityTextView.getText().toString().trim());
                productQuantity++;
                mQuantityTextView.setText(String.valueOf(productQuantity));

                if (productQuantity > 0) {
                    mRemoveBtn.setEnabled(true);
                }
            }
        });

        mRemoveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                productQuantity = Integer.valueOf(mQuantityTextView.getText().toString().trim());

                if (productQuantity > 0) {
                    productQuantity--;
                    mQuantityTextView.setText(String.valueOf(productQuantity));
                } else if (productQuantity == 0) {
                    mRemoveBtn.setEnabled(false);
                }
            }
        });

        mNameTextEdit.setOnTouchListener(mTouchListener);
        mQuantityTextView.setOnTouchListener(mTouchListener);
        mPriceTextEdit.setOnTouchListener(mTouchListener);
        mSupplierNameTextEdit.setOnTouchListener(mTouchListener);
        mSuppliearPhoneTextEdit.setOnTouchListener(mTouchListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor_activity, menu);
        return true;
    }

    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        // hide delete btn if a new product
        if (mCurrentProductUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                saveItem();
                return true;
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            case android.R.id.home:
                if (!mProductHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes.
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveItem() {
        String sName = mNameTextEdit.getText().toString().trim();
        String sPrice = mPriceTextEdit.getText().toString().trim();
        String sQuantity = mQuantityTextView.getText().toString().trim();
        String sSupplier = mSupplierNameTextEdit.getText().toString().trim();
        String sSupplierPhone = mSuppliearPhoneTextEdit.getText().toString().trim();

        // check if new product
        if (mCurrentProductUri == null && TextUtils.isEmpty(sName) && TextUtils.isEmpty(sPrice) && TextUtils.isEmpty(sQuantity) && TextUtils.isEmpty(sSupplier) && TextUtils.isEmpty(sSupplierPhone)) {
            Toast.makeText(this, R.string.toast_fill_fields, Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(sName)) {
            Toast.makeText(this, R.string.toast_missing_name, Toast.LENGTH_LONG).show();
            return;
        }
        if (TextUtils.isEmpty(sPrice)) {
            Toast.makeText(this, R.string.toast_missing_price, Toast.LENGTH_LONG).show();
            return;
        }

        if (TextUtils.isEmpty(sQuantity)) {
            Toast.makeText(this, R.string.toast_missing_quantity, Toast.LENGTH_LONG).show();
            return;
        }
        if (TextUtils.isEmpty(sSupplier)) {
            Toast.makeText(this, R.string.toast_missing_supplier_name, Toast.LENGTH_LONG).show();
            return;
        }
        if (TextUtils.isEmpty(sSupplierPhone)) {
            Toast.makeText(this, R.string.toast_missing_supplier_phone, Toast.LENGTH_LONG).show();
            return;
        }

        ContentValues values = new ContentValues();
        values.put(ProductTable.COLUMN_NAME_PRODUCT_NAME, sName);
        values.put(ProductTable.COLUMN_NAME_SUPLIER_NAME, sSupplier);
        values.put(ProductTable.COLUMN_NAME_SUPLIER_PHONE_NUMBER, sSupplierPhone);

        int price = 0;
        if (!TextUtils.isEmpty(sPrice)) {
            price = Integer.parseInt(sPrice);
            if (price < 0) {
                price = 0;
            }
        }

        values.put(ProductTable.COLUMN_NAME_PRICE, price);

        int quantity = 0;
        if (!TextUtils.isEmpty(sQuantity)) {
            quantity = Integer.parseInt(sQuantity);
            if (quantity < 0) {
                quantity = 0;
            }
        }

        values.put(ProductTable.COLUMN_NAME_QUANTITY, quantity);

        if (mCurrentProductUri == null) {
            Uri newUri = getContentResolver().insert(ProductTable.CONTENT_URI, values);

            if (newUri == null) {
                Toast.makeText(this, getString(R.string.editor_insert_product_error), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_insert_product_ok), Toast.LENGTH_SHORT).show();
            }
        } else {
            int rowsCountAffected = getContentResolver().update(mCurrentProductUri, values, null, null);

            if (rowsCountAffected == 0) {
                Toast.makeText(this, getString(R.string.editor_update_product_error), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_update_product_ok), Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {
                ProductTable._ID,
                ProductTable.COLUMN_NAME_PRODUCT_NAME,
                ProductTable.COLUMN_NAME_PRICE,
                ProductTable.COLUMN_NAME_QUANTITY,
                ProductTable.COLUMN_NAME_SUPLIER_NAME,
                ProductTable.COLUMN_NAME_SUPLIER_PHONE_NUMBER,
        };

        return new CursorLoader(this,       // Parent activity context.
                mCurrentProductUri, // Provider content URI to query.
                projection,                         // Columns to include in the resulting Cursor.
                null,                      // No selection clause.
                null,                   // No selection arguments.
                null);                     // Default sort order.
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor.
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        if (cursor.moveToFirst()) {
            int nameColumnIndex = cursor.getColumnIndex(ProductTable.COLUMN_NAME_PRODUCT_NAME);
            int priceColumnIndex = cursor.getColumnIndex(ProductTable.COLUMN_NAME_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(ProductTable.COLUMN_NAME_QUANTITY);
            int supplierColumnIndex = cursor.getColumnIndex(ProductTable.COLUMN_NAME_SUPLIER_NAME);
            int supplierPhoneColumnIndex = cursor.getColumnIndex(ProductTable.COLUMN_NAME_SUPLIER_PHONE_NUMBER);

            String name = cursor.getString(nameColumnIndex);
            int price = cursor.getInt(priceColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            String supplierName = cursor.getString(supplierColumnIndex);
            String supplierPhone = cursor.getString(supplierPhoneColumnIndex);

            mNameTextEdit.setText(name);
            mPriceTextEdit.setText(Integer.toString(price));
            mQuantityTextView.setText(Integer.toString(quantity));
            mSupplierNameTextEdit.setText(supplierName);
            mSuppliearPhoneTextEdit.setText(supplierPhone);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear all data
        mNameTextEdit.setText("");
        mPriceTextEdit.setText("");
        mQuantityTextView.setText("");
        mSupplierNameTextEdit.setText("");
        mSuppliearPhoneTextEdit.setText("");
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.dialog_unsaved_changes);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.dialog_delete);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteItem();
            }
        });

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteItem() {
        if (mCurrentProductUri != null) {
            int rowsCountDeleted = getContentResolver().delete(mCurrentProductUri, null, null);

            if (rowsCountDeleted == 0) {
                Toast.makeText(this, getString(R.string.toast_delete_product_error), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.toast_delete_product_ok), Toast.LENGTH_SHORT).show();
            }
        }

        finish();
    }


}
