package io.github.hhth24340005.minesweeper

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.pointer.PointerButtons
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.pointer.isTertiaryPressed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import io.github.hhth24340005.minesweeper.logic.CellState

public data class CellPreferences(
  val cellSize: Dp,
  val imageBitmapOf: @Composable (CellState) -> ImageBitmap,
)

@Composable
public fun Cell(
  state: CellState,
  preferences: CellPreferences,
  onLeftClick: () -> Unit = {},
  onRightClick: () -> Unit = {},
  onMiddleClick: () -> Unit = {},
) {
  Box(
    Modifier
      .size(preferences.cellSize)
      .clickable(onClick = onLeftClick)
      .onPointerEvent(cond = { it.isSecondaryPressed }, onRightClick)
      .onPointerEvent(cond = { it.isTertiaryPressed }, onMiddleClick),
  ) {
    Image(
      bitmap = preferences.imageBitmapOf(state),
      contentDescription = "$state",
      modifier = Modifier.fillMaxSize(),
      filterQuality = FilterQuality.None,
    )
  }
}

private fun Modifier.onPointerEvent(
  cond: (PointerButtons) -> Boolean,
  action: () -> Unit,
): Modifier =
  pointerInput(action) {
    awaitPointerEventScope {
      while (true) {
        val event = awaitPointerEvent()
        if (event.type == PointerEventType.Press && cond(event.buttons)) {
          action()
        }
      }
    }
  }
