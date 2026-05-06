package com.goforer.phogal.data.model.local.error

import kotlinx.serialization.Serializable
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Serializable
@Parcelize
data class Errors(
    val errors: List<String>
) : Parcelable {
    companion object {
        fun empty() = Errors(
            errors = emptyList()
        )
    }
}