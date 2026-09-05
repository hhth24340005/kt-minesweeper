package io.github.hhth24340005.minesweeper

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.yield
import kotlin.coroutines.cancellation.CancellationException

public suspend fun <R> race(
  build: RaceBuilder<R>.() -> Unit,
): R {
  val onWin =
    coroutineScope coroutine@{
      val racers =
        buildList jobs@{
          RaceBuilderImpl(this@coroutine, this@jobs).build()
        }.merge()
      racers.firstOrNull() ?: error("No racer was registered")
    }
  yield()
  return onWin()
}

public interface RaceBuilder<R> : CoroutineScope {
  public fun racer(
    block:
      suspend CoroutineScope.() -> (suspend () -> R),
  )

  public fun whileActive(
    block: suspend CoroutineScope.() -> Nothing,
  ) {
    racer {
      block()
    }
  }

  public fun Job.onJoin(
    onWin: suspend () -> R,
  ) {
    racer {
      try {
        join()
        onWin
      } catch (e: CancellationException) {
        cancel(e)
        throw e
      }
    }
  }

  public fun <T> Deferred<T>.onAwait(
    onWin: suspend (T) -> R,
  ) {
    racer {
      try {
        val result = await();
        { onWin(result) }
      } catch (e: CancellationException) {
        cancel(e)
        throw e
      }
    }
  }
}

@PublishedApi
internal class RaceBuilderImpl<R>(
  private val coroutine: CoroutineScope,
  private val jobs: MutableList<
    Flow<suspend () -> R>,
  >,
) : RaceBuilder<R>,
  CoroutineScope by coroutine {
  override fun racer(
    block:
      suspend CoroutineScope.() -> (suspend () -> R),
  ) {
    jobs +=
      flow {
        emit(coroutine.block())
      }
  }
}
