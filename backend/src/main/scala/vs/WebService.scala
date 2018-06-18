package vs

import java.util.Date

import akka.actor.ActorSystem
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.server.Directives
import akka.stream.scaladsl.Flow
import protocols.Protocol
import protocols.Protocol.{ChatMessage, StatusRequest}
import upickle.default._

import scala.concurrent.duration._
import scala.util.Failure
class WebService(implicit system: ActorSystem) extends Directives{

  val theRoom = Room.create(system)
  import system.dispatcher
  system.scheduler.schedule(15.seconds, 15.seconds) {
    theRoom.injectMessage(StatusRequest(sender = "server"))
    theRoom.injectMessage(ChatMessage(sender = "server", s"Ping, the time is ${new Date().toString}"))
  }

  def route = {
    get {
      pathSingleSlash {
        getFromResource("web/index.html")
      } ~ path("chat") {
        parameter('name) { name =>
          handleWebSocketMessages(websocketRoomFlow(sender = name))
        }
      }
    }
  }

  def websocketRoomFlow(sender:String): Flow[Message, Message, Any] = Flow[Message]
    .collect {
      case TextMessage.Strict(msg) => msg
    }
    .via(theRoom.roomInFlow(sender))
    .map {
      case msg: Protocol.Message => TextMessage.Strict(write(msg))
    }
    .via(reportErrorsFlow)

  def reportErrorsFlow[T]: Flow[T, T, Any] =
    Flow[T]
      .watchTermination()((_, f) => f.onComplete {
        case Failure(cause) =>
          println(s"WS stream failed with $cause")
        case _ => // ignore regular completion
      })
}
