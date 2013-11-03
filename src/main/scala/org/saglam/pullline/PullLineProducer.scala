package org.saglam.pullline

import akka.actor._
import scala.reflect.ClassTag
import scala.collection.mutable
import org.saglam.pullline.Messages._
import scala.util.Try

class PullLineProducer[R: ClassTag](workerLocation: String, rightLocation: String, bufferSize: Int)
    extends Actor with ActorLogging {

  val worker = context.actorSelection(workerLocation)
  val right = context.actorSelection(rightLocation)
  val rightBuffer = mutable.Queue.empty[R]

  override def postStop(): Unit = {
    log.info("PullLineProducer stopped")
  }

  def empty: Receive = {
    case Pull =>
      log.debug("Pull requested from {}. State: empty", sender)
      worker ! Work(None)

    case WorkDone(result: Try[R]) =>
      log.debug("Work done {}", result)
      result.foreach { _result =>
        rightBuffer.enqueue(_result)
        right ! WorkIsReady
      }
      worker ! Work(None)
      context.become(ready)

    case Exhausted => context.become(exhausted)
  }

  def ready: Receive = {
    case Pull =>
      log.debug("Pull requested from {}. State: ready", sender)
      sender ! PullDone(Some(rightBuffer.dequeue()))
      worker ! Work(None)
      if (rightBuffer.isEmpty)
        context.become(empty)

    case WorkDone(result: Try[R]) =>
      result.foreach(rightBuffer.enqueue(_))
      if (rightBuffer.size < bufferSize)
        worker ! Work(None)

    case Exhausted => context.become(exhausted)
  }

  def exhausted: Receive = {
    case Pull =>
      log.debug("Pull requested from {}. State: exhausted", sender)
      if (rightBuffer.isEmpty) {
        sender ! NoMoreData
        worker ! PoisonPill
      } else {
        sender ! PullDone(Some(rightBuffer.dequeue()))
      }

    case Exhausted =>
  }

  def receive = empty
}