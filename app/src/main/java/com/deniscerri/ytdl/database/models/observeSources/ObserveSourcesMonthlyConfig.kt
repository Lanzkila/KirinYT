package com.kirinyt.app.database.models.observeSources

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ObserveSourcesMonthlyConfig(
    var everyMonthDay: Int,
    var startsMonth: Int
) : Parcelable