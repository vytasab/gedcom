package lt.node.gedcom.model

import _root_.javax.persistence._
import _root_.java.util.Date

//import org.jasypt.contrib.org.apache.commons.codec_1_3.binary.Base64
//import java.security.MessageDigest

/**
This class represents a user with login privileges on the website.
 */

// TODO CB15-4/vsh: reikėtų čia pridėti optional MultiMwedia mblob
@Entity
@Table(name = "audit")
class Audit {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  var id: Long = _

  @Column(name = "entity", unique = false, nullable = false, length = 10)
  var entityName: String = ""
  // cases: Us-User,
  // Pe-Person, PE-PersonEvent, PA-PersonAttrib,
  // Fa-Family, FA-FamilyAttrib,
  // ED-EventDetail
  // MM-MultiMedia

  @Column(unique = false, nullable = false)
  var entityId: Long = _

  @Column(unique = false, nullable = false, length = 10)
  var action: String = ""
  // cases: add, upd, del;  in the future: ...

  @Column(unique = false, nullable = false, length = 64000)
  var message: String = ""
  // action: add, edit, del;  in the future: ...
  // add:  <locale><fieldName>text</fieldName>...</locale>
  // edit: <locale><fieldName><old>text</old><new>text</new></fieldName>...</locale>
  // del:  <deleted><fieldName>text</fieldName>...</deleted>
  // _:    <locale>?</locale>

  @Temporal(TemporalType.TIMESTAMP)
  @Column(nullable = false)
  var time: Date = new Date()

  @Column(unique = false, nullable = true)
  var submitter: String = ""

  def setFields(u: User, entityName: String, entityId: Long, action: String, message: String): Unit = {
    this.entityName = entityName
    this.entityId = entityId
    this.action = action
    this.message = message
    this.submitter = u.getSubmitter()
  }

  def setSubmitter(u: User) = this.submitter = u.getSubmitter()

}

