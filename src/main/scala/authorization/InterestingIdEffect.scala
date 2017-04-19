package authorization

import cats.free.{Free, Inject}
import free.inject

sealed trait InterestingIdEffect[A]

final case class InterestingId(id: Int) extends InterestingIdEffect[Int]

class InterestingIdEffects[F[_]](implicit i: Inject[InterestingIdEffect, F]) {
  def interestingId(id: Int): Free[F, Int] =
    inject(InterestingId(id))
}

object InterestingIdEffects {
  implicit def interestingIdEffects[F[_]](implicit i: Inject[InterestingIdEffect, F]): InterestingIdEffects[F] =
    new InterestingIdEffects
}
