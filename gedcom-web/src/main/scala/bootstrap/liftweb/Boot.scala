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

import java.util.Locale
import _root_.java.text.MessageFormat
import org.slf4j.{LoggerFactory, Logger}

import _root_.scala.xml.NodeSeq

import net.liftweb.http._
import net.liftweb.http.ParsePath
import net.liftweb.http.provider._
import net.liftweb.http.RewriteRequest
import net.liftweb.sitemap._
import net.liftweb.util.Helpers._
import net.liftweb.widgets.menu.MenuWidget
import net.liftweb.widgets.autocomplete.AutoComplete
import net.liftweb.mapper._
import net.liftweb.common._
import _root_.lt.node.gedcom._
import model._
import _root_.lt.node.gedcom.rest.GedcomRest
import net.liftweb.util.{Mailer, Props}
import javax.mail.{PasswordAuthentication, Authenticator}
import scala.Some
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.core.util.StatusPrinter

/**
 * A class that's instantiated early and run.  It allows the application
 * to modify lift's environment
 */

class Boot /*extends Logger*/ extends Loggable {
  // http://kevinlocke.name/bits/2013/02/05/configuring-logging-in-lift-tests/
  def boot {
    println("==================================================================")
    val log/*: Logger*/ = LoggerFactory.getLogger("Boot")
    println("" + log.isDebugEnabled + " " + log.isWarnEnabled + " " + log.isInfoEnabled + " " + log.isErrorEnabled + " " + log.isTraceEnabled)
    log.debug("=====================================================================")
    println("LiftRules.resourceServerPath = " + LiftRules.resourceServerPath)
    println("ResourceServer.baseResourceLocation = " + ResourceServer.baseResourceLocation)
    log.debug("====== ===== ==== === == = Boot gedcom-web = == === ==== ===== ======")
    log.error("====== ===== ==== === == = Boot gedcom-web = == === ==== ===== ======")
    logger.debug("[LoggerContext]...")
    val lc: LoggerContext = LoggerFactory.getILoggerFactory.asInstanceOf[LoggerContext]
    StatusPrinter.print(lc)
    logger.debug("...[LoggerContext]")

    log.debug("in Boot: System.getProperty(\"run.mode\") = " + System.getProperty("run.mode"))
    /*log.debug("System properties: ")
    var propNames = System.getProperties.propertyNames()
    while (propNames.hasMoreElements) {
      var pn = propNames.nextElement().toString
      log.debug(  pn + "  ->  " +  System.getProperties.getProperty(pn))
    }*/
    println("in Boot: System.getProperty(\"run.mode\") = " + System.getProperty("run.mode"))
    /*println("System properties: ")
    var propNamesx = System.getProperties.propertyNames()
    while (propNamesx.hasMoreElements) {
      var pnx = propNamesx.nextElement().toString
      println(  pnx + "  ->  " +  System.getProperties.getProperty(pnx))
    }*/


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

//    LiftRules.resourceBundleFactories prepend {
//      case (name, locale) =>
//        //tryo { java.util.ResourceBundle.getBundle("i18n." + name, locale) } openOr null
//        tryo { ResourceBundle.getBundle("text." + name, locale) } openOr null
//    }

    // Set locale dependent text
    LiftRules.resourceNames = List(/*"i18n.lingua", "lift",*/ "text")
// TODO B417-7/vsh  atsikratyti "i18n.lingua", "lift"


//    LiftRules.resourceBundleFactories prepend {
//      case (name, locale) =>
//        //log.debug(MessageFormat.format("Boot LiftRules.resourceBundleFactories  - |{0}| - |{1}|", name, locale))
//        tryo { ResourceBundle.getBundle(/*"text." + */name, locale) } openOr null
//    }

    ResourceServer.allow{
      //case "menu" :: _ => true    // AA26-2/vsh no need this line ?!
      //case "raphael" :: _ => true // does not work AC17/vsh
      //case "ui" :: _ => true
            //case "ui" :: "css" :: "smoothness" :: "jquery-ui-1.7.1.custom.css" :: Nil => true
            //case "ui" :: "js" :: "jquery-ui-1.7.1.custom.min.js" :: Nil => true
      //      //case "wymeditor" :: "jquery.wymeditor.pack.js" :: Nil => true
      //      //case "wymeditor" :: "lang" :: "en.js" :: Nil => true
      //      //case "wymeditor" :: "skins" :: "default" :: "skin.js" :: Nil => true
      //      case "wymeditor" :: _ => true
      case _ => true                // D615-6/vsh this line is necessary !!!
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
    Schemifier.schemify(true, /*Log*/ Schemifier.infoF _ )

    // B321-1 =============================================
    // Set up a site map ...
    LiftRules.setSiteMap(SiteMap(MenuInfo.entries: _*))
    // ... and show it in JS based menu
    //LiftRules.setSiteMap(entries)
    // B321-1 =============================================

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

    //LiftRules.statelessDispatchTable.append(ImageInfo.serveImage)
    LiftRules.statelessDispatchTable.append(MultiMediaService.serveImage)  // // D605-6/vsh  uncommented
    // D605-6/vsh  no such method:  LiftRules.statelessDispatch.append(MultiMediaService.serveImage)

    LiftRules.localeCalculator = localeCalculator _

    LiftSession

    //-- Charles F. Munat: Encrypting user passwords with Jasypt and JPA []... ====================
    LiftRules.dispatch.prepend{
      case Req(List("logout"), "", _) => AccessControl.logout
    }

    //-- http://blog.getintheloop.eu/2009/05/03/url-rewriting-with-the-lift-framework
    LiftRules.statelessRewrite.prepend( {
      case RewriteRequest(ParsePath(List("validation", page), _, _, _), _, _) => {
        log.debug("RewriteRequest 'validation' page = |%s|".format(page))
        RewriteResponse(ParsePath(List("login", "newPassword"), "", true, false),
          Map("code" -> page), true)
      }
      case RewriteRequest(ParsePath(List("addendup", page), _, _, _), _, _) =>
        log.debug("S.inStatefulScope_?= |%s|", S.inStatefulScope_?)
        log.debug("RewriteRequest 'addendup' signup page=|%s| ".format(page))
        log.debug("userOption = |%s|", Model.createNamedQuery[User]("findUserByValidationCode", "code" -> page).findOne)
        S.setSessionAttribute("kodas", page)
        log.debug("S.getSessionAttribute('kodas')= |%s|".format(S.getSessionAttribute("kodas")))
        RewriteResponse(ParsePath(List("login", "addendup"), "", true, false),
          Map("code" -> page), true)

    } )

    LiftRules.dispatch.prepend{
      case Req /*guestState*/ ("admin" :: page, "", _)
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


  LiftRules.noticesAutoFadeOut.default.set((notices: NoticeType.Value) => {
    notices match {
      case NoticeType.Notice => Full((5 seconds, 5 seconds))
      case NoticeType.Warning => Full((5 seconds, 4 seconds))
      case NoticeType.Error => Full((5 seconds, 3 seconds))
      case _ => Empty
    }
  })

  configureMailer()
  //SendMailTestTLS.render
  //SendGridTest.render

  //-- Locale calculation
  //-- http://www.assembla.com/wiki/show/liftweb/Internationalization
  //log.debug(MessageFormat.format("Locale.getDefault() - {0}", Locale.getDefault().toString))
  def localeCalculator(request: Box[HTTPRequest]): Locale =
    request.flatMap(r => {
      val cookieName = "vsh.gedcom"; //  "your.cookie.name"
      def localeCookie(in: String): HTTPCookie =
        HTTPCookie(cookieName, Full(in),
          Full(S.hostName), Full(S.contextPath), Full(2629743), Empty, Empty)
      def localeFromString(in: String): Locale = {
        val x = in.split("_").toList
        //log.debug(MessageFormat.format("localeCalculator localeFromString - |{0}|", new Locale(x.head, x.last).toString))
        new Locale(x.head, x.last)
      }
      def calcLocale: Box[Locale] =
        S.findCookie(cookieName).map(_.value.map(localeFromString)).openOr(Full(LiftRules.defaultLocaleCalculator(request)))
      S.get("locale") match {
        case f@Full(selectedLocale) =>
          //log.debug(MessageFormat.format("localeCalculator f@Full(selectedLocale) - |{0}|", selectedLocale.toString))
          S.addCookie(localeCookie(selectedLocale))
          tryo(localeFromString(selectedLocale))
        case _ =>
          //log.debug(MessageFormat.format("localeCalculator _ "))
          S.param("locale") match {
            case Full(null) =>
              //log.debug(MessageFormat.format("localeCalculator  _ Full(null) - |{0}|", calcLocale.toString))
              calcLocale
            case f@Full(selectedLocale) =>
              //log.debug(MessageFormat.format("localeCalculator _ f@Full(selectedLocale) - |{0}|", selectedLocale.toString))
              S.addCookie(localeCookie(selectedLocale))
              tryo(localeFromString(selectedLocale))
            case _ => calcLocale
          }
      }
    }).openOr(Locale.getDefault())


  private def configureMailer() {
    val log: Logger = LoggerFactory.getLogger("configureMailer")
    log.debug("[]... ... ...")
    log.debug("TEST: Props.fileName |" + Props.fileName + "|")
    log.debug("TEST: Props.propFileName |" + Props.propFileName + "|")
    log.debug("TEST: Props.modeName |" + Props.modeName + "|")
    log.debug("TEST: Props.mode |" + Props.mode.toString + "|")
    log.debug("TEST: Props.props |" + Props.props.toString + "|")
//    log.debug("mail.smtp.auth |" + Props.get("mail.smtp.auth", "false") + "|")

    var isAuth =  Props.get("mail.smtp.auth", "false").toBoolean

    /*Mailer.customProperties =*/
    Props.get("mail.smtp.host", "localhost") match {
      case "smtp.gmail.com" =>
        log.debug("smtp.gmail.com |" + Props.get("mail.smtp.host", "localhost") + "|")
        isAuth = true
        /*Map(
          "mail.debug" -> "true",
          "mail.smtp.ssl.trust" -> "smtp.gmail.com", //  !!! http://stackoverflow.com/questions/16632334/could-not-convert-socket-to-tls
          "mail.transport.protocol" -> "smtp",
          "mail.smtp.host" -> "smtp.gmail.com",
          "mail.smtp.port" -> "587",
          "mail.smtp.auth" -> "true" //,
                                                     // http://stackoverflow.com/questions/13918374/java-email-exception-server-is-not-trusted
          //"mail.smtp.ssl.enable" -> "true",        // Bypass the SSL authentication
          //"mail.smtp.starttls.enable" -> "true",   // Bypass the SSL authentication
          //"mail.smtp.ssl.socketFactory" -> sf
          )*/
      case host =>
        log.debug("host |" + Props.get("mail.smtp.host", "localhost") + "|")
        /*Map(
          "mail.smtp.host" -> host,
          "mail.smtp.port" -> Props.get("mail.smtp.port", "25"),
          "mail.smtp.auth" -> isAuth.toString
        )*/
        val msg = ("completeQuery: \"mail.smtp.host\" is not \"smtp.gmail.com\"")
        log.debug(msg)
        S.redirectTo("/errorPage", () => {
          ErrorXmlMsg.set(Some(Map("location" -> <p>Boot.configureMailer</p>, "message" -> <p>{msg}</p>)))
        })
    }

    if (isAuth) {
      log.debug("isAuth |" + isAuth.toString + "|")
      (Full(Props.get("mail.smtp.user").get), Full(Props.get("mail.smtp.pass").get)) match {
      case (Full(username), Full(password)) =>
        log.debug("username =|" + username + "|  password =|" + password + "|")
        Mailer.authenticator = Full(new Authenticator() {
            override def getPasswordAuthentication = new PasswordAuthentication(username, password)
          })
        case _ => log.error("Username/password not supplied for Mailer.")
      }
    }
    log.debug("... ... ...[] Mailer.authenticator =|" + Mailer.authenticator.get.toString + "|")
  }

}

object Locales {
  val langs = List(/*"--", */"lt_LT", "en_EN"/*, "de", "pl", "ru"*/)

  val aMap = langs.map{ x => (<_>{x}</_>.text, x)}.toMap

  object LocalesVar extends SessionVar[Map[String, String]](aMap)

}

object ErrorXmlMsg extends SessionVar[Box[Map[String, NodeSeq]]](Empty)

object InfoXmlMsg extends SessionVar[Box[NodeSeq]](Empty)

object PersonIds extends SessionVar [List[Long]] (List())

object FamilyIds extends SessionVar [List[Long]] (List())
