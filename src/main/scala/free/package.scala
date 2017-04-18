import cats.free.{Free, Inject}

package object free {

  final class FreeInjectPartiallyApplied[F[_], G[_]] private[free] {
    def apply[A](fa: F[A])(implicit I : Inject[F, G]): Free[G, A] =
      Free.liftF(I.inj(fa))
  }

  def inject[F[_], G[_]]: FreeInjectPartiallyApplied[F, G] = new FreeInjectPartiallyApplied
}
