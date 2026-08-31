package io.github.hhth24340005.minesweeper

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerButton
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import io.github.hhth24340005.minesweeper.logic.CellState
import io.github.hhth24340005.minesweeper.logic.MinesweeperStage
import io.github.hhth24340005.minesweeper.logic.MinesweeperStage.Status
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select
import kotlin.time.ComparableTimeMark
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource
import kotlin.use

@Composable
public fun Game(
  gridComposer: GridComposer,
  uninitializedStage: MinesweeperStage.Uninitialized,
  time: TimeSource.WithComparableMarks = TimeSource.Monotonic,
): Deferred<GameResult> {
  val deferred =
    remember(uninitializedStage) {
      CompletableDeferred<GameResult>()
    }
  LaunchedRenderer(uninitializedStage) {
    layerOf().use { layer0 ->
      val stage =
        run {
          val clickedCell =
            layer0 { complete ->
              val deferred =
                gridComposer.UninitializedGrid(uninitializedStage.rows)
              LaunchedEffect(deferred) {
                complete(deferred.await())
              }
            }
          uninitializedStage.initialize(clickedCell)
        }
      val result =
        layer0 { completeGame ->
          Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
          ) {
            val clicks =
              gridComposer.Grid(stage.rows) { cell ->
                if (stage.status != Status.Playing) {
                  return@Grid CellHighlight.None
                }
                when (cell.status) {
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
                      stage
                        .concealedNeighborsOf(
                          cell,
                        ).isEmpty() -> CellHighlight.None

                      stage.isBulkRevealReady(
                        cell,
                      ) -> CellHighlight.BulkRevealReady

                      stage.isBulkMarkReady(
                        cell,
                      ) -> CellHighlight.BulkMarkReady

                      else -> CellHighlight.NotResolved
                    }
                  }
                }
              }
            LaunchedRenderer(clicks) {
              val parentJob = Job(coroutineContext[Job])
              val coroutine = CoroutineScope(coroutineContext + parentJob)
              val clickJob =
                coroutine.launch {
                  while (true) {
                    val (click, cell) = clicks.firstOrNull() ?: break
                    when (click) {
                      PointerButton.Primary -> {
                        stage.reveal(cell)
                      }

                      PointerButton.Secondary -> {
                        stage.toggleMark(cell)
                      }
                    }
                  }
                }
              val winJob =
                coroutine.launch {
                  stage.awaitWin()
                  clickJob.cancel()
                  layerOf().use { layer1 ->
                    layer1 { proceed ->
                      Text(
                        "You win!",
                        modifier =
                          Modifier.clickable { proceed(Unit) },
                        fontSize = 4.em,
                        textDecoration = TextDecoration.Underline,
                      )
                    }
                  }
                }
              val loseJob =
                coroutine.launch {
                  stage.awaitLose()
                  clickJob.cancel()
                  layerOf().use { layer1 ->
                    layer1 { proceed ->
                      Text(
                        "You lose!",
                        modifier =
                          Modifier.clickable { proceed(Unit) },
                        fontSize = 4.em,
                        textDecoration = TextDecoration.Underline,
                      )
                    }
                  }
                }
              val result =
                select {
                  winJob.onJoin { GameResult.Win }
                  loseJob.onJoin { GameResult.Lose }
                }
              parentJob.cancelAndJoin()
              completeGame(result)
            }
            Box(
              modifier = Modifier.fillMaxSize(),
              contentAlignment = Alignment.TopEnd,
            ) {
              val stopwatch = remember { Stopwatch(time) }
              val elapsed by stopwatch.elapsed(1.seconds)
              var color by remember { mutableStateOf(Color.LightGray) }
              Text(
                text = formatDuration(elapsed),
                color = color,
                fontFamily = FontFamily.Monospace,
                fontSize = 1.5.em,
                modifier = Modifier.padding(5.dp),
              )

              LaunchedEffect(stopwatch, stage.status) {
                if (stage.status == Status.Playing) {
                  stopwatch.resume()
                }
                color =
                  when (stage.status) {
                    Status.Playing -> Color.LightGray
                    Status.Lose -> Color.Red
                    Status.Win -> Color.Yellow
                  }
              }
            }
            if (stage.status == Status.Playing) {
              Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.BottomEnd,
              ) {
                Text(
                  text =
                    "${
                      stage.cellCount -
                        stage.revealedCount -
                        stage.mineCount
                    } can be opened",
                  color = Color.LightGray,
                  fontFamily = FontFamily.Monospace,
                  fontSize = 1.5.em,
                  modifier = Modifier.padding(5.dp),
                )
              }
            }
          }
        }
      deferred.complete(result)
    }
  }
  return deferred
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
  fun elapsed(
    updateEvery: Duration = 1.seconds,
  ): State<Duration> {
    val elapsed = remember { mutableStateOf(Duration.ZERO) }
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

  suspend fun whileRunning(
    suspension: suspend () -> Unit,
  ) {
    try {
      origin += pausedDuration
      pausedAt = null
      suspension()
    } finally {
      pausedAt = time.markNow()
    }
  }

  suspend fun resume() =
    whileRunning { awaitCancellation() }
}

private fun formatDuration(
  duration: Duration,
): String =
  duration.toComponents { hours, minutes, seconds, _ ->
    val hoursStr = hours.toString().padStart(2, '0')
    val minutesStr = minutes.toString().padStart(2, '0')
    val secondsStr = seconds.toString().padStart(2, '0')
    if (0 < hours) {
      "${hoursStr}h ${minutesStr}m ${secondsStr}s"
    } else {
      "${minutesStr}m ${secondsStr}s"
    }
  }
