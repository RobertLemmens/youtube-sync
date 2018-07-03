package vs

import org.scalajs.dom
import dom.{KeyboardEvent, document, html}

import scala.scalajs.js
import org.scalajs.dom.raw._
import protocols.Protocol
import upickle.default._

object Frontend {

  /**
    * Initialize our buttons class scope as we might need them everywhere.
    */
  val addButton = dom.document.getElementById("addButton").asInstanceOf[HTMLButtonElement]
  val playButton = dom.document.getElementById("playButton").asInstanceOf[HTMLButtonElement]
  val pauseButton = dom.document.getElementById("pauseButton").asInstanceOf[HTMLButtonElement]
  val nextButton = dom.document.getElementById("nextButton").asInstanceOf[HTMLButtonElement]
  val sendMessageButton = dom.document.getElementById("sendMessageButton").asInstanceOf[HTMLButtonElement]
  val enableAutoPlayButton = dom.document.getElementById("enableAutoplayButton").asInstanceOf[HTMLButtonElement]
  val disableAutoPlayButton = dom.document.getElementById("disableAutoplayButton").asInstanceOf[HTMLButtonElement]
  val connectButton = dom.document.getElementById("connectButton").asInstanceOf[HTMLButtonElement]


  var following = "" // The room leader
  var playlist = Set.empty[(Boolean, String)] // true = now playing, or next to play when Play is called

  /**
    * Entrypoint
    *
    * @param args
    */
  def main(args: Array[String]): Unit = {
    addPlayer(document.body)
    hideMain()
  }

  /**
    * Hide the login interface (joining a room). When you join a room this gets called and the main interface is shown.
    *
    */
  def hideLogin(): Unit = {
    document.getElementById("login").setAttribute("style", "display: none;")
  }

  /**
    * Hide the main interface.
    *
    */
  def hideMain(): Unit = {
    document.getElementById("mainNav").setAttribute("style", "display: none;")
    document.getElementById("mainRow1").setAttribute("style", "display: none;")
  }

  /**
    * Show the main interface. Gets called after a room is joined.
    *
    */
  def showMain(): Unit = {
    document.getElementById("mainNav").removeAttribute("style")
    document.getElementById("mainRow1").removeAttribute("style")
  }


  /**
    * Creates a youtube player. Basically the frontend entry point. (this gets called in main).
    *
    * @param targetNode
    */
  def addPlayer(targetNode: dom.Node): Unit = {
    var tag = org.scalajs.dom.document.createElement("script").asInstanceOf[org.scalajs.dom.html.Script]
    tag.src = "https://www.youtube.com/iframe_api"
    var firstScriptTag = org.scalajs.dom.document.getElementsByTagName("script").item(0)
    firstScriptTag.parentNode.insertBefore(tag, firstScriptTag)
    targetNode.appendChild(firstScriptTag)

    (org.scalajs.dom.window.asInstanceOf[js.Dynamic]).onYouTubeIframeAPIReady = () => {
      createPlayer()
    }
  }

  /**
    * Frontend setup.
    *
    * @param player
    */
  def setupUI(player: Player): Unit = {
    connectButton.onclick = { (event: org.scalajs.dom.raw.Event) =>
      val userNameField = dom.document.getElementById("name").asInstanceOf[HTMLInputElement]
      val serverNameField = dom.document.getElementById("room").asInstanceOf[HTMLInputElement]
      joinChat(userNameField.value, serverNameField.value, player)
      hideLogin()
      showMain()
    }
  }

