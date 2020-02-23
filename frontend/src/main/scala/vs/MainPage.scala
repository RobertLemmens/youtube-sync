package vs

import org.scalajs.dom
import org.scalajs.dom.raw.HTMLButtonElement

/**
  * @author Robert Lemmens 
  *         2/23/20
  */
class MainPage {

  val colorButton = dom.document.getElementById("invertColor").asInstanceOf[HTMLButtonElement]
  val controlPanelSize = dom.document.getElementById("controlPanelSizeButton").asInstanceOf[HTMLButtonElement]
  var isSmall = false // controlpanel size
  var isDark = false // theme boolean


  def init(): Unit = {
    bindButtonClicks()
  }

  def bindButtonClicks(): Unit = {
    colorButton.onclick = { _: org.scalajs.dom.raw.Event =>
      isDark = StylingHandler.toggleDarkMode(isDark)
    }
    controlPanelSize.onclick = { _: org.scalajs.dom.raw.Event =>
      isSmall = StylingHandler.togglePanelSize(isSmall)
    }
  }

}
