package vs

import org.scalajs.dom
import dom.{KeyboardEvent, Node, document, html}


class ChatBox {

  def initChat(): Unit = {
    val mainDiv = document.getElementById("chatbox")
    val mainList = document.createElement("ul")
    mainList.setAttribute("class","")
    mainList.setAttribute("id", "chatcollection")
    mainDiv.appendChild(mainList)

  }

  def addMessage(message: String, sender: String, color: String): Unit = {
    val messageLi = document.createElement("li")
    messageLi.setAttribute("class", "chat-message collection-item")

    val usernameDiv = document.createElement("div")
    usernameDiv.setAttribute("class", "left red-text text-username")

    val usernameSpan = document.createElement("span")
    usernameSpan.setAttribute("class", s"new badge $color badge-username")
    usernameSpan.setAttribute("data-badge-caption", s"$sender")
    usernameDiv.appendChild(usernameSpan)

    val messageDiv = document.createElement("div")
    messageDiv.setAttribute("class", "text-message")
    messageDiv.innerHTML = message

    messageLi.appendChild(usernameDiv)
    messageLi.appendChild(messageDiv)

    document.getElementById("chatcollection").appendChild(messageLi)

    val mainDiv = document.getElementById("chatbox")
    mainDiv.scrollTop = mainDiv.scrollHeight
  }
}
