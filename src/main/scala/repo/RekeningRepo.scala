package repo

import domain.Rekening

import scala.concurrent.Future

class RekeningRepo {

  private val allRekeningen = Seq(
    Rekening(1),
    Rekening(2),
    Rekening(3),
    Rekening(4))

  def getAll(authorizedForRekeningen: Option[Set[Int]]): Future[Seq[Rekening]] = {
    val rekeningen = authorizedForRekeningen match {
      case Some(filter) => allRekeningen.filter(filter contains _.id)
      case None => allRekeningen
    }
    Future.successful(rekeningen)
  }

  def getById(id: Int): Future[Option[Rekening]] =
    Future.successful(allRekeningen.find(_.id == id))
}
