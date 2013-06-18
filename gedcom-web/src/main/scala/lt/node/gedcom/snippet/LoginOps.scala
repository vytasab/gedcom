package lt.node.gedcom.snippet

import _root_.java.sql.Date
import _root_.java.util.regex.Pattern
import _root_.java.text.{ParsePosition, SimpleDateFormat}
import _root_.javax.persistence.NoResultException


import bootstrap.liftweb._

import _root_.net.liftweb._
import common.{Logger, Empty, Full}
import http.js.JsCmds.FocusOnLoad
import util._
import mapper._
import util.Mailer
import util.Mailer._
import util.Helpers._
import http.{SHtml, S}

import _root_.lt.node.gedcom._
import model.{Model, User, Person}
import _root_.lt.node.gedcom.util.{UberScreen, LongMsgs}
import java.security.MessageDigest
import org.jasypt.contrib.org.apache.commons.codec_1_3.binary.Base64


class LoginOps {

  val log = Logger("LoginOps");
  //object userVar extends RequestVar[Box[User]](Empty)

  def login = {

    def logUserIn() = {
      try {
        log.debug("logUserIn: Props.fileName |" + Props.fileName + "|")
        log.debug("logUserIn: Props.propFileName |" + Props.propFileName + "|")
        log.debug("logUserIn: Props.modeName |" + Props.modeName + "|")
        log.debug("logUserIn: Props.mode |" + Props.mode.toString + "|")
        RequestedURL(Empty)
        Model.createNamedQuery[User]("findUserByEmailAddress",
          "emailAddress" -> S.param("emailAddress").openOr("")).findOne match {
          case Some(user) =>
            log.debug("logUserIn: rasta db; password=" + S.param("password").openOr(""))
            log.debug("logUserIn: user.passwordHash=" +  user.passwordHash )
            log.debug("logUserIn: user.passwordSalt=" +  user.passwordSalt )
            log.debug("logUserIn: hash(pw)=" + hashx(S.param("password").openOr("")))
            log.debug("logUserIn: hash(pw + this.passwordSalt)=" + hashx(S.param("password").openOr("") + user.passwordSalt))

            if (user.authenticate(S.param("password").openOr(""))) {
              log.debug("logUserIn: user is authenticated")
              CurrentUser(Full(user))
              CurrentUserId(Full(user.id))
              val persons: List[Person] = Model.createNamedQuery[Person]("findPersonByGivnSurn",
                "nameGivn" -> user.firstName, "nameSurn" -> user.lastName).findAll.toList
              log.debug("logUserIn:  persons.size=" + persons.size)
              persons match {
                case x :: Nil => // go to canvas
                  log.debug("logUserIn:  case x :: Nil")
                  RequestedURL(Full(<_>/rest/person/{x.id}</_>.text))
                case x :: xs => // go to search form
                  RequestedURL(Full("/gedcom/personsSublist"))
                case _ => // go to decision making page
                  log.debug("logUserIn:  case _")
                  /* D618-2/vsh: netinka mnotetims, nes dažnai pamaišo mergautinė pavardė
                  S.unsetSessionAttribute("role")
                  S.setSessionAttribute("aNameGivn", user.firstName)
                  S.setSessionAttribute("aNameSurn", user.lastName)
                  S.unsetSessionAttribute("aGender")
                  RequestedURL(Full("/gedcom/addeditPerson"))
                  */
                  RequestedURL(Full("/gedcom/personsSublist"))
              }
            } else {
              val msg = S.?("invalid.user.authent")
              log.warn("logUserIn: " + msg)
              S.warning(msg)
              RequestedURL(Full("/login/login"))
            }
          case None =>
            val msg = S.?("unable.find.email")
            log.warn("logUserIn: " + msg)
            S.warning(msg)
            RequestedURL(Full("/login/login"))

        }
      } catch {
        case x: NoResultException =>
          log.error("logUserIn: nerasta NoResultException")
          S.error(x.getMessage)
        case e: Exception =>
          log.error("logUserIn: nerasta Exception")
          S.error(e.getMessage)
      } finally {
        log.debug("logUserIn:  finally; S.hostAndPath = " + S.hostAndPath)
        //if (AccessControl.isAuthenticated_?) {
        log.debug("logUserIn: finally block")
        S.redirectTo(RequestedURL.openOr("/"))
        RequestedURL(Empty)
        //}
      }

    }

    "#emailAddress *" #> (FocusOnLoad(
        <input type="text" size="24" name="emailAddress"/>)) &
      "#password *" #> <input type="password" name="password" size="16"/> &
      "#submit" #> SHtml.submit(S.?("log.in"), logUserIn)

    //    "#emailAddress *" #> (FocusOnLoad(
    //        <input id="login_form_email_address" type="text" size="24" name="emailAddress" value=" "/>)) &
    //      "#password *" #> <input id="login_form_password" type="password" name="password" size="16" value=" "/> &
    //      "#submit" #> SHtml.submit(S.?("log.in"), logUserIn)

  }

  def hashx(in: String): String = {
    //new String((new Base64) encode (MessageDigest.getInstance("SHA")).digest(in.getBytes("UTF-8")))
    println (" 1 ")
    val aaa: Array[Byte] = (MessageDigest.getInstance("SHA")).digest(in.getBytes("UTF-8"))
    println(" 2 |" + aaa.toString + "|")
    val bbb: String = new String((new Base64) encode aaa)
    println(" 3 |" + bbb + "|")
    bbb
    //new String((new Base64) encode (MessageDigest.getInstance("SHA")).digest(in.getBytes("UTF-8")))
  }


  def addendup = {
    log.debug("addendup -- S.param('code')= |" + S.param("code").openOr("") + "|")
    log.debug("addendup -- S.getSessionAttribute('page')= |" + S.getSessionAttribute("page") + "|")
    S.setSessionAttribute("page", S.param("code").openOr(""))

    RequestedURL(Empty)
    Model.createNamedQuery[User]("findUserByValidationCode",
      "code" -> S.getSessionAttribute("page").openOr("")).findOne match {
      case Some(user) =>
        if (user.validationExpiry > System.currentTimeMillis()) {
          user.validationCode = null
          user.validationExpiry = 0
          user.validated = true
          Model.mergeAndFlush(user)
          CurrentUser(Empty) // CurrentUser(Full(user))
          CurrentUserId(Empty) // CurrentUserId(Full(user.id))
          S.notice(S.?("success.user.create"))
          S.redirectTo("/infoPage", () => {
            InfoXmlMsg.set(Full(LongMsgs.getMsg("endup.validation")))
            log.debug("InfoXmlMsg: " + InfoXmlMsg.is/*.open_!*/.toString)
          })
        } else {
          S.warning(S ? "psw.validation.expired")
          //RewriteResponse(ParsePath(List("login", "lostPassword"), "", true, false), Map(), true)
          S.redirectTo("/login/lostPassword")
        }
      case None =>
        S.redirectTo("/errorPage", () => {
          ErrorXmlMsg.set(Some(Map(
            "location" -> <p>LoginOps.addendup</p>,
            "message" -> <p>No User found via 'findUserByValidationCode' for = |{S.getSessionAttribute("page").openOr("")}|</p>)))
          S.unsetSessionAttribute("page")
          //"message" -> <p>No User found via 'findUserByValidationCode' for = |{S.param("code").openOr("")}|</p>)))
        })
    }
    "#invisible" #> <p></p>
  }


  def lostPassword = {

    def resetPassword() = {
      Model.createNamedQuery[User]("findUserByEmailAddress",
        "emailAddress" -> S.param("emailAddress").openOr("")).findOne match {
        case Some(user) =>
          S.notice(S.?("instruct.by.mail"))
          SendMail.sendMail2User("password-reset", user)
        case None =>
          S.error(S ? "unable.find.email") // S.error("Unable to find your email address.")
      }
      S.redirectTo("/login/login")
    }

    "#lostPasswordMsg" #> (LongMsgs.getMsg("lostPassword.msg")) &
      "#emailAddress *" #> (FocusOnLoad(<input type="text" size="16" name="emailAddress" />)) &
      "#submit" #> SHtml.submit(S.?("reset.password"), resetPassword)

//    "#lostPasswordMsg" #> (LongMsgs.getMsg("lostPassword.msg")) &
//      "#emailAddress *" #> (FocusOnLoad(
//          <input id="login_form_email_address" type="text" size="24" name="emailAddress" value=" "/>)) &
//      "#submit" #> SHtml.submit(S.?("reset.password"), resetPassword)
  }


  def changePassword = {
    log.debug("changePassword -- S.param('code')= |" + S.param("code").openOr("") + "|")
    S.setSessionAttribute("page", S.param("code").openOr(""))

    def updatePassword() = {
      RequestedURL(Empty) // RequestedURL(Full("/login/login"))
      log.debug("changePassword: password confirmation " + S.param("password") + " " + S.param("confirmation"))
      if (S.param("password") != S.param("confirmation")) {
        log.error("changePassword: " + S.?("typed.psws.are.unequal"))
        S.error(S.?("typed.psws.are.unequal"))
      } else {
        // TODO AB29-1/vsh find more restrictive pattern for password
        //val ptrn = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).{8,}"
        //val ptrn = ".{6,}"
        val ptrn = Props.get("regex.password").openOr(".")
        val quality = Pattern.compile(ptrn)
        val password = S.param("password").openOr("")

        if (!(quality.matcher(password).matches)) {
          val msg = S.?("bad.password.quality")
          log.error("changePassword: " + msg)
          S.error(msg)
        } else {
          /*try {*/
          Model.createNamedQuery[User]("findUserByValidationCode",
            "code" -> S.getSessionAttribute("page").openOr("")).findOne match {
            case Some(user) =>
              if (user.validationExpiry > System.currentTimeMillis()) {
                S.param("password").map(user.password = _)
                user.validationCode = null
                user.validationExpiry = 0
                user.validated = true
                Model.merge(user)
                CurrentUser(Empty) // CurrentUser(Full(user))
                CurrentUserId(Empty) // CurrentUserId(Full(user.id))
                S.notice(S.?("success.psw.change"))
                //S.notice("You have successfully changed your password and are now logged in.")
                log.info(<_>User {user.emailAddress} has successfully changed password</_>.text)
                SendMail.sendMail2User("change-password", user)
                S.redirectTo("/")
              } else {
                S.warning(S.?("psw.validation.expired"))
                S.redirectTo("/login/lostPassword")
              }
            case None =>
              S.redirectTo("/errorPage", () => {
                ErrorXmlMsg.set(Some(Map(
                  "location" -> <p>LoginOps.changePassword</p>,
                  "message" -> <p>No User found via 'findUserByValidationCode' for = |{S.getSessionAttribute("page").openOr("")}|</p>)))
                //              "message" -> <p>No User found via 'findUserByValidationCode' for = |{S.param("code").openOr("")}|</p>)))
              })
          }

          /*catch {
            case x: NoResultException =>
              log.error("changePassword: NoResultException: " + x.toString)
              S.error("That validation code has expired.")
              S.warning(S ? "psw.validation.expired") //S.warning("That validation code has expired.")
              S.redirectTo("/login/resetPassword")
            case y: Exception =>
              log.error("changePassword: Exception: " + y.toString)
              println(y)
              S.redirectTo("/")
          } //finally { } */
        }
      }
    }

    "#password" #> (FocusOnLoad(<input type="password" name="password" size="16"/>)) &
      "#confirmation" #> (<input type="password" name="confirmation" size="16"/>) &
      "#submit" #> SHtml.submit(S.?("save.changes"), updatePassword)

//    "#password" #> (FocusOnLoad(<input id="password" type="password" name="password" size="24" value=" "/>)) &
//      "#confirmation" #> (<input id="confirmation" type="password" name="confirmation" size="24" value=" "/>) &
//      "#submit" #> SHtml.submit(S.?("save.changes"), updatePassword)
  }

}


