import akka.actor.{ Props, ActorSystem }
import akka.testkit.{ ImplicitSender, TestKit }
import java.util.UUID
import org.scalatest.{ WordSpecLike, BeforeAndAfterAll }
import org.scalatest.matchers.ShouldMatchers
import org.saglam.pullline.Messages._
import org.saglam.pullline._
import scala.util.Success

object PullLineProducerSpec {
  class RandomStringProducer(count: Int) extends PullLineWorker[Unit, String] {

    var produced = 0

    override def doWork(work: Option[Unit]): Option[String] = {
      if (produced == count)
        None
      else {
        produced += 1
        Some(UUID.randomUUID().toString)
      }
    }
  }
}

class PullLineProducerSpec(_system: ActorSystem)
    extends TestKit(_system)
    with ImplicitSender
    with WordSpecLike
    with ShouldMatchers
    with BeforeAndAfterAll {

  def this() = this(ActorSystem("PullLineProducerSpec"))

  import PullLineProducerSpec._

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "A RandomStringProducer" should {

    "produce 2 random strings" in {
      val producer = system.actorOf(Props(new RandomStringProducer(2)))
      producer ! Work(None)
      expectMsgPF() { case WorkDone(Success(result)) => result }
      producer ! Work(None)
      expectMsgPF() { case WorkDone(Success(result)) => result }
      producer ! Work(None)
      expectMsg(Exhausted)
    }
  }

  "A PullLineProducer" should {

    "produce 2 random strings" in {
      system.actorOf(Props(new RandomStringProducer(2)),
        "producer")
      val pullLineProducer = system.actorOf(Props(
        new PullLineProducer[String]("/user/producer", testActor.path.toString,
          1000)), "pullLineProducer")

      pullLineProducer ! Pull
      expectMsg(WorkIsReady)
      pullLineProducer ! Pull
      expectMsgPF() { case PullDone(Some(result)) => result }
      pullLineProducer ! Pull
      expectMsgPF() {
        case PullDone(Some(result)) => result
        case WorkIsReady =>
          pullLineProducer ! Pull
          expectMsgPF() { case PullDone(Some(result)) => result }
      }
      pullLineProducer ! Pull
      expectMsg(NoMoreData)
    }
  }

}
