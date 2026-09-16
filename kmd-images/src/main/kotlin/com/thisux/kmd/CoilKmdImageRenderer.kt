package com.thisux.kmd

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage

object CoilKmdImageRenderer : KmdImageRenderer {
    @Composable
    override fun render(
        image: Image,
        modifier: Modifier,
    ) {
        AsyncImage(
            model = image.source,
            contentDescription = image.alt,
            contentScale = ContentScale.FillWidth,
            modifier =
                modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp)),
        )
    }
}
