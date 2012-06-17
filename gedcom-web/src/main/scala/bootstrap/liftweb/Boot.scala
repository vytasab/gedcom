/*
 * Copyright 2008 WorldWide Conferencing, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 */
package bootstrap.liftweb

import _root_.java.util.Locale
import _root_.java.text.MessageFormat
import org.slf4j.{LoggerFactory, Logger}

import _root_.scala.xml.NodeSeq

import _root_.net.liftweb._
import http._
import http.provider._
import sitemap._
import util.Helpers._
import widgets.menu.MenuWidget
import widgets.autocomplete.AutoComplete
import mapper._
import common._
import _root_.lt.node.gedcom._
import model._
import _root_.lt.node.gedcom.rest.GedcomRest
/**
 * A class that's instantiated early and run.  It allows the application
 * to modify lift's environment
 */

class Boot extends Loggable {
  def boot {
    println("==================================================================")
    //deprec LogBoot._log4JSetup
    //deprec Slf4jLogBoot.enable
    val log: Logger = LoggerFactory.getLogger("Boot");
    //log.debug("GedCom ab24-3"); //log.info("GedCom ab24-3"); log.warn("GedCom ab24-3");
    //    //---------------------------------------
    //    println("------->" + (Locale.getAvailableLocales.
    //        toList.sort(_.getDisplayName < _.getDisplayName).
    //        map(lo => (lo.toString, lo.getDisplayName))) /* % ("id" -> fieldId)*/ .toString)
    //    println()
    //    println("------->" + MappedTimeZone.timeZoneList.toString)
    //    println()
    //    //    println("------->" + (SHtml.select(Locale.getAvailableLocales.
    //    //        toList.sort(_.getDisplayName < _.getDisplayName).
    //    //        map(lo => (lo.toString, lo.getDisplayName)),
    //    //      Full(this.is), set)) /*% ("id" -> fieldId)*/.toString)
    //    //---------------------------------------

    //    LogBoot.defaultProps =
    //        """<?xml version="1.0" encoding="UTF-8" ?>
    //        <!DOCTYPE log4j:configuration SYSTEM "log4j.dtd">
    //        <log4j:configuration xmlns:log4j="http://jakarta.apache.org/log4j/">
    //        <appender name="appender" class="org.apache.log4j.ConsoleAppender">
    //        <layout class="org.apache.log4j.SimpleLayout"/>
    //        </appender>
    //        <root>
    //        <priority value ="DEBUG"/>
    //        <appender-ref ref="appender"/>
    //        </root>
    //        </log4j:configuration>
    //        """

    // where to search snippet
    LiftRules.addToPackages("lt.node.gedcom")

    LiftRules.resourceBundleFactories prepend {
      case (name, locale) =>
        tryo { java.util.ResourceBundle.getBundle("i18n." + name, locale) } openOr null
    }

    // Set locale dependent text
    LiftRules.resourceNames = List("i18n.lingua", "lift", "text")
// TODO B417-7/vsh  atsikratyti "i18n.lingua", "lift"

    ResourceServer.allow{
      // AA26-2/vsh no need this line ?!
      case "menu" :: _ => true
      //case "raphael" :: _ => true // does not work AC17/vsh
      //      case "ui" :: _ => true
      //      //case "ui" :: "css" :: "smoothness" :: "jquery-ui-1.7.1.custom.css" :: Nil => true
      //      //case "ui" :: "js" :: "jquery-ui-1.7.1.custom.min.js" :: Nil => true
      //      //case "wymeditor" :: "jquery.wymeditor.pack.js" :: Nil => true
      //      //case "wymeditor" :: "lang" :: "en.js" :: Nil => true
      //      //case "wymeditor" :: "skins" :: "default" :: "skin.js" :: Nil => true
      //      case "wymeditor" :: _ => true
    }
    // Force the request to be UTF-8
    LiftRules.early.append((req: HTTPRequest) => {
      req.setCharacterEncoding("UTF-8")
    })

    DefaultConnectionIdentifier.jndiName = "jdbc/datasource-gedcom"
    if (!DB.jndiJdbcConnAvailable_?) {
      log.debug(MessageFormat.format("Boot DB.jndiJdbcConnAvailable - {0}", "no"))
      DB.defineConnectionManager(DefaultConnectionIdentifier, DBVendor)
    } else
      log.debug(MessageFormat.format("Boot DB.jndiJdbcConnAvailable - {0}", "yes"))

    // where to search snippet
    LiftRules.addToPackages("lt.node.gedcom")
    // !!! B324-4/vsh DB initialization MUST preceed  Schemifier.schemify ...
    Schemifier.schemify(true, /*Log*/ Schemifier.infoF _ /*, User, ImageInfo, ImageBlob, AuctionItem, AuctionBid*/) //, ModelMovedTospaSpa

    //    // Set up a site map
    //    //    val entries = SiteMap(
    //    val entries: List[Menu] = List(
    //      Menu(Loc("Home", "index" :: Nil, ?("Home"))),
    //      Menu(Loc("Authors", "authors" :: "list" :: Nil, ?("Author List"))),
    //      Menu(Loc("Add Author", "authors" :: "add" :: Nil, ?("Add Author"), Hidden)),
    //      Menu(Loc("Books", "books" :: "list" :: Nil, ?("Book List"))),
    //      Menu(Loc("Add Book", "books" :: "add" :: Nil, ?("Add Book"), Hidden)),
    //      Menu(Loc("BookSearch", "books" :: "search" :: Nil, ?("Book Search"))),
    //
    //      Menu(Loc("UserCred", "credentials" :: "index" :: Nil, ?("Credentials"))),
    //      Menu(Loc("UserLogin", "login" :: "index" :: Nil, ?("Login"))),
    //      Menu(Loc("UserReset", "password_reset" :: "index" :: Nil, ?("Password Reset")))
    //    )
    //    // Set up a site map ...
    //    LiftRules.setSiteMap(SiteMap(entries: _*))

    // B321-1=============================================
    // Set up a site map ...
    LiftRules.setSiteMap(SiteMap(MenuInfo.entries: _*))
    // ... and show it in JS based menu
    //LiftRules.setSiteMap(entries)
    // B321-1=============================================
//    // Google-group: Lift: [Does my Url Rewriting look correct?]
//    // https://github.com/dpp/starting_point/blob/menu_fun/src/main/scala/bootstrap/liftweb/Boot.scala
//    // Build SiteMap
//    def sitemap = SiteMap(
//      Menu.i("Home") / "index"/*,
//      ForumInfo.menu,
//      AForum.menu,
//      AThread.menu*//*,
//
//      // more complex because this menu allows anything in the
//      // /static path to be visible
//      Menu(Loc("Static", Link(List("static"), true, "/static/index"),
//	       "Static Content"))*/
//    )
//
//    //def sitemapMutators = User.sitemapMutator
//    //// set the sitemap.  Note if you don't want access control for
//    //// each page, just comment this line out.
//    //LiftRules.setSiteMapFunc(() => sitemapMutators(sitemap))
    // B321-1=============================================

    // Locale calculation
    // http://www.assembla.com/wiki/show/liftweb/Internationalization
    def localeCalculator(request: Box[HTTPRequest]): Locale =
      request.flatMap(r => {
        val cookieName = "vsh.gedcom"; //  "your.cookie.name"
        def localeCookie(in: String): HTTPCookie =
          HTTPCookie(cookieName, Full(in),
            Full(S.hostName), Full(S.contextPath), Full(2629743), Empty, Empty)
        def localeFromString(in: String): Locale = {
          val x = in.split("_").toList;
          new Locale(x.head, x.last)
        }
        def calcLocale: Box[Locale] =
          S.findCookie(cookieName).map(
            _.value.map(localeFromString)
          ).openOr(Full(LiftRules.defaultLocaleCalculator(request)))
        S.get("locale") match {
          case f@Full(selectedLocale) =>
            S.addCookie(localeCookie(selectedLocale))
            tryo(localeFromString(selectedLocale))
          case _ =>
            S.param("locale") match {
              case Full(null) => calcLocale
              case f@Full(selectedLocale) =>
                S.addCookie(localeCookie(selectedLocale))
                tryo(localeFromString(selectedLocale))
              case _ => calcLocale
            }
        }
      }).openOr(Locale.getDefault())

    LiftRules.localeCalculator = localeCalculator _ //    val lithuanianChef = new Locale("lt_LT") // chef


    // Charles F. Munat: Encrypting user passwords with Jasypt and JPA []... ====================
    LiftRules.dispatch.prepend{
      case Req(List("logout"), "", _) => AccessControl.logout
    }

    // http://blog.getintheloop.eu/2009/05/03/url-rewriting-with-the-lift-framework
    LiftRules.statelessRewrite.prepend(/*NamedPF("UserValidation")*/ {
      case RewriteRequest(ParsePath(List("validation", page), _, _, _), _, _) => {
        log.debug("RewriteRequest validation page = " + page);
        RewriteResponse(ParsePath(List("login", "changePassword"), "", true, false),
          Map("code" -> page.toString), true)
      }
      case RewriteRequest(ParsePath(List("addendup", page), _, _, _), _, _) => {
        log.debug("S.inStatefulScope_?= |" + S.inStatefulScope_? + "|")
        log.debug("RewriteRequest signup page = " + page);
        log.debug("userOption = |" +
          Model.createNamedQuery[User]("findUserByValidationCode",
          "code" -> page).findOne + "|")

        S.setSessionAttribute("kodas", page)
        log.debug("S.getSessionAttribute('kodas')= |" + S.getSessionAttribute("kodas") + "|")
        RewriteResponse(ParsePath(List("login", "addendup"), "", true, false),
          Map("code" -> page.toString), true)

//        val userOption: Option[User] = Model.createNamedQuery[User]("findUserByValidationCode",
//          "code" -> page).findOne //.getSingleResult()
//        userOption match {
//          case Some(user) =>
//            if (user.validationExpiry > System.currentTimeMillis()) {
//              user.validationCode = null
//              user.validationExpiry = 0
//              user.validated = true
//              Model.mergeAndFlush(user)
//              CurrentUser(Empty)       // CurrentUser(Full(user))
//              CurrentUserId(Empty)     // CurrentUserId(Full(user.id))
//              S.notice("You have successfully finished your user account creation.")
//              //S.notice("You have successfully finished your user account creation and are now logged in.")
//
//              //val persons: List[Person] = Model.createNamedQuery[Person]("findPersonByGivnSurn",
//              //  "nameGivn" -> user.firstName, "nameSurn" -> user.lastName).findAll.toList
//              //log.debug("login:  persons.size=" + persons.size)
//              //persons match {
//              //  case x :: Nil => // go to canvas
//              //    log.debug("login:  case x :: Nil")
//              //    //RequestedURL(Full(<_>/rest/person/{x.id}</_>.text))
//              //    RewriteResponse(ParsePath(List("rest", "person", x.id.toString), "", true, false),
//              //      Map(), true)
//              //  case x :: xs => // go to search form
//              //  //RequestedURL(Full("/gedcom/personsSublist"))
//              //    RewriteResponse(ParsePath(List("gedcom", "personsSublist"), "", true, false),
//              //      Map(/*"code" -> page.toString*/), true)
//              //  case _ => // go to decision making page
//              //    log.debug("login:  case _")
//              //    S.unsetSessionAttribute("role")
//              //    S.setSessionAttribute("aNameGivn", user.firstName)
//              //    S.setSessionAttribute("aNameSurn", user.lastName)
//              //    S.unsetSessionAttribute("aGender")
//              //    //RequestedURL(Full("/gedcom/addeditPerson"))
//              //    RewriteResponse(ParsePath(List("gedcom", "addeditPerson"), "", true, false),
//              //      Map(), true)
//              //}
//              //RewriteResponse(ParsePath(List("gedcom", "personsSublist"), "", true, false),
//              //  Map(/*"code" -> page.toString*/), true)
//              //RewriteResponse(ParsePath(List("login", "login"), "", true, false), Map(), true)
//              InfoXmlMsg.set(Full(LongMsgs.longMsgs("endup.validation")(S.locale.getLanguage)))
//              log.debug("InfoXmlMsg: " + InfoXmlMsg.is.open_!.toString)
//              RewriteResponse(ParsePath(List("infoPage"), "", true, false), Map(), true)
//            } else {
//              S.error("That validation code has expired.")
//              RewriteResponse(ParsePath(List("login", "lostPassword"), "", true, false), Map(), true)
//            }
//          case None =>
//            //S.setSessionAttribute("appErrorLocation", "Boot.LiftRules.statelessRewrite: addendup ")
//            //S.setSessionAttribute("appError", "No User found via 'findUserByValidationCode' for = " + page)
//            //.openOr(Map("location" -> <p>no info message</p>, "message" -> <p>strange: no info message</p>))
//            //    val errLocMsg: Map[String,NodeSeq] = ErrorXmlMsg.openOr(Map("location" -> <p>no info message</p>, "message" -> <p>strange: no info message</p>))
//            ErrorXmlMsg.set(Some(Map(
//              "location" -> <p>Boot.LiftRules.statelessRewrite: addendup</p>,
//              "message" -> <p>No User found via 'findUserByValidationCode' for = {page}</p>)))
//            RewriteResponse(ParsePath(List("errorPage"), "", true, false),
//              Map(/*"code" -> page.toString*/), true)
//        }
      }
    })

    LiftRules.dispatch.prepend{
      case Req /*uestState*/ ("admin" :: page, "", _)
        if !AccessControl.isAuthenticated_? =>
        S.error("Please log in to view the page you requested.")
        RequestedURL(Full(S.uri))
        () => Full(RedirectResponse("/login"))
    }
  }