object UserAdd extends UberScreen {
  // ab26-5/vsh:
  // http://lift.la/lifts-screen
  // http://scala-programming-language.1934581.n4.nabble.com/JPA-find-or-create-new-td1966598.html
  val log = Logger("UserAdd");
  log.debug("[]...")

  //object userVar extends RequestVar[Box[User]](Empty)

  val firstName = field(S ? "firstName", "Ad", notNull, trim,
    valMinLen(2, S ? "flName.too.short"),
    valMaxLen(40, S ? "flName.too.long")
  )

  val lastName = field(S ? "lastName", "Minas", notNull, trim,
    valMinLen(2, S ? "flName.too.short"),
    valMaxLen(40, S ? "flName.too.long")
  )

  val yyyymmddPattern = Pattern.compile("^(19|20)\\d\\d\\-(0[1-9]|1[012])\\-(0[1-9]|[12][0-9]|3[01])$")
  val birthDate = field((S ? "birthdate") + " format yyyy-mm-dd ", "1970-01-01", notNull, trim,
    valRegex(yyyymmddPattern, S ? "wrong.bdate.format")
  )

  val plainPsw = password(S ? "plainPsw", "sanimda", notNull, trim,
    valRegex(Pattern.compile(Props.get("regex.password").openOr(".")), S ? "bad.password.quality"))

