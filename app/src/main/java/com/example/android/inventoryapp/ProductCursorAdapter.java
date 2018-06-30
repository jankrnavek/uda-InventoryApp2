package com.example.android.inventoryapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.DatabaseContract.ProductTable;

public class ProductCursorAdapter extends CursorAdapter {
    private static final String LOG_TAG = ProductCursorAdapter.class.getSimpleName();

    public ProductCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {

        TextView nameTextView = view.findViewById(R.id.tv_name);
        TextView priceTextView = view.findViewById(R.id.tv_price);
        TextView quantityTextView = view.findViewById(R.id.tv_quantity);

        int nameColumnIndex = cursor.getColumnIndex(ProductTable.COLUMN_NAME_PRODUCT_NAME);
        int priceColumnIndex = cursor.getColumnIndex(ProductTable.COLUMN_NAME_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(ProductTable.COLUMN_NAME_QUANTITY);

        final String sName = cursor.getString(nameColumnIndex);
        final String sPrice = cursor.getString(priceColumnIndex);

        final String sQuantity = cursor.getString(quantityColumnIndex);
        final int quantity = cursor.getInt(quantityColumnIndex);

        final Uri uri = ContentUris.withAppendedId(ProductTable.CONTENT_URI, cursor.getInt(cursor.getColumnIndexOrThrow(ProductTable._ID)));

        nameTextView.setText(sName);
        priceTextView.setText(sPrice);
        quantityTextView.setText(sQuantity);

        Button sellBtn = view.findViewById(R.id.btn_sale);
        sellBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (quantity > 0) {
                    int newQuantity = quantity - 1;

                    ContentValues values = new ContentValues();
                    values.put(ProductTable.COLUMN_NAME_QUANTITY, newQuantity);

                    context.getContentResolver().update(uri, values, null, null);
                } else {
                    Toast.makeText(context, context.getString(R.string.toast_sold_out), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
