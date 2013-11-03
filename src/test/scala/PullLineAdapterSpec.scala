import akka.actor.{ Props, ActorSystem }
import akka.testkit.{ ImplicitSender, TestKit }
import java.util.UUID
import org.scalatest.{ WordSpecLike, BeforeAndAfterAll }
import org.scalatest.matchers.ShouldMatchers
import org.saglam.pullline.Messages._
import org.saglam.pullline._
import scala.util.Success

object PullLineAdapterSpec {
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

  class OccurenceWorker extends PullLineWorker[String, Int] {
    def doWork(work: Option[String]): Option[Int] = work.map(_.count(_ == '0'))
  }
}

class PullLineAdapterSpec(_system: ActorSystem)
    extends TestKit(_system)
    with ImplicitSender
    with WordSpecLike
    with ShouldMatchers
    with BeforeAndAfterAll {

  def this() = this(ActorSystem("PullLineAdapterSpec"))

  import PullLineAdapterSpec._

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "An OccurenceAdapter" should {

    "process 2 strings" in {
      val adapter = system.actorOf(Props[OccurenceWorker])
      adapter ! Work(Some("0011"))
      expectMsg(WorkDone(Success(2)))
      adapter ! Work(Some("1000"))
      expectMsg(WorkDone(Success(3)))
    }
  }

  "A PullLineAdapter" should {

    "process 2 strings" in {
      system.actorOf(Props(new RandomStringProducer(2)),
        "producer")
      system.actorOf(
        Props(
          new PullLineProducer[String](
            "/user/producer",
            "/user/pullLineAdapter",
            1000)), "pullLineProducer")

      system.actorOf(Props[OccurenceWorker], "occurenceWorker")
      val pullLineAdapter = system.actorOf(
        Props(
          new PullLineAdapter[String, Int](
            "/user/occurenceWorker",
            "/user/pullLineProducer",
            testActor.path.toString,
            1000
          )
        ),
        "pullLineAdapter"
      )

      pullLineAdapter ! Pull
      expectMsg(WorkIsReady)
      pullLineAdapter ! Pull
      expectMsgPF() { case PullDone(Some(result)) => result }
      pullLineAdapter ! Pull
      expectMsgPF() {
        case PullDone(Some(result)) => result
        case WorkIsReady =>
          pullLineAdapter ! Pull
          expectMsgPF() { case PullDone(Some(result)) => result }
      }
      pullLineAdapter ! Pull
      expectMsg(NoMoreData)
    }
  }

}
