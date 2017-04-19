import authorization._
import cats.data.Coproduct
import cats.free.Free
import cats.instances.future._
import cats.~>
import interpreters.{GoForItInterpreter, RechtenInterpreter}
import repo.RekeningRepo

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

object Test {

  type LogOrInterestingId[A] = Coproduct[LogEffect, InterestingIdEffect, A]
  type Effect[A] = Coproduct[BetalenEffect, LogOrInterestingId, A]

  val authorizedForRekeningen = Set(2, 4)

  def logEffectNaturalTransformation: (LogEffect ~> Future) = new (LogEffect ~> Future) {
    override def apply[A](fa: LogEffect[A]): Future[A] = fa match {
      case Log(v) => {
        println("Logging: " + v)
        Future.successful(())
      }
    }
  }

  def interestingIdEffectNaturalTransformation: (InterestingIdEffect ~> Future) = new (InterestingIdEffect ~> Future) {
    override def apply[A](fa: InterestingIdEffect[A]): Future[A] = fa match {
      case InterestingId(id) =>
        Future.successful(id)
    }
  }

  def main(args: Array[String]): Unit = {

    var rekeningRepo = new RekeningRepo
    print(new GoForItInterpreter(rekeningRepo).interpreter, logEffectNaturalTransformation, getAst(rekeningRepo))

    rekeningRepo = new RekeningRepo
    print(new RechtenInterpreter(rekeningRepo).interpreter(authorizedForRekeningen), logEffectNaturalTransformation, getAst(rekeningRepo))

    // Make sure both threads finish.
    Thread.sleep(10 * 1000)
  }

  private def getAst(rekeningRepo: RekeningRepo)(implicit be: BetalenEffects[Effect], le: LogEffects[Effect], ide: InterestingIdEffects[Effect]) = {
    val ast = for {
      _ <- be.fromFuture(rekeningRepo.delete(3, authorizedForRekeningen))
      _ <- le.log("Delete rekening with id 3")

      betaalopdracht <- be.getBetaalopdracht(3)
      _ <- le.log("Get betaalopdracht with id 3")

      allRekeningen <- be.getAllRekeningen()
      _ <- le.log("Get all rekeningen")

      firstId = allRekeningen.head.id
      interestingId <- ide.interestingId(firstId)
      rekening <- be.getRekeningById(interestingId)
      _ <- le.log("Get rekening with id " + firstId)
    } yield (betaalopdracht, allRekeningen, rekening)
    ast
  }

  def print[A](
                betalenEffectNaturalTransformation: BetalenEffect ~> Future,
                logEffectNaturalTransformation: LogEffect ~> Future,
                ast: Free[Effect, A]): Unit = {
    val x: (LogOrInterestingId ~> Future) = logEffectNaturalTransformation or interestingIdEffectNaturalTransformation
    ast.foldMap(betalenEffectNaturalTransformation or x).andThen {
      case Success(l) => println(l)
      case Failure(e) => e.printStackTrace()
    }
  }


}
