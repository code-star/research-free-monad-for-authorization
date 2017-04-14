package interpreters

import authorization.BetalenEffect
import cats.free.Free
import tolk.Tolk

import scala.concurrent.Future

/**
  * Created by wgr21717 on 14/04/2017.
  */
class GoForItInterpreter extends Interpreter {

  override def interpret[A](betalenEffect: BetalenEffect[Free[BetalenEffect, A]]): Future[A] = {
    Tolk.interpret(this, betalenEffect)
  }
}
