import akka.actor.ActorSystem
import akka.testkit.{ ImplicitSender, TestKit }
import org.saglam.pullline.PullLineWorker
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.{ BeforeAndAfterAll, WordSpecLike }

object PullLineConsumerSpec {
  class RandomStringConsumer extends PullLineWorker[Unit] {

    override def doWork(work: Option[Any]): Option[Unit] = {
      work.map { str => log.info(str.toString) }
    }
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

}
