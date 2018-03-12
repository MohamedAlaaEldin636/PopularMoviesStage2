package com.example.android.popularmovies.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Mohamed on 2/27/2018.
 *
 */

public class Trailer implements Parcelable {

    private final String name;
    private final String site;
    private final String key;

    public Trailer(String name, String site, String key) {
        this.name = name;
        this.site = site;
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public String getSite() {
        return site;
    }

    public String getKey() {
        return key;
    }

    // ---- After Implementing Parcelable

    private Trailer(Parcel in) {
        name = in.readString();
        site = in.readString();
        key = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(site);
        dest.writeString(key);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    static final Parcelable.Creator<Trailer> CREATOR
            = new Parcelable.Creator<Trailer>() {

        @Override
        public Trailer createFromParcel(Parcel in) {
            return new Trailer(in);
        }

        @Override
        public Trailer[] newArray(int size) {
            return new Trailer[size];
        }
    };
}
