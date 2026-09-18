package com.thisux.kmd

import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isHeading
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.thisux.kmd.internal.HeadingLevelKey
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class KmdSemanticsTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun headingExposesLevel() {
        rule.setContent { Kmd("# Hello") }
        rule.onNode(isHeading()).assert(hasText("Hello"))
        rule.onNode(isHeading()).assert(SemanticsMatcher.expectValue(HeadingLevelKey, 1))
    }

    @Test
    fun nestedHeadingKeepsItsLevel() {
        rule.setContent { Kmd("## Section") }
        rule.onNode(isHeading()).assert(SemanticsMatcher.expectValue(HeadingLevelKey, 2))
    }

    @Test
    fun taskListExposesCheckboxState() {
        rule.setContent {
            Kmd(
                """
                - [x] Build parser
                - [ ] Build renderer
                """.trimIndent(),
            )
        }
        val boxes = rule.onAllNodes(isToggleable())
        boxes[0].assertIsOn()
        boxes[1].assertIsOff()
        boxes[0].assert(
            SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Checkbox),
        )
        boxes[0].assert(
            SemanticsMatcher.expectValue(SemanticsProperties.ToggleableState, ToggleableState.On),
        )
    }

    @Test
    fun listExposesCollectionInfo() {
        rule.setContent {
            Kmd(
                """
                - tap
                - swipe
                """.trimIndent(),
            )
        }
        rule.onNode(
            SemanticsMatcher.keyIsDefined(SemanticsProperties.CollectionInfo),
        ).assertExists()
    }

    @Test
    fun tableExposesCollectionInfo() {
        rule.setContent {
            Kmd(
                """
                | Name | Role |
                |------|------|
                | Sam  | Dev  |
                """.trimIndent(),
            )
        }
        rule.onNode(
            SemanticsMatcher.keyIsDefined(SemanticsProperties.CollectionInfo),
        ).assertExists()
    }

    @Test
    fun linkTextIsExposed() {
        rule.setContent { Kmd("[KMD](https://thisux.com)") }
        rule.onNodeWithText("KMD").assertExists()
    }

    @Test
    fun imagePlaceholderUsesImageRole() {
        rule.setContent { Kmd("![A cat](https://example.com/cat.png)") }
        rule.onNodeWithText("A cat").assert(
            SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Image),
        )
    }
}
