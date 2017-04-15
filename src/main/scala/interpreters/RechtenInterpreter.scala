package interpreters

import authorization._
import cats.syntax.semigroup._
import cats.~>
import domain.Betaalopdracht
import interpreters.MrMonoid._
import tolk.Tolk

import scala.concurrent.{ExecutionContext, Future}

object RechtenInterpreter {

  def interpreter(authorizedForRekeningIds: Set[Int])(implicit ec: ExecutionContext): (BetalenEffect ~> Future) = new (BetalenEffect ~> Future) {
    override def apply[A](betalenEffect: BetalenEffect[A]): Future[A] = betalenEffect match {
      case GetRekeningById(id) =>
        if (authorizedForRekeningIds contains id)
          Tolk.interpreter(betalenEffect)
        else
          Future.successful(None)

      case GetAllRekeningen(rekeningIdFilter) =>
        Tolk.interpreter(GetAllRekeningen(rekeningIdFilter |+| Some(authorizedForRekeningIds)))

      case gb @ GetBetaalopdracht(id) =>
        val betaalopdrachtOptFut: Future[Option[Betaalopdracht]] = Tolk.interpreter(gb)
        betaalopdrachtOptFut map {
          case Some(bo) if !(authorizedForRekeningIds contains bo.rekeningId) => None
          case other => other
        }
    }
  }
}
