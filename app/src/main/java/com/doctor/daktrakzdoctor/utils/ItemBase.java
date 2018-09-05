package com.doctor.daktrakzdoctor.utils;

import android.os.Parcel;
import android.os.Parcelable;

public class ItemBase implements Parcelable {
	
	public ItemBase() {  }

	public ItemBase(Parcel in) {
		readFromParcel(in);
	}
	
	@Override
	public int describeContents() {
		return 0;
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) { }
	
	/**
	 * 
	 * @param in
	 */
	protected void readFromParcel(Parcel in) { }
	
	public final static Creator<ItemBase> CREATOR = new Creator<ItemBase>() {
		public ItemBase createFromParcel(Parcel in) {
			return new ItemBase(in);
		}

		public ItemBase[] newArray(int size) {
			return new ItemBase[size];
		}
	};
}
