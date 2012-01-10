package lt.node.gedcom.snippet

import _root_.scala.xml._
import bootstrap.liftweb.{CurrentUser, AccessControl}

//import _root_.bootstrap.liftweb._

//import _root_.lt.node.gedcom.model.{ModelMovedTospaSpa, User}
//import _root_.lt.node.gedcom.util.{UberScreen,Utilits}
//import _root_.lt.node.gedcom.util.UberScreen

import _root_.javax.persistence.NoResultException
import _root_.net.liftweb._
import common.Logger
import xml.{Group, NodeSeq}
import util.Helpers._
import http.{SHtml, S}
/**
 * Created by IntelliJ IDEA.
 * User: padargas
 * Date: 11/23/10
 * Time: 1:37 PM
 * To change this template use File | Settings | File Templates.
 */

//class LoginOps

//object LoginOps extends LoginOps

class SendMailTest /*extends lt.node.gedcom.util.UberScreen*/ {
  val log = /*LoggerFactory.get*/ Logger("SendMailTest");


  def sendMailTest(xhtml: NodeSeq): NodeSeq = {

    def done() = {
      try {
        AccessControl.isAuthenticated_? match {
          case true =>
            SendMail.sendMail2User("test", CurrentUser.is.get)
            log.info("---------- the message has been sent successfully ---------")
          case _ =>
            log.info("---------- no logged in user ---------")
        }
      } catch {
        case x: NoResultException =>
          S.error("That validation code has expired.")
          S.redirectTo("/password_reset")
        case y: Exception => println(y)
        S.redirectTo("/")
      }
    }

    bind("test", xhtml,
      "submit" -> SHtml.submit("Send Test Mail", done))
  }





  /**/

  // http://scala-programming-language.1934581.n4.nabble.com/JPA-find-or-create-new-td1966598.html
  //    def addUser(xhtml: NodeSeq): NodeSeq = {
  //        val user = userVar.is.openOr(new User)
  //        def doAdd(): Unit = {
  //            log.debug("[doAdd]...")
  //            if (user.emailAddress.length == 0) {
  //                error("no_user_email")
  //            } else {
  //                log.debug("[doAdd] createdOn " +   classifier.createdOn)
  //                classifier.updatedOn = new Date()
  //                ModelMovedTospaSpa.merge(classifier)
  //            }
  //            notice("Classifier " + classifier.name + " added or updates")
  //            log.debug("...[doAdd]")
  //            redirectTo("index")
  //        }
  //        val sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
  //
  //    }

  //  def login(xhtml: NodeSeq): NodeSeq = {
  //    def logUserIn() = {
  //      try
  //      {
  //        val user: User = ModelMovedTospaSpa.createNamedQuery[User]("findUserByEmailAddress",
  //          "emailAddress" -> S.param("emailAddress").openOr("")).getSingleResult()
  //        if (user.authenticate(S.param("password").openOr(""))) {
  //          CurrentUser(Full(user))
  //          CurrentUserId(Full(user.id))
  //        }
  //        else {
  //          S.error("Unable to log you in.")
  //        }
  //      } catch {
  //        case x: NoResultException =>
  //          S.error("Unable to log you in.")
  //        case e: Exception => S.error(e.getMessage)
  //      }
  //      if (AccessControl.isAuthenticated_?) {
  //        /*redirectTo*/ RedirectTo(RequestedURL.openOr("/"))
  //        RequestedURL(Empty)
  //      }
  //    }
  //    bind("login", xhtml,
  //      "emailAddress" -> (FocusOnLoad(
  //          <input id="login_form_email_address" type="text" size="24"
  //                 name="emailAddress" value=" "/>)),
  //      "password" -> <input id="login_form_password" type="password"
  //                           name="password" size="16" value=" "/>,
  //      "submit" -> SHtml.submit("Log In", logUserIn))
  //  }

