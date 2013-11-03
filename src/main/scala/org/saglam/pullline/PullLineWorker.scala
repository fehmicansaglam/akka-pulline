package org.saglam.pullline

import akka.actor._
import org.saglam.pullline.Messages._
import scala.util.{ Failure, Success }

abstract class PullLineWorker[T] extends Actor with ActorLogging {

  def doWork(work: Option[Any]): Option[T]

  def receive = {
    case Work(work) =>
      try {
        doWork(work) match {
          case None => sender ! Exhausted
          case Some(result) => sender ! WorkDone(Success(result))
        }
      } catch {
        case t: Throwable =>
          log.error(t, "Exception")
          sender ! WorkDone(Failure(t))
      }
  }
}