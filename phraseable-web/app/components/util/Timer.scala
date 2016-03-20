package components.util

import com.google.inject.ImplementedBy

@ImplementedBy(classOf[SystemTimer])
trait Timer {
  def currentTimeMillis(): Long
}

object Timer {
  val systemTimer: Timer = new SystemTimer
}
