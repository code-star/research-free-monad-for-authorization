package tolk

import authorization._
import cats.~>
import repo.{BetaalopdrachtRepo, RekeningRepo}

import scala.concurrent.Future

class Tolk(rekeningRepo: RekeningRepo) {

  // Pretend we get this instance injected:
  val betaalopdrachtRepo = new BetaalopdrachtRepo

  def interpreter: (BetalenEffect ~> Future) = new (BetalenEffect ~> Future) {
    override def apply[A](betalenEffect: BetalenEffect[A]): Future[A] = betalenEffect match {
      case GetRekeningById(id) =>
        rekeningRepo.getById(id)
      case GetAllRekeningen(rekeningIdFilter) =>
        rekeningRepo.getAll(rekeningIdFilter)
      case GetBetaalopdracht(id) =>
        betaalopdrachtRepo.getById(id)
      case FromFuture(fut) =>
        fut
    }
  }
}
