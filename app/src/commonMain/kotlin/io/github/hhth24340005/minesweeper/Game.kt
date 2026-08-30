package io.github.hhth24340005.minesweeper

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerButton
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.em
import io.github.hhth24340005.minesweeper.logic.MinesweeperStage
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select

@Composable
public fun Game(
  gridComposer: GridComposer,
  uninitializedStage: MinesweeperStage.Uninitialized,
): Deferred<GameResult> {
  val deferred =
    remember(uninitializedStage) {
      CompletableDeferred<GameResult>()
    }
  LaunchedRenderer(uninitializedStage) {
    layerOf().use { layer0 ->
      val clickedCell =
        layer0 { complete ->
          val deferred =
            gridComposer.UninitializedGrid(uninitializedStage.rows)
          LaunchedEffect(deferred) {
            complete(deferred.await())
          }
        }
      val stage = uninitializedStage.initialize(clickedCell)
      val result =
        layer0 { completeGame ->
          Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
          ) {
            val clicks = gridComposer.Grid(stage.rows)
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
