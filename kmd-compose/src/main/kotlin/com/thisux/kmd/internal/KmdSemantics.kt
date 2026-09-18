package com.thisux.kmd.internal

import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.semantics.SemanticsPropertyReceiver

internal val HeadingLevelKey = SemanticsPropertyKey<Int>("KmdHeadingLevel")

internal var SemanticsPropertyReceiver.kmdHeadingLevel by HeadingLevelKey
