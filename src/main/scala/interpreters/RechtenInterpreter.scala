package interpreters

import authorization._
import cats.syntax.semigroup._
import cats.~>
import domain.Betaalopdracht
import interpreters.MrMonoid._
import repo.RekeningRepo
import tolk.Tolk

import scala.concurrent.{ExecutionContext, Future}

class RechtenInterpreter(rekeningRepo: RekeningRepo) {

  def interpreter(authorizedForRekeningIds: Set[Int])(implicit ec: ExecutionContext): (BetalenEffect ~> Future) = new (BetalenEffect ~> Future) {
    override def apply[A](betalenEffect: BetalenEffect[A]): Future[A] = betalenEffect match {
      case GetRekeningById(id) =>
        if (authorizedForRekeningIds contains id)
          new Tolk(rekeningRepo).interpreter(betalenEffect)
        else
          Future.successful(None)

      case GetAllRekeningen(rekeningIdFilter) =>
        new Tolk(rekeningRepo).interpreter(GetAllRekeningen(rekeningIdFilter |+| Some(authorizedForRekeningIds)))

      case gb @ GetBetaalopdracht(id) =>
        val betaalopdrachtOptFut: Future[Option[Betaalopdracht]] = new Tolk(rekeningRepo).interpreter(gb)
        betaalopdrachtOptFut map {
          case Some(bo) if !(authorizedForRekeningIds contains bo.rekeningId) => None
          case other => other
        }

      case FromFuture(fut) =>
        fut
    }
  }
}
