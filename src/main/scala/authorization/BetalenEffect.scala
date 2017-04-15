package authorization

import cats.free.Free
import domain.{Betaalopdracht, Rekening}

sealed trait BetalenEffect[A]

object BetalenEffect {

  def getRekeningById(id: Int): Free[BetalenEffect, Option[Rekening]] =
    Free.liftF(GetRekeningById(id))

  def getAllRekeningen(filter: Option[Set[Int]] = None): Free[BetalenEffect, Seq[Rekening]] =
    Free.liftF(GetAllRekeningen(filter))

  def getBetaalopdracht(id: Int): Free[BetalenEffect, Option[Betaalopdracht]] =
    Free.liftF(GetBetaalopdracht(id))
}

// We know which authorization we need beforehand.
final case class GetRekeningById(id: Int) extends BetalenEffect[Option[Rekening]]

// We need to incorporate authorization in the database query.
final case class GetAllRekeningen(filter: Option[Set[Int]]) extends BetalenEffect[Seq[Rekening]]

// We only know if we can get this `Betaalopdracht` in hindsight.
final case class GetBetaalopdracht(id: Int) extends BetalenEffect[Option[Betaalopdracht]]
