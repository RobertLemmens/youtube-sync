package vs

import org.scalajs.dom
import org.scalajs.dom.document
import org.scalajs.dom.raw.{HTMLInputElement, HTMLUListElement}
import vs.Frontend.isDark

/**
  * @author Robert Lemmens 
  *         2/23/20
  */
object StylingHandler {

  def toggleDarkMode(): Unit = {
    val mainCard = document.getElementById("controlPanel")
    val playlistLabel =  document.getElementById("playlistLabel")
    val usersLabel = document.getElementById("usersLabel")
    val urlField = document.getElementById("videoUrlField").asInstanceOf[HTMLInputElement]
    val chatField = dom.document.getElementById("chatField").asInstanceOf[HTMLInputElement]
    val playList = document.getElementById("playlistList").asInstanceOf[HTMLUListElement]
    val userList = document.getElementById("userList").asInstanceOf[HTMLUListElement]
    val chatList = dom.document.getElementById("chatcollection").asInstanceOf[HTMLUListElement]

    if(!isDark) {
      mainCard.setAttribute("class", "card grey darken-4")
      usersLabel.setAttribute("class", "flow-text white-text")
      playlistLabel.setAttribute("class", "flow-text white-text")
      urlField.setAttribute("class", "white-text")
      chatField.setAttribute("class", "white-text")
      chatList.setAttribute("class", "white-text")

      isDark = true
    } else {
      mainCard.setAttribute("class", "card")
      usersLabel.setAttribute("class", "flow-text")
      playlistLabel.setAttribute("class", "flow-text")
      urlField.removeAttribute("class")
      chatField.removeAttribute("class")
      chatList.removeAttribute("class")

      isDark = false
    }
  }

}