  val plainPsw2 = password(S ? "plainPsw2", "sanimda", notNull, trim,
    valRegex(Pattern.compile(Props.get("regex.password").openOr(".")), S ? "bad.password.quality"))

  val emailAddress = field(S ? "emailAddress", "mailbox@somemail.xx", notNull, trim,
    valRegex(MappedEmail.emailPattern, S ? "wrong.email.format")
  )

  val visosLokales = (java.util.Locale.getAvailableLocales.
    toList.sortWith(_.getDisplayName < _.getDisplayName).
    map(lo => (lo.toString, lo.getDisplayName))).toSeq
  val lokale = select[String]("Lokalė", "lt", visosLokales)

  val tinezona = select[String]("Timezone", "Europe/Vilnius", MappedTimeZone.timeZoneList.toSeq)

  //def helpAsHtml = Full(<p>Čia bus Helpas</p>)

  override def validations = passwordComparison _ :: super.validations

  def passwordComparison(): Errors = {
    if (plainPsw.is != plainPsw2.is)
      S ? "typed.psws.are.unequal"
    else Nil
  }

// B301-2  override protected def redirectBack() {
//    S.seeOther("/")
//  }

  override def finish() {
    S.notice("firstName " + firstName.is)
    S.notice("firstName " + lastName.is)
    S.notice("plainPsw " + plainPsw.is)
    S.notice("emailAddress " + emailAddress.is)
    S.notice("lokale " + lokale.is.openOr("lt") /*.open_!*/)
    S.notice("tinezona " + tinezona.is.openOr("Europe/Vilnius") /*.open_!*/)
    val newuser = new User
    newuser.firstName = firstName.is
    newuser.lastName = lastName.is
    newuser.birthDate = new SimpleDateFormat("yyyy-MM-dd").parse(birthDate.is, new ParsePosition(0))
    newuser.locale = lokale.is.openOr("lt") //.toString/*.open_!*/
    newuser.timezone = tinezona.is.openOr("Europe/Vilnius") //toString/*.open_!*/
    newuser.emailAddress = emailAddress.is
    newuser.password_=(plainPsw.is) // !!! the "password_=" method generates passwordSalt, passwordHash, uniqueid
    newuser.setValidation
// TODO B417-7/vsh  check for email uniqueness and maybe firstName, firstName
    Model.mergeAndFlush(newuser)
    SendMail.sendMail2User("new-user", newuser)
    //S.setSessionAttribute("appInfo", LongMsgs.getMsg("after.add.user"))

    S.redirectTo("/infoPage", () => {
      InfoXmlMsg.set(Full(LongMsgs.getMsg("after.add.user")))
    })
  }
}


