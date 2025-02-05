package com.example.ads.activity.data.dto.pub

import java.io.Serializable

class IKAdjustAttribution : Serializable {
    var trackerToken: String? = null
    var trackerName: String? = null
    var network: String? = null
    var campaign: String? = null
    var adgroup: String? = null
    var creative: String? = null
    var clickLabel: String? = null
    var adid: String? = null
    var costType: String? = null
    var costAmount: Double? = null
    var costCurrency: String? = null
    var fbInstallReferrer: String? = null
}