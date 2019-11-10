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

        // Lookup view for data population

        TextView man_name = (TextView) convertView.findViewById(R.id.prodManname);

        TextView model_name = (TextView) convertView.findViewById(R.id.prodModelName);

        // Populate the data into the template view using the data object

        man_name.setText(product.man_name);

        model_name.setText(product.model_name);

        // Return the completed view to render on screen

        return convertView;

    }

}
