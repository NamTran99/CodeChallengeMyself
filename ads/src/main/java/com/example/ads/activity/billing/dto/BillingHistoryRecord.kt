package com.example.ads.activity.billing.dto

import android.os.Parcel
import android.os.Parcelable
import org.json.JSONObject


class BillingHistoryRecord() : Parcelable {
    var productId: String? = null
    var purchaseToken: String? = null
    var purchaseTime: Long = 0
    var developerPayload: String? = null
    var signature: String? = null

    constructor(parcel: Parcel) : this() {
        productId = parcel.readString()
        purchaseToken = parcel.readString()
        purchaseTime = parcel.readLong()
        developerPayload = parcel.readString()
        signature = parcel.readString()
    }

    constructor(dataAsJson: String, signature: String?) : this(
        JSONObject(dataAsJson),
        signature
    )

    constructor(json: JSONObject, signature: String?) : this() {
        productId = json.getString("productId")
        purchaseToken = json.getString("purchaseToken")
        purchaseTime = json.getLong("purchaseTime")
        developerPayload = json.getString("developerPayload")
        this.signature = signature
    }

    constructor(
        productId: String?, purchaseToken: String?, purchaseTime: Long,
        developerPayload: String?, signature: String?
    ) : this() {
        this.productId = productId
        this.purchaseToken = purchaseToken
        this.purchaseTime = purchaseTime
        this.developerPayload = developerPayload
        this.signature = signature
    }

    override fun toString(): String {
        return "BillingHistoryRecord{" +
                "productId='" + productId + '\'' +
                ", purchaseToken='" + purchaseToken + '\'' +
                ", purchaseTime=" + purchaseTime +
                ", developerPayload='" + developerPayload + '\'' +
                ", signature='" + signature + '\'' +
                '}'
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(productId)
        parcel.writeString(purchaseToken)
        parcel.writeLong(purchaseTime)
        parcel.writeString(developerPayload)
        parcel.writeString(signature)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<BillingHistoryRecord> {
        override fun createFromParcel(parcel: Parcel): BillingHistoryRecord {
            return BillingHistoryRecord(parcel)
        }

        override fun newArray(size: Int): Array<BillingHistoryRecord?> {
            return arrayOfNulls(size)
        }
    }

}