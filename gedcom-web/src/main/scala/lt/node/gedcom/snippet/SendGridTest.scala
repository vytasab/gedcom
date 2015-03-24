package lt.node.gedcom.snippet

/**
 * development time
 */

import javax.mail._
import javax.mail.internet._
import javax.mail.Authenticator
import javax.mail.PasswordAuthentication
import java.util.Properties

object SendGridTest {
  /*main(args: Array[String]*/
  def render {
    new SendGridTest().test
  }
  private final val SMTP_HOST_NAME = "smtp.sendgrid.net"
  private final val SMTP_AUTH_USER = "vsh"
  private final val SMTP_AUTH_PWD = "laikinas"
}

class SendGridTest {
  def test {
    val props: Properties = new Properties
    props.put("mail.transport.protocol", "smtp")
    props.put("mail.smtp.host", SendGridTest.SMTP_HOST_NAME)
    props.put("mail.smtp.port", "587")
    props.put("mail.smtp.auth", "true")
    props.put("mail.debug", "true")
    true

    val auth: Authenticator = new /*SendGridTest#*/SMTPAuthenticator
    val mailSession: Session = Session.getDefaultInstance(props, auth)
    val transport: Transport = mailSession.getTransport
    val message: MimeMessage = new MimeMessage(mailSession)
    val multipart: Multipart = new MimeMultipart("alternative")
    val part1: BodyPart = new MimeBodyPart
    part1.setText("This is multipart mail and u read part1,,,")
    val part2: BodyPart = new MimeBodyPart
    part2.setContent("<b>This is multipart mail and u read part2……</b>", "text/html")

    multipart.addBodyPart(part1)
    multipart.addBodyPart(part2)
    message.setContent(multipart)
    message.setFrom(new InternetAddress("vytasab@gmail.com"))


    message.setSubject("This is the subject")
    message.addRecipient(Message.RecipientType.TO, new InternetAddress("dalia.sabaniene@gmail.com"))


    transport.connect
    transport.sendMessage(message, message.getRecipients(Message.RecipientType.TO))
    transport.close
  }

  private class SMTPAuthenticator extends javax.mail.Authenticator {
    override def getPasswordAuthentication: PasswordAuthentication = {
      val username = SendGridTest.SMTP_AUTH_USER
      val password = SendGridTest.SMTP_AUTH_PWD
      return new PasswordAuthentication(username, password)
    }
  }

}


