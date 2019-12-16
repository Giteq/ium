package com.google.codelabs.appauth;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class ProductAdapter extends ArrayAdapter<Product> {

    public ProductAdapter(Context context, ArrayList<Product> users) {

        super(context, 0, users);

    }

    @Override

    public View getView(int position, View convertView, ViewGroup parent) {

        // Get the data item for this position

        Product product = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view

        if (convertView == null) {

            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_product, parent, false);

        }

        TextView man_name = (TextView) convertView.findViewById(R.id.prodManname);

        man_name.setText(product.man_name);

        return convertView;

    }

}
