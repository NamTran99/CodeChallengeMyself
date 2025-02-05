package com.example.ads.activity.mediation.custom.utils

enum class IKInitializationStatus(val code: Int) {
    NOT_INITIALIZED(5),
    DOES_NOT_APPLY(4),
    INITIALIZING(3),
    INITIALIZED_UNKNOWN(2),
    INITIALIZED_FAILURE(0),
    INITIALIZED_SUCCESS(1)
}