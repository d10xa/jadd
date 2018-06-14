package ru.d10xa.jadd

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.LoggerContext
import com.typesafe.scalalogging.LazyLogging
import org.slf4j.LoggerFactory

trait LoggingUtil {
  def enableDebug(): Unit
}

object LoggingUtil extends LoggingUtil with LazyLogging {
  override def enableDebug(): Unit = {
    val loggerContext = LoggerFactory.getILoggerFactory.asInstanceOf[LoggerContext]
    val rootLogger = loggerContext.getLogger("ru.d10xa.jadd")
    rootLogger.setLevel(Level.DEBUG)
    logger.debug("Debug mode enabled")
  }
}
