package lt.node.gedcom.snippet

import javax.mail._
import java.util.Properties
import internet.{AddressException, InternetAddress, MimeMessage}
import net.liftweb.common.Logger
import net.liftweb.http.ResponseShortcutException

object SendMailTestTLS {
  val log = Logger("SendMailTestTLS");

  def render(/*args: Array[String]*/) {
    val username/*: String*/ = "vytasab@gmail.com" // "username@gmail.com"
    val password/*: String*/ = "paratunka" // "password"
    val props: Properties = new Properties
    props.put("mail.debug", "true")
    props.put("mail.transport.protocol", "smtp")
    props.put("mail.smtp.auth", "true")

    props.put("mail.smtp.starttls.enable", "true")
    //Bypass the SSL authentication
    props.put("mail.smtp.ssl.enable", "true");
    //props.put("mail.smtp.ssl.enable", "false");
    //props.put("mail.smtp.starttls.enable", "false")

    props.put("mail.smtp.host", "smtp.gmail.com")
    props.put("mail.smtp.port", "587")
    //props.put("mail.smtp.port", "465")
    //props.put("mail.smtp.ssl.trust","smtpserver")

    //props.put("mail.smtp.ssl.trust", "true")
    props.put("mail.smtp.ssl.trust", "false")

    props.put("mail.user", username)
    props.put("mail.password", password)
    val session: Session = Session.getInstance(props, new Authenticator {
      protected override def getPasswordAuthentication: PasswordAuthentication = {
        return new PasswordAuthentication(username, password)
      }
    })
    val transport: Transport = session.getTransport
    try {
      log.debug("try []...")
      val message: Message = new MimeMessage(session)
      message.setFrom(new InternetAddress("vytasab@gmail.com"))
      log.debug("... 1")

      //message.setRecipients(Message.RecipientType.TO, InternetAddress.parse("to-email@gmail.com"))
      //val addressTo: Address = new Address { def getType: String = "vytasab@gmail.com" }
      //message.setRecipient(Message.RecipientType.TO, addressTo)
      message.setRecipient(Message.RecipientType.TO, new InternetAddress(username))
      log.debug("... 2")

      message.setSubject("Testing Subject")
      log.debug("... 3")
      message.setText("Dear Mail Crawler," + "\n\n No spam to my email, please!")
      //log.debug("... 4.0")
      //log.debug("|" +  transport.isConnected.toString + "|")
      log.debug("... 4.01")
      log.debug("|" +  transport.getURLName.toString + "|")

      //Transport.send(message)
      //transport.connect("https://smtp.gmail.com", 587, username, password)
      transport.connect("smtp.gmail.com", 587, username, password)
      log.debug("... 4.1")
      transport.sendMessage(message, message.getRecipients(Message.RecipientType.TO))

      log.debug("... 5")
      System.out.println("Done")
      log.debug("Dooonnneee")
    }
    catch {
      case e: ResponseShortcutException => {
        log.info("ResponseShortcutException: " + e.toString)
      }
      case e: AddressException => {
        log.error("AddressException: " + e.toString)
        throw new RuntimeException(e)
      }
      case e: MessagingException => {
        log.error("MessagingException: " + e.toString)
        throw new RuntimeException(e)
      }
      case e: Exception => {
        log.error("Exception: " + e.toString)
        throw new RuntimeException(e)
      }
    } finally {
      log.debug("finally ...[]")

    }
  }
}