object UserEdit extends UberScreen {
  // AC12-7/vsh: made from UserAdd
  // http://lift.la/lifts-screen
  // http://scala-programming-language.1934581.n4.nabble.com/JPA-find-or-create-new-td1966598.html
  val log = Logger("UserEdit");
  log.debug("[]...")

  val user: User = Model.find(classOf[User], CurrentUserId.is/*.open_!*/).get
  val firstName = field(S ? "firstName", user.firstName,
    notNull, trim,
    valMinLen(2, S ? "flName.too.short"),
    valMaxLen(40, S ? "flName.too.long")
  )

  val lastName = field(S ? "lastName", user.lastName,
    notNull, trim,
    valMinLen(2, S ? "flName.too.short"),
    valMaxLen(40, S ? "flName.too.long")
  )

  val yyyymmddPattern = Pattern.compile("^(19|20)\\d\\d\\-(0[1-9]|1[012])\\-(0[1-9]|[12][0-9]|3[01])$")
  val birthDate = field((S ? "birthdate") + " format yyyy-mm-dd ",
    new SimpleDateFormat("yyyy-MM-dd").format(user.birthDate),
    notNull, trim,
    valRegex(yyyymmddPattern, S ? "wrong.bdate.format")
  )

  val emailAddress = field(S ? "emailAddress", user.emailAddress,
    notNull, trim,
    valRegex(MappedEmail.emailPattern, S ? "wrong.email.format")
  )

