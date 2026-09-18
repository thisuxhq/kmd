package com.thisux.kmd

import androidx.compose.foundation.text.BasicText
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class KmdRenderersTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun headingOverrideReceivesModifier() {
        var receivedModifier = false
        rule.setContent {
            Kmd(
                markdown = "# Hello",
                renderers =
                    KmdRenderers {
                        heading { _, modifier ->
                            receivedModifier = true
                            BasicText("Replaced heading", modifier)
                        }
                    },
            )
        }
        rule.onNodeWithText("Replaced heading").assertExists()
        assertTrue(receivedModifier)
    }

    @Test
    fun checkboxOverrideIsUsed() {
        rule.setContent {
            Kmd(
                markdown = "- [x] Build parser",
                renderers =
                    KmdRenderers {
                        checkbox { checked, modifier ->
                            BasicText(if (checked) "done" else "todo", modifier)
                        }
                    },
            )
        }
        rule.onNodeWithText("done").assertExists()
    }

    @Test
    fun inlineCodeOverrideIsUsed() {
        rule.setContent {
            Kmd(
                markdown = "Use `Kmd`.",
                renderers =
                    KmdRenderers {
                        inlineCode { code, modifier ->
                            BasicText("code:${code.code}", modifier)
                        }
                    },
            )
        }
        rule.onNodeWithText("code:Kmd").assertExists()
    }
}
