package vs

import java.util.Date

import akka.actor.ActorSystem
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.server.Directives
import akka.stream.scaladsl.Flow
import protocols.Protocol
import protocols.Protocol.{ChatMessage, StatusRequest, World, WorldRequest}
import upickle.default._

import scala.concurrent.duration._
import scala.util.Failure

class WebService(implicit system: ActorSystem) extends Directives{

  var rooms = Set.empty[(String, Room)] //list of all rooms
  import system.dispatcher

  /**
    * Routing, defines our uri schema and what they do.
    *
    * @return
    */
  def route = {
    get {
      pathSingleSlash {
        getFromResource("web/index.html")
      } ~
        path("RomanticBeach.ttf")(getFromResource("RomanticBeach.ttf")) ~
        path("img1.jpg")(getFromResource("img1.jpg")) ~
        path("frontend-fastopt.js")(getFromResource("frontend-fastopt.js")) ~
        path("chat") {
          parameter('name, 'room) { (name, room) =>
            rooms.find(_._1 == room) match {
              case Some((s,r)) =>
                handleWebSocketMessages(websocketRoomFlow(sender = name, room = (s,r)))
              case None =>
                handleWebSocketMessages(websocketRoomFlow(sender = name, room = createRoom(room)))
            }
          }
        }
    }
  }

  /**
    * Create a new sync room.
    *
    * @param name room name
    * @return
    */
  def createRoom(name: String): (String, Room) = {
    val room = Room.create(system)
    rooms += (name -> room)
    system.scheduler.schedule(5.seconds, 5.seconds) {
      room.injectMessage(StatusRequest(sender = "server"))
    }
    system.scheduler.schedule(60.seconds, 60.seconds) {
      room.injectMessage(WorldRequest(sender = "server"))
    }
    rooms.find(_._1 == name).get // unsafe
  }

  /**
    * Handle messaging for sync rooms inside flow
    *
    * @param sender
    * @param room
    * @return
    */
  def websocketRoomFlow(sender:String, room: (String, Room)): Flow[Message, Message, Any] = Flow[Message]
    .collect {
      case TextMessage.Strict(msg) => msg
    }
    .via(room._2.roomInFlow(sender))
    .map {
      case msg: Protocol.Message => TextMessage.Strict(write(msg))
    }
    .via(reportErrorsFlow)

  /**
    * Simply prints errors if something fails.
    *
    * @tparam T
    * @return
    */
  def reportErrorsFlow[T]: Flow[T, T, Any] =
    Flow[T]
      .watchTermination()((_, f) => f.onComplete {
        case Failure(cause) =>
          println(s"WS stream failed with $cause")
        case _ => // ignore regular completion
      })
}
