package org.saglam.pullline

import scala.reflect.ClassTag
import akka.actor._
import org.saglam.pullline.Messages._

class PullLineConsumer[L: ClassTag](workerLocation: String, leftLocation: String)
    extends Actor with ActorLogging {

  val worker = context.actorSelection(workerLocation)
  val left = context.actorSelection(leftLocation)

  override def preStart(): Unit = {
    left ! Pull
  }

  override def postStop(): Unit = {
    log.info("PullLineConsumer stopped")
    left ! PoisonPill
  }

  def receive = {
    case PullDone(result: L) =>
      worker ! Work(Some(result))

    case WorkDone(_) | WorkIsReady =>
      left ! Pull

    case NoMoreData =>
      log.info("No more data")
      worker ! PoisonPill
      self ! PoisonPill
  }
}