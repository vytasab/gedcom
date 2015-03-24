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

object CurrentUser extends /*RequestVar*/SessionVar[Box[User]](Empty)

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

  //def isAuthenticated_?() = isAuthenticatedInternal_?()  //&& isHttps_?()

  def isAuthenticated_?(): Boolean =  //-----------------------------------------
//     if (S.request.map(_.request.scheme == "https").openOr(false)) {
//       log.debug("isAuthenticated_?(): https")
//       /*case true => */
       CurrentUser.is match {
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
//     } else {
//        log.debug("isAuthenticated_?(): http")
//        //S.notice("Please, use https protocol in order to login (if you have Login rights)")
//        S.notice("Use secure (https) request scheme only")
//        CurrentUser.set(Empty)
//        CurrentUserId.set(Empty)
//        // F227-5/vsh: neveikia tai: RedirectResponse("/___use__https__protocol_only___!!!")
//        false
//    } //-----------------------------------------


    //    if (S.request.map(_.request.scheme == "https").openOr(false)) {
    //      true
    //    } else {
    //        val cruri = CurrentReq.value.request.uri  //.contextPath
    //        log.debug("CurrentReq.value.request.uri=" + cruri)
    //        if (cruri.contains("/errorPage") ||
    //          cruri.endsWith("/gedcom/")  || cruri.endsWith("/gedcom") ||
    //          cruri.endsWith("/gedcom-web/")  || cruri.endsWith("/gedcom-web")
    //        ) {
    //          val msg = ("!!! Use secure request scheme \"https\" only")
    //          log.debug(msg)
    //          S.redirectTo("/errorPage", () => {
    //            ErrorXmlMsg.set(Some(Map(
    //              "location" -> <p>AccessControl.isHttps_?()</p>,
    //              "message" -> <p>{msg}</p>)))
    //          })
    //        } //else {}
    //      false
    //    }

  def toGo(usual: String): String = {
    //log.debug("request.scheme =  " + S.uri)
    //S.request.map(_.request.scheme == "https").openOr(false)
    // CurrentReq.value.request.scheme
    //-------------------------------------
//    val cruri = CurrentReq.value.request.uri  //.contextPath
//    log.debug("whereToRedirect(): CurrentReq.value.request.uri=" + cruri)
//    if (cruri.contains("/errorPage") /*|| cruri.endsWith("/gedcom/")  || cruri.endsWith("/gedcom") ||
//       cruri.endsWith("/gedcom-web/")  || cruri.endsWith("/gedcom-web")*/ ) {
//      "/errorPage"
//    } else if (CurrentReq.value.request.scheme ==  "https"
//        /*S.request.map(_.request.scheme == "https").openOr(false)*/) {
//        "/"
//    } else {
//      val msg = ("Use secure request scheme \"https\" only")
//      log.debug(msg)
//      ErrorXmlMsg.set(Some(Map(
//        "location" -> <p>AccessControl.whereToRedirect()</p>,
//        "message" -> <p>{msg}</p>)))
//      "/errorPage"
//    }
    //-------------------------------------
//    if (S.request.map(_.request.scheme == "https").openOr(false)) {
//      "/"
//    } else {
//        val cruri = CurrentReq.value.request.uri  //.contextPath
//        log.debug("CurrentReq.value.request.uri=" + cruri)
//        if (cruri.contains("/errorPage") ||
//          cruri.endsWith("/gedcom/")  || cruri.endsWith("/gedcom") ||
//          cruri.endsWith("/gedcom-web/")  || cruri.endsWith("/gedcom-web"))
//        {
//          val msg = ("!!! Use secure request scheme \"https\" only")
//          log.debug(msg)
//          //S.redirectTo("/errorPage", () => {
//          ErrorXmlMsg.set(Some(Map(
//            "location" -> <p>AccessControl.isHttps_?()</p>,
//            "message" -> <p>{msg}</p>)))
//          //})
//          "/errorPage"
//        } else {
//          "/"
//        }
//    }
    //-------------------------------------
    //log.debug("whereToRedirect: S.request.toString=|" + S.request.get.toString + "|")
//    println("[toGo]... ")
//    val  isHttps= S.request.map(_.request.scheme == "https").openOr(false)
//    if (isHttps) {
//      println("S.request.map(_.request.scheme == \"https\").openOr(false)=|" + isHttps + "|")
//      log.debug("S.request.map(_.request.scheme == \"https\").openOr(false)=|" + isHttps + "|")
//      //"/index_________.html"
//      usual
//    } else {
//        val cruri = CurrentReq.value.request.uri  //.contextPath
//        println("toGo: CurrentReq.value.request.uri=|" + cruri + "|")
//        log.debug("toGo: CurrentReq.value.request.uri=|" + cruri + "|")
////        if (cruri.contains("/errorPage") ||
////          cruri.endsWith("/gedcom/")  || cruri.endsWith("/gedcom") ||
////          cruri.endsWith("/gedcom-web/")  || cruri.endsWith("/gedcom-web"))
////        {
////          val msg = ("!!! Use secure request scheme \"https\" only")
////          log.debug(msg)
////          //S.redirectTo("/errorPage", () => {
////          ErrorXmlMsg.set(Some(Map(
////            "location" -> <p>AccessControl.isHttps_?()</p>,
////            "message" -> <p>{msg}</p>)))
////          //})
////          "/errorPage"
////        } else {
////          "/"
////        }
////      "/___use__https__protocol_only___!!!"
//      usual
//    }
    //-------------------------------------
    usual
  }

  def isHttps_?(): Boolean = (S.request.map(_.request.scheme == "https").openOr(false))

  def isDeveloper_?(): Boolean = isAuthenticated_?() && CurrentUser.is.get.emailAddress == "vytasab@gmail.com"

  def userIs(): String =
    isAuthenticated_?() match {
      case true if CurrentUser.is.get.emailAddress == "vytasab@gmail.com" => "admin"
      case true => "loggedIn"
      case false => "guest"
    }

}
