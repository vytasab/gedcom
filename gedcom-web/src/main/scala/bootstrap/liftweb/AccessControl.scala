package bootstrap.liftweb

import org.slf4j.{LoggerFactory, Logger}
import _root_.net.liftweb._
import http._
import common._
import _root_.lt.node.gedcom._
import model._

/**
 * Created by IntelliJ IDEA.
 * User: padargas
 * Date: 3/25/11
 * Time: 6:18 PM
 * To change this template use File | Settings | File Templates.
 */

// Charles F. Munat: Encrypting user passwords with Jasypt and JPA []... ====================

object CurrentUserId extends SessionVar[Box[Long]](Empty)

object RequestedURL extends SessionVar[Box[String]](Empty)

object CurrentUser extends RequestVar[Box[User]](Empty)

object AccessControl {
  val log: Logger = LoggerFactory.getLogger("Boot;AccessControl");

  def logout(): Box[LiftResponse] = {
    log.debug("logout: Done!")
    S.notice("logout: Done!")
    CurrentUser(Empty)
    CurrentUserId(Empty)
    Full(RedirectResponse(("/")))
    //Full(RedirectResponse(S.param("path").openOr("/")))
  }

  def isAuthenticated_?() = CurrentUser.is match {
    case Empty => CurrentUserId.is match {
      case Empty => false
      case Full(id) => Model.find(classOf[User], id) match {
        case Some(m) =>
          CurrentUser.set(Full(m))
          CurrentUserId.set(Full(m.id))
          true
        case None => false
      }
      case _ => false
    }
    case Full(m) => true
    case _ => false
  }

  def isDeveloper_?() = isAuthenticated_?() && CurrentUser.is.get.emailAddress == "vytasab@gmail.com"

  def userIs(): String =
    isAuthenticated_?() match {
      case true if CurrentUser.is.get.emailAddress == "vytasab@gmail.com" => "admin"
      case true => "loggedIn"
      case false => "guest"
    }

}
