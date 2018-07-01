package vs

import akka.actor._
import akka.stream.OverflowStrategy
import akka.stream.scaladsl._
import protocols.Protocol

case class Subscriber(name: String, var status: PlayerStatus, var isLeader: Boolean)
case class PlayerStatus(playerStatus: Int, currentTime: Double, videoUrl: String)

trait Room {
  def roomInFlow(sender: String): Flow[String, Protocol.Message, Any]

  def injectMessage(message: Protocol.Message): Unit
}

object Room {
  def create(system: ActorSystem): Room = {
    val roomActor = system.actorOf(Props(new Actor {
      var subscribers = Set.empty[(Subscriber, ActorRef)]
      var playlist = Set.empty[(Boolean, String)]

      override def receive: Receive = {
        case NewParticipant(name, subscriber) =>
          context.watch(subscriber)
          if(subscribers.isEmpty) {
            subscribers += (Subscriber(name, PlayerStatus(0,0, ""), isLeader = true) -> subscriber)
            dispatch(Protocol.Joined(name, subscribers.map(c => c._1.isLeader -> c._1.name)))
          }
          else{
            subscribers += (Subscriber(name, PlayerStatus(0,0, ""), isLeader = false) -> subscriber)
            dispatch(Protocol.Joined(name, subscribers.map(c => c._1.isLeader -> c._1.name)))
          }
        case msg: ReceivedMessage =>
          println("Received: " + msg.message)
          if(msg.message.equals("/play"))
            dispatch(msg.toPlayMessage)
          else if(msg.message.equals("/pause"))
            dispatch(msg.toPauseMessage)
          else if(msg.message.contains("/status")) {
            val splittedMsg = msg.message.split(" ")
            subscribers.find(_._1.name == msg.sender).get._1.status = PlayerStatus(splittedMsg(1).toInt, splittedMsg(2).toDouble, splittedMsg(3))
            dispatch(msg.toStatusMessage(subscribers.find(_._1.name == msg.sender).get._1.status))
          }
          else if(msg.message.contains("/add")) {
            val playing = msg.message.split(" ")(1).toBoolean
            val videoId = msg.message.split(" ")(2)
            playlist += (playing -> videoId)
            dispatch(msg.toAddMessage)
          }
          else if(msg.message.equals("/next")){

            val nowPlaying = playlist.find(_._1)
            val next = nowPlaying match {
              case Some(x) =>
                playlist -= x
                if(playlist.isEmpty)
                  playlist += (false -> "xy_NKN75Jhw")
                playlist.head
              case None =>
                if(playlist.isEmpty)
                  playlist += (false -> "xy_NKN75Jhw")
                playlist.head
            }
            playlist = Set(true -> next._2) ++ playlist.filterNot(_ == next)
            dispatch(msg.toLoadMessage(next._2))
            dispatch(Protocol.PlaylistUpdate(playlist))
          }
          else if(msg.message.equals("/playlist")) {
            dispatch(Protocol.PlaylistUpdate(playlist))
          }
          else if(msg.message.equals("/members")) {
            dispatch(Protocol.MemberStatus(subscribers.map(c => c._1.isLeader -> c._1.name)))
          }
          else
            dispatch(msg.toChatMessage)
        case msg: Protocol.StatusRequest => dispatch(msg)
        case msg: Protocol.ChatMessage => dispatch(msg)
        case msg: Protocol.PlayVideo => dispatch(msg)
        case msg: Protocol.PauseVideo => dispatch(msg)

        case ParticipantLeft(person) =>
          val entry @ (name, ref) = subscribers.find(_._1.name == person).get
          ref ! Status.Success(Unit)
          subscribers -= entry
          if(name.isLeader && subscribers.nonEmpty)
            subscribers.head._1.isLeader = true // if the leader left, set the next in the list as leader

          dispatch(Protocol.Left(person, subscribers.map(c => c._1.isLeader -> c._1.name)))
        case Terminated(sub) =>
          subscribers = subscribers.filterNot(_._2 == sub)
      }
      def sendAdminMessage(msg: String): Unit = dispatch(Protocol.ChatMessage("admin", msg))
      def dispatch(msg: Protocol.Message): Unit = subscribers.foreach(_._2 ! msg) // send msg to all actorref
      def members = subscribers.map(_._1).toSeq
    }))

    def roomInSink(sender: String) = Sink.actorRef[ChatEvent](roomActor, ParticipantLeft(sender))

    new Room {
      def roomInFlow(sender: String): Flow[String, Protocol.ChatMessage, Any] = {
        val in = Flow[String]
          .map(ReceivedMessage(sender, _))
          .to(roomInSink(sender))
        val out = Source
          .actorRef[Protocol.ChatMessage](64, OverflowStrategy.fail)
          .mapMaterializedValue(roomActor ! NewParticipant(sender, _))

        Flow.fromSinkAndSource(in, out)
      }

      override def injectMessage(message: Protocol.Message): Unit = roomActor ! message
    }

  }
  private sealed trait ChatEvent
  private case class NewParticipant(name: String, subscriber: ActorRef) extends ChatEvent
  private case class ParticipantLeft(name: String) extends ChatEvent
  private case class ReceivedMessage(sender: String, message: String) extends ChatEvent {
    def toChatMessage: Protocol.ChatMessage = Protocol.ChatMessage(sender, message)
    def toPlayMessage: Protocol.PlayVideo = Protocol.PlayVideo(sender)
    def toPauseMessage: Protocol.PauseVideo = Protocol.PauseVideo(sender)
    def toStatusMessage(playerStatus: PlayerStatus): Protocol.StatusMessage = Protocol.StatusMessage(sender, playerStatus.playerStatus, playerStatus.currentTime, playerStatus.videoUrl)
    def toAddMessage: Protocol.AddVideo = Protocol.AddVideo(sender, message.split(" ")(2))
    def toLoadMessage(nextId: String): Protocol.LoadVideo = Protocol.LoadVideo(sender, nextId)
  }
}