  val visosLokales = (java.util.Locale.getAvailableLocales.
    toList.sortWith(_.getDisplayName < _.getDisplayName).
    map(lo => (lo.toString, lo.getDisplayName))).toSeq
  val lokale = select[String]("Lokalė", user.locale, visosLokales)

  val tinezona = select[String]("Timezone", user.timezone, MappedTimeZone.timeZoneList.toSeq)

  //def helpAsHtml = Full(<p>Čia bus Helpas</p>)

  override def validations = /*passwordComparison _ ::*/ super.validations

// B301-2  override protected def redirectBack() {
//    S.seeOther("/")
//  }

  override def finish() {
    S.notice("firstName " + firstName.is)
    S.notice("lastName " + lastName.is)
    S.notice("emailAddress " + emailAddress.is)
    S.notice("lokale " + lokale.is/*.open_!*/)
    S.notice("tinezona " + tinezona.is/*.open_!*/)
    user.firstName = firstName.is
    log.debug("UserEdit " + user.firstName)
    user.lastName = lastName.is
    user.birthDate = new SimpleDateFormat("yyyy-MM-dd").parse(birthDate.is, new ParsePosition(0))
    user.locale = lokale.is.openOr("lt")
    user.timezone = tinezona.is.openOr("Europe/Vilnius")
    user.emailAddress = emailAddress.is
    Model.mergeAndFlush(user)
    S.redirectTo(RequestedURL.openOr("/"))
  }
}


object SendMail /*extends UberScreen*/ {
  // B204-5/vsh init
  // google-group:Lift: [how do you set system properties in lift]
  // http://java.sun.com/products/javamail/javadocs/com/sun/mail/smtp/package-summary.html
  val log = Logger("SendMail")
  //  log.debug("[]...")

