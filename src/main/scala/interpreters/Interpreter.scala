package interpreters

import authorization.{BetalenEffect, BetalenEffectF}
import cats.free.Free

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.reflect.runtime.universe._

/**
  * Created by wgr21717 on 14/04/2017.
  */
trait Interpreter {

  def interpret[A](betalenEffect: BetalenEffect[BetalenEffectF[A]]): Future[A]
}
