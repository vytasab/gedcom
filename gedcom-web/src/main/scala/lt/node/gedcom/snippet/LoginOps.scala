package lt.node.gedcom.snippet

import _root_.java.util.regex.Pattern
import _root_.java.text.{ParsePosition, SimpleDateFormat}
import _root_.javax.persistence.NoResultException
import _root_.java.security.MessageDigest
import org.jasypt.contrib.org.apache.commons.codec_1_3.binary.Base64

import _root_.bootstrap.liftweb._
import _root_.net.liftweb._
import common.{Logger, Empty, Full}
import http.js.JsCmds.FocusOnLoad
import http.CurrentReq
import util._
import mapper._
import util.Helpers._
import http.{SHtml, S}

import _root_.lt.node.gedcom._
import model.{Model, User, Person}
import _root_.lt.node.gedcom.util.{UberScreen, LongMsgs}


class LoginOps {

  val log = Logger("LoginOps")
  //object userVar extends RequestVar[Box[User]](Empty)
  val reqScheme = CurrentReq.value.request.scheme

  def login = {

    def logUserIn() = {
      try {
        //log.debug("logUserIn: CurrentReq.value.request.params |" + CurrentReq.value.request.params.toList.toString() + "|")
        // --> DEBUG LoginOps - logUserIn: CurrentReq.value.request.params |
        // List(HTTPParam(emailAddress,List(vytasab@gmail.com)),
        // HTTPParam(password,List(1...3)),
        // HTTPParam(F406902577514FHAZSK,List(Prisijungti)))|
        log.debug("logUserIn: CurrentReq.value.request.scheme |" + reqScheme + "|")
        log.debug("logUserIn: Props.fileName |" + Props.fileName + "|")
        log.debug("logUserIn: Props.propFileName |" + Props.propFileName + "|")
        log.debug("logUserIn: Props.modeName |" + Props.modeName + "|")
        log.debug("logUserIn: Props.mode |" + Props.mode.toString + "|")
        RequestedURL(Empty)
        Model.createNamedQuery[User]("findUserValidatedByEmailAddress",
          "emailAddress" -> S.param("emailAddress").openOr("")).findOne match {
          case Some(user) =>
            //log.debug("logUserIn: rasta db; password=" + S.param("password").openOr(""))
            //log.debug("logUserIn: user.passwordHash=" +  user.passwordHash )
            //log.debug("logUserIn: user.passwordSalt=" +  user.passwordSalt )
            //log.debug("logUserIn: hash(pw)=" + hashx(S.param("password").openOr("")))
            //log.debug("logUserIn: hash(pw + this.passwordSalt)=" + hashx(S.param("password").openOr("") + user.passwordSalt))

            if (user.authenticate(S.param("password").openOr(""))) {
              log.debug("logUserIn: user is authenticated")
              CurrentUser.set(Full(user))
              CurrentUserId.set(Full(user.id))
              val persons: List[Person] = Model.createNamedQuery[Person]("findPersonByGivnSurn",
                "nameGivn" -> user.firstName, "nameSurn" -> user.lastName).findAll.toList
              log.debug("logUserIn:  persons.size=" + persons.size)
              persons match {
                case x :: Nil => // go to canvas
                  log.debug("logUserIn:  case x :: Nil")
                  RequestedURL(Full(<_>/rest/person/
                    {x.id}
                  </_>.text))
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
    println(" 1 ")
    val aaa: Array[Byte] = (MessageDigest.getInstance("SHA")).digest(in.getBytes("UTF-8"))
    println(" 2 |" + aaa.toString + "|")
    val bbb: String = new String((new Base64) encode aaa)
    println(" 3 |" + bbb + "|")
    bbb
    //new String((new Base64) encode (MessageDigest.getInstance("SHA")).digest(in.getBytes("UTF-8")))
  }


  def addendup = {
    log.debug("addendup -- S.param('code')= |" + S.param("code").openOr("") + "|")
    //log.debug("addendup -- S.getSessionAttribute('page')= |" + S.getSessionAttribute("page") + "|")
    //S.setSessionAttribute("page", S.param("code").openOr(""))
    val aCode = S.param("code").openOr("")

    //    val validationCode = aCode.substring(2)
    aCode.substring(0, 2) match {
      case "1_" => // admin has approved add/edit rights
        RequestedURL(Empty)
        Model.createNamedQuery[User]("findUserByValidationCode", "code" -> aCode.substring(2)).findOne match {
          case Some(user) =>
            if (user.validationExpiry > System.currentTimeMillis()) {
              log.info("Approve new user: " + user.firstName + " " + user.lastName + "email: " + user.emailAddress)
              user.validationCode = null
              user.validationExpiry = 0
              user.validated = true
              Model.mergeAndFlush(user)
              CurrentUser(Empty) // CurrentUser(Full(user))
              CurrentUserId(Empty) // CurrentUserId(Full(user.id))
              SendMail.sendMail2User("user-approval", user)
              S.notice(S.?("success.user.create"))
              val aMsg = "%s %s %s: %s".format(user.firstName, user.lastName, user.emailAddress, LongMsgs.getMsgText("allow"))
              S.redirectTo("/infoPage", () => {
                InfoXmlMsg.set(Full(<span>
                  {aMsg}
                </span>))
              })
              log.debug("InfoXmlMsg: " + aMsg)
            } else {
              log.warn("Approval is failed for new user: " + user.firstName + " " + user.lastName + "email: " + user.emailAddress)
              SendMail.sendMail2User("user-approval-failed", user)
              //TODO  turbūt 'else' nereikia redirectinimų
              S.warning(S ? "psw.validation.expired")
              S.redirectTo("/infoPage", () => {
                ErrorXmlMsg.set(Some(Map(
                  "location" -> <p>LoginOps.addendup</p>,
                  "message" -> <p>
                    {S ? "psw.validation.expired"}
                  </p>)))
                //S.unsetSessionAttribute("page")
              })
            }
          case None =>
            log.warn("Approve: No User found via 'findUserByValidationCode' for = |" + aCode + "|")
            //TODO  turbūt nereikia redirectinimų
            S.redirectTo("/errorPage", () => {
              ErrorXmlMsg.set(Some(Map(
                "location" -> <p>LoginOps.addendup</p>,
                "message" -> <p>No User found via 'findUserByValidationCode' for = |
                  {aCode}
                  |</p>)))
              //S.unsetSessionAttribute("page")
            })
        }
      case "0_" => // admin hasn't approoved add/edit rights
        RequestedURL(Empty)
        Model.createNamedQuery[User]("findUserByValidationCode", "code" -> aCode.substring(2)).findOne match {
          case Some(user) =>
            log.info("Reject new user: " + user.firstName + " " + user.lastName + "email: " + user.emailAddress)
            /*user.validationCode = null
            user.validationExpiry = 0
            user.validated = false
            Model.mergeAndFlush(user)*/
            Model.removeAndFlush(user)
            CurrentUser(Empty) // CurrentUser(Full(user))
            CurrentUserId(Empty) // CurrentUserId(Full(user.id))
            SendMail.sendMail2User("user-rejection", user)
            S.notice(S ? "refuse")
            val aMsg = "%s %s %s: %s".format(user.firstName, user.lastName, user.emailAddress, LongMsgs.getMsgText("refuse"))
            S.redirectTo("/infoPage", () => {
              InfoXmlMsg.set(Full(<span>
                {aMsg}
              </span>))
            })
            log.debug("InfoXmlMsg: " + aMsg)
          case None =>
            log.warn("Reject: No User found via 'findUserByValidationCode' for = |" + aCode + "|")
          //S.redirectTo("/errorPage", () => {
          //  ErrorXmlMsg.set(Some(Map(
          //    "location" -> <p>LoginOps.addendup</p>,
          //    "message" -> <p>No User found via 'findUserByValidationCode' for = |{S.getSessionAttribute("page").openOr("")}|</p>)))
          //  S.unsetSessionAttribute("page")
          //  //"message" -> <p>No User found via 'findUserByValidationCode' for = |{S.param("code").openOr("")}|</p>)))
          //})
        }
      case _ => // send email to admin
        RequestedURL(Empty)
        Model.createNamedQuery[User]("findUserByValidationCode", "code" -> aCode).findOne match {
          case Some(user) =>
            if (user.validationExpiry > System.currentTimeMillis()) {
              SendMail.sendMail2User("set-user-approval", user)
              /*user.validationCode = null
              user.validationExpiry = 0
              user.validated = true
              Model.mergeAndFlush(user)*/
              CurrentUser(Empty) // CurrentUser(Full(user))
              CurrentUserId(Empty) // CurrentUserId(Full(user.id))
              //S.notice(S.?("success.user.create"))
              S.redirectTo("/infoPage", () => {
                InfoXmlMsg.set(Full(LongMsgs.getMsg("endup.validation")))
                log.debug("InfoXmlMsg: " + InfoXmlMsg.is.toString)
              })
            } else {
              S.warning(S ? "psw.validation.expired")
              //   //RewriteResponse(ParsePath(List("login", "lostPassword"), "", true, false), Map(), true)
              // ? S.redirectTo("/login/lostPassword")
              // ToDo_ nebaigta
              S.redirectTo("/errorPage", () => {
                ErrorXmlMsg.set(Some(Map(
                  "location" -> <p>LoginOps.addendup</p>,
                  "message" -> <p>
                    {S ? "psw.validation.expired"}
                  </p>)))
                //S.unsetSessionAttribute("page")
              })
              /*S.redirectTo("/infoPage", () => {
                InfoXmlMsg.set(Full(LongMsgs.getMsg("endup.validation")))
                log.debug("InfoXmlMsg: " + InfoXmlMsg.is.openTheBox.toString)
              })*/
            }
          case None =>
            S.redirectTo("/errorPage", () => {
              ErrorXmlMsg.set(Some(Map(
                "location" -> <p>LoginOps.addendup</p>,
                "message" -> <p>No User found via 'findUserByValidationCode' for = |
                  {aCode}
                  |</p>)))
              //S.unsetSessionAttribute("page")
            })
        }
    }
    "#invisible" #> <p></p>
  }


  def lostPassword = {
    log.debug("[lostPassword]... AccessControl.isAuthenticated_?(): " + AccessControl.isAuthenticated_?())
    log.debug("[lostPassword]... CurrentUser.is.isDefined: " + CurrentUser.is.isDefined)
    log.debug("[lostPassword]... CurrentUser.is.toString: " + CurrentUser.is.toString)

    def resetPassword() = {
      Model.createNamedQuery[User]("findUserValidatedByEmailAddress",
        "emailAddress" -> S.param("emailAddress").openOr("")).findOne match {
        case Some(user) =>
          S.notice(S.?("instruct.by.mail"))
          SendMail.sendMail2User("password-reset", user)
        case None =>
          S.error(S ? "unable.find.email") // S.error("Unable to find your email address.")
          val aMsg = "%s: %s".format(S.param("emailAddress").openOr(""), S.?("unable.find.email"))
          log.warn("InfoXmlMsg: " + aMsg)
          S.redirectTo("/infoPage", () => {
            InfoXmlMsg.set(Full(<span>
              {aMsg}
            </span>))
          })
      }
      //S.redirectTo("/login/login")
      val aMsg = "%s: %s".format(S.param("emailAddress").openOr(""), S.?("instruct.by.mail"))
      log.info("InfoXmlMsg: " + aMsg)
      S.redirectTo("/infoPage", () => {
        InfoXmlMsg.set(Full(<span>
          {aMsg}
        </span>))
      })
    }

    "#lostPasswordMsg" #> (LongMsgs.getMsg("lostPassword.msg")) &
      "#emailAddress *" #> (FocusOnLoad(<input type="text" size="16" name="emailAddress"/>)) &
      "#submit" #> SHtml.submit(S.?("reset.password"), resetPassword)
    /*"#lostPasswordMsg" #> (LongMsgs.getMsg("lostPassword.msg")) &
      "#emailAddress *" #> (FocusOnLoad(<input id="login_form_email_address" type="text" size="24" name="emailAddress" value=" "/>)) &
      "#submit" #> SHtml.submit(S.?("reset.password"), resetPassword)*/
  }


  //  def changePassword = {
  //    //log.debug("[changePassword]... ")
  //    log.debug("[changePassword]... AccessControl.isAuthenticated_?(): " + AccessControl.isAuthenticated_?())
  //    log.debug("[changePassword]... CurrentUser.is.isDefined: " + CurrentUser.is.isDefined)
  //    log.debug("[changePassword]... CurrentUser.is.toString: " + CurrentUser.is.toString)
  //    log.debug("changePassword  S.param('code')= |" + S.param("code").openOr("") + "|")
  //    S.setSessionAttribute("page", S.param("code").openOr(""))
  //    val aCode = S.param("code").openOr("")
  //
  //    def updatePassword() = {
  //      RequestedURL(Empty) // RequestedURL(Full("/login/login"))
  //      log.debug("changePassword: password confirmation " + S.param("password") + " " + S.param("confirmation"))
  //      if (S.param("password") != S.param("confirmation")) {
  //        log.error("changePassword: " + S.?("typed.psws.are.unequal"))
  //        S.error(S.?("typed.psws.are.unequal"))
  //        // ToDo  DC01-7/vsh  nulūžta po pakartotinio laukų įvedimo
  //        //val aMsg = "%s %s %s: %s".format(user.firstName, user.lastName, user.emailAddress, S.?("success.psw.change"))
  //        //log.info("InfoXmlMsg: " + aMsg)
  //        //S.redirectTo("/infoPage", () => {InfoXmlMsg.set(Full( <span>{aMsg}</span>))})
  //
  //      } else {
  //        // TODO AB29-1/vsh find more restrictive pattern for password
  //        val ptrn = Props.get("regex.password").openOr(".{6,}")
  //        val quality = Pattern.compile(ptrn)
  //        val password = S.param("password").openOr("")
  //
  //        if (!(quality.matcher(password).matches)) {
  //          val msg = S.?("bad.password.quality")
  //          log.error("changePassword: " + msg)
  //          S.error(msg)
  //        } else {
  //          /*try {*/
  //          log.debug("[changePassword] AccessControl.isAuthenticated_?(): " + AccessControl.isAuthenticated_?())
  //          log.debug("[changePassword] CurrentUser.is.isDefined: " + CurrentUser.is.isDefined)
  //          log.debug("[changePassword] CurrentUser.is.toString: " + CurrentUser.is.toString)
  //          CurrentUser.is match {
  //            case Full(user) =>
  //              user.password = password    //  too wise way :)  //S.param("password").map(user.password = _)
  //              user.validationCode = null
  //              user.validationExpiry = 0
  //              user.validated = true
  //              Model.mergeAndFlush(user)
  //              CurrentUser(Empty)
  //              CurrentUserId(Empty)
  //              S.notice(S.?("success.psw.change"))
  //              //S.notice("You have successfully changed your password and are now logged in.")
  //              //log.info(<_>User {user.emailAddress} has successfully changed password</_>.text)
  //              SendMail.sendMail2User("change-password", user)
  //              //S.redirectTo("/")
  //              val aMsg = "%s %s %s: %s".format(user.firstName, user.lastName, user.emailAddress, S.?("success.psw.change"))
  //              log.info("InfoXmlMsg: " + aMsg)
  //              S.redirectTo("/infoPage", () => {InfoXmlMsg.set(Full( <span>{aMsg}</span>))})
  //
  //            case Empty =>
  //              Model.createNamedQuery[User]("findUserByValidationCode", "code" -> aCode).findOne match {
  //                case Some(user) =>
  //                  if (user.validationExpiry > System.currentTimeMillis()) {
  //                    user.password = password    //  too wise way :)  //S.param("password").map(user.password = _)
  //                    user.validationCode = null
  //                    user.validationExpiry = 0
  //                    user.validated = true
  //                    Model.mergeAndFlush(user)
  //                    CurrentUser(Empty)
  //                    CurrentUserId(Empty)
  //                    S.notice(S.?("success.psw.change"))
  //                    //S.notice("You have successfully changed your password and are now logged in.")
  //                    //log.info(<_>User {user.emailAddress} has successfully changed password</_>.text)
  //                    SendMail.sendMail2User("change-password", user)
  //                    //S.redirectTo("/")
  //                    val aMsg = "%s %s %s: %s".format(user.firstName, user.lastName, user.emailAddress, S.?("success.psw.change"))
  //                    log.info("InfoXmlMsg: " + aMsg)
  //                    S.redirectTo("/infoPage", () => {InfoXmlMsg.set(Full( <span>{aMsg}</span>))})
  //                  } else {
  //                    S.warning(S.?("psw.validation.expired"))
  //                    //S.redirectTo("/login/lostPassword")
  //                    val aMsg = "%s %s %s: %s".format(user.firstName, user.lastName, user.emailAddress, S.?("psw.validation.expired"))
  //                    log.info("InfoXmlMsg: " + aMsg)
  //                    S.redirectTo("/infoPage", () => {InfoXmlMsg.set(Full( <span>{aMsg}</span>))})
  //                  }
  //                case None =>
  //                  S.redirectTo("/errorPage", () => {
  //                    ErrorXmlMsg.set(Some(Map(
  //                      "location" -> <p>LoginOps.changePassword</p>,
  //                      "message" -> <p>No User found via 'findUserByValidationCode' for = |{aCode}|</p>)))
  //                  })
  //              }
  //            case _ =>
  //              S.redirectTo("/errorPage", () => {
  //                ErrorXmlMsg.set(Some(Map(
  //                  "location" -> <p>LoginOps.changePassword CurrentUser.is case _</p>,
  //                  "message" -> <p>Strange error - {"%s ".format(CurrentUser.is.toString)}</p>)))
  //              })
  //
  //          }
  //        /*catch {
  //      case x: NoResultException =>
  //        log.error("changePassword: NoResultException: " + x.toString)
  //        S.error("That validation code has expired.")
  //        S.warning(S ? "psw.validation.expired") //S.warning("That validation code has expired.")
  //        S.redirectTo("/login/resetPassword")
  //      case y: Exception =>
  //        log.error("changePassword: Exception: " + y.toString)
  //        println(y)
  //        S.redirectTo("/")
  //    } //finally { } */
  //        }
  //      }
  //    }
  //
  //    "#password" #> (FocusOnLoad(<input type="password" name="password" size="16"/>)) &
  //      "#confirmation" #> (<input type="password" name="confirmation" size="16"/>) &
  //      "#submit" #> SHtml.submit(S.?("save.changes"), updatePassword)
  //
  ////    "#password" #> (FocusOnLoad(<input id="password" type="password" name="password" size="24" value=" "/>)) &
  ////      "#confirmation" #> (<input id="confirmation" type="password" name="confirmation" size="24" value=" "/>) &
  ////      "#submit" #> SHtml.submit(S.?("save.changes"), updatePassword)
  //  }
  //
  //}
}

object ChangePassword extends UberScreen {
  val log = Logger("ChangePassword")
  val aCode = S.param("code").openOr("")
  log.debug(("[] ... S.param('code')= |%s", aCode))
  /*if (aCode.length == 0) {
    log.info(("InfoXmlMsg: %s", LongMsgs.getMsgText("exec.lost.psw")))
    S.redirectTo("/infoPage", () => {
      InfoXmlMsg.set(Full(<span>
        {LongMsgs.getMsg("exec.lost.psw")}
      </span>))
    })
  }*/


  val reqScheme = CurrentReq.value.request.scheme
  log.debug("CurrentReq.value.request.scheme |" + reqScheme + "|")

  val plainPsw = password(S ? "plainPsw", "sanimda", notNull, trim,
    valRegex(Pattern.compile(Props.get("regex.password").openOr(".")), S ? "bad.password.quality"))

  val plainPsw2 = password(S ? "plainPsw2", "zanimda", notNull, trim,
    valRegex(Pattern.compile(Props.get("regex.password").openOr(".")), S ? "bad.password.quality"))

  def helpAsHtml = Full(<p>Čia bus Helpas</p>)

  override def validations = passwordComparison _ :: super.validations

  def passwordComparison(): Errors = {
    if (plainPsw.is != plainPsw2.is)
      S ? "typed.psws.are.unequal"
    else Nil
  }

  // B301-2  override protected def redirectBack() { S.seeOther("/") }

  override def finish() {
    S.notice("plainPsw " + plainPsw.is)
    //newuser.password_=(plainPsw.is) // !!! the "password_=" method generates passwordSalt, passwordHash, uniqueid
    //newuser.setValidation
    //Model.mergeAndFlush(newuser)
    /*SendMail.sendMail2User("new-user", newuser)
    S.redirectTo("/infoPage", () => {InfoXmlMsg.set(Full(LongMsgs.getMsg("after.add.user")))})*/
    /*val user: User = Model.find(classOf[User], CurrentUserId.is/*.openTheBox*/).get
    user.password_=(plainPsw.is) // !!! the "password_=" method generates passwordSalt, passwordHash, uniqueid
    Model.mergeAndFlush(user)
    S.redirectTo(RequestedURL.openOr("/"))*/

    log.debug("AccessControl.isAuthenticated_?(): " + AccessControl.isAuthenticated_?())
    log.debug("CurrentUser.is.isDefined: " + CurrentUser.is.isDefined)
    log.debug("CurrentUser.is.toString: " + CurrentUser.is.toString)
    log.debug("S.param('code')= |" + S.param("code").openOr("") + "|")
    //S.setSessionAttribute("page", S.param("code").openOr(""))
    val aCode = S.param("code").openOr("")
    CurrentUser.is match {
      case Full(user) =>
        user.password_=(plainPsw.is) // !!! the "password_=" method generates passwordSalt, passwordHash, uniqueid
        //user.password = password    //  too wise way :)  //S.param("password").map(user.password = _)
        user.validationCode = null
        user.validationExpiry = 0
        user.validated = true
        Model.mergeAndFlush(user)
        CurrentUser(Empty)
        CurrentUserId(Empty)
        S.notice(S.?("success.psw.change"))
        SendMail.sendMail2User("change-password", user)
        val aMsg = "%s %s %s: %s".format(user.firstName, user.lastName, user.emailAddress, S.?("success.psw.change"))
        log.info("InfoXmlMsg: " + aMsg)
        S.redirectTo("/infoPage",() => InfoXmlMsg.set(Full(<span>{aMsg}</span>)))

      case Empty => {
        aCode match {
          case str if str.length == 0 => {
            log.info(("InfoXmlMsg: %s", LongMsgs.getMsgText("exec.lost.psw")))
            S.redirectTo("/infoPage", () => {
              InfoXmlMsg.set(Full(<span>
                {LongMsgs.getMsg("exec.lost.psw")}
              </span>))
            })
          }
          case _ => {
            Model.createNamedQuery[User]("findUserByValidationCode", "code" -> aCode).findOne match {
              case Some(user) => {
                if (user.validationExpiry > System.currentTimeMillis()) {
                  user.password_=(plainPsw.is) // !!! the "password_=" method generates passwordSalt, passwordHash, uniqueid
                  //user.password = password    //  too wise way :)  //S.param("password").map(user.password = _)
                  user.validationCode = null
                  user.validationExpiry = 0
                  user.validated = true
                  Model.mergeAndFlush(user)
                  CurrentUser(Empty)
                  CurrentUserId(Empty)
                  S.notice(S.?("success.psw.change"))
                  SendMail.sendMail2User("change-password", user)
                  //S.redirectTo("/")
                  val aMsg = "%s %s %s: %s".format(user.firstName, user.lastName, user.emailAddress, S.?("success.psw.change"))
                  log.info("InfoXmlMsg: " + aMsg)
                  S.redirectTo("/infoPage", () => {
                    InfoXmlMsg.set(Full(<span>
                      {aMsg}
                    </span>))
                  })
                } else {
                  S.warning(S.?("psw.validation.expired"))
                  //S.redirectTo("/login/lostPassword")
                  val aMsg = "%s %s %s: %s".format(user.firstName, user.lastName, user.emailAddress, S.?("psw.validation.expired"))
                  log.info("InfoXmlMsg: " + aMsg)
                  S.redirectTo("/infoPage", () => {
                    InfoXmlMsg.set(Full(<span>
                      {aMsg}
                    </span>))
                  })
                }
              }
              case None => {
                S.redirectTo("/errorPage", () => {
                  ErrorXmlMsg.set(Some(Map(
                    "location" -> <p>LoginOps.changePassword</p>,
                    "message" -> <p>No User found via 'findUserByValidationCode' for = |
                      {aCode}
                      |</p>)))
                })
              }
            }
          }
        }
      }
      case _ => {
        S.redirectTo("/errorPage", () => {
          ErrorXmlMsg.set(Some(Map(
            "location" -> <p>LoginOps.changePassword CurrentUser.is case _</p>,
            "message" -> <p>Strange error -
              {"%s ".format(CurrentUser.is.toString)}
            </p>)))
        })
      }
    }
  }
}

object UserAdd extends UberScreen {
  // ab26-5/vsh:
  // http://lift.la/lifts-screen
  // http://scala-programming-language.1934581.n4.nabble.com/JPA-find-or-create-new-td1966598.html
  val log = Logger("UserAdd")
  log.debug("[]...")
  val reqScheme = CurrentReq.value.request.scheme
  log.debug("UserAdd: CurrentReq.value.request.scheme |" + reqScheme + "|")

  val firstName = field(S ? "firstName", "", notNull, trim,
    valMinLen(2, S ? "flName.too.short"),
    valMaxLen(40, S ? "flName.too.long")
  )

  val lastName = field(S ? "lastName", "", notNull, trim,
    valMinLen(2, S ? "flName.too.short"),
    valMaxLen(40, S ? "flName.too.long")
  )

  val yyyymmddPattern = Pattern.compile("^(19|20)\\d\\d\\-(0[1-9]|1[012])\\-(0[1-9]|[12][0-9]|3[01])$")
  val birthDate = field((S ? "birthdate") + " format yyyy-mm-dd ", "1970-01-01", notNull, trim,
    valRegex(yyyymmddPattern, S ? "wrong.bdate.format")
  )

  val plainPsw = password(S ? "plainPsw", "sanimda", notNull, trim,
    valRegex(Pattern.compile(Props.get("regex.password").openOr(".")), S ? "bad.password.quality"))

  val plainPsw2 = password(S ? "plainPsw2", "zanimda", notNull, trim,
    valRegex(Pattern.compile(Props.get("regex.password").openOr(".")), S ? "bad.password.quality"))

  val emailAddress = field(S ? "emailAddress", "" /*"mailbox@somemail.xx"*/ , notNull, trim,
    valRegex(MappedEmail.emailPattern, S ? "wrong.email.format")

  )

  val visosLokales = (java.util.Locale.getAvailableLocales.
    toList.sortWith(_.getDisplayName < _.getDisplayName).
    map(lo => (lo.toString, lo.getDisplayName))).toSeq
  val lokale = select[String]("Lokalė", "lt", visosLokales)

  val timezona = select[String]("Timezone", "Europe/Vilnius", MappedTimeZone.timeZoneList.toSeq)

  //def helpAsHtml = Full(<p>Čia bus Helpas</p>)

  override def validations = passwordComparison _ :: emailUniqueness _ :: super.validations

  def passwordComparison(): Errors = {
    if (plainPsw.is != plainPsw2.is)
      S ? "typed.psws.are.unequal"
    else Nil
  }

  def emailUniqueness(): Errors = {
    /*val personsx: List[Person] = Model.createNamedQuery[Person]("findPersonByGivnSurn",
      "nameGivn" -> user.firstName, "nameSurn" -> user.lastName).findAll.toList*/
    //    val users: List[User] = Model.createNamedQuery[User]("findUserByEmailAddress",
    //      "emailAddress" -> emailAddress.is).findAll.toList
    //    //Model.createNamedQuery[User]("findUserByEmailAddress", "emailAddress" -> emailAddress).getResultList().size
    //    log.debug("UserAdd.emailUniqueness: users.size=" + users.size)
    //    users.size  match {
    //      case 0 => Nil
    //      case _ =>
    //        log.debug("UserAdd.emailUniqueness: is not unique:" + emailAddress)
    //        S ? "emai.not.unique"
    //    }
    Model.createNamedQuery[User]("findUserValidatedByEmailAddress", "emailAddress" -> emailAddress.is).findOne match {
      case Some(user) =>
        log.debug("UserAdd.emailUniqueness is failed")
        S ? "email.not.unique"
      case None => Nil
    }
  }

  // B301-2  override protected def redirectBack() { S.seeOther("/") }

  override def finish() {
    S.notice("firstName " + firstName.is)
    S.notice("firstName " + lastName.is)
    S.notice("plainPsw " + plainPsw.is)
    S.notice("emailAddress " + emailAddress.is)
    S.notice("lokale " + lokale.is.openOr("lt") /*.open_!*/)
    S.notice("timezona " + timezona.is.openOr("Europe/Vilnius") /*.open_!*/)
    val newuser = new User
    newuser.firstName = firstName.is
    newuser.lastName = lastName.is
    newuser.birthDate = new SimpleDateFormat("yyyy-MM-dd").parse(birthDate.is, new ParsePosition(0))
    newuser.locale = lokale.is.openOr("lt") //.toString/*.open_!*/
    newuser.timezone = timezona.is.openOr("Europe/Vilnius") //toString/*.open_!*/
    newuser.emailAddress = emailAddress.is
    newuser.password_=(plainPsw.is) // !!! the "password_=" method generates passwordSalt, passwordHash, uniqueid
    newuser.setValidation
    // TODO B417-7/vsh  check for email uniqueness and maybe firstName, firstName
    Model.mergeAndFlush(newuser)
    SendMail.sendMail2User("new-user", newuser)
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
  val reqScheme = CurrentReq.value.request.scheme
  log.debug("UserEdit: CurrentReq.value.request.scheme |" + reqScheme + "|")

  val user: User = Model.find(classOf[User], CurrentUserId.is.openTheBox).get
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

  val timezona = select[String]("Timezone", user.timezone, MappedTimeZone.timeZoneList.toSeq)

  //def helpAsHtml = Full(<p>Čia bus Helpas</p>)

  override def validations = emailUniqueness _ :: super.validations

  def emailUniqueness(): Errors = {
    Model.createNamedQuery[User]("findUserValidatedByEmailAddress", "emailAddress" -> emailAddress.is).findOne match {
      case Some(user) =>
        log.debug("UserEdit.emailUniqueness is failed")
        S ? "email.not.unique"
      case None => Nil
    }
  }

  // B301-2  override protected def redirectBack() { S.seeOther("/") }

  override def finish() {
    S.notice("firstName " + firstName.is)
    S.notice("lastName " + lastName.is)
    S.notice("emailAddress " + emailAddress.is)
    S.notice("lokale " + lokale.is /*.open_!*/)
    S.notice("timezona " + timezona.is /*.open_!*/)
    user.firstName = firstName.is
    log.debug("UserEdit " + user.firstName)
    user.lastName = lastName.is
    user.birthDate = new SimpleDateFormat("yyyy-MM-dd").parse(birthDate.is, new ParsePosition(0))
    user.locale = lokale.is.openOr("lt")
    user.timezone = timezona.is.openOr("Europe/Vilnius")
    user.emailAddress = emailAddress.is
    Model.mergeAndFlush(user)
    S.redirectTo(RequestedURL.openOr("/"))
  }
}


//object SendMail {
//  // B204-5/vsh init
//  // google-group:Lift: [how do you set system properties in lift]
//  // http://java.sun.com/products/javamail/javadocs/com/sun/mail/smtp/package-summary.html
//  val log = Logger("SendMail")
//  //  log.debug("[]...")
//
//  def sendMail2User(reason: java.lang.String, user: User) {
//    //log.debug("sendMail hostFunc |" + Props.get("admin.mailhost").open_! + "|")
//    //log.debug("sendMail admin.mail |" + Props.get("admin.mail")/*.open_!*/ + "|")
//    log.debug("sendMail Mailer.properties.toString |" + Mailer.properties.toString + "|")
//
//    log.debug("sendMail reason |" + reason + "|")
//    /*val getHostPortApp = Props.get("host__").openOr("localhost") +
//      Props.get("_port_").openOr(":8080") + Props.get("__app").openOr("/gedcom/")*/
//    val getHostPortApp = CurrentReq.value.request.scheme + "://" +
//      //Props.get("host__").openOr("localhost") +
//      CurrentReq.value.request.serverName +
//      ":" + CurrentReq.value.request.serverPort /*Props.get("_port_").openOr(":8080")*/ +
//      Props.get("__app").openOr("/")
//    log.debug("sendMail getHostPortApp |" + getHostPortApp + "|")
//    //val getApp = Props.get("__app").openOr("/")
//    log.debug("sendMail getHostPortApp |" + getHostPortApp + "|")
//
//    /*reason match {
//      case "test" => {}
//      case _ => {log.debug("sendMail user.emailAddress |" + user.emailAddress + "|")}
//    }*/
//
//    reason match {
//      case "new-user" => {
//        log.debug("-----|new-user|")
//        val code = /*"https://" +*/ getHostPortApp + "addendup/" + user.validationCode
//
//        /*Thread.currentThread().setContextClassLoader(getClass().getClassLoader())
//        // https://groups.google.com/forum/?fromgroups=#!topic/liftweb/FW6E0MSEYzs*/
//
//        Mailer.blockingSendMail /*sendMail*/ (
//          From(Props.get("admin.mail").openOr("vytasab@gmail.com")),
//          Subject(LongMsgs.getMsgText("new.user.subj")),
//          List(
//            xmlToMailBodyType(//XHTMLMailBodyType(
//              <html>
//                <head>
//                  <title>LongMsgs.getMsgText("new.user.subj")</title>
//                </head>
//                <body>
//                  {(LongMsgs.getMsg("new.user.p1"))}<p>
//                  <a href={code}>
//                    {code}
//                  </a>
//                </p>{(LongMsgs.getMsg("new.user.p2"))}<p>
//                  {new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(user.validationExpiry))}
//                </p>
//                  <p>
//                    {(LongMsgs.getMsg("new.user.p3"))}
//                  </p>
//                </body>
//              </html>),
//            To(user.emailAddress)
//          ): _*
//        )
//      }
//
//      case "set-user-approval" => {
//        //todo_  DB20-3/vsh  nebaigta dar
//        log.debug("-----|set-user-approval|")
//        val codeShortestYes = getHostPortApp + "addendup/1_" + user.validationCode
//        val codeShortestNo = getHostPortApp + "addendup/0_" + user.validationCode
//        Mailer. /*blockingSendMail*/ sendMail(
//          From(user.emailAddress),
//          Subject(LongMsgs.getMsgText("user.approval.subj")),
//          List(
//            xmlToMailBodyType(
//              <html>
//                <head>
//                  <title>LongMsgs.getMsgText("new.user.subj")</title>
//                </head>
//                <body>
//                  <p>
//                    <b>
//                      {user.firstName}{user.lastName}
//                      :
//                      {user.emailAddress}
//                    </b>
//                  </p>
//                  <p>
//                    {(LongMsgs.getMsg("allow"))}
//                    -
//                    <a href={codeShortestYes}>
//                      {codeShortestYes}
//                    </a>
//                  </p>
//                  <p>
//                    {(LongMsgs.getMsg("refuse"))}
//                    -
//                    <a href={codeShortestNo}>
//                      {codeShortestNo}
//                    </a>
//                  </p>
//                  <p>
//                    {(LongMsgs.getMsg("new.user.p2"))}{new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(user.validationExpiry))}
//                  </p>
//                </body>
//              </html>),
//            To(Props.get("admin.mail").openOr("vytasab@gmail.com"))
//          ): _*
//        )
//      }
//
//      case "user-approval" => {
//        log.debug("-----|user-approval|")
//        Mailer. /*blockingSendMail*/ sendMail(
//          From(Props.get("admin.mail").openOr("vytasab@gmail.com")),
//          Subject(LongMsgs.getMsgText("admin.response")),
//          List(
//            xmlToMailBodyType(
//              <html>
//                <head>
//                  <title>
//                    {LongMsgs.getMsgText("admin.response")}
//                  </title>
//                </head>
//                <body>
//                  {(LongMsgs.getMsg("new.user.allow"))}
//                </body>
//              </html>),
//            To(user.emailAddress)
//          ): _*
//        )
//      }
//
//      case "user-approval-failed" => {
//        log.debug("-----|user-approval-failed|")
//        Mailer. /*blockingSendMail*/ sendMail(
//          From("gedcom"),
//          Subject({
//            LongMsgs.getMsgText("admin.response")
//          }),
//          List(
//            xmlToMailBodyType(
//              <html>
//                <head>
//                  <title>
//                    {LongMsgs.getMsgText("admin.response")}
//                  </title>
//                </head>
//                <body>
//                  {"Failed to approve new user: " + user.firstName + " " + user.lastName + "email: " + user.emailAddress}
//                </body>
//              </html>),
//            To(Props.get("admin.mail").openOr("vytasab@gmail.com")),
//            CC(user.emailAddress)
//          ): _*
//        )
//      }
//
//      case "user-rejection" => {
//        log.debug("-----|user-rejection|")
//        Mailer. /*blockingSendMail*/ sendMail(
//          From(Props.get("admin.mail").openOr("vytasab@gmail.com")),
//          Subject(LongMsgs.getMsgText("admin.response")),
//          List(
//            xmlToMailBodyType(
//              <html>
//                <head>
//                  <title>
//                    {LongMsgs.getMsgText("admin.response")}
//                  </title>
//                </head>
//                <body>
//                  {LongMsgs.getMsg("new.user.refuse")}
//                </body>
//              </html>),
//            To(user.emailAddress)
//          ): _*
//        )
//      }
//
//      case "user-rejection-failed" => {
//        // send mail to admin
//        log.debug("-----|user-rejection-failed|")
//        Mailer. /*blockingSendMail*/ sendMail(
//          From("gedcom"),
//          Subject({
//            LongMsgs.getMsgText("admin.response")
//          }),
//          List(
//            xmlToMailBodyType(
//              <html>
//                <head>
//                  <title>
//                    {LongMsgs.getMsgText("admin.response")}
//                  </title>
//                </head>
//                <body>
//                  {"Failed to reject new user: " + user.firstName + " " + user.lastName + "email: " + user.emailAddress}
//                </body>
//              </html>),
//            To(Props.get("admin.mail").openOr("vytasab@gmail.com")) /*,CC(user.emailAddress)*/
//          ): _*
//        )
//      }
//
//      case "change-password" => {
//        Mailer.sendMail(
//          From(<_>
//            {Props.get("admin.mail").openOr("admin.mail")}
//          </_>.text),
//          Subject(LongMsgs.getMsgText("change.password.subj")),
//          List(
//            To(user.emailAddress),
//            xmlToMailBodyType(
//              <html>
//                <head>
//                  <!--title>Your credentials have been changed.</title-->
//                  <title>
//                    {LongMsgs.getMsgText("change.password.subj")}
//                  </title>
//                </head>
//                <body>
//                  <p>
//                    {/*Unparsed*/ (LongMsgs.getMsg("change.password.p1"))}<span>
//                    {Props.get("admin.mail").openOr("admin.mail")}
//                  </span>
//                  </p>
//                </body>
//              </html>)
//          ): _*
//        )
//      }
//
//      case "password-reset" => {
//        //TODO ar reikalingas protokolas http(s) ir getHostPortApp ?
//        val code = getHostPortApp + "validation/" + user.setValidation
//        Model.mergeAndFlush(user)
//        Mailer.sendMail(
//          From(<_>
//            {Props.get("admin.mail").openOr("admin.mail")}
//          </_>.text),
//          Subject(LongMsgs.getMsgText("password.reset.subj")),
//          List(
//            To(user.emailAddress),
//            xmlToMailBodyType(<html>
//              <head>
//                <title>LongMsgs.getMsgText("password.reset.subj")</title>
//              </head>
//              <body>
//                <p>
//                  {(LongMsgs.getMsg("password.reset.p1"))}{getHostPortApp}{(LongMsgs.getMsg("password.reset.p2"))}
//                </p>
//                <p>
//                  <a href={code}>
//                    {code}
//                  </a>
//                </p>{(LongMsgs.getMsg("new.user.p3"))}<p>
//                {new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(user.validationExpiry))}
//              </p>
//              </body>
//            </html>)
//          ): _*
//        )
//      }
//
//      case "test" => {
//        Mailer. /*blockingSendMail*/ sendMail(
//          //From(<_>{Props.get("admin.mail").openOr("admin.mail")}</_>.text),
//          From("vytasab@gmail.com"),
//          //Subject("Test message: " + LongMsgs.getMsgText("password.reset.subj")),
//          Subject("Text!!!"),
//          List(
//            //To(user.emailAddress),
//            To("vytasab@gmail.com"),
//            //To("dalia.sabaniene@gmail.com"),
//            PlainMailBodyType("Woo! I can text :-)")
//          ): _*
//        )
//      }
//
//      case _ =>
//    }
//  }
//}

