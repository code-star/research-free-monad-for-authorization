package interpreters

import cats.Monoid

object MrMonoid {

  implicit def mrMonoid[A] = new Monoid[Option[Set[A]]] {
    override def empty: Option[Set[A]] = None

    override def combine(xOpt: Option[Set[A]], yOpt: Option[Set[A]]): Option[Set[A]] = (xOpt, yOpt) match {
      case (Some(x), None) => Some(x)
      case (None, Some(y)) => Some(y)
      case (Some(x), Some(y)) => Some(x intersect y)
      case _ => None
    }
  }

}
