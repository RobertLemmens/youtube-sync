package vs

import org.scalajs.dom
import dom.{KeyboardEvent, document, html}

import scala.scalajs.js
import org.scalajs.dom.raw._
import protocols.Protocol
import upickle.default._

object Frontend {

  val joinButton = dom.document.getElementById("joinButton").asInstanceOf[HTMLButtonElement]
  val addButton = dom.document.getElementById("addButton").asInstanceOf[HTMLButtonElement]
  val playButton = dom.document.getElementById("playButton").asInstanceOf[HTMLButtonElement]
  val pauseButton = dom.document.getElementById("pauseButton").asInstanceOf[HTMLButtonElement]
  val nextButton = dom.document.getElementById("nextButton").asInstanceOf[HTMLButtonElement]
  val sendMessageButton = dom.document.getElementById("sendMessageButton").asInstanceOf[HTMLButtonElement]

  val enableAutoPlayButton = dom.document.getElementById("enableAutoplayButton").asInstanceOf[HTMLButtonElement]
  val disableAutoPlayButton = dom.document.getElementById("disableAutoplayButton").asInstanceOf[HTMLButtonElement]


  var following = ""
  // true = now playing, or next to play when Play is called
  var playlist = Set.empty[(Boolean, String)]

  def main(args: Array[String]): Unit = {
    addPlayer(document.body)
  }

  def addClickedMessage(player: Player): Unit = {
    val inputText = document.getElementById("videoUrl").asInstanceOf[html.Input].value.toString
    appendLog("you added " + inputText)
  }


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

  def setupUI(player: Player): Unit = {
    joinButton.onclick = { (event: org.scalajs.dom.raw.Event) =>
      val userNameField = dom.document.getElementById("userNameField").asInstanceOf[HTMLInputElement]
      val serverNameField = dom.document.getElementById("serverNameField").asInstanceOf[HTMLInputElement]
      joinChat(userNameField.value, serverNameField.value, player)
    }
  }

  def joinChat(name: String, room: String, player: Player): Unit = {
    joinButton.disabled = true

    appendLog(s"Trying to join as '$name'...")
    val chat = new WebSocket(getWebsocketUri(dom.document, name, room))
    chat.onopen = { (event: org.scalajs.dom.raw.Event) ⇒
      appendLog("Connection succesfull")
      sendMessageButton.disabled = false


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
          enableAutoPlayButton.disabled = true
          disableAutoPlayButton.disabled = false
      }
      disableAutoPlayButton.onclick = {
        event: org.scalajs.dom.raw.Event =>
          chat.send("/settings false")
          disableAutoPlayButton.disabled = true
          enableAutoPlayButton.disabled = false
      }

      chat.send("/playlist")
      chat.send("/members")
      event
    }
    chat.onerror = { (event: org.scalajs.dom.raw.Event) ⇒
      appendLog(s"Failed: code: ${event.toString}")
      joinButton.disabled = false
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

      }
    }
    chat.onclose = { (event: org.scalajs.dom.raw.Event) ⇒
      appendLog("Connection to chat lost. You can try to rejoin manually.")
      joinButton.disabled = false
      sendMessageButton.disabled = true
    }
  }

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

  def onPlayerReady(event: YTEvent): Unit = {
    val p = event.target.asInstanceOf[Player]
    setupUI(p)
    p.loadVideoById(playlist.find(_._1 == true).get._2, 0.0, "large")
    appendLog("playlist video: " + p.getVideoUrl())

  }

  def onPlayerError(event: YTEvent): Unit = {
    val p = event.target.asInstanceOf[Player]
    p.clearVideo()
  }

  def onPlayerStateChange(event: YTEvent): Unit = {

  }

//  def appendPar(targetNode: dom.Node, text: String): Unit = {
//    val parNode = document.createElement("p")
//    val textNode = document.createTextNode(text)
//    parNode.appendChild(textNode)
//    targetNode.appendChild(parNode)
//  }

  def appendLog(text: String): Unit = {
    val logNode = dom.document.getElementById("logArea").asInstanceOf[HTMLTextAreaElement]
    logNode.value += "\n"+text
  }

  def updateUserList(members: Set[(Boolean, String)]): Unit = {
    val userList = dom.document.getElementById("userList").asInstanceOf[HTMLUListElement]
    userList.innerHTML = ""
    members.foreach{
      name =>
        val liNode = document.createElement("li")
        if(name._1) {
          following = name._2
          liNode.setAttribute("class", "collection-item active red accent-4")
        }
        else
          liNode.setAttribute("class", "collection-item")
        liNode.appendChild(document.createTextNode(name._2))
        userList.appendChild(liNode)
    }
  }

  def updatePlayList(newList: Set[(Boolean, String)]): Unit = {
    playlist = newList
    val playlistList = dom.document.getElementById("playlistList").asInstanceOf[HTMLUListElement]
    playlistList.innerHTML = ""
    playlist.foreach{
      item =>
        val liNode = document.createElement("li")

        if(item._1) {
          liNode.appendChild(document.createTextNode("Now playing " + item._2))
          liNode.setAttribute("class", "collection-item active red accent-4")
        }
        else {
          liNode.appendChild(document.createTextNode("--- " + item._2))
          liNode.setAttribute("class", "collection-item")
        }
        playlistList.appendChild(liNode)
    }
  }

  def getWebsocketUri(document: Document, name: String, room: String): String = {
    val wsProtocol = if (dom.document.location.protocol == "https:") "wss" else "ws"

    s"$wsProtocol://${dom.document.location.host}/chat?name=$name&room=$room"
  }

}
