package interpreters

import authorization.BetalenEffect
import cats.~>
import tolk.Tolk

import scala.concurrent.Future

object GoForItInterpreter {

  def interpreter: (BetalenEffect ~> Future) = Tolk.interpreter
}
