package org.saglam.pullline

import akka.actor._
import org.saglam.pullline.Messages._
import scala.util.{ Failure, Success }

abstract class PullLineWorker[I, O] extends Actor with ActorLogging {

  def doWork(work: Option[I]): Option[O]

  def receive = {
    case Work(work: Option[I]) =>
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