package vs

import akka.actor._
import akka.stream.OverflowStrategy
import akka.stream.scaladsl._
import protocols.Protocol

import scala.collection.mutable.{ListBuffer, Stack}

case class Subscriber(name: String, var status: PlayerStatus, var isLeader: Boolean)
case class PlayerStatus(playerStatus: Int, currentTime: Double, videoUrl: String)

trait Room {
  def roomInFlow(sender: String): Flow[String, Protocol.Message, Any]

  def injectMessage(message: Protocol.Message): Unit
}

object Room {
  /**
    * Basically a glorified apply method. We just called it create here to make sure we explicitly call this.
    *
    * @param system
    * @return
    */
  def create(system: ActorSystem): Room = {
    val roomActor = system.actorOf(Props(new Actor {

      // The var corner e.g. danger zone
      var subscribers = Set.empty[(Subscriber, ActorRef)] //list of connected users
      var playlist = new ListBuffer[(Boolean, String)]() // the video playlist, listbuffer for mutability
      var autoplay = false //autoplay
      var leaderMode = false // only leader can control playback
      var leaderStatus: PlayerStatus = PlayerStatus(0,0,"") //keep track of leader status
      // End of the var corner

      /**
        * Handle incoming messages. The gut of this application.
        *
        * @return
        */
      override def receive: Receive = {
        case NewParticipant(name, subscriber) => handlePersonJoined(name, subscriber)
        case msg: ReceivedMessage => handleReceivedMessage(msg)
        case msg: Protocol.StatusRequest => dispatch(msg)
        case msg: Protocol.ChatMessage => dispatch(msg)
        case msg: Protocol.PlayVideo => dispatch(msg)
        case msg: Protocol.PauseVideo => dispatch(msg)
        case msg: Protocol.WorldRequest => dispatch(msg)
        case msg: Protocol.World => dispatch(msg)
        case ParticipantLeft(person) => handlePersonleft(person)
        case Terminated(sub) => subscribers = subscribers.filterNot(_._2 == sub)
      }

      /**
        * Sends a message with user "admin" to all clients. If needed.
        *
        * @param msg
        */
      def sendAdminMessage(msg: String): Unit = dispatch(Protocol.ChatMessage("admin", msg))

      /**
        * Sends message to all clients
        *
        * @param msg
        */
      def dispatch(msg: Protocol.Message): Unit = subscribers.foreach(_._2 ! msg) // send msg to all actorref

      /**
        * Returns list of "Subscriber" case class. Holds the user information, without the corresponding actor.
        *
        * @return
        */
      def members = subscribers.map(_._1).toSeq

      /**
        * Skip towards the next video if possible. If there is no next video to be played, elevator music will be added
        * and played instead.
        *
        * @param msg
        */
      def nextVideo(msg: ReceivedMessage): Unit = {
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

        playlist.update(playlist.indexOf(next), next.copy(_1 = true, _2 = next._2)) // set next op true
        dispatch(msg.toLoadMessage(next._2))
        dispatch(Protocol.PlaylistUpdate(playlist.toList))
      }

      def addVideo(msg: ReceivedMessage): Unit = {
          val messageParts = msg.message.split(" ")
          if (messageParts.length == 3 ) {
          val playing = msg.message.split(" ")(1).toBoolean
          val videoId = msg.message.split(" ")(2)
          playlist += (playing -> videoId)
          dispatch(msg.toAddMessage)
        }
      }

      def updateLeaderStatus(state: Int, time: Double, url: String): Unit = {
        leaderStatus = PlayerStatus(state, time, url)
      }

      def statusTick(msg: ReceivedMessage): Unit = {
        val splittedMsg = msg.message.split(" ")
        if(subscribers.find(_._1.name == msg.sender).get._1.isLeader) {
          updateLeaderStatus(splittedMsg(1).toInt, splittedMsg(2).toDouble, splittedMsg(3))
        }
        subscribers.find(_._1.name == msg.sender).get._1.status = PlayerStatus(splittedMsg(1).toInt, splittedMsg(2).toDouble, splittedMsg(3))
        if(autoplay && subscribers.find(_._1.name == msg.sender).get._1.isLeader && splittedMsg(1).toInt == 0) { // autoplay the next video
          nextVideo(msg)
        }
        dispatch(msg.toStatusMessage(subscribers.find(_._1.name == msg.sender).get._1.status))
      }

      /**
        *   Handle a chat message. First check if it starts with a command, if not, dispatch as normal chat message.
        */
      def handleReceivedMessage(msg: ReceivedMessage): Unit = {
        println("Received: " + msg.message)
        if(msg.message.equals("/play"))
          dispatch(msg.toPlayMessage)
        else if(msg.message.equals("/pause"))
          dispatch(msg.toPauseMessage)
        else if(msg.message.contains("/status"))
          statusTick(msg)
        else if(msg.message.contains("/add"))
          addVideo(msg)
        else if(msg.message.equals("/next"))
          nextVideo(msg)
        else if(msg.message.equals("/playlist"))
          dispatch(Protocol.PlaylistUpdate(playlist.toList))
        else if(msg.message.equals("/members"))
          dispatch(Protocol.MemberStatus(subscribers.map(c => c._1.isLeader -> c._1.name)))
        else if(msg.message.contains("/settings"))
          autoplay = msg.message.split(" ")(1).toBoolean
        else if(msg.message.equals("/world")) {
          dispatch(Protocol.World(leaderStatus.playerStatus,leaderStatus.currentTime,leaderStatus.videoUrl, autoplay, subscribers.map(c => c._1.isLeader -> c._1.name), playlist.toList))
        }
        else
          dispatch(msg.toChatMessage)
      }

      /**
        * Handle joining person. Add to subscriber list and dispatch message
        * @param name
        * @param subscriber
        */
      def handlePersonJoined(name: String, subscriber: ActorRef): Unit = {
        context.watch(subscriber)
        if(subscribers.isEmpty)
          subscribers += (Subscriber(name, PlayerStatus(0,0, ""), isLeader = true) -> subscriber)
        else
          subscribers += (Subscriber(name, PlayerStatus(0,0, ""), isLeader = false) -> subscriber)

        dispatch(Protocol.Joined(name, subscribers.map(c => c._1.isLeader -> c._1.name)))
      }

      /**
        * Handle leaving person. Remove from subscriber list and dispatch message
        * @param person
        */
      def handlePersonleft(person: String): Unit = {
        val entry @ (name, ref) = subscribers.find(_._1.name == person).get
        ref ! Status.Success(Unit)
        subscribers -= entry
        if(name.isLeader && subscribers.nonEmpty)
          subscribers.head._1.isLeader = true // if the leader left, set the next in the list as leader
        println("userlist: " + subscribers.map(c => c._1.isLeader -> c._1.name))
        dispatch(Protocol.Left(person, subscribers.map(c => c._1.isLeader -> c._1.name)))
      }

    }))

    def roomInSink(sender: String) = Sink.actorRef[ChatEvent](roomActor, ParticipantLeft(sender))

    /**
      * Create and return our room as Flow[_]
      */
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

      /**
        * Inject message into the room by calling this on the object itself.
        *
        * @param message
        */
      override def injectMessage(message: Protocol.Message): Unit = roomActor ! message

    }

  }

  /**
    * Some basic ADT and transformer to our own Protocol types for chat messages / events
    */
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
