object Protocol {
  sealed trait PlayerMessage
  case object Play extends PlayerMessage
  case object Pause extends PlayerMessage
  case object Next extends PlayerMessage
  case object Previous extends PlayerMessage
  case class Add(url: String) extends PlayerMessage
  case class Remove(url: String) extends PlayerMessage

  sealed trait UserMessage
  case class Joined(user: String) extends UserMessage
  case class Left(user: String) extends UserMessage
}