package io.github.hhth24340005.minesweeper

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import arrow.core.getOrElse
import arrow.core.left
import arrow.core.right
import io.github.hhth24340005.minesweeper.CellClick.Companion.filterIsLeft
import io.github.hhth24340005.minesweeper.logic.CellState
import io.github.hhth24340005.minesweeper.logic.MinesweeperStage
import io.github.hhth24340005.minesweeper.logic.MinesweeperStage.Status
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlin.let
import kotlin.time.Duration
import kotlin.time.TimeSource

@Composable
public fun Game(
  gridComposer: GridComposer,
  uninitializedStage: MinesweeperStage.Uninitialized,
  time: TimeSource.WithComparableMarks = TimeSource.Monotonic,
): Deferred<GameResult> =
  LaunchedRenderer(uninitializedStage) {
    val stopwatch = Stopwatch()
    val stage =
      renderAndGetFirst {
        val clicks =
          gridComposer
            .Grid(
              uninitializedStage.rows.map { it.map(::Cell) },
            ).filterIsLeft()
        flow {
          val gameCancellation = Job()
          while (true) {
            race {
              async { clicks.first() }
                .onAwait { (identity) ->
                  emit(uninitializedStage.initialize(identity).right())
                }
              gameCancellation.onJoin {
                emit(GameResult.Canceled.left())
              }
              launch { awaitPause() }
                .onJoin {
                  awaitPauseDismissal(
                    stopwatch = stopwatch,
                    cancelGame = { gameCancellation.complete() },
                  )
                }
            }
          }
        }
      }.getOrElse { return@LaunchedRenderer it }
    render {
      val clicks =
        gridComposer.Grid(
          stage.rows.map {
            it.map { cell ->
              Cell(cell, cell.status, cellHighlightOf(cell, stage))
            }
          },
        )
      stage.play(clicks, stopwatch = stopwatch)
    }
  }

public sealed interface GameResult {
  public data object Win : GameResult

  public data object Lose : GameResult

  public data object Canceled : GameResult
}

private fun cellHighlightOf(
  cell: MinesweeperStage.Cell,
  stage: MinesweeperStage,
): CellHighlight {
  if (stage.status != Status.Playing) {
    return CellHighlight.None
  }
  return when (cell.status) {
    is CellState.Concealed -> {
      CellHighlight.Concealed
    }


    is CellState.Marked -> {
      CellHighlight.Marked
    }


    is CellState.ConcealedMine,
    is CellState.Revealed0,
    -> {
      CellHighlight.None
    }

    else -> {
      when {
        stage.concealedNeighborsOf(cell).isEmpty()
        -> CellHighlight.None

        stage.isBulkRevealReady(cell)
        -> CellHighlight.BulkRevealReady

        stage.isBulkMarkReady(cell)
        -> CellHighlight.BulkMarkReady

        else -> CellHighlight.NotResolved
      }
    }
  }
}

@Composable
context(stopwatch: Stopwatch)
private fun MinesweeperStage.play(
  clicks: Flow<CellClick<MinesweeperStage.Cell>>,
): Deferred<GameResult> =
  LaunchedRenderer(Unit) {
    val gameCancellation = Job()
    race {
      whileActive {
        runService(
          clicks,
          cancelGame = { gameCancellation.complete() },
        )
      }
      gameCancellation.onJoin { GameResult.Canceled }
      launch { awaitWin() }.onJoin { showWinOverlay() }
      launch { awaitLose() }.onJoin { showLoseOverlay() }
    }
  }

context(renderer: RendererScope, stopwatch: Stopwatch)
private suspend fun MinesweeperStage.runService(
  clicks: Flow<CellClick<MinesweeperStage.Cell>>,
  cancelGame: () -> Unit,
): Nothing {
  while (true) {
    race {
      whileActive { resumeInput(clicks) }
      whileActive { stopwatch.resume() }
      launch { awaitPause() }
        .onJoin { awaitPauseDismissal(cancelGame) }
    }
  }
}

private suspend fun MinesweeperStage.resumeInput(
  clicks: Flow<CellClick<MinesweeperStage.Cell>>,
): Nothing {
  while (true) {
    clicks.collect { click ->
      when (click) {
        is CellClick.Left -> reveal(click.cellIdentity)
        is CellClick.Right -> toggleMark(click.cellIdentity)
      }
    }
  }
}

