package ru.d10xa.jadd

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.LoggerContext
import com.typesafe.scalalogging.LazyLogging
import org.slf4j.LoggerFactory

trait LoggingUtil {
  def enableDebug(): Unit
  def quiet(): Unit
}

object LoggingUtil extends LoggingUtil with LazyLogging {

  private def rootLogger: Logger =
    LoggerFactory.getILoggerFactory.asInstanceOf[LoggerContext].getLogger("ru.d10xa.jadd")

  override def enableDebug(): Unit = {
    rootLogger.setLevel(Level.DEBUG)
    logger.debug("Debug mode enabled")
  }

  override def quiet(): Unit = {
    rootLogger.setLevel(Level.ERROR)
  }
}