  //  def changePassword(xhtml: NodeSeq): NodeSeq = {
  //    def updatePassword() = {
  //      try
  //      {
  //        val user: User =
  //          ModelMovedTospaSpa.createNamedQuery[User]("findUserByValidationCode",
  //            "code" -> S.param("code").openOr("")).getSingleResult()
  //        if (user.validationExpiry > System.currentTimeMillis()) {
  //          if (S.param("password") != S.param("confirmation"))
  //            S.error("Password and confirmation do not match.")
  //          else {
  //            val quality =
  //              Pattern.compile("^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).{8,}")
  //            val password = S.param("password").openOr("")
  //            if (quality.matcher(password).matches) {
  //              S.param("password").map(user.password = _)
  //              user.validationCode = null
  //              user.validationExpiry = 0
  //              ModelMovedTospaSpa.merge(user)
  //              CurrentUser(Full(user))
  //              CurrentUserId(Full(user.id))
  //              S.notice(
  //                "You have successfully changed your password and " +
  //                    "are now logged in.")
  //              Mailer.sendMail(
  //                From("webmas...@xxx.org"),
  //                Subject("[XXX] Credentials changed"),
  //                List(
  //                  To(user.emailAddress),
  //                  xmlToMailBodyType(
  //                    <html>
  //                      <head>
  //                        <title>
  //                           Your credentials have been changed.
  //                         </title>
  //                      </head>
  //                      <body>
  //                        <p>
  //                           Someone, probably you, used a validation email
  //                           sent to this address to successfully change
  //                           your credentials. If this wasn't done by you,
  //                           please contact the webmas...@xxx.org.
  //                         </p>
  //                      </body>
  //                    </html>
  //                  )
  //                ): _*
  //              )
  //              /*redirectTo*/ RedirectTo("/")
  //            } else {
  //              S.error("The password you entered is invalid.")
  //            }
  //          }
  //        }
  //        else {
  //          S.error("That validation code has expired.")
  //          /*redirectTo*/ RedirectTo("/password_reset")
  //        }
  //      } catch {
  //        case x: NoResultException =>
  //          S.error("That validation code has expired.")
  //          /*redirectTo*/ RedirectTo("/password_reset")
  //        case y: Exception => println(y)
  //        /*redirectTo*/ RedirectTo("/")
  //      }
  //    }
  //    bind("change", xhtml,
  //      "password" -> (FocusOnLoad(<input id="password" type="password"
  //                                        name="password" size="24" value=" "/>)),
  //      "confirmation" -> <input id="confirmation" type="password"
  //                               name="confirmation" size="24" value=" "/>,
  //      "submit" -> SHtml.submit("Save Changes", updatePassword))
  //  }

  //  def passwordReset(xhtml: NodeSeq): NodeSeq = {
  //    def resetPassword() = {
  //      try
  //      {
  //        val user: User =
  //          ModelMovedTospaSpa.createNamedQuery[User]("findUserByEmailAddress",
  //            "emailAddress" ->
  //                S.param("emailAddress").openOr("")).getSingleResult()
  //        val code = "http://localhost:8888/validation/" +
  //            user.setValidation
  //        S.notice("Instructions have been mailed to you.")
  //        ModelMovedTospaSpa.merge(user)
  //        Mailer.sendMail(
  //          From("webmas...@xxx.org"),
  //          Subject("[XXX] Important information"),
  //          List(
  //            To(user.emailAddress),
  //            xmlToMailBodyType(<html>
  //              <head>
  //                <title>Reset Information</title>
  //              </head>
  //              <body>
  //                <p>
  //                     Someone, probably you, used your email address to
  //                     request that your credentials for the XXX site be
  //                     reset. To make changes to your site credentials,
  //                     follow the link below:
  //                   </p>
  //                <p>
  //                  <a href={code}>{code}</a>
  //                </p>
  //                <p>
  //                     This validation address will expire at
  //                     {new Date(user.validationExpiry)}.
  //                   </p>
  //              </body>
  //            </html>)
  //          ): _*
  //        )
  //      } catch {
  //        case x: NoResultException => S.error("Unable to find your email address.")
  //        case _ => S.error("A problem was encountered.")
  //      }
  //      /*redirectTo*/ RedirectTo("/login")
  //    }
  //    bind("reset", xhtml,
  //      "emailAddress" -> (FocusOnLoad(
  //          <input id="login_form_email_address" type="text" size="24"
  //                 name="emailAddress" value=" "/>)),
  //      "submit" -> SHtml.submit("Reset Password", resetPassword))
  //  }

}