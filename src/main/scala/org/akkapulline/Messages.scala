package org.akkapulline

import scala.util.Try

object Messages {
  case class Work[I](work: Option[I])
  case class WorkDone[O](result: Try[O])
  case object Exhausted
  case object Consume
  case object Pull
  case class PullDone[T](data: Option[T])
  case object NoMoreData
  case object WorkIsReady
}
