package interpreters

import authorization.BetalenEffect
import cats.~>
import repo.RekeningRepo
import tolk.Tolk

import scala.concurrent.Future

class GoForItInterpreter(rekeningRepo: RekeningRepo) {

  def interpreter: (BetalenEffect ~> Future) = new Tolk(rekeningRepo).interpreter
}
