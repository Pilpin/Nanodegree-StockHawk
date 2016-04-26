package com.sam_chordas.android.stockhawk.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.db.chart.model.LineSet;

public class ParcelableLineSet extends LineSet implements Parcelable {

    public ParcelableLineSet(){
        super();
    }

    protected ParcelableLineSet(Parcel in) {
    }

    public static final Creator<ParcelableLineSet> CREATOR = new Creator<ParcelableLineSet>() {
        @Override
        public ParcelableLineSet createFromParcel(Parcel in) {
            return new ParcelableLineSet(in);
        }

        @Override
        public ParcelableLineSet[] newArray(int size) {
            return new ParcelableLineSet[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }
}
