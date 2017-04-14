package authorization

import cats.Functor
import cats.free.Free
import domain.{Betaalopdracht, Rekening}

sealed trait BetalenEffect[+A] {
  def map[B](mapping: A => B): BetalenEffect[B]
}

object BetalenEffect {

  implicit val betalenEffectFunctor: Functor[BetalenEffect] = new Functor[BetalenEffect] {
    override def map[A, B](fa: BetalenEffect[A])(f: (A) => B): BetalenEffect[B] =
      fa.map(f)
  }

  def getRekeningById(id: Int): BetalenEffectF[Option[Rekening]] =
    Free.liftF(GetRekeningById(id, identity))

  def getAllRekeningen(filter: Option[Set[Int]] = None): BetalenEffectF[Seq[Rekening]] =
    Free.liftF(GetAllRekeningen(filter, identity))

  def getBetaalopdracht(id: Int): BetalenEffectF[Option[Betaalopdracht]] =
    Free.liftF(GetBetaalopdracht(id, identity))

  def shortCircuit[A]: BetalenEffectF[A] =
    Free.liftF(ShortCircuit[A]())
}

// We know which authorization we need beforehand.
final case class GetRekeningById[+A](id: Int, f: Option[Rekening] => A) extends BetalenEffect[A] {
  override def map[B](mapping: (A) => B): BetalenEffect[B] =
    GetRekeningById(id, f andThen mapping)
}

// We need to incorporate authorization in the database query.
final case class GetAllRekeningen[+A](filter: Option[Set[Int]], f: (Seq[Rekening]) => A) extends BetalenEffect[A] {
  override def map[B](mapping: (A) => B): BetalenEffect[B] =
    GetAllRekeningen(filter, f andThen mapping)
}

// We only know if we can get this `Betaalopdracht` in hindsight.
final case class GetBetaalopdracht[+A](id: Int, f: Option[Betaalopdracht] => A) extends BetalenEffect[A] {
  override def map[B](mapping: (A) => B): BetalenEffect[B] =
    GetBetaalopdracht(id, f andThen mapping)
}

final case class ShortCircuit[A]() extends BetalenEffect[A] {
  override def map[B](mapping: (A) => B): BetalenEffect[B] = ShortCircuit()
}
