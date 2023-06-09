package com.example.trelloclone.activities.model

import android.os.Parcel
import android.os.Parcelable

data class SelcetedMembers(
    val id: String = "",
    val image: String =""
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(image)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SelcetedMembers> {
        override fun createFromParcel(parcel: Parcel): SelcetedMembers {
            return SelcetedMembers(parcel)
        }

        override fun newArray(size: Int): Array<SelcetedMembers?> {
            return arrayOfNulls(size)
        }
    }
}