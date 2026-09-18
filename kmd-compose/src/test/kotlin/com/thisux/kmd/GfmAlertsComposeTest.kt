package com.thisux.kmd

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class GfmAlertsComposeTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun noteAlertRendersKindAndBody() {
        rule.setContent {
            Kmd(
                markdown =
                    """
                    > [!NOTE]
                    > Streaming is the first optimization.
                    """.trimIndent(),
                extensions = listOf(GfmAlerts),
            )
        }
        rule.onNodeWithText("Note").assertExists()
        rule.onNodeWithText("Streaming is the first optimization.").assertExists()
    }
}
