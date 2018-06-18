package vs

import scala.scalajs.js
import js.annotation._
import org.scalajs.dom._

trait YTEvent extends js.Object {
  val target:  js.UndefOr[Player] = js.native
  val data: js.UndefOr[js.Object] = js.native
}

object YTEvent {
  def apply(
             target:  js.UndefOr[Player] = js.undefined,
             data: js.UndefOr[js.Object] = js.undefined
           ): YTEvent = {
    val result = js.Dynamic.literal()
    target.foreach(result.target = _)
    data.foreach(result.data = _)
    result.asInstanceOf[YTEvent]
  }
}


//https://developers.google.com/youtube/
trait PlayerEvents extends js.Object {
  val onReady:  js.UndefOr[(YTEvent) => Any] = js.native
  val onStateChange: js.UndefOr[(YTEvent) => Any] = js.native
  val onError:  js.UndefOr[(YTEvent) => Any] = js.native
}


object PlayerEvents {
  def apply(
             onReady:  js.UndefOr[(YTEvent) => Any] = js.undefined,
             onStateChange: js.UndefOr[(YTEvent) => Any] = js.undefined,
             onError:  js.UndefOr[(YTEvent) => Any] = js.undefined
           ): PlayerEvents = {
    val result = js.Dynamic.literal()
    onReady.foreach(result.onReady = _)
    onStateChange.foreach(result.onStateChange = _)
    onError.foreach(result.onError = _)
    result.asInstanceOf[PlayerEvents]
  }
}



trait VideoIdStartOptions extends js.Object {
  var videoId:js.UndefOr[String] = js.native
  var startSeconds:js.UndefOr[Double] = js.native
  var endSeconds:js.UndefOr[Double] = js.native
  var suggestedQuality:js.UndefOr[String] = js.native
}

object VideoIdStartOptions {
  def apply(
             videoId: js.UndefOr[String] = js.undefined,
             startSeconds: js.UndefOr[Double] = js.undefined,
             endSeconds: js.UndefOr[Double] = js.undefined,
             suggestedQuality: js.UndefOr[String] = js.undefined
           ): VideoIdStartOptions = {
    val result = js.Dynamic.literal()
    videoId.foreach(result.videoId = _)
    startSeconds.foreach(result.startSeconds = _)
    endSeconds.foreach(result.endSeconds = _)
    suggestedQuality.foreach(result.suggestedQuality = _)
    result.asInstanceOf[VideoIdStartOptions]
  }
}

trait PlayerVars extends js.Object {
  var playsinline:js.UndefOr[Double] = js.native
}

object PlayerVars {
  def apply(
             playsinline: js.UndefOr[Double] = js.undefined
           ): PlayerVars = {
    val result = js.Dynamic.literal()
    playsinline.foreach(result.playsinline = _)
    result.asInstanceOf[PlayerVars]
  }
}



trait PlayerOptions extends js.Object {
  var height:String = js.native
  var width:String = js.native
  var videoId:String = js.native
  var events:PlayerEvents = js.native
  var playerVars:PlayerVars = js.native
}

object PlayerOptions {
  def apply(
             height: js.UndefOr[String] = js.undefined,
             width: js.UndefOr[String] = js.undefined,
             videoId: js.UndefOr[String] = js.undefined,
             events: js.UndefOr[PlayerEvents] = js.undefined,
             playerVars: js.UndefOr[PlayerVars] = js.undefined
           ): PlayerOptions = {
    val result = js.Dynamic.literal()
    height.foreach(result.height = _)
    width.foreach(result.width = _)
    videoId.foreach(result.videoId = _)
    events.foreach(result.events = _)
    playerVars.foreach(result.playerVars = _)
    result.asInstanceOf[PlayerOptions]
  }
}

@JSName("YT.Player")
class Player protected() extends js.Object {
  def this(divId:String, settings:PlayerOptions) = this()

  //https://developers.google.com/youtube/iframe_api_reference?hl=en#Playback_controls
  // queueing
  def loadVideoById(videoId:String, startSeconds:Double, suggestedQuality:String):Unit =  js.native
  def loadVideoById(opts:VideoIdStartOptions):Unit =  js.native
  def cueVideoById(videoId:String,startSeconds:Double,suggestedQuality:String):Unit =  js.native
  def cueVideoById(opts:VideoIdStartOptions):Unit = js.native
  // TODO: obj syntax
  def cueVideoByUrl(mediaContentUrl:String,startSeconds:Double,suggestedQuality:String):Unit = js.native
  def loadVideoByUrl(mediaContentUrl:String,startSeconds:Double,suggestedQuality:String):Unit = js.native
  // TODO: ...

  // controls
  def playVideo():Unit = js.native
  def pauseVideo():Unit = js.native
  def stopVideo():Unit = js.native
  def seekTo(seconds:Double, allowSeekAhead:Boolean):Unit = js.native
  def clearVideo():Unit = js.native
  def nextVideo():Unit = js.native
  def previousVideo():Unit = js.native
  def playVideoAt(index:Double):Unit = js.native
  def mute():Unit = js.native
  def unMute():Unit = js.native
  def isMuted():Boolean = js.native
  def setVolume(vol:Double):Unit = js.native
  def getVolume():Double = js.native
  def setSize(w:Double, h:Double):Unit = js.native
  def getPlaybackRate():Double = js.native
  def setPlaybackRate(rate:Double):Unit = js.native
  def getAvailablePlaybackRates():js.Array[Double] = js.native
  def setLoop(loop:Boolean):Unit = js.native
  def setShuffle(shufflePlaylist:Boolean):Unit = js.native
  def getVideoLoadedFraction():Double = js.native

  // -1 – unstarted
  // 0 – ended
  // 1 – playing
  // 2 – paused
  // 3 – buffering
  // 5 – video cued
  def getPlayerState():Double = js.native

  def getCurrentTime():Double = js.native

  // values are highres, hd1080, hd720, large, medium and small. It will also return undefined
  def getPlaybackQuality():String = js.native
  def setPlaybackQuality(quality:String):Unit = js.native
  def getAvailableQualityLevels():js.Array[String] = js.native
  def getDuration():Double = js.native
  def getVideoUrl():String = js.native
  def getVideoEmbedCode():String = js.native
  def getPlaylist():js.Array[String] = js.native
  def getPlaylistIndex():Double = js.native
  def addEventListener(event:String, functionName:String):Unit = js.native
  def removeEventListener(event:String, functionName:String):Unit = js.native

  // dom
  def getIframe():js.Object = js.native
  def destroy():Unit = js.native

}

object Player{
  object State{
    val UNSTARTED = -1
    val ENDED = 0
    val PLAYING = 1
    val PAUSED = 2
    val BUFFERING = 3
    val CUED = 5
  }
}