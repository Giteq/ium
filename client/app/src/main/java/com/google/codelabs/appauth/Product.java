package com.google.codelabs.appauth;

import android.os.Parcel;
import android.os.Parcelable;

public class Product implements Parcelable {
    public String man_name;
    public String model_name;
    public Integer price;
    public Integer quantity;
    private int mData;

    public Product(String man_name, String model_name, Integer price, Integer quantity) {

        this.man_name = man_name;
        this.model_name = model_name;
        this.price = price;
        this.quantity = quantity;

    }

    // example constructor that takes a Parcel and gives you an object populated with it's values
    private Product(Parcel in) {
        mData = in.readInt();
    }


    /* everything below here is for implementing Parcelable */

    // 99.9% of the time you can just ignore this
    @Override
    public int describeContents() {
        return 0;
    }

    // write your object's data to the passed-in Parcel
    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(mData);
    }

    // this is used to regenerate your object. All Parcelables must have a CREATOR that implements these two methods
    public static final Parcelable.Creator<Product> CREATOR = new Parcelable.Creator<Product>() {
        public Product createFromParcel(Parcel in) {
            return new Product(in);
        }

        public Product[] newArray(int size) {
            return new Product[size];
        }
    };

}
