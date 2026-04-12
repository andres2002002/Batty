package com.habitiora.batty.domain.model

import androidx.compose.runtime.Immutable

@Immutable
@JvmInline
value class DndBypassState(val channelCanBypass: Boolean)