package io.github.hhth24340005.minesweeper

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import io.github.hhth24340005.minesweeper.CellClick.Companion.filterIsLeft
import io.github.hhth24340005.minesweeper.logic.CellState
import io.github.hhth24340005.minesweeper.logic.MinesweeperStage
import io.github.hhth24340005.minesweeper.logic.MinesweeperStage.Status
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlin.time.ComparableTimeMark
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource

@Composable
public fun Game(
  gridComposer: GridComposer,
  uninitializedStage: MinesweeperStage.Uninitialized,
  time: TimeSource.WithComparableMarks = TimeSource.Monotonic,
): Deferred<GameResult> =
  LaunchedRenderer(uninitializedStage) {
    val stage =
      run {
        val (identity) =
          renderAndGetFirst {
            gridComposer
              .Grid(
                uninitializedStage.rows.map { it.map(::Cell) },
              ).filterIsLeft()
          }
        uninitializedStage.initialize(identity)
      }
    render {
      val clicks =
        gridComposer.Grid(
          stage.rows.map {
            it.map { cell ->
              Cell(cell, cell.status, cellHighlightOf(cell, stage))
            }
          },
        )
      stage.play(clicks)
    }
  }

public sealed interface GameResult {
  public data object Win : GameResult

  public data object Lose : GameResult

  public data object Canceled : GameResult
}

private class Stopwatch(
  private val time: TimeSource.WithComparableMarks = TimeSource.Monotonic,
) {
  private var origin: ComparableTimeMark = time.markNow()
  private var pausedAt: ComparableTimeMark? = null

  private val pausedDuration: Duration get() =
    pausedAt?.let { time.markNow() - it } ?: Duration.ZERO

  @Composable
  fun rememberElapsed(
    updateEvery: Duration = 1.seconds,
  ): State<Duration> {
    val elapsed =
      remember {
        mutableStateOf((pausedAt ?: time.markNow()) - origin)
      }
    LaunchedEffect(updateEvery) {
      val start = time.markNow()
      var i = 1
      while (true) {
        elapsed.value = (pausedAt ?: time.markNow()) - origin
        delay(start + (updateEvery * i++) - time.markNow())
      }
    }
    return elapsed
  }

  suspend fun <T> whileRunning(
    suspension: suspend () -> T,
  ): T {
    try {
      origin += pausedDuration
      pausedAt = null
      return suspension()
    } finally {
      pausedAt = time.markNow()
    }
  }

  suspend fun resume(): Nothing =
    whileRunning { awaitCancellation() }
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
private fun MinesweeperStage.play(
  clicks: Flow<CellClick<MinesweeperStage.Cell>>,
): Deferred<GameResult> =
  LaunchedRenderer(Unit) {
    race {
      whileActive { runService(clicks) }
      launch { awaitWin() }.onJoin { showWinOverlay() }
      launch { awaitLose() }.onJoin { showLoseOverlay() }
    }
  }

context(renderer: RendererScope)
private suspend fun MinesweeperStage.runService(
  clicks: Flow<CellClick<MinesweeperStage.Cell>>,
): Nothing {
  val stopwatch = Stopwatch()
  while (true) {
    race {
      whileActive { resumeInput(clicks) }
      whileActive { stopwatch.resume() }
      launch { awaitPause() }
        .onJoin { awaitPauseDismissal(stopwatch) }
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
    val isHovered by interaction.collectIsHoveredAsState()
    val background =
      if (complete != null && isHovered) {
        Color.LightGray
      } else {
        Color.White
      }
    Box(
      Modifier
        .clickable { complete?.invoke(Unit) }
        .background(background)
        .clip(RoundedCornerShape(5.dp))
        .hoverable(interaction),
    ) {
      Text(text = "Pause")
    }
  }

context(renderer: RendererScope)
private suspend fun awaitPauseDismissal(
  stopwatch: Stopwatch,
) {
  race {
    launch {
      renderer.renderCompletable(
        modifier =
          Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f)),
      ) { complete ->
        val elapsed by stopwatch.rememberElapsed()
        Column {
          Text(
            text = "Resume",
            Modifier
              .padding(10.dp)
              .clip(RoundedCornerShape(10.dp))
              .background(Color.White)
              .clickable { complete?.invoke(Unit) },
            fontSize = 4.em,
          )
          Text(
            text = formatDuration(elapsed),
            color = Color.White,
            fontSize = 4.em,
          )
        }
      }
    }.onJoin {}
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
