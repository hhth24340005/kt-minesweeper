package io.github.hhth24340005.minesweeper

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.hhth24340005.minesweeper.logic.CellState.Concealed
import io.github.hhth24340005.minesweeper.logic.CellState.Marked
import io.github.hhth24340005.minesweeper.logic.CellState.Revealed0
import io.github.hhth24340005.minesweeper.logic.CellState.Revealed1
import io.github.hhth24340005.minesweeper.logic.CellState.Revealed2
import io.github.hhth24340005.minesweeper.logic.CellState.Revealed3
import io.github.hhth24340005.minesweeper.logic.CellState.Revealed4
import io.github.hhth24340005.minesweeper.logic.CellState.Revealed5
import io.github.hhth24340005.minesweeper.logic.CellState.Revealed6
import io.github.hhth24340005.minesweeper.logic.CellState.Revealed7
import io.github.hhth24340005.minesweeper.logic.CellState.Revealed8
import io.github.hhth24340005.minesweeper.logic.CellState.RevealedMine
import io.github.hhth24340005.minesweeper.logic.Stage
import io.github.hhth24340005.minesweeper.resources.Res
import io.github.hhth24340005.minesweeper.resources.cell_concealed
import io.github.hhth24340005.minesweeper.resources.cell_marked
import io.github.hhth24340005.minesweeper.resources.cell_revealed_0
import io.github.hhth24340005.minesweeper.resources.cell_revealed_1
import io.github.hhth24340005.minesweeper.resources.cell_revealed_2
import io.github.hhth24340005.minesweeper.resources.cell_revealed_3
import io.github.hhth24340005.minesweeper.resources.cell_revealed_4
import io.github.hhth24340005.minesweeper.resources.cell_revealed_5
import io.github.hhth24340005.minesweeper.resources.cell_revealed_6
import io.github.hhth24340005.minesweeper.resources.cell_revealed_7
import io.github.hhth24340005.minesweeper.resources.cell_revealed_8
import io.github.hhth24340005.minesweeper.resources.cell_revealed_mine
import org.jetbrains.compose.resources.imageResource

@Composable
@Preview
public fun App() {
  val cellPreferences =
    CellPreferences(
      cellSize = 64.dp,
      imageBitmapOf = { state ->
        when (state) {
          is Concealed -> imageResource(Res.drawable.cell_concealed)
          is Marked -> imageResource(Res.drawable.cell_marked)
          is Revealed0 -> imageResource(Res.drawable.cell_revealed_0)
          is Revealed1 -> imageResource(Res.drawable.cell_revealed_1)
          is Revealed2 -> imageResource(Res.drawable.cell_revealed_2)
          is Revealed3 -> imageResource(Res.drawable.cell_revealed_3)
          is Revealed4 -> imageResource(Res.drawable.cell_revealed_4)
          is Revealed5 -> imageResource(Res.drawable.cell_revealed_5)
          is Revealed6 -> imageResource(Res.drawable.cell_revealed_6)
          is Revealed7 -> imageResource(Res.drawable.cell_revealed_7)
          is Revealed8 -> imageResource(Res.drawable.cell_revealed_8)
          is RevealedMine -> imageResource(Res.drawable.cell_revealed_mine)
        }
      },
    )
  Game(
    cellPreferences = cellPreferences,
    stage = Stage.prepare(width = 10, height = 10, mineDensity = 0.2),
  )
}
