package authorization

import cats.free.Free
import domain.{Betaalopdracht, Rekening}

import scala.concurrent.Future

sealed trait BetalenEffect[A]

object BetalenEffect {

  def getRekeningById(id: Int): Free[BetalenEffect, Option[Rekening]] =
    Free.liftF(GetRekeningById(id))

  def getAllRekeningen(filter: Option[Set[Int]] = None): Free[BetalenEffect, Seq[Rekening]] =
    Free.liftF(GetAllRekeningen(filter))

  def getBetaalopdracht(id: Int): Free[BetalenEffect, Option[Betaalopdracht]] =
    Free.liftF(GetBetaalopdracht(id))

  def fromFuture[A](fut: Future[A]): Free[BetalenEffect, A] =
    Free.liftF(FromFuture(fut))
}

// We know which authorization we need beforehand.
final case class GetRekeningById(id: Int) extends BetalenEffect[Option[Rekening]]

// We need to incorporate authorization in the database query.
final case class GetAllRekeningen(filter: Option[Set[Int]]) extends BetalenEffect[Seq[Rekening]]

// We only know if we can get this `Betaalopdracht` in hindsight.
final case class GetBetaalopdracht(id: Int) extends BetalenEffect[Option[Betaalopdracht]]

// To provide an upgrade path in which all `Future[A]`'s can eventually be replaced by a `BetalenEffect[A]`.
final case class FromFuture[A](fut: Future[A]) extends BetalenEffect[A]
