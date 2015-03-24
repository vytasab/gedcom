package lt.node.gedcom.snippet


import _root_.scala.xml._

//import _root_.bootstrap.liftweb._

//import _root_.lt.node.gedcom.model.{ModelMovedTospaSpa, User}
//import _root_.lt.node.gedcom.util.{UberScreen,Utilits}
//import _root_.lt.node.gedcom.util.UberScreen


import _root_.net.liftweb._
import common.Logger
import xml.{Group, NodeSeq}
import util.Helpers._
import http.SHtml

/**
 * Created by IntelliJ IDEA.
 * User: padargas
 * Date: 11/23/10
 * Time: 1:37 PM
 * To change this template use File | Settings | File Templates.
 */

//class LoginOps

//object LoginOps extends LoginOps

class Bandymas extends lt.node.gedcom.util.UberScreen {
  val log = /*LoggerFactory.get*/ Logger("Bandymas");

  //object userVar extends RequestVar[Box[User]](Empty)

  /* */

  def bandymas(xhtml: Group): NodeSeq = {
    //val user = userVar.is.openOr(new User) // <=!!
    def doAdd(): Unit = {
      //      log.debug("[doAdd]...")
      //      if (user.firstName.length == 0) {
      //        error("no_firstName")
      //      } else if (!MappedEmail.validEmailAddr_?(user.emailAddress)) {
      //        error("emailAddress is invalid")
      //      } else {
      //        log.debug("[doAdd] createdOn " + user.createdOn)
      //        user.updatedOn = new Date()
      //        ModelMovedTospaSpa.merge(User)
      //      }
      //      notice("User " + user.firstName + " added or updated")
      //      log.debug("...[doAdd]")
      //      RedirectTo("index")
    }
    var plainPsw = ""
    var plainPsw2 = ""
    var firstName = ""
    var lastName = ""
    var emailAddress = ""
    var description = ""

    //textarea("description", "default", 5, 80)

    bind("z", xhtml,
      //      "id" -> SHtml.hidden(() => {
      //        log.debug("----- user add/update: id=" + user.id);
      //        user.id = currentId
      //      }),
      "plainPsw" -> SHtml.text(plainPsw, plainPsw = _),
      "plainPsw2" -> SHtml.text(plainPsw2, plainPsw2 = _),
      "firstName" -> SHtml.text(/*user.*/ firstName, /*user.*/ firstName = _),
      "lastName" -> SHtml.text(/*user.*/ lastName, /*user.*/ lastName = _),
      "emailAddress" -> SHtml.text(/*user.*/ emailAddress, /*user.*/ emailAddress = _),
      //"description" -> SHtml.text(/*user.*/description, /*user.*/description = _),
      "description" -> textarea("description", "default", 5, 80),
      //      "locale" -> select[String]
      /*
def textarea(name: String, default: String, rows: Int = 5, cols: Int = 80) = {
Console println (" inSide textarea")
makeField[String](name, default, field => SHtml.textarea(field.is,
 field.set(_), "rows" -> rows.toString, "cols" -> cols.toString))
Console println (" textarea via makeField made")
}
def select[T](name: String, default: T, choices: Seq[(T, String)]) = {
makeField[Box[T]](name, Full(default), field => SHtml.selectObj(choices,
 field.is, (t: T) => field.set(Full(t))))
}
      */
      //      "code" -> SHtml.text(user.code, user.code = _),
      //      "note" -> SHtml.textarea(user.note, user.note = _, "rows" -> "2", "cols" -> "60", "class" -> "small"),
      //      "useAsAttrib" -> SHtml.checkbox(bool, (bool) => {
      //        if (bool) user.useAsAttrib = 1 else user.useAsAttrib = 0;
      //        log.debug("----- user useAsAttrib: bool=" + bool.toString);
      //        log.debug("----- user useAsAttrib: useAsAttrib=" + user.useAsAttrib)
      //      }),
      //      "createdOnHidden" -> SHtml.hidden(() => {
      //
      //        log.debug("----- add/update: id=" + user.id);
      //        user.createdOn = createdOn
      //      }),
      //"createdOn" -> (<span>{if (user.createdOn == null) sdf.format(new Date()) else sdf.format(user.createdOn)}</span>),
      //"updatedOn" -> (<span>{sdf.format(new Date())}</span>),

      "submit" -> SHtml.submit("Save", doAdd),
      "cancel" -> SHtml.link("index", () => {}, Text(("Cancel")))
    )
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
