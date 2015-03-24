package lt.node.gedcom

import lt.node.gedcom.util.LongMsgs
import model.{Model, User}

import _root_.net.liftweb._
import net.liftweb.util.{Props, Mailer}
import _root_.net.liftweb.util.Mailer._
import common.Logger
//import util.Helpers._
import http.CurrentReq
import java.text.SimpleDateFormat
import java.sql.Date

/**
 * Created by IntelliJ IDEA.
 * User: padargas
 * Date: 11/23/10
 * Time: 1:37 PM
 * To change this template use File | Settings | File Templates.
 */
object SendMail {
  // DC04-3/vsh (piece of)LoginOps.scala --> SendMail
  // B204-5/vsh init
  // google-group:Lift: [how do you set system properties in lift]
  // http://java.sun.com/products/javamail/javadocs/com/sun/mail/smtp/package-summary.html
  val log = Logger("SendMail")
  //  log.debug("[]...")

  def sendMail2User(reason: java.lang.String, user: User) {
    //log.debug("sendMail hostFunc |" + Props.get("admin.mailhost").open_! + "|")
    //log.debug("sendMail admin.mail |" + Props.get("admin.mail")/*.open_!*/ + "|")
    log.debug(("sendMail Mailer.properties.toString |%s|", Mailer.properties.toString))
    log debug("sendMail reason |%s|".format(reason))
    /*val getHostPortApp = Props.get("host__").openOr("localhost") +
      Props.get("_port_").openOr(":8080") + Props.get("__app").openOr("/gedcom/")*/
    val getHostPortApp = CurrentReq.value.request.scheme + "://" +
      //Props.get("host__").openOr("localhost") +
      CurrentReq.value.request.serverName +
      ":" + CurrentReq.value.request.serverPort /*Props.get("_port_").openOr(":8080")*/ +
      Props.get("__app").openOr("/")
    log.debug("sendMail getHostPortApp |" + getHostPortApp + "|")
    //val getApp = Props.get("__app").openOr("/")
    log.debug("sendMail getHostPortApp |" + getHostPortApp + "|")

    /*reason match {
      case "test" => {}
      case _ => {log.debug("sendMail user.emailAddress |" + user.emailAddress + "|")}
    }*/

    reason match {
      case "new-user" => {
        log.debug("-----|new-user|")
        val code = /*"https://" +*/ getHostPortApp + "addendup/" + user.validationCode

        /*Thread.currentThread().setContextClassLoader(getClass().getClassLoader())
        // https://groups.google.com/forum/?fromgroups=#!topic/liftweb/FW6E0MSEYzs*/

        Mailer.blockingSendMail /*sendMail*/ (
          From(Props.get("admin.mail").openOr("vytasab@gmail.com")),
          Subject(LongMsgs.getMsgText("new.user.subj")),
          List(
            xmlToMailBodyType(//XHTMLMailBodyType(
              <html>
                <head>
                  <title>LongMsgs.getMsgText("new.user.subj")</title>
                </head>
                <body>
                  {(LongMsgs.getMsg("new.user.p1"))}<p>
                  <a href={code}>
                    {code}
                  </a>
                </p>{(LongMsgs.getMsg("new.user.p2"))}<p>
                  {new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(user.validationExpiry))}
                </p>
                  <p>
                    {(LongMsgs.getMsg("new.user.p3"))}
                  </p>
                </body>
              </html>),
            To(user.emailAddress)
          ): _*
        )
      }

      case "set-user-approval" => {
        //todo_  DB20-3/vsh  nebaigta dar
        log.debug("-----|set-user-approval|")
        val codeShortestYes = getHostPortApp + "addendup/1_" + user.validationCode
        val codeShortestNo = getHostPortApp + "addendup/0_" + user.validationCode
        Mailer. /*blockingSendMail*/ sendMail(
          From(user.emailAddress),
          Subject(LongMsgs.getMsgText("user.approval.subj")),
          List(
            xmlToMailBodyType(
              <html>
                <head>
                  <title>LongMsgs.getMsgText("new.user.subj")</title>
                </head>
                <body>
                  <p>
                    <b>
                      {user.firstName}{user.lastName}
                      :
                      {user.emailAddress}
                    </b>
                  </p>
                  <p>
                    {(LongMsgs.getMsg("allow"))}
                    -
                    <a href={codeShortestYes}>
                      {codeShortestYes}
                    </a>
                  </p>
                  <p>
                    {(LongMsgs.getMsg("refuse"))}
                    -
                    <a href={codeShortestNo}>
                      {codeShortestNo}
                    </a>
                  </p>
                  <p>
                    {(LongMsgs.getMsg("new.user.p2"))}{new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(user.validationExpiry))}
                  </p>
                </body>
              </html>),
            To(Props.get("admin.mail").openOr("vytasab@gmail.com"))
          ): _*
        )
      }

