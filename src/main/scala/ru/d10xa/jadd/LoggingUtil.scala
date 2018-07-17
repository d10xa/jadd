package ru.d10xa.jadd

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.LoggerContext
import com.typesafe.scalalogging.LazyLogging
import org.slf4j.LoggerFactory
import org.slf4j.helpers.SubstituteLoggerFactory

trait LoggingUtil {
  def enableDebug(): Unit
  def quiet(): Unit
}

object LoggingUtil extends LoggingUtil with LazyLogging {

  override def enableDebug(): Unit = {
    // https://travis-ci.org/d10xa/jadd/builds/396169599
    // java.lang.ClassCastException: org.slf4j.helpers.SubstituteLoggerFactory
    //   cannot be cast to ch.qos.logback.classic.LoggerContext
    LoggerFactory.getILoggerFactory match {
      case l: LoggerContext =>
        l.getLogger("ru.d10xa.jadd").setLevel(Level.DEBUG)
        logger.debug("Debug mode enabled")
      case l: SubstituteLoggerFactory =>
        def logger = l.getLogger("ru.d10xa.jadd")
        logger.info(s"SubstituteLoggerFactory used. Can not enable debug mode ${logger.getClass}")
    }
  }

  override def quiet(): Unit = {
    LoggerFactory.getILoggerFactory match {
      case l: LoggerContext =>
        l.getLogger("ru.d10xa.jadd").setLevel(Level.ERROR)
      case l: SubstituteLoggerFactory =>
        def logger = l.getLogger("ru.d10xa.jadd")
        logger.info(s"SubstituteLoggerFactory used. Can not enable quiet mode ${logger.getClass}")
    }
  }
}
