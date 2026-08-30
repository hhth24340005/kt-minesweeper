@file:OptIn(ExperimentalComposeUiApi::class)

package io.github.hhth24340005.minesweeper

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.UiComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerButton
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import io.github.hhth24340005.minesweeper.logic.CellState
import io.github.hhth24340005.minesweeper.logic.MinesweeperStage
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
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.vectorResource

public interface GridComposer {
  public companion object {
    public fun hexOf(): GridComposer =
      HexGridComposer()
  }

  @Composable
  public fun UninitializedGrid(
    rows: List<List<MinesweeperStage.Uninitialized.Cell>>,
  ): Deferred<MinesweeperStage.Uninitialized.Cell>

  @Composable
  public fun Grid(
    rows: List<List<MinesweeperStage.Cell>>,
  ): Flow<Pair<PointerButton, MinesweeperStage.Cell>>
}

private class HexGridComposer : GridComposer {
  @Composable
  override fun UninitializedGrid(
    rows: List<List<MinesweeperStage.Uninitialized.Cell>>,
  ): Deferred<MinesweeperStage.Uninitialized.Cell> {
    val grid =
      Grid(
        rows.map { row ->
          row.map { CellState.Concealed to it }
        },
      )
    val deferred =
      remember(grid) {
        CompletableDeferred<MinesweeperStage.Uninitialized.Cell>()
      }
    val race =
      remember(grid) {
        grid.mapNotNull { (click, cell) ->
          cell.takeIf { click == PointerButton.Primary }
        }
      }

    LaunchedEffect(deferred, race) {
      deferred.complete(race.first())
    }

    return deferred
  }

  @Composable
  override fun Grid(
    rows: List<List<MinesweeperStage.Cell>>,
  ): Flow<Pair<PointerButton, MinesweeperStage.Cell>> =
    Grid(
      rows.map { row ->
        row.map { it.status to it }
      },
    )


  @Composable
  @JvmName("GridPrivate")
  private fun <T : Any> Grid(
    rows: List<List<Pair<CellState, T>>>,
  ): Flow<Pair<PointerButton, T>> {
    val ret =
      remember {
        MutableSharedFlow<Pair<PointerButton, T>>(
          extraBufferCapacity = 1,
          onBufferOverflow = BufferOverflow.DROP_OLDEST,
        )
      }
    val paddingX =
      maxOf(
        vectorResource(Res.drawable.hex_revealed).defaultWidth / 4f,
        vectorResource(Res.drawable.hex_concealed).defaultWidth / 4f,
      )
    val paddingY =
      maxOf(
        vectorResource(Res.drawable.hex_revealed).defaultHeight / 4f,
        vectorResource(Res.drawable.hex_concealed).defaultHeight / 4f,
      )
    Box(
      modifier =
        Modifier
          .fillMaxSize()
          .background(Color.DarkGray),
      contentAlignment = Alignment.Center,
    ) {
      Box(
        modifier =
          Modifier
            .background(
              Color(0xFFFFFFFF),
              RoundedCornerShape(maxOf(paddingX, paddingY)),
            ).padding(paddingX, paddingY),
      ) {
        GridCol {
          rows.forEach { row ->
            key(row) {
              Row {
                row.forEachIndexed { colIndex, (status, identity) ->
                  key(status, identity) {
                    val flow = Cell(colIndex, status)
                    LaunchedEffect(flow) {
                      flow.collect { button ->
                        ret.emit(button to identity)
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
    return ret.asSharedFlow()
  }

  @Composable
  private fun GridCol(
    content:
      @Composable @UiComposable
      () -> Unit,
  ) {
    Layout(content) { measurables, constraints ->
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
  }

  @Composable
  private fun Cell(
    colIndex: Int,
    cellState: CellState,
  ): Flow<PointerButton> {
    val images = cellImages(cellState)
    val width = images.maxOf { it.defaultWidth }
    val height = images.maxOf { it.defaultHeight }

    val coroutine = rememberCoroutineScope()
    val flow =
      remember {
        MutableSharedFlow<PointerButton>(
          extraBufferCapacity = 1,
          onBufferOverflow = BufferOverflow.DROP_OLDEST,
        )
      }
    Box(
      modifier =
        Modifier
          .size(width, height)
          .leftClickable {
            coroutine.launch { flow.emit(PointerButton.Primary) }
          }.rightClickable {
            coroutine.launch { flow.emit(PointerButton.Secondary) }
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
