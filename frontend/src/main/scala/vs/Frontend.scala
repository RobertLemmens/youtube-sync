package vs

import org.scalajs.dom
import dom.{KeyboardEvent, document, html}

import scala.scalajs.js
import org.querki.jquery._
import org.scalajs.dom.raw._

object Frontend {

  val joinButton = dom.document.getElementById("joinButton").asInstanceOf[HTMLButtonElement]
  val addButton = dom.document.getElementById("addButton").asInstanceOf[HTMLButtonElement]


  def main(args: Array[String]): Unit = {
    addPlayer(document.body)
    appendPar(document.body, "Hello user")
  }

  def addClickedMessage(player: Player): Unit = {
    val inputText = document.getElementById("videoUrl").asInstanceOf[html.Input].value.toString
    appendPar(document.body, "you added " + inputText)

    player.getPlayerState() match {
      case Player.State.PAUSED => player.playVideo()
      case Player.State.PLAYING => player.pauseVideo()
    }
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
    $("#click-me-button").click(() => addClickedMessage(player))
  }

  def joinChat(name: String): Unit = {
    joinButton.disabled = true

    appendPar(document.body,s"Trying to join as '$name'...")
    val chat = new WebSocket("")
    chat.onopen = { (event: org.scalajs.dom.raw.Event) ⇒
      appendPar(document.body, "Connection succesfull")
      addButton.disabled = false

      val addField = dom.document.getElementById("videoUrlField").asInstanceOf[HTMLInputElement]
      addField.focus()
      addField.onkeypress = { (event: KeyboardEvent) ⇒
        if (event.keyCode == 13) {
          addButton.click()
          event.preventDefault()
        }
      }
      addButton.onclick = { (event: org.scalajs.dom.raw.Event) ⇒
        chat.send(addField.value)
        addField.value = ""
        addField.focus()
        event.preventDefault()
      }

      event
    }
    chat.onerror = { (event: org.scalajs.dom.raw.Event) ⇒
      appendPar(document.body,s"Failed: code: ${event.toString}")
      joinButton.disabled = false
      addButton.disabled = true
    }
    chat.onmessage = { (event: MessageEvent) ⇒
      val wsMsg = read[Protocol.Message](event.data.toString)

      wsMsg match {
        case Protocol.ChatMessage(sender, message) ⇒ appendPar(document.body, s"$sender said: $message")
        case Protocol.Joined(member, _)            ⇒ appendPar(document.body,s"$member joined!")
        case Protocol.Left(member, _)              ⇒ appendPar(document.body,s"$member left!")
      }
    }
    chat.onclose = { (event: Event) ⇒
      playground.insertBefore(p("Connection to chat lost. You can try to rejoin manually."), playground.firstChild)
      joinButton.disabled = false
      sendButton.disabled = true
    }

  }
  def createPlayer(): Player = {
     val player = new Player("player", PlayerOptions(
      width = "100%",
      height = "100%",
      videoId = "M7lc1UVf-VE",
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

  def onPlayerReady(event: Event): Unit = {
    val p = event.target.asInstanceOf[Player]
    p.playVideo()

    setupUI(p)

  }

  def onPlayerError(event: Event): Unit = {
    val p = event.target.asInstanceOf[Player]
    p.clearVideo()
  }

  def onPlayerStateChange(event: Event): Unit = {

  }

  def appendPar(targetNode: dom.Node, text: String): Unit = {
    val parNode = document.createElement("p")
    val textNode = document.createTextNode(text)
    parNode.appendChild(textNode)
    targetNode.appendChild(parNode)
  }

}
