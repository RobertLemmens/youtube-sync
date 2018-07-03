package protocols

object Protocol {
  sealed trait Message
  import upickle.default._
  implicit val readWriter: ReadWriter[Message] = ReadWriter.merge(
    macroRW[Left], macroRW[Joined], macroRW[ChatMessage],
    macroRW[PlayVideo], macroRW[PauseVideo], macroRW[AddVideo],
    macroRW[StatusRequest], macroRW[StatusMessage], macroRW[StatusUpdate],
    macroRW[LoadVideo], macroRW[PlaylistUpdate], macroRW[MemberStatus],
    macroRW[SettingsUpdate]
  )

  case class ChatMessage(sender: String, message: String) extends Message
  case class Joined(member: String, allMembers: Set[(Boolean, String)]) extends Message
  case class Left(member: String, allMembers: Set[(Boolean, String)]) extends Message
  case class PlayVideo(sender: String) extends Message
  case class PauseVideo(sender: String) extends Message
  case class AddVideo(sender: String, videoUrl: String) extends Message
  case class LoadVideo(sender: String, videoId: String) extends Message
  case class StatusRequest(sender: String) extends Message
  case class StatusMessage(sender: String, status: Int, time: Double, videoUrl: String) extends Message
  case class StatusUpdate(status: Int, time: Double, videoUrl: String) extends Message
  case class PlaylistUpdate(playlist: Set[(Boolean, String)]) extends Message
  case class MemberStatus(members: Set[(Boolean, String)]) extends Message
  case class SettingsUpdate(autoplay: Boolean) extends Message
}