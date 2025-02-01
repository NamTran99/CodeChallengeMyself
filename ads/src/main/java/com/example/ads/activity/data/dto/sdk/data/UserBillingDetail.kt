package com.example.ads.activity.data.dto.sdk.data

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "ik_sdk_user_billing_config")
data class UserBillingDetail(
    @PrimaryKey
    @ColumnInfo(name = "orderId")
    val orderId: String,
    @ColumnInfo(name = "endDate")
    val endDate: String
) : Parcelable {
    override fun toString(): String {
        return super.toString()
    }
}