@file:OptIn(ExperimentalComposeUiApi::class)

package io.github.hhth24340005.minesweeper

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerButton
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import io.github.hhth24340005.minesweeper.logic.CellState
import io.github.hhth24340005.minesweeper.resources.Res
import io.github.hhth24340005.minesweeper.resources.hex_concealed
import io.github.hhth24340005.minesweeper.resources.hex_flag
import io.github.hhth24340005.minesweeper.resources.hex_mine
import io.github.hhth24340005.minesweeper.resources.hex_number_1
import io.github.hhth24340005.minesweeper.resources.hex_number_2
import io.github.hhth24340005.minesweeper.resources.hex_number_3
import io.github.hhth24340005.minesweeper.resources.hex_number_4
import io.github.hhth24340005.minesweeper.resources.hex_number_5
import io.github.hhth24340005.minesweeper.resources.hex_number_6
import io.github.hhth24340005.minesweeper.resources.hex_number_7
import io.github.hhth24340005.minesweeper.resources.hex_number_8
import io.github.hhth24340005.minesweeper.resources.hex_revealed
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.merge
import org.jetbrains.compose.resources.vectorResource

public data class Cell<T : Any>(
  public val identity: T,
  public val status: CellState = CellState.Concealed,
  public val highlight: CellHighlight = CellHighlight.Concealed,
)

public enum class CellHighlight {
  None,
  Concealed,
  Marked,
  NotResolved,
  BulkRevealReady,
  BulkMarkReady,
}

public sealed interface CellClick<out T : Any> {
  public companion object {
    public fun <T : Any> Flow<CellClick<T>>.filterIsLeft(): Flow<Left<T>> =
      filterIsInstance<Left<T>>()

    public fun <T : Any> Flow<CellClick<T>>.filterIsRight(): Flow<Right<T>> =
      filterIsInstance<Right<T>>()
  }

  public val cellIdentity: T

  public data class Left<out T : Any>(override val cellIdentity: T) :
    CellClick<T>

  public data class Right<out T : Any>(override val cellIdentity: T) :
    CellClick<T>
}

public interface GridRenderer {
  public companion object {
    public val hex: GridRenderer =
      HexGridRenderer
  }

  @Composable
  public fun <T : Any> Grid(
    rows: List<List<Cell<T>>>,
  ): Flow<CellClick<T>>
}

private object HexGridRenderer : GridRenderer {
  @Composable
  override fun <T : Any> Grid(
    rows: List<List<Cell<T>>>,
  ): Flow<CellClick<T>> =
    GridBox {
      GridCol(rows, { it.identity }) { row ->
        GridRow(row)
      }.merge()
    }

  @Composable
  private inline fun <R : Any> GridBox(
    paddingX: Dp =
      maxOf(
        vectorResource(Res.drawable.hex_revealed).defaultWidth / 4f,
        vectorResource(Res.drawable.hex_concealed).defaultWidth / 4f,
      ),
    paddingY: Dp =
      maxOf(
        vectorResource(Res.drawable.hex_revealed).defaultHeight / 4f,
        vectorResource(Res.drawable.hex_concealed).defaultHeight / 4f,
      ),
    color: Color = Color.White,
    shape: Shape = RoundedCornerShape(maxOf(paddingX, paddingY)),
    content: @Composable () -> R,
  ): R {
    lateinit var ret: R
    Box(
      modifier =
        Modifier
          .background(color, shape)
          .padding(paddingX, paddingY),
    ) {
      ret = content()
    }
    return ret
  }

  @Composable
  private inline fun <T, R> GridCol(
    rows: List<List<T>>,
    rowKeyOf: (T) -> Any? = { it },
    rowContent: @Composable (List<T>) -> R,
  ): List<R> {
    lateinit var ret: List<R>
    Layout(
      {
        ret =
          rows.map { row ->
            key(row.map { rowKeyOf(it) }) {
              rowContent(row)
            }
          }
      },
    ) { measurables, constraints ->
      val placeable = measurables.map { it.measure(constraints) }
      val cellH = placeable.maxOf { it.height }
      val rowSpacing = cellH * 3 / 4
      val totalH = rowSpacing * (placeable.size - 1) + cellH
      val maxW = placeable.maxOf { it.width }

      layout(maxW, totalH) {
        placeable.forEachIndexed { i, p ->
          p.place((maxW - p.width) / 2, i * rowSpacing)
        }
      }
    }
    return ret
  }

  @Composable
  private fun <T : Any> GridRow(
    row: List<Cell<T>>,
  ): Flow<CellClick<T>> {
    lateinit var ret: Flow<CellClick<T>>
    Row {
      ret =
        row
          .mapIndexed { colIndex, (identity, status, highlight) ->
            key(identity) {
              Cell(colIndex, status, highlight)
                .mapNotNull { button ->
                  when (button) {
                    PointerButton.Primary -> CellClick.Left(identity)
                    PointerButton.Secondary -> CellClick.Right(identity)
                    else -> null
                  }
                }
            }
          }.merge()
    }
    return ret
  }


