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

public data class CellPreferences(
  val cellSize: Dp,
  val textureConcealed: ImageBitmap,
  val textureMarked: ImageBitmap,
  val textureRevealed0: ImageBitmap,
  val textureRevealed1: ImageBitmap,
  val textureRevealed2: ImageBitmap,
  val textureRevealed3: ImageBitmap,
  val textureRevealed4: ImageBitmap,
  val textureRevealed5: ImageBitmap,
  val textureRevealed6: ImageBitmap,
  val textureRevealed7: ImageBitmap,
  val textureRevealed8: ImageBitmap,
  val textureRevealedMine: ImageBitmap,
)

public sealed interface CellState {
  public companion object {
    public fun revealedOfFromMinesAround(
      value: Int,
    ): CellState =
      when (value) {
        0 -> Revealed0

        1 -> Revealed1

        2 -> Revealed2

        3 -> Revealed3

        4 -> Revealed4

        5 -> Revealed5

        6 -> Revealed6

        7 -> Revealed7

        8 -> Revealed8

        else -> throw IllegalArgumentException(
          "Only 0-8 are allowed but got: $value",
        )
      }
  }

  public fun textureOf(
    pref: CellPreferences,
  ): ImageBitmap

  public data object Concealed : CellState {
    override fun textureOf(
      pref: CellPreferences,
    ): ImageBitmap =
      pref.textureConcealed
  }

  public data object Marked : CellState {
    override fun textureOf(
      pref: CellPreferences,
    ): ImageBitmap =
      pref.textureMarked
  }

  public data object Revealed0 : CellState {
    override fun textureOf(
      pref: CellPreferences,
    ): ImageBitmap =
      pref.textureRevealed0
  }

  public data object Revealed1 : CellState {
    override fun textureOf(
      pref: CellPreferences,
    ): ImageBitmap =
      pref.textureRevealed1
  }

  public data object Revealed2 : CellState {
    override fun textureOf(
      pref: CellPreferences,
    ): ImageBitmap =
      pref.textureRevealed2
  }

  public data object Revealed3 : CellState {
    override fun textureOf(
      pref: CellPreferences,
    ): ImageBitmap =
      pref.textureRevealed3
  }

  public data object Revealed4 : CellState {
    override fun textureOf(
      pref: CellPreferences,
    ): ImageBitmap =
      pref.textureRevealed4
  }

  public data object Revealed5 : CellState {
    override fun textureOf(
      pref: CellPreferences,
    ): ImageBitmap =
      pref.textureRevealed5
  }

  public data object Revealed6 : CellState {
    override fun textureOf(
      pref: CellPreferences,
    ): ImageBitmap =
      pref.textureRevealed6
  }

  public data object Revealed7 : CellState {
    override fun textureOf(
      pref: CellPreferences,
    ): ImageBitmap =
      pref.textureRevealed7
  }

  public data object Revealed8 : CellState {
    override fun textureOf(
      pref: CellPreferences,
    ): ImageBitmap =
      pref.textureRevealed8
  }

  public data object RevealedMine : CellState {
    override fun textureOf(
      pref: CellPreferences,
    ): ImageBitmap =
      pref.textureRevealedMine
  }
}

@Composable
context(pref: CellPreferences)
public fun CellState.draw(
  onLeftClick: () -> Unit,
  onRightClick: () -> Unit,
  onMiddleClick: () -> Unit,
) {
  Box(
    Modifier
      .size(pref.cellSize)
      .clickable(onClick = onLeftClick)
      .onPointerEvent(cond = { it.isSecondaryPressed }, onRightClick)
      .onPointerEvent(cond = { it.isTertiaryPressed }, onMiddleClick),
  ) {
    Image(
      bitmap = textureOf(pref),
      contentDescription = "$this",
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
