package ui.widget

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp

@Composable
fun MainWrapper(content: @Composable() () -> Unit) {
    val verticalState = rememberScrollState()
    MaterialTheme {
        Box {
            VerticalScrollbar(
                modifier = Modifier.align(Alignment.CenterEnd)
                    .alpha(0.6f),
                style = ScrollbarStyle(
                    unhoverColor = Color.LightGray,
                    hoverColor = Color.Black,
                    minimalHeight = 50.dp,
                    hoverDurationMillis = 100,
                    shape = RectangleShape,
                    thickness = 8.dp
                ),
                adapter = rememberScrollbarAdapter(verticalState)
            )
            Column(
                modifier = Modifier
                    .padding(horizontal = 25.dp)
                    .fillMaxWidth()
                    .verticalScroll(state = verticalState),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                content()
            }
        }
    }
}