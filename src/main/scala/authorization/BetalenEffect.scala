package authorization

import cats.free.{Free, Inject}
import domain.{Betaalopdracht, Rekening}

import scala.concurrent.Future
import free.inject

sealed trait BetalenEffect[A]

class BetalenEffects[F[_]](implicit i: Inject[BetalenEffect, F]) {

  def getRekeningById(id: Int): Free[F, Option[Rekening]] =
    inject(GetRekeningById(id))

  def getAllRekeningen(filter: Option[Set[Int]] = None): Free[F, Seq[Rekening]] =
    inject(GetAllRekeningen(filter))

  def getBetaalopdracht(id: Int): Free[F, Option[Betaalopdracht]] =
    inject(GetBetaalopdracht(id))

  def fromFuture[A](fut: Future[A]): Free[F, A] =
    inject(FromFuture(fut): BetalenEffect[A])
}

object BetalenEffects {
  implicit def betalenEffects[F[_]](implicit i: Inject[BetalenEffect, F]): BetalenEffects[F] =
    new BetalenEffects
}

// We know which authorization we need beforehand.
final case class GetRekeningById(id: Int) extends BetalenEffect[Option[Rekening]]

// We need to incorporate authorization in the database query.
final case class GetAllRekeningen(filter: Option[Set[Int]]) extends BetalenEffect[Seq[Rekening]]

// We only know if we can get this `Betaalopdracht` in hindsight.
final case class GetBetaalopdracht(id: Int) extends BetalenEffect[Option[Betaalopdracht]]

// To provide an upgrade path in which all `Future[A]`'s can eventually be replaced by a `BetalenEffect[A]`.
final case class FromFuture[A](fut: Future[A]) extends BetalenEffect[A]
