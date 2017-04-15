package repo

import domain.Betaalopdracht

import scala.concurrent.Future

class BetaalopdrachtRepo {

  private var allBetaalopdrachten = Seq(
    Betaalopdracht(1, 1),
    Betaalopdracht(2, 2),
    Betaalopdracht(3, 3),
    Betaalopdracht(4, 4))

  def getById(id: Int): Future[Option[Betaalopdracht]] =
    Future.successful(allBetaalopdrachten.filter(_.id == id).headOption)
}
