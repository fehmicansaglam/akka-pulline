package org.akkapulline

import scala.reflect.ClassTag
import akka.actor._
import org.akkapulline.Messages._

class PullLineConsumer[L: ClassTag](workerLocation: String, leftLocation: String)
    extends Actor with ActorLogging {

  val worker = context.actorSelection(workerLocation)
  val left = context.actorSelection(leftLocation)

  override def postStop(): Unit = {
    log.info("PullLineConsumer stopped")
    left ! PoisonPill
  }

  def receive = {
    case Consume => left ! Pull

    case PullDone(result: Option[L]) => worker ! Work(result)

    case WorkDone(_) | WorkIsReady => left ! Pull

    case NoMoreData =>
      log.info("No more data in the line. Exiting.")
      worker ! PoisonPill
  }
}