package io.github.hhth24340005.minesweeper

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.delay
import kotlin.time.ComparableTimeMark
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource

public class Stopwatch(
  private val time: TimeSource.WithComparableMarks = TimeSource.Monotonic,
) {
  private var origin: ComparableTimeMark = time.markNow()
  private var pausedAt: ComparableTimeMark? = origin

  private val pausedDuration: Duration
    get() =
      pausedAt?.let { time.markNow() - it } ?: Duration.ZERO

  @Composable
  public fun rememberElapsed(
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

  public suspend fun <T> whileRunning(
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

  public suspend fun resume(): Nothing =
    whileRunning { awaitCancellation() }
}
