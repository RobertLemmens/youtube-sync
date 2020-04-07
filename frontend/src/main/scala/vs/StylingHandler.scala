package vs

import org.scalajs.dom
import org.scalajs.dom.document
import org.scalajs.dom.raw.{HTMLButtonElement, HTMLDivElement, HTMLInputElement, HTMLUListElement}

/**
  * @author Robert Lemmens 
  *         2/23/20
  */
object StylingHandler {


  val playButton = dom.document.getElementById("playButton").asInstanceOf[HTMLButtonElement]
  val pauseButton = dom.document.getElementById("pauseButton").asInstanceOf[HTMLButtonElement]
  val nextButton = dom.document.getElementById("nextButton").asInstanceOf[HTMLButtonElement]
  val enableAutoPlayButton = dom.document.getElementById("enableAutoplayButton").asInstanceOf[HTMLButtonElement]
  val disableAutoPlayButton = dom.document.getElementById("disableAutoplayButton").asInstanceOf[HTMLButtonElement]

  // zet duplicate code in een setDefaultStyle
  def togglePanelSize(isSmall: Boolean): Boolean = {
      val icon = dom.document.getElementById("controlPanelSizeIcon")
      icon.innerHTML = ""
      if(isSmall) {
        icon.appendChild(document.createTextNode("keyboard_arrow_right"))
        val playerContainer = document.getElementById("playerContainer")
        val controlPanelContainer = document.getElementById("controlPanelContainer")
        val nonPlaybackItems = document.getElementById("nonPlaybackItems")
        val title = document.getElementById("titleText")
        title.innerHTML = ""
        title.appendChild(document.createTextNode("Youtube-Sync"))
        pauseButton.removeAttribute("style")
        playButton.removeAttribute("style")
        nextButton.removeAttribute("style")
        enableAutoPlayButton.removeAttribute("style")
        disableAutoPlayButton.removeAttribute("style")
        nonPlaybackItems.removeAttribute("style")
        playerContainer.setAttribute("class", "col s9")
        controlPanelContainer.setAttribute("class", "col s3")
      } else {
        icon.appendChild(document.createTextNode("keyboard_arrow_left"))
        val playerContainer = document.getElementById("playerContainer")
        val controlPanelContainer = document.getElementById("controlPanelContainer")
        val nonPlaybackItems = document.getElementById("nonPlaybackItems")
        val title = document.getElementById("titleText")
        title.innerHTML = ""
        title.appendChild(document.createTextNode("YS"))
        pauseButton.setAttribute("style", "width:100%; margin-top:5px;")
        playButton.setAttribute("style", "width:100%; margin-top:5px;")
        nextButton.setAttribute("style", "width:100%; margin-top:5px; ")
        enableAutoPlayButton.setAttribute("style", "width:100%; margin-top:5px;")
        disableAutoPlayButton.setAttribute("style", "width:100%; margin-top:5px;")
        nonPlaybackItems.setAttribute("style", "display: none;")
        playerContainer.setAttribute("class", "col s11")
        controlPanelContainer.setAttribute("class", "col s1")
      }
    !isSmall
  }

  def toggleDarkMode(isDark: Boolean): Boolean = {
    val mainCard = document.getElementById("controlPanel")
    val playlistLabel =  document.getElementById("playlistLabel")
    val usersLabel = document.getElementById("usersLabel")
    val playList = document.getElementById("playlist-modal")
    val userList = document.getElementById("userlist-modal")

    val urlField = document.getElementById("videoUrlField").asInstanceOf[HTMLInputElement]
    val chatField = dom.document.getElementById("chatField").asInstanceOf[HTMLInputElement]
    val chatList = dom.document.getElementById("chatcollection").asInstanceOf[HTMLUListElement]

    if(isDark) {
      mainCard.setAttribute("class", "card")
      playList.setAttribute("class", "modal")
      userList.setAttribute("class", "modal")
      usersLabel.setAttribute("class", "flow-text")
      playlistLabel.setAttribute("class", "flow-text")
      urlField.removeAttribute("class")
      chatField.removeAttribute("class")
      chatList.removeAttribute("class")
    } else {
      mainCard.setAttribute("class", "card grey darken-4")
      playList.setAttribute("class", "modal grey darken-4")
      userList.setAttribute("class", "modal grey darken-4")
      usersLabel.setAttribute("class", "flow-text white-text")
      playlistLabel.setAttribute("class", "flow-text white-text")
      urlField.setAttribute("class", "white-text")
      chatField.setAttribute("class", "white-text")
      chatList.setAttribute("class", "white-text")
    }
    !isDark
  }

}
