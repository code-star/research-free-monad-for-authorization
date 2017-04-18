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

  type Effect[A] = Coproduct[BetalenEffect, LogEffect, A]

  val authorizedForRekeningen = Set(2, 4)

  def logEffectNaturalTransformation: (LogEffect ~> Future) = new (LogEffect ~> Future) {
    override def apply[A](fa: LogEffect[A]): Future[A] = fa match {
      case Log(v) => {
        println("Logging: " + v)
        Future.successful(())
      }
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

  private def getAst(rekeningRepo: RekeningRepo)(implicit be: BetalenEffects[Effect], le: LogEffects[Effect]) = {
    val ast = for {
      _ <- be.fromFuture(rekeningRepo.delete(3, authorizedForRekeningen))
      _ <- le.log("Delete rekening with id 3")

      betaalopdracht <- be.getBetaalopdracht(3)
      _ <- le.log("Get betaalopdracht with id 3")

      allRekeningen <- be.getAllRekeningen()
      _ <- le.log("Get all rekeningen")

      firstId = allRekeningen.head.id
      rekening <- be.getRekeningById(firstId)
      _ <- le.log("Get rekening with id " + firstId)
    } yield (betaalopdracht, allRekeningen, rekening)
    ast
  }

  def print[A](
                betalenEffectNaturalTransformation: BetalenEffect ~> Future,
                logEffectNaturalTransformation: LogEffect ~> Future,
                ast: Free[Effect, A]): Unit = {
    ast.foldMap(betalenEffectNaturalTransformation or logEffectNaturalTransformation).andThen {
      case Success(l) => println(l)
      case Failure(e) => e.printStackTrace()
    }
  }


}