  /**
    * The bread and butter of the frontend. Connection and message handling is all done in here.
    *
    * @param name the username
    * @param room the name of the room
    * @param player youtube player reference
    */
  def joinChat(name: String, room: String, player: Player): Unit = {
    appendLog(s"Trying to join as '$name'...")
    val chat = new WebSocket(getWebsocketUri(dom.document, name, room))
    chat.onopen = { (event: org.scalajs.dom.raw.Event) ⇒
      appendLog("Connection succesfull")
      sendMessageButton.disabled = false
      val roomLabelP = document.getElementById("roomLabel").asInstanceOf[HTMLParagraphElement]
      roomLabelP.innerHTML = ""
      roomLabelP.appendChild(document.createTextNode(s"Room: $room"))


      val chatField = dom.document.getElementById("chatField").asInstanceOf[HTMLInputElement]
      chatField.focus()
      chatField.onkeypress = { (event: KeyboardEvent) ⇒
        if (event.keyCode == 13) {
          sendMessageButton.click()
          event.preventDefault()
        }
      }
      sendMessageButton.onclick = { (event: org.scalajs.dom.raw.Event) ⇒
        chat.send(chatField.value)
        chatField.value = ""
        chatField.focus()
        event.preventDefault()
      }
      playButton.onclick = { (event: org.scalajs.dom.raw.Event) =>
        chat.send("/play")
      }
      pauseButton.onclick = { (event: org.scalajs.dom.raw.Event) =>
        chat.send("/pause")
      }
      nextButton.onclick = { event: org.scalajs.dom.raw.Event =>
        chat.send("/next")
      }
      addButton.onclick = { event: org.scalajs.dom.raw.Event =>
        val urlField = dom.document.getElementById("videoUrlField").asInstanceOf[HTMLInputElement]
        val videoId = urlField.value.split("v=")(1)
        appendLog("sending server: " + videoId)
        chat.send("/add false " + videoId)
      }
      enableAutoPlayButton.onclick = {
        event: org.scalajs.dom.raw.Event =>
          chat.send("/settings true")
          enableAutoPlayButton.setAttribute("disabled", "true")
          disableAutoPlayButton.removeAttribute("disabled")
      }
      disableAutoPlayButton.onclick = {
        event: org.scalajs.dom.raw.Event =>
          chat.send("/settings false")
          disableAutoPlayButton.setAttribute("disabled", "true")
          enableAutoPlayButton.removeAttribute("disabled")
      }

      chat.send("/playlist")
      chat.send("/members")
      event
    }
    chat.onerror = { (event: org.scalajs.dom.raw.Event) ⇒
      appendLog(s"Failed: code: ${event.toString}")
      sendMessageButton.disabled = true
    }
    chat.onmessage = { (event: MessageEvent) ⇒

      val wsMsg = read[Protocol.Message](event.data.toString)

      wsMsg match {
        case Protocol.ChatMessage(sender, message) ⇒
          appendLog(s"$sender said: $message")
        case Protocol.Joined(member, allMembers) ⇒
          updateUserList(allMembers)
          appendLog(s"$member joined!")
        case Protocol.Left(member, allMembers) ⇒
          updateUserList(allMembers)
          appendLog(s"$member left!")
        case Protocol.PlayVideo(sender) =>
          appendLog(s"$sender started playback")
          player.playVideo()
        case Protocol.PauseVideo(sender) =>
          appendLog(s"$sender paused playback")
          player.getPlayerState() match {
            case Player.State.PAUSED => println("Already pasued")
            case Player.State.PLAYING => player.pauseVideo()
          }
        case Protocol.AddVideo(sender, url) =>
          appendLog(s"$sender added $url to the queue")
          playlist += (false -> url)
          updatePlayList(playlist)
        case Protocol.StatusRequest(sender) =>
          chat.send("/status " + player.getPlayerState() + " " + player.getCurrentTime() + " " + player.getVideoUrl())
        case Protocol.StatusMessage(sender, status, time, url) =>
          if(sender.equals(following)) {
            if(checkForUpdate(player, status, time, url)){
              appendLog("Doing an update")
              updatePlayer(player, status, time ,url)
            }
          }
        case Protocol.StatusUpdate(status, time, url) =>
          updatePlayer(player, status, time, url)
        case Protocol.LoadVideo(sender, videoId) =>
          appendLog(s"$sender started playback for $videoId")
          player.loadVideoById(videoId, 0.0, "large")
        case Protocol.PlaylistUpdate(newPlayList) =>
          updatePlayList(newPlayList)
        case Protocol.MemberStatus(members) => {
          updateUserList(members)
        }
        case Protocol.SettingsUpdate(setting) => {
          if(setting) {
            enableAutoPlayButton.setAttribute("disabled", "true")
            disableAutoPlayButton.removeAttribute("disabled")
          } else {
            disableAutoPlayButton.setAttribute("disabled", "true")
            enableAutoPlayButton.removeAttribute("disabled")
          }
        }

      }
    }
    chat.onclose = { (event: org.scalajs.dom.raw.Event) ⇒
      appendLog("Connection to chat lost. You can try to rejoin manually.")
      sendMessageButton.disabled = true
    }
  }

  /**
    * Initialize the youtube player.
    *
    * @return
    */
  def createPlayer(): Player = {
    val player = new Player("player", PlayerOptions(
      width = "100%",
      height = "100%",
      videoId = "xy_NKN75Jhw",
      events = PlayerEvents(
        onReady = onPlayerReady _,
        onError = onPlayerError _,
        onStateChange = onPlayerStateChange _
      ),
      playerVars = PlayerVars(
        playsinline = 1.0
      )
    ))
    player
  }