      case "user-approval" => {
        log.debug("-----|user-approval|")
        Mailer. /*blockingSendMail*/ sendMail(
          From(Props.get("admin.mail").openOr("vytasab@gmail.com")),
          Subject(LongMsgs.getMsgText("admin.response")),
          List(
            xmlToMailBodyType(
              <html>
                <head>
                  <title>
                    {LongMsgs.getMsgText("admin.response")}
                  </title>
                </head>
                <body>
                  {(LongMsgs.getMsg("new.user.allow"))}
                </body>
              </html>),
            To(user.emailAddress)
          ): _*
        )
      }

      case "user-approval-failed" => {
        log.debug("-----|user-approval-failed|")
        Mailer. /*blockingSendMail*/ sendMail(
          From("gedcom"),
          Subject({
            LongMsgs.getMsgText("admin.response")
          }),
          List(
            xmlToMailBodyType(
              <html>
                <head>
                  <title>
                    {LongMsgs.getMsgText("admin.response")}
                  </title>
                </head>
                <body>
                  {"Failed to approve new user: " + user.firstName + " " + user.lastName + "email: " + user.emailAddress}
                </body>
              </html>),
            To(Props.get("admin.mail").openOr("vytasab@gmail.com")),
            CC(user.emailAddress)
          ): _*
        )
      }

      case "user-rejection" => {
        log.debug("-----|user-rejection|")
        Mailer. /*blockingSendMail*/ sendMail(
          From(Props.get("admin.mail").openOr("vytasab@gmail.com")),
          Subject(LongMsgs.getMsgText("admin.response")),
          List(
            xmlToMailBodyType(
              <html>
                <head>
                  <title>
                    {LongMsgs.getMsgText("admin.response")}
                  </title>
                </head>
                <body>
                  {LongMsgs.getMsg("new.user.refuse")}
                </body>
              </html>),
            To(user.emailAddress)
          ): _*
        )
      }

      case "user-rejection-failed" => {
        // send mail to admin
        log.debug("-----|user-rejection-failed|")
        Mailer. /*blockingSendMail*/ sendMail(
          From("gedcom"),
          Subject({
            LongMsgs.getMsgText("admin.response")
          }),
          List(
            xmlToMailBodyType(
              <html>
                <head>
                  <title>
                    {LongMsgs.getMsgText("admin.response")}
                  </title>
                </head>
                <body>
                  {"Failed to reject new user: " + user.firstName + " " + user.lastName + "email: " + user.emailAddress}
                </body>
              </html>),
            To(Props.get("admin.mail").openOr("vytasab@gmail.com")) /*,CC(user.emailAddress)*/
          ): _*
        )
      }

      case "change-password" => {
        Mailer.sendMail(
          From(<_>
            {Props.get("admin.mail").openOr("admin.mail")}
          </_>.text),
          Subject(LongMsgs.getMsgText("change.password.subj")),
          List(
            To(user.emailAddress),
            xmlToMailBodyType(
              <html>
                <head>
                  <!--title>Your credentials have been changed.</title-->
                  <title>
                    {LongMsgs.getMsgText("change.password.subj")}
                  </title>
                </head>
                <body>
                  <p>
                    {/*Unparsed*/ (LongMsgs.getMsg("change.password.p1"))}<span>
                    {Props.get("admin.mail").openOr("admin.mail")}
                  </span>
                  </p>
                </body>
              </html>)
          ): _*
        )
      }

      case "password-reset" => {
        val code = getHostPortApp + "validation/" + user.setValidation
        Model.mergeAndFlush(user)
        Mailer.sendMail(
          From(<_>
            {Props.get("admin.mail").openOr("admin.mail")}
          </_>.text),
          Subject(LongMsgs.getMsgText("password.reset.subj")),
          List(
            To(user.emailAddress),
            xmlToMailBodyType(<html>
              <head>
                <title>LongMsgs.getMsgText("password.reset.subj")</title>
              </head>
              <body>
                <p>
                  {(LongMsgs.getMsg("password.reset.p1"))}{getHostPortApp}{(LongMsgs.getMsg("password.reset.p2"))}
                </p>
                <p>
                  <a href={code}>
                    {code}
                  </a>
                </p>{(LongMsgs.getMsg("new.user.p3"))}<p>
                {new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(user.validationExpiry))}
              </p>
              </body>
            </html>)
          ): _*
        )
      }

      case "test" => {
        Mailer. /*blockingSendMail*/ sendMail(
          //From(<_>{Props.get("admin.mail").openOr("admin.mail")}</_>.text),
          From("vytasab@gmail.com"),
          //Subject("Test message: " + LongMsgs.getMsgText("password.reset.subj")),
          Subject("Text!!!"),
          List(
            //To(user.emailAddress),
            To("vytasab@gmail.com"),
            //To("dalia.sabaniene@gmail.com"),
            PlainMailBodyType("Woo! I can text :-)")
          ): _*
        )
      }

      case _ =>
    }
  }
}
