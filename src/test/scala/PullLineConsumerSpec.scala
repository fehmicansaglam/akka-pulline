import akka.actor.{ Props, ActorSystem }
import akka.testkit.{ EventFilter, ImplicitSender, TestKit }
import org.akkapulline.Messages._
import org.akkapulline._
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.{ BeforeAndAfterAll, WordSpecLike }
import scala.util.Success

object PullLineConsumerSpec {
  class StringConsumer extends PullLineWorker[String, Unit] {
    def doWork(work: Option[String]): Option[Unit] = work.map(log.info(_))
  }
}

class PullLineConsumerSpec(_system: ActorSystem)
    extends TestKit(_system)
    with ImplicitSender
    with WordSpecLike
    with ShouldMatchers
    with BeforeAndAfterAll {

  def this() = this(ActorSystem("PullLineConsumerSpec"))

  import PullLineConsumerSpec._

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "A StringConsumer" should {

    "consume 2 strings" in {
      val consumer = system.actorOf(Props[StringConsumer])
      val message = "SOME_WORK"
      EventFilter.info(message, occurrences = 2) intercept {
        consumer ! Work(Some(message))
        expectMsgPF() { case WorkDone(Success(result)) => result }
        consumer ! Work(Some(message))
        expectMsgPF() { case WorkDone(Success(result)) => result }
      }
    }
  }

}