  @Composable
  private fun Cell(
    colIndex: Int,
    cellState: CellState,
    highlight: CellHighlight,
  ): Flow<PointerButton> {
    val images = cellImages(cellState)
    val width = images.maxOf { it.defaultWidth }
    val height = images.maxOf { it.defaultHeight }

    val flow =
      remember {
        MutableSharedFlow<PointerButton>(
          extraBufferCapacity = 1,
          onBufferOverflow = BufferOverflow.DROP_OLDEST,
        )
      }
    val subscription by flow.subscriptionCount.collectAsState()
    val interaction = remember { MutableInteractionSource() }
    val isHovered by interaction.collectIsHoveredAsState()
    val background =
      if (0 < subscription && isHovered) {
        when (highlight) {
          CellHighlight.None -> Color.Transparent
          CellHighlight.Concealed -> Color.Cyan.copy(alpha = 0.5f)
          CellHighlight.Marked -> Color.Yellow.copy(alpha = 0.3f)
          CellHighlight.NotResolved -> Color.Yellow.copy(alpha = 0.3f)
          CellHighlight.BulkRevealReady -> Color.Magenta.copy(alpha = 0.5f)
          CellHighlight.BulkMarkReady -> Color.Magenta.copy(alpha = 0.5f)
        }
      } else {
        Color.Transparent
      }
    Box(
      modifier =
        Modifier
          .size(width, height)
          .clip(HexagonShape)
          .hoverable(interaction)
          .background(background)
          .border(1.dp, background, HexagonShape)
          .leftClickable {
            flow.tryEmit(PointerButton.Primary)
          }.rightClickable {
            flow.tryEmit(PointerButton.Secondary)
          },
    ) {
      images.forEach {
        Image(
          imageVector = it,
          contentDescription = "$cellState",
          modifier = Modifier.fillMaxSize(),
        )
      }
    }
    return flow.asSharedFlow()
  }

  private object HexagonShape : Shape {
    override fun createOutline(
      size: Size,
      layoutDirection: LayoutDirection,
      density: Density,
    ): Outline {
      val (width, height) = size
      val path =
        Path().apply {
          moveTo(width / 2f, 0f)
          lineTo(width, height / 4f)
          lineTo(width, height * 3f / 4f)
          lineTo(width / 2f, height)
          lineTo(0f, height * 3f / 4f)
          lineTo(0f, height / 4f)
          close()
        }
      return Outline.Generic(path)
    }
  }
}

private fun Modifier.leftClickable(
  onClick: () -> Unit,
): Modifier =
  clickable(onClick) { it == PointerButton.Primary }

private fun Modifier.rightClickable(
  onClick: () -> Unit,
): Modifier =
  clickable(onClick) { it == PointerButton.Secondary }

private fun Modifier.clickable(
  onClick: () -> Unit,
  eventFilter: (PointerButton) -> Boolean,
): Modifier =
  pointerInput(onClick) {
    awaitPointerEventScope {
      while (true) {
        val event = awaitPointerEvent()
        if (event.type == PointerEventType.Press &&
          event.button?.let(eventFilter) == true
        ) {
          onClick()
        }
      }
    }
  }

@Composable
private fun cellImages(
  state: CellState,
): List<ImageVector> =
  when (state) {
    CellState.Concealed -> {
      listOf(vectorResource(Res.drawable.hex_concealed))
    }

    CellState.ConcealedMine -> {
      listOf(
        vectorResource(Res.drawable.hex_concealed),
        vectorResource(Res.drawable.hex_mine),
      )
    }

    CellState.Marked -> {
      listOf(
        vectorResource(Res.drawable.hex_concealed),
        vectorResource(Res.drawable.hex_flag),
      )
    }

    CellState.Revealed0 -> {
      listOf(vectorResource(Res.drawable.hex_revealed))
    }

    CellState.Revealed1 -> {
      listOf(
        vectorResource(Res.drawable.hex_revealed),
        vectorResource(Res.drawable.hex_number_1),
      )
    }

    CellState.Revealed2 -> {
      listOf(
        vectorResource(Res.drawable.hex_revealed),
        vectorResource(Res.drawable.hex_number_2),
      )
    }

    CellState.Revealed3 -> {
      listOf(
        vectorResource(Res.drawable.hex_revealed),
        vectorResource(Res.drawable.hex_number_3),
      )
    }

    CellState.Revealed4 -> {
      listOf(
        vectorResource(Res.drawable.hex_revealed),
        vectorResource(Res.drawable.hex_number_4),
      )
    }

    CellState.Revealed5 -> {
      listOf(
        vectorResource(Res.drawable.hex_revealed),
        vectorResource(Res.drawable.hex_number_5),
      )
    }

    CellState.Revealed6 -> {
      listOf(
        vectorResource(Res.drawable.hex_revealed),
        vectorResource(Res.drawable.hex_number_6),
      )
    }

    CellState.Revealed7 -> {
      listOf(
        vectorResource(Res.drawable.hex_revealed),
        vectorResource(Res.drawable.hex_number_7),
      )
    }

    CellState.Revealed8 -> {
      listOf(
        vectorResource(Res.drawable.hex_revealed),
        vectorResource(Res.drawable.hex_number_8),
      )
    }

    CellState.RevealedMine -> {
      listOf(
        vectorResource(Res.drawable.hex_revealed),
        vectorResource(Res.drawable.hex_mine),
      )
    }
  }
