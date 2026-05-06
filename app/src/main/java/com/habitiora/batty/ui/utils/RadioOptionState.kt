package com.habitiora.batty.ui.utils

import androidx.compose.runtime.Immutable

@Immutable
data class RadioOptionState(
    val id: String,
    val title: String,
    val description: String
)