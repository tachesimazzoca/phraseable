package components.util

import javax.inject.Singleton

@Singleton
class SystemTimer extends Timer {
  def currentTimeMillis(): Long = System.currentTimeMillis
}
