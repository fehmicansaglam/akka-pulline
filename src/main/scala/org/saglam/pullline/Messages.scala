package org.saglam.pullline

import scala.util.Try

object Messages {
  case class Work(work: Option[Any])
  case class WorkDone[T](result: Try[T])
  case object Exhausted
  case object Pull
  case class PullDone[T](data: T)
  case object NoMoreData
  case object WorkIsReady
}
