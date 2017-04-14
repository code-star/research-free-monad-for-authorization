package interpreters

import authorization._
import cats.free.Free
import domain.Betaalopdracht
import tolk.Tolk

import scala.concurrent.Future
import scala.reflect.runtime.universe._
import MrMonoid._
import cats.syntax.semigroup._

/**
  * Created by wgr21717 on 14/04/2017.
  */
class RechtenInterpreter(val rekeningFilter: Set[Int]) extends Interpreter {

  override def interpret[A](betalenEffect: BetalenEffect[BetalenEffectF[A]]): Future[A] = {

    betalenEffect match {
      case GetRekeningById(id, _) =>

        if (rekeningFilter contains id)
          Tolk.interpret(this, betalenEffect)
        else
          Future.failed(new IllegalStateException("oh noes"))

      case GetAllRekeningen(filter, fn) =>
        Tolk.interpret(this, GetAllRekeningen(filter |+| Some(rekeningFilter), fn))

      case GetBetaalopdracht(id, fn) =>
        def shortCircuitIfNoAuthorization(boOpt: Option[Betaalopdracht]): BetalenEffectF[A] =
          boOpt match {
            case Some(bo) if !(rekeningFilter contains bo.rekeningId) => BetalenEffect.shortCircuit
            case _ => fn(boOpt)
          }
        Tolk.interpret(this, GetBetaalopdracht(id, shortCircuitIfNoAuthorization))

      case ShortCircuit() =>
        Tolk.interpret(this, betalenEffect)
    }
  }
}
