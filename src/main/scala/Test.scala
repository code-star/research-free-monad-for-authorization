import authorization.BetalenEffect
import authorization.BetalenEffect._
import cats.free.Free
import cats.~>
import interpreters.{GoForItInterpreter, RechtenInterpreter}

import scala.concurrent.ExecutionContext.Implicits.global
import cats.instances.future._
import scala.concurrent.Future
import scala.util.{Failure, Success}

object Test {

  val authorizedForRekeningen = Set(2, 4)

  def main(args: Array[String]): Unit = {

    val ast = for {
      betaalopdracht <- getBetaalopdracht(3)
      allRekeningen <- getAllRekeningen()
      firstId = allRekeningen.head.id
      rekening <- getRekeningById(firstId)
    } yield (betaalopdracht, allRekeningen, rekening)

    print(GoForItInterpreter.interpreter, ast)
    print(RechtenInterpreter.interpreter(authorizedForRekeningen), ast)

    // Make sure both threads finish.
    Thread.sleep(10 * 1000)
  }

  def print[A](naturalTransformation: BetalenEffect ~> Future, ast: Free[BetalenEffect, A]): Unit = {
    ast.foldMap(naturalTransformation).andThen {
      case Success(l) => println(l)
      case Failure(e) => e.printStackTrace()
    }
  }


}
