package authorization

import cats.free.{Free, Inject}
import free.inject

sealed trait LogEffect[A]

class LogEffects[F[_]](implicit i: Inject[LogEffect, F]) {
  def log(v: String): Free[F, Unit] =
    inject(Log(v))
}

object LogEffects {
  implicit def logEffects[F[_]](implicit i: Inject[LogEffect, F]): LogEffects[F] =
    new LogEffects
}

final case class Log(v: String) extends LogEffect[Unit]
