import authorization.BetalenEffect
import authorization.BetalenEffect._
import cats.free.Free
import cats.~>
import interpreters.{GoForItInterpreter, RechtenInterpreter}

import scala.concurrent.ExecutionContext.Implicits.global
import cats.instances.future._
import domain.{Betaalopdracht, Rekening}
import repo.RekeningRepo

import scala.concurrent.Future
import scala.util.{Failure, Success}

object Test {

  val authorizedForRekeningen = Set(2, 4)

  def main(args: Array[String]): Unit = {

    var rekeningRepo = new RekeningRepo
    print(new GoForItInterpreter(rekeningRepo).interpreter, getAst(rekeningRepo))

    rekeningRepo = new RekeningRepo
    print(new RechtenInterpreter(rekeningRepo).interpreter(authorizedForRekeningen), getAst(rekeningRepo))

    // Make sure both threads finish.
    Thread.sleep(10 * 1000)
  }

  private def getAst(rekeningRepo: RekeningRepo) = {
    val ast = for {
      _ <- fromFuture(rekeningRepo.delete(3, authorizedForRekeningen))
      betaalopdracht <- getBetaalopdracht(3)
      allRekeningen <- getAllRekeningen()
      firstId = allRekeningen.head.id
      rekening <- getRekeningById(firstId)
    } yield (betaalopdracht, allRekeningen, rekening)
    ast
  }

  def print[A](naturalTransformation: BetalenEffect ~> Future, ast: Free[BetalenEffect, A]): Unit = {
    ast.foldMap(naturalTransformation).andThen {
      case Success(l) => println(l)
      case Failure(e) => e.printStackTrace()
    }
  }


}
