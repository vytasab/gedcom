package lt.node.gedcom {
package model {

import _root_.javax.persistence._
import java.text.SimpleDateFormat

import _root_.java.util.Date

import org.jasypt.contrib.org.apache.commons.codec_1_3.binary.Base64
import java.security.MessageDigest

/**
This class represents a user with login privileges on the website.
 */

@Entity
@Table(name = "users")
class User /*extends BaseEntity*/ {
  // TODO B320-7/vsh manau, kad nereikia to  <==  AB23-3/vsh  uncomment extends...
  // TODO B320-7/vsh Pridėti auditą
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  var id: Long = _

  @Column(name = "EMAIL_ADDRESS", unique = true, nullable = false, length = 50)
  var emailAddress: String = ""
  // email

  @Column(name = "VALIDATION_CODE")
  var validationCode: String = ""

  @Column(name = "VALIDATION_EXPIRY")
  var validationExpiry: Long = _

  @Column(name = "PASSWORD_SALT")
  var passwordSalt: String = ""
  // password_swt

  @Column(name = "PASSWORD_HASH")
  var passwordHash: String = ""
  //

  //@Column(name = "IS_ADMINISTRATOR")
  //var isAdministrator: Boolean = false
  var superuser: Boolean = false

  @Column(name = "IS_ACTIVE")
  var isActive: Boolean = true
  // not in use

  @Column(unique = false, nullable = false, length = 50)
  var firstName: String = ""

  @Column(unique = false, nullable = false, length = 50)
  var lastName: String = ""

  @Column(unique = false, nullable = false, length = 10)
  var locale: String = ""

  @Column(unique = false, nullable = false, length = 30)
  var timezone: String = ""

  @Column(unique = true, nullable = false, length = 32)
  var uniqueid: String = ""
  // added to be ProtoUser compatible

  var validated: Boolean = false
  // added to be ProtoUser compatible

  @Temporal(TemporalType.DATE)
  @Column(nullable = true)
  var birthDate: Date = new Date()

  @Temporal(TemporalType.TIMESTAMP)
  @Column(nullable = true)
  var createdOn: Date = new Date()

  @Temporal(TemporalType.TIMESTAMP)
  @Column(nullable = true)
  var updatedOn: Date = new Date()

  //@OneToMany(mappedBy="submitter", targetEntity=classOf[Person], cascade=Array(CascadeType.REMOVE))
  //var persons : java.util.Set[Person] = new java.util.HashSet[Person]()
  //@OneToMany(mappedBy="submitter", targetEntity=classOf[Family], cascade=Array(CascadeType.REMOVE))
  //var families : java.util.Set[Family] = new java.util.HashSet[Family]()
  //@OneToMany(mappedBy="submitter", targetEntity=classOf[PersonEvent], cascade=Array(CascadeType.REMOVE))
  //var personevents : java.util.Set[PersonEvent] = new java.util.HashSet[PersonEvent]()
  //@OneToMany(mappedBy = "submitter", targetEntity = classOf[Audit], cascade=Array(CascadeType.REMOVE))
  //var auditRecs: java.util.Set[Audit] = new java.util.HashSet[Audit]()


  /*create a SHA hash from a String */

  def hash(in: String): String = {
    //println ("User hash in=" + in + "| ")
    //val x: String = new String((new Base64) encode (MessageDigest.getInstance("SHA")).digest(in.getBytes("UTF-8")))
    //println ("User hash=" + x + "| ")
    //x
    new String((new Base64) encode (MessageDigest.getInstance("SHA")).digest(in.getBytes("UTF-8")))
  }

  def password: String = this.passwordHash

  def password_=(pw: String) = {
    this.passwordSalt = randomString(16)
    this.passwordHash = hash(pw + this.passwordSalt)
    // AB29-1/vsh []...
    this.uniqueid = randomString(32)
  }

  def authenticate(pw: String) = {
    (hash(pw + this.passwordSalt) == this.passwordHash) && (this.validated)
    //hash(pw + this.passwordSalt) == this.passwordHash && isActive
  }

  def setValidation: String = {
    this.validated = false   // B209-3
    this.validationCode = randomString(32)
    this.validationExpiry = System.currentTimeMillis + 7*24*3600*1000 // seven days // old value was 7400000L
    this.validationCode
  }

  @PrePersist
  def nullValidation = {
    if (this.validationCode.length > 0 && this.validationExpiry <
      System.currentTimeMillis()) {
      this.validationCode = null
      this.validationExpiry = 0
    }
  }

  /**
   * Create a random string of a given size
   * AB29-1/vsh: copied: from net.liftweb.util.StringHelpers
   */
  /*random numbers generator */
  //private def random = new java.security.SecureRandom

  def randomString(size: Int): String = {
    val random = new java.security.SecureRandom
    def addChar(pos: Int, lastRand: Int, sb: StringBuilder): StringBuilder = {
      if (pos >= size) sb
      else {
        val randNum = if ((pos % 6) == 0) random.nextInt else lastRand
        sb.append((randNum & 0x1f) match {
          case n if n < 26 => ('A' + n).toChar
          case n if n < 52 => ('a' + (n - 26)).toChar
          case n => ('0' + (n - 26)).toChar
        })
        addChar(pos + 1, randNum >> 5, sb)
      }
    }
    addChar(0, 0, new StringBuilder(size)).toString
  }

  //  @OneToMany(mappedBy="classifier", targetEntity=classOf[ClassValues], cascade=Array(CascadeType.REMOVE))
  //  var classValues : java.util.Set[ClassValues] = new java.util.HashSet[ClassValues]()

  def getSubmitter(): String =
  //"<id_%d.%s.%s.%s>%s".format(this.id,this.firstName,this.lastName,
  //  (new SimpleDateFormat("yyyy-MM-dd")).format(this.birthDate),this.emailAddress)
    "%s,%s,%s,%s,%d,%s".format(this.firstName, this.lastName,
      (new SimpleDateFormat("yyyy-MM-dd")).format(this.birthDate),
      this.emailAddress, this.id,
      (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(new Date())
    )

  def getModifier(): String = getSubmitter()

}

}

}



