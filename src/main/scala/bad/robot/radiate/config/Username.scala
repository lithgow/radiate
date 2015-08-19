package bad.robot.radiate.config

import bad.robot.radiate.OptionSyntax._

import scalaz.{Success, Validation}

object Username {

  def validate(username: Option[String]): Validation[String, Username] = {
    Success(NonEmptyOption(username).map(Username.apply).getOrElse(NoUsername))
  }

  // deprecated
  def username(username: String): Username = {
    if (username == null) NoUsername else new Username(username)
  }
}

// todo implicit to convert the SimpleHTTP username
case class Username (value: String) {
  def asSimpleHttp: bad.robot.http.configuration.Username = {
    bad.robot.http.configuration.Username.username(value)
  }
}

object NoUsername extends Username("no username")