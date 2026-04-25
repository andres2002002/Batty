package com.habitiora.batty.domain.model

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationImportant
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.ui.graphics.vector.ImageVector
import com.habitiora.batty.R
import kotlinx.serialization.Serializable

@Serializable
enum class AlertPolicy(
    val icon: ImageVector,
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int
) {
    All(
        icon = Icons.Filled.NotificationsActive,
        titleRes = R.string.alert_policy_all,
        descriptionRes = R.string.alert_policy_all_description
    ),
    OnlyCritical(
        icon = Icons.Filled.NotificationImportant,
        titleRes = R.string.alert_policy_only_critical,
        descriptionRes = R.string.alert_policy_critical_description
    ),
    Disabled(
        icon = Icons.Filled.NotificationsOff,
        titleRes = R.string.alert_policy_disabled,
        descriptionRes = R.string.alert_policy_disabled_description
    );
    val isDisabled: Boolean
        get() = this == Disabled
    val isAll: Boolean
        get() = this == All
    val isOnlyCritical: Boolean
        get() = this == OnlyCritical
    val isNotDisabled: Boolean
        get() = !isDisabled
}