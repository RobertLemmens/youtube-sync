package vs

import org.scalajs.dom
import org.scalajs.dom.document
import org.scalajs.dom.raw.{HTMLButtonElement, HTMLInputElement}

/**
  * @author Robert Lemmens 
  *         2/23/20
  */
class SetupPage {

  // Color buttons
  val redButton = dom.document.getElementById("red").asInstanceOf[HTMLButtonElement]
  val greenButton = dom.document.getElementById("green").asInstanceOf[HTMLButtonElement]
  val pinkButton = dom.document.getElementById("pink").asInstanceOf[HTMLButtonElement]
  val blueButton = dom.document.getElementById("blue").asInstanceOf[HTMLButtonElement]
  val purpleButton = dom.document.getElementById("purple").asInstanceOf[HTMLButtonElement]
  val brownButton = dom.document.getElementById("brown").asInstanceOf[HTMLButtonElement]
  val connectButton = dom.document.getElementById("connectButton").asInstanceOf[HTMLButtonElement]

  var selectedColor = "red" //selected display color for username

  val userNameField = dom.document.getElementById("name").asInstanceOf[HTMLInputElement]
  val serverNameField = dom.document.getElementById("room").asInstanceOf[HTMLInputElement]

  def initSetupPage(connectFunction: (HTMLInputElement, HTMLInputElement, String) => Unit): Unit = {
    setupColorSelectors()
    connectButton.onclick = { (event: org.scalajs.dom.raw.Event) =>
        connectFunction.apply(userNameField, serverNameField, selectedColor)
    }
  }


  def setupColorSelectors(): Unit = {
    redButton.onclick =  (event: org.scalajs.dom.raw.Event) => {
      selectedColor = "red"
      redButton.innerHTML = "Selected"
      greenButton.innerHTML = ""
      pinkButton.innerHTML = ""
      blueButton.innerHTML= ""
      purpleButton.innerHTML= ""
      brownButton.innerHTML= ""
    }
    greenButton.onclick =  (event: org.scalajs.dom.raw.Event) => {
      selectedColor = "green"
      redButton.innerHTML = ""
      greenButton.innerHTML = "Selected"
      pinkButton.innerHTML = ""
      blueButton.innerHTML= ""
      purpleButton.innerHTML= ""
      brownButton.innerHTML= ""
    }
    pinkButton.onclick =  (event: org.scalajs.dom.raw.Event) => {
      selectedColor = "pink"
      redButton.innerHTML = ""
      greenButton.innerHTML = ""
      pinkButton.innerHTML = "Selected"
      blueButton.innerHTML= ""
      purpleButton.innerHTML= ""
      brownButton.innerHTML= ""
    }
    blueButton.onclick =  (event: org.scalajs.dom.raw.Event) => {
      selectedColor = "blue"
      redButton.innerHTML = ""
      greenButton.innerHTML = ""
      pinkButton.innerHTML = ""
      blueButton.innerHTML= "Selected"
      purpleButton.innerHTML= ""
      brownButton.innerHTML= ""
    }
    purpleButton.onclick =  (event: org.scalajs.dom.raw.Event) => {
      selectedColor = "purple"
      redButton.innerHTML = ""
      greenButton.innerHTML = ""
      pinkButton.innerHTML = ""
      blueButton.innerHTML= ""
      purpleButton.innerHTML= "Selected"
      brownButton.innerHTML= ""
    }
    brownButton.onclick =  (event: org.scalajs.dom.raw.Event) => {
      selectedColor = "brown"
      redButton.innerHTML = ""
      greenButton.innerHTML = ""
      pinkButton.innerHTML = ""
      blueButton.innerHTML= ""
      purpleButton.innerHTML= ""
      brownButton.innerHTML= "Selected"
    }
  }

}