context(renderer: RendererScope)
private suspend fun awaitPause() =
  renderer.renderCompletable(
    alignment = Alignment.TopEnd,
  ) { complete ->
    val interaction = remember { MutableInteractionSource() }
    val focusRequester = remember { FocusRequester() }
    val isHovered by interaction.collectIsHoveredAsState()
    val background =
      if (complete != null && isHovered) {
        Color.LightGray
      } else {
        Color.White
      }
    LaunchedEffect(Unit) {
      focusRequester.requestFocus()
    }
    Box(
      modifier =
        Modifier
          .padding(10.dp)
          .onKeyEvent { e ->
            if (e.type == KeyEventType.KeyDown && e.key == Key.Escape) {
              return@onKeyEvent complete
                ?.invoke(Unit)
                ?.let { true }
                ?: false
            }
            false
          }.focusable()
          .focusRequester(focusRequester),
      contentAlignment = Alignment.BottomStart,
    ) {
      Box(
        Modifier
          .clickable { complete?.invoke(Unit) }
          .background(background)
          .clip(RoundedCornerShape(5.dp))
          .hoverable(interaction),
      ) {
        Text(
          text = "Pause",
          fontWeight = FontWeight.Bold,
          fontSize = 1.em,
        )
      }
    }
  }

context(renderer: RendererScope, stopwatch: Stopwatch)
private suspend fun awaitPauseDismissal(
  cancelGame: () -> Unit,
) {
  renderer.renderCompletable(
    modifier =
      Modifier
        .fillMaxSize()
        .background(Color.Black.copy(alpha = 0.5f)),
  ) { complete ->
    val elapsed by stopwatch.rememberElapsed()
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
      focusRequester.requestFocus()
    }
    Box(
      contentAlignment = Alignment.Center,
    ) {
      Column(
        Modifier
          .onKeyEvent { e ->
            if (e.type == KeyEventType.KeyDown && e.key == Key.Escape) {
              return@onKeyEvent complete
                ?.invoke(Unit)
                ?.let { true }
                ?: false
            }
            false
          }.focusable()
          .focusRequester(focusRequester),
        horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        Text(
          modifier =
            Modifier
              .clip(RoundedCornerShape(10.dp))
              .clickable { complete?.invoke(Unit) }
              .padding(vertical = 5.dp, horizontal = 20.dp),
          text = "Resume",
          fontSize = 2.em,
          color = Color.White,
        )
        Spacer(
          Modifier.size(width = 0.dp, height = 10.dp),
        )
        Text(
          text = "Quit",
          modifier =
            Modifier.clickable {
              if (complete != null) {
                cancelGame()
                complete(Unit)
              }
            },
          color = Color.White,
          fontSize = 2.em,
        )
      }
      Box(
        Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter,
      ) {
        Text(
          text = formatDuration(elapsed),
          color = Color.White,
          fontSize = 1.5.em,
        )
      }
    }
  }
}

private suspend fun RendererScope.showWinOverlay(): GameResult {
  renderCompletable(
    modifier =
      Modifier
        .fillMaxSize()
        .background(Color.Black.copy(alpha = 0.5f)),
  ) { complete ->
    Text(
      "You win!",
      fontSize = 4.em,
      color = Color.White,
      modifier = Modifier.clickable { complete?.invoke(Unit) },
    )
  }
  return GameResult.Win
}

private suspend fun RendererScope.showLoseOverlay(): GameResult {
  renderCompletable(
    modifier =
      Modifier
        .fillMaxSize()
        .background(Color.Black.copy(alpha = 0.5f)),
  ) { complete ->
    Text(
      "You lose!",
      fontSize = 4.em,
      color = Color.White,
      modifier = Modifier.clickable { complete?.invoke(Unit) },
    )
  }
  return GameResult.Lose
}

private fun formatDuration(
  duration: Duration,
): String =
  duration.toComponents { hours, minutes, seconds, _ ->
    val hoursStr = "${hours.toString().padStart(2, '0')}h"
    val minutesStr = "${minutes.toString().padStart(2, '0')}m"
    val secondsStr = "${seconds.toString().padStart(2, '0')}s"
    if (0 < hours) {
      "$hoursStr $minutesStr $secondsStr"
    } else {
      "$minutesStr $secondsStr"
    }
  }