  // ...[] ===================================================================================

  LiftRules.dispatch.prepend(GedcomRest.dispatch)

  S.addAround(DB.buildLoanWrapper)

  // Initiate all the widgets
  //CalendarMonthView.init
  //CalendarWeekView.init
  //CalendarDayView.init
  //TreeView.init
  //Sparklines.init
  //TableSorter.init
  MenuWidget.init
  AutoComplete.init


  //}

  LiftRules.noticesAutoFadeOut.default.set((notices: NoticeType.Value) => {
    notices match {
      case NoticeType.Notice => Full((5 seconds, 5 seconds))
      case NoticeType.Warning => Full((5 seconds, 4 seconds))
      case NoticeType.Error => Full((5 seconds, 3 seconds))
      case _ => Empty
    }
  })

}

// ...[] ===================================================================================

object Locales {
  val langs = List(/*"--",*/ "lt_LT", "en_EN"/*, "de", "pl", "ru"*/)

  val aMap = langs.map{ x => (<_>{x}</_>.text, x)}.toMap

  object LocalesVar extends SessionVar[Map[String, String]](aMap)

}

object ErrorXmlMsg extends SessionVar[Box[Map[String, NodeSeq]]](Empty)

object InfoXmlMsg extends SessionVar[Box[NodeSeq]](Empty)

object PersonIds extends SessionVar [List[Long]] (List())

object FamilyIds extends SessionVar [List[Long]] (List())
