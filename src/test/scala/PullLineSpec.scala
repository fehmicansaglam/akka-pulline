import akka.actor.{ Props, ActorSystem }
import akka.testkit.{ EventFilter, ImplicitSender, TestKit }
import java.util.UUID
import org.scalatest.{ WordSpecLike, BeforeAndAfterAll }
import org.scalatest.matchers.ShouldMatchers
import org.akkapulline.Messages._
import org.akkapulline._

object PullLineSpec {

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

  class OccurenceAdapter extends PullLineWorker[String, Int] {
    def doWork(work: Option[String]): Option[Int] = work.map(_.count(_ == '0'))
  }

  class AvgOccurenceAdapter extends PullLineWorker[Int, Int] {
    var total = 0
    var count = 0
    def doWork(work: Option[Int]): Option[Int] = work.map { num =>
      total += num
      count += 1
      total / count
    }
  }

  class AvgOccurenceConsumer extends PullLineWorker[Int, Unit] {
    def doWork(work: Option[Int]): Option[Unit] = work.map {
      log.info("Average occurences: {}", _)
    }
  }
}

class PullLineSpec(_system: ActorSystem)
    extends TestKit(_system)
    with ImplicitSender
    with WordSpecLike
    with ShouldMatchers
    with BeforeAndAfterAll {

  def this() = this(ActorSystem("PullLineSpec"))

  import PullLineSpec._

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "A Pull Line" should {

    "process 2 random strings" in {
      val producer = system.actorOf(Props(new RandomStringProducer(2)),
        "producer")
      val occurenceAdapter = system.actorOf(Props[OccurenceAdapter],
        "occurenceAdapter")
      val avgOccurenceAdapter = system.actorOf(Props[AvgOccurenceAdapter],
        "avgOccurenceAdapter")
      val avgOccurenceConsumer = system.actorOf(Props[AvgOccurenceConsumer],
        "avgOccurenceConsumer")

      val pullLineProducer = system.actorOf(
        Props(
          new PullLineProducer[String](
            "/user/producer",
            "/user/pullLineAdapter1",
            1000)
        ),
        "pullLineProducer"
      )

      val pullLineAdapter1 = system.actorOf(
        Props(
          new PullLineAdapter[String, Int](
            "/user/occurenceAdapter",
            "/user/pullLineProducer",
            "/user/pullLineAdapter2",
            1000)
        ),
        "pullLineAdapter1"
      )

      val pullLineAdapter2 = system.actorOf(
        Props(
          new PullLineAdapter[Int, Int](
            "/user/avgOccurenceAdapter",
            "/user/pullLineAdapter1",
            "/user/pullLineConsumer",
            1000)
        ),
        "pullLineAdapter2"
      )

      val pullLineConsumer = system.actorOf(
        Props(
          new PullLineConsumer[Int](
            "/user/avgOccurenceConsumer",
            "/user/pullLineAdapter2"
          )
        ),
        "pullLineConsumer"
      )

      EventFilter.info(pattern = "Average occurences: [0-9]+", occurrences = 2) intercept {
        pullLineConsumer ! Consume
      }
    }
  }

}