  def sendMail2User(reason: java.lang.String, user: User): Unit = {
    //log.debug("sendMail hostFunc |" + Props.get("admin.mailhost").open_! + "|")
    //log.debug("sendMail admin.mail |" + Props.get("admin.mail")/*.open_!*/ + "|")
    log.debug("sendMail Mailer.properties.toString |" + Mailer.properties.toString + "|")

    /*var props = Map[String, String](
      "mail.smtp.host" -> Props.get("mail.smtp.host").open_!,
      "mail.smtp.port" -> Props.get("mail.smtp.port").open_!,
      "mail.smtp.starttls.enable" -> Props.get("mail.smtp.starttls.enable").open_!,
      "mail.smtp.auth" -> Props.get("mail.smtp.auth").open_!
    )
    props = Map[String, String](
      "mail.smtp.host" -> "smtp.gmail.com",
      "mail.smtp.port" ->  "587",
      "mail.smtp.auth" -> "true",
      "mail.smtp.starttls.enable" -> "true"
    )*/

//    //Mailer.customProperties = props
//    /*    Mailer.authenticator = Full(new Authenticator() {
//          override def getPasswordAuthentication = new
//              PasswordAuthentication("vsh@node.lt", "av...a")
//        })
//    */
//    var isAuth = Props.get("mail.smtp.auth", "false").toBoolean
//
//    Mailer.customProperties = Props.get("mail.smtp.host", "localhost") match {
//      case "smtp.gmail.com" =>
//        log.debug("sendMail mail.smtp.host |" + Props.get("mail.smtp.host", "localhost") + "|")
//        isAuth = true
//        Map(
//          "mail.transport.protocol" -> "smtp",
//          "mail.smtp.host" -> "smtp.gmail.com",
//          "mail.smtp.port" -> "587",
//          //"mail.smtp.port" -> "465",
//          "mail.smtp.auth" -> "true",
//          "mail.smtp.starttls.enable" -> "true")
//      case host =>
//        log.debug("sendMail mail.smtp.host |" + Props.get("mail.smtp.host", "localhost") + "|")
//        Map(
//        "mail.smtp.host" -> host,
//        "mail.smtp.port" -> Props.get("mail.smtp.port", "25"),
//        "mail.smtp.auth" -> isAuth.toString
//      )
//    }
//
//    if (isAuth) {
//      //(Props.get("mail.user"), Props.get("mail.password")) match {
//      //(Full("vytasab"), Full("paratunka")) match {
//      (Full("vytasab__@gmail.com"), Full("paratunka__")) match {
//        case (Full(username), Full(password)) =>
//          Mailer.authenticator = Full(new Authenticator() {
//            override def getPasswordAuthentication = new
//                PasswordAuthentication(username, password)
//          }
////          /*Mailer.authenticator = Full(new Authenticator() {
////            override def getPasswordAuthentication = new
////                PasswordAuthentication(username, password)
////          }*/
////          authenticator = new Authenticator()
//////          {
//////            override def getPasswordAuthentication = new
//////                PasswordAuthentication(username, password)
//////          }
////          Full(authenticator)
//          )
//        case _ => new Exception("Username/password not supplied for Mailer.")
//      }
//    }


    log.debug("sendMail reason |" + reason + "|")
    val getHostPortApp = Props.get("host__").openOr("localhost") +
      Props.get("_port_").openOr(":8080") + Props.get("__app").openOr("/gedcom-web/")
    log.debug("sendMail getHostPortApp |" + getHostPortApp + "|")
    reason match {
    case "test" =>
    case _ =>
      log.debug("sendMail user.emailAddress |" + user.emailAddress + "|")
  }

    reason match {
      case "new-user" => {
        log.debug("-----|new-user|")
        //val code = "http://" + Props.get("host.port.app").openOr("localhost:8080/gedcom-web/")
        val code = "http://" + getHostPortApp/*Props.get("host.port.app").openOr("localhost:8080/gedcom-web/")*/ +
          "addendup/" + user.validationCode

        /*Thread.currentThread().setContextClassLoader(getClass().getClassLoader())
        // https://groups.google.com/forum/?fromgroups=#!topic/liftweb/FW6E0MSEYzs*/

        Mailer.blockingSendMail/*sendMail*/(
          From(Props.get("admin.mail").openOr("vytasab@gmail.com")),
          Subject(LongMsgs.getMsgText("new.user.subj")),
          List(
            xmlToMailBodyType( //XHTMLMailBodyType(
              <html>
                <head>
                  <title>LongMsgs.getMsgText("new.user.subj")</title>
                </head>
                <body>
                  { /*Unparsed*/ (LongMsgs.getMsg("new.user.p1"))}
                  <p>
                    <a href={code}>{code}</a>
                  </p>
                  { /*Unparsed*/ (LongMsgs.getMsg("new.user.p2"))}
                  <p>
                    { /*new Date(user.validationExpiry)*/ }
                    {new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(user.validationExpiry))}
                  </p>
                </body>
              </html>),
              To(/*"dalia.sabaniene@gmail.com"*/user.emailAddress)
          ): _*
        )
      }

      case "change-password" => {
        Mailer.sendMail(
          From(<_>{Props.get("admin.mail").openOr("admin.mail")}</_>.text),
          Subject(LongMsgs.getMsgText("change.password.subj")),
          List(
            To(user.emailAddress),
            xmlToMailBodyType(
              <html>
                <head>
                  <!--title>Your credentials have been changed.</title-->
                  <title>LongMsgs.getMsgText(change.password.subj")</title>
                </head>
                <body>
                  <p>
                    { /*Unparsed*/ (LongMsgs.getMsg("change.password.p1"))}
                    <span>{Props.get("admin.mail").openOr("admin.mail")}</span>
                  </p>
                </body>
              </html>)
          ): _*
        )
      }

      case "password-reset" => {
        val code = "http://" + getHostPortApp + "validation/" + user.setValidation
        Model.mergeAndFlush(user)
        Mailer.sendMail(
          From(<_>{Props.get("admin.mail").openOr("admin.mail")}</_>.text),
          Subject(LongMsgs.getMsgText("password.reset.subj")),
          List(
            To(user.emailAddress),
            xmlToMailBodyType(<html>
              <head>
                <title>LongMsgs.getMsgText("password.reset.subj")</title>
              </head>
              <body>
                <p>
                  { /*Unparsed*/ (LongMsgs.getMsg("password.reset.p1"))}
                  {getHostPortApp/*Props.get("host.port.app").openOr("localhost:8080/gedcom-web/")*/}
                  { /*Unparsed*/ (LongMsgs.getMsg("password.reset.p2"))}
                </p>
                <p>
                  <a href={code}>{code}</a>
                </p>
                { /*Unparsed*/ (LongMsgs.getMsg("new.user.p2"))}
                <p>
                  { /*new Date(user.validationExpiry)*/ }
                  {new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(user.validationExpiry))}
                </p>
              </body>
            </html>)
          ): _*
        )
      }

      case "test" =>
        //val code = "http://" + getHostPortApp /*Props.get("host.port.app").openOr("localhost:8080/gedcom-web/")*/ + "validation/" + "aaAAbbBBccCCddDD"
        /*Mailer.sendMail(
          From("vytasab@gmail.com"),
          Subject("Text!!!"),
          List(
            To("vytasab@gmail.com"),
            PlainMailBodyType("Woo! I can text :-)") ): _*)
        */
        Mailer./*blockingSendMail*/sendMail(
            //From(<_>{Props.get("admin.mail").openOr("admin.mail")}</_>.text),
            From("vytasab@gmail.com"),
            //Subject("Test message: " + LongMsgs.getMsgText("password.reset.subj")),
            Subject("Text!!!"),
            List(
              //To(user.emailAddress),
              To("vytasab@gmail.com"),
              //To("dalia.sabaniene@gmail.com"),
              PlainMailBodyType("Woo! I can text :-)")
              /*xmlToMailBodyType(
                <html>
                  <head>
                    <title>Test message / Tiesiog žinutė</title>
                  </head>
                  <body>
                    <p>Testas, panaudota: password-reset</p>
                    <p>Test message for  {Props.get("admin.mail")/*.open_!*/.toString}.</p>
                    <p>
                      { /*Unparsed*/ (LongMsgs.getMsg("password.reset.p1"))}
                      {getHostPortApp/*Props.get("host.port.app").openOr("localhost:8080/gedcom-web/")*/}
                      { /*Unparsed*/ (LongMsgs.getMsg("password.reset.p2"))}
                    </p>
                    <p>
                      <a href={code}>{code}</a>
                    </p>
                    { /*Unparsed*/ (LongMsgs.getMsg("new.user.p2"))}
                    <p>
                      {new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date())}
                    </p>
                  </body>
                </html>)*/
            ): _*
          )

      case _ =>
    }
  }
}