  /**
    * This checks if the player is in some form out of sync with the "leader" of the room. If this returns true,
    * updatePlayer gets called.
    *
    * @param player
    * @param status
    * @param time
    * @param url
    * @return
    */
  def checkForUpdate(player: Player, status: Int, time: Double, url: String): Boolean = {
    val urlEen = player.getVideoUrl().split("v=")(1)
    val urlTwee = url.split("v=")(1)
    if(player.getPlayerState() != status) {
      appendLog("State out of sync")
      true
    }
    else if(!urlEen.equals(urlTwee)) {
      appendLog("Video url out of sync")
      true
    }
    else if(!(Math.abs(player.getCurrentTime()-time) < 2)){
      appendLog("Time out of sync by " + Math.abs(player.getCurrentTime()-time))
      true
    }
    else {
      false
    }
  }

  /**
    * This gets called when the player is deemed out of sync with the "leader" of the room. It checks whats wrong and updates the
    * respecting fields.
    *
    * @param player
    * @param status
    * @param time
    * @param url
    */
  def updatePlayer(player: Player, status: Int, time: Double, url: String): Unit = {
    appendLog("Trying to update..")
    val urlEen = player.getVideoUrl().split("v=")(1)
    val urlTwee = url.split("v=")(1)
    if(!urlEen.equals(urlTwee)) {
      appendLog("Loading video " + url)
      player.loadVideoById(urlTwee, time, "large")
    } else if(player.getPlayerState() != status) {
      if(status == Player.State.PLAYING) {
        appendLog("Starting video playback")
        player.playVideo()
      }
      else if(status == Player.State.PAUSED)
        player.pauseVideo()
      else if(status == Player.State.UNSTARTED)
        player.stopVideo()
    } else if(player.getCurrentTime() != time) {
      player.seekTo(time, true)
    }
  }

  /**
    * Gets called when the youtube player is ready. Usually when its done initializing. We use this to start setting up
    * the interface when its ready for it.
    *
    * @param event
    */
  def onPlayerReady(event: YTEvent): Unit = {
    val p = event.target.asInstanceOf[Player]
    setupUI(p)
    p.loadVideoById(playlist.find(_._1 == true).get._2, 0.0, "large")
    appendLog("playlist video: " + p.getVideoUrl())

  }

  /**
    * Gets called by the youtube player when an error occurs.
    *
    * @param event
    */
  def onPlayerError(event: YTEvent): Unit = {
    val p = event.target.asInstanceOf[Player]
    appendLog("An error occured during playback")
    p.clearVideo()
  }

  /**
    * Gets called by the youtube player when playback state changes.
    *
    * @param event
    */
  def onPlayerStateChange(event: YTEvent): Unit = {

  }

  /**
    * Appends messages to the log area(chat field).
    *
    * @param text
    */
  def appendLog(text: String): Unit = {
    val logNode = dom.document.getElementById("logArea").asInstanceOf[HTMLTextAreaElement]
    logNode.value += "\n"+text
  }

  /**
    * Updates the userlist component.
    *
    * @param members
    */
  def updateUserList(members: Set[(Boolean, String)]): Unit = {
    val userList = dom.document.getElementById("userList").asInstanceOf[HTMLUListElement]
    userList.innerHTML = ""
    members.foreach{
      name =>
        val liNode = document.createElement("li")
        if(name._1) {
          following = name._2
          liNode.setAttribute("class", "collection-item active red")
        }
        else
          liNode.setAttribute("class", "collection-item")
        liNode.appendChild(document.createTextNode(name._2))
        userList.appendChild(liNode)
    }
  }

  /**
    * Updates the playlist component.
    *
    * @param newList
    */
  def updatePlayList(newList: Set[(Boolean, String)]): Unit = {
    playlist = newList
    val playlistList = dom.document.getElementById("playlistList").asInstanceOf[HTMLUListElement]
    playlistList.innerHTML = ""
    playlist.foreach{
      item =>
        val liNode = document.createElement("li")

        if(item._1) {
          liNode.appendChild(document.createTextNode("Now playing " + item._2))
          liNode.setAttribute("class", "collection-item active red")
        }
        else {
          liNode.appendChild(document.createTextNode("--- " + item._2))
          liNode.setAttribute("class", "collection-item")
        }
        playlistList.appendChild(liNode)
    }
  }

  /**
    * Gets us the correct URL for the websocket connection. This implies that you're hosting the frontend on the same
    * URL as the backend is running on.
    *
    * @param document
    * @param name
    * @param room
    * @return
    */
  def getWebsocketUri(document: Document, name: String, room: String): String = {
    val wsProtocol = if (dom.document.location.protocol == "https:") "wss" else "ws"

    s"$wsProtocol://${dom.document.location.host}/chat?name=$name&room=$room"
  }

}
