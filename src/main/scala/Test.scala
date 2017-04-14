import authorization.BetalenEffect
import authorization.BetalenEffect._
import cats.free.Free
import interpreters.{GoForItInterpreter, Interpreter, RechtenInterpreter}
import tolk.Tolk

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

object Test {

  val authorizedForRekeningen = Set(2, 4)

  val rechteninterpreter = new RechtenInterpreter(authorizedForRekeningen)
  val gointerpreter = new GoForItInterpreter

  def main(args: Array[String]): Unit = {

    val ast = for {
      betaalopdracht <- getBetaalopdracht(3)
      allRekeningen <- getAllRekeningen()
      firstId = allRekeningen.head.id
      rekening <- getRekeningById(firstId)
    } yield (betaalopdracht, allRekeningen, rekening)

    print(rechteninterpreter, ast)
    print(gointerpreter, ast)
  }

  def print[A](interpreter: Interpreter, ast: Free[BetalenEffect, A]): Unit = {
    val result = Tolk.reallyInterpret(interpreter, ast)

    result.andThen {
      case Success(l) => println(l)
      case Failure(e) => e.printStackTrace()
    }
  }


}
