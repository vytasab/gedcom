package lt.node.gedcom.snippet

import bootstrap.liftweb._
import lt.node.gedcom.util.LongMsgs
import scala.Some
import javax.mail.internet.AddressException
import javax.mail.MessagingException

import _root_.net.liftweb._
import _root_.net.liftweb.util.Mailer
import _root_.net.liftweb.util.Mailer._
import common.{Full, Logger}
import util.Helpers._
import http.{ResponseShortcutException, SHtml, S}
/**
 * Created by IntelliJ IDEA.
 * User: padargas
 * Date: 11/23/10
 * Time: 1:37 PM
 * To change this template use File | Settings | File Templates.
 */
class SendMailTest /*extends lt.node.gedcom.util.UberScreen*/ {
  val log = /*LoggerFactory.get*/ Logger("SendMailTest");
  var emailBody = "Hello everybody!"

  def sendMailTest = {

    def done(): Unit = {
      try {
        AccessControl.isAuthenticated_? match {
          case true =>
            //SendMail.sendMail2User("test", CurrentUser.is.get)
            //log.info("---------- the message has been sent successfully ---------")
            log.info("----------  logged in user ---------")
          case _ =>
            log.info("---------- no logged in user ---------")
        }

       //SendMail.sendMail2User("test", /*CurrentUser.is.get*/null)
       //SendMail.sendMail2User("test", null)
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


      } catch {
        case x: ResponseShortcutException =>
          S.redirectTo("/infoPage", () => InfoXmlMsg.set(Full(<p>ResponseShortcutException it is OK here</p>)) )
        case x: AddressException  =>
          S.redirectTo("/errorPage", () => {
            ErrorXmlMsg.set(Some(Map(
              "location" -> <p>SendMailTest:sendMailTest</p>,
              "message" -> <p>{x}</p>)))
          })
        //S.redirectTo("/infoPage", () => InfoXmlMsg.set(Full(<p>ResponseShortcutException it is OK here</p>)) )
        case xx: MessagingException  =>
          S.redirectTo("/errorPage", () => {
            ErrorXmlMsg.set(Some(Map(
              "location" -> <p>SendMailTest:sendMailTest</p>,
              "message" -> <p>{xx}</p>)))
          })
        //S.redirectTo("/infoPage", () => InfoXmlMsg.set(Full(<p>ResponseShortcutException it is OK here</p>)) )
        case y: Exception => //println(y)
        S.redirectTo("/errorPage", () => {
          ErrorXmlMsg.set(Some(Map(
            "location" -> <p>SendMailTest:sendMailTest</p>,
            "message" -> <p>{y}</p>)))
        })
      } finally{
        S.redirectTo("/", () => {
          InfoXmlMsg.set(Full(LongMsgs.getMsg("endup.validation")))})
      }
    }

    "#emailBody" #> SHtml.text(emailBody, emailBody = _) &
      "#submit" #> SHtml.submit(S.?("mail.send.test"), done)
  }
}