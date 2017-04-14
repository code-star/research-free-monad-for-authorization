package tolk

import authorization._
import cats.free.Free
import domain.{Betaalopdracht, Rekening}
import interpreters.Interpreter
import repo.{BetaalopdrachtRepo, RekeningRepo}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object Tolk {

  val rekeningRepo = new RekeningRepo
  val betaalopdrachtRepo = new BetaalopdrachtRepo

  def interpret[A](interpreter: Interpreter, betalenEffect: BetalenEffect[BetalenEffectF[A]]): Future[A] = {

   betalenEffect match {
      case GetRekeningById(id, fn) =>
        val rekeningFut = rekeningRepo.getById(id)
        doTheRest(interpreter, rekeningFut, fn)
      case GetAllRekeningen(filterFunctionWoot, fn) =>
        val rekeningenFut = rekeningRepo.getAll(filterFunctionWoot)
        doTheRest(interpreter, rekeningenFut, fn)
      case GetBetaalopdracht(id, fn) =>
        val betaalopdrachtFut = betaalopdrachtRepo.getById(id)
        doTheRest(interpreter, betaalopdrachtFut, fn)
      case ShortCircuit() =>
        Future.failed(new IllegalStateException("Wa wa waaaaaa...."))
    }
  }

  def reallyInterpret[A](interpreter: Interpreter, betalenEffect: BetalenEffectF[A]): Future[A] = {
    betalenEffect.fold(
      a => Future.successful(a),
      interpreter.interpret
    )
  }

  def doTheRest[I, A](interpreter: Interpreter, fut: Future[I], f: I => BetalenEffectF[A]): Future[A] = {
    fut.map(f).flatMap(reallyInterpret(interpreter, _))
  }
}
