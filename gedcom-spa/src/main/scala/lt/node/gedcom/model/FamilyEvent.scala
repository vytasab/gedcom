package lt.node.gedcom.model

import javax.persistence._

import scala.collection.JavaConversions._
import _root_.scala.xml.NodeSeq

import _root_.net.liftweb._
import http.S
import common._

/**
FAMILY_EVENT_STRUCTURE: = [
n [ ANUL | CENS | DIV | DIVF ] [Y|<NULL>]       { 1:1 }
+1 <<EVENT_DETAIL>>       { 0:1 }
|
n [ ENGA | MARR | MARB | MARC ] [Y|<NULL>]       { 1:1 }
+1 <<EVENT_DETAIL>>       { 0:1 }
|
n [ MARL | MARS ] [Y|<NULL>]       { 1:1 }
+1 <<EVENT_DETAIL>>       { 0:1 }
|
n  EVEN               { 1:1 }
+1 <<EVENT_DETAIL>>       { 0:1 }  ]
 */

@Entity
@Table(name = "familyevent")
class FamilyEvent {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  var id: Long = _

  @Column(unique = false, nullable = false, length = 4)
  var tag: String = ""

  @ManyToOne(fetch = FetchType.LAZY, optional = true)
  var familyevent: Family = _

  @OneToMany(mappedBy = "familyevent", targetEntity = classOf[EventDetail], cascade = Array(CascadeType.ALL))
  var familydetails: java.util.Set[EventDetail] = new java.util.HashSet[EventDetail]()
  // !!!  suboptimal 1-to-1 implementation for PersonEvent-EventDetail

  var submitter = ""

  def setSubmitter(u: User) = this.submitter = u.getSubmitter()


  def getEventDetail(em: EntityManager) = {
    val retrievedEntity: java.util.List[EventDetail] = em.createNamedQuery("findEventDetailByFamilyEvent").
      setParameter("familyevent", this).
      getResultList().asInstanceOf[java.util.List[EventDetail]]
    this.familydetails = new java.util.HashSet[EventDetail](retrievedEntity)
  }

  // AC07-2/vsh workaround for wrong 'eventdetails' setting

  override def toString(/*em: EntityManager*/) = "familyevent:[" + id + "] " + tag + "; " +
    " eventdetail: { " + familydetails.toString() + " }"


  def getClone(): FamilyEventClone = {
    FamilyEventClone(tag, (if (familyevent==null) "0" else familyevent.id.toString))
  }

  def getAuditXml(old: Box[FamilyEventClone]): NodeSeq = {
    val lang = S.locale.getLanguage
    old match {
      case Full(x) =>
        AuditHelper.checkChanges(List(
          ("tag", "", tag, x.tag),
          ("familyevent_id", "", (if (familyevent==null) "0" else familyevent.id.toString), x.familyevent_id)))
      case Empty =>
        AuditHelper.checkAddFields(List(
          ("tag", "", tag),
          ("familyevent_id", "", (if (familyevent==null) "0" else familyevent.id.toString))))
      case _ =>
        NodeSeq.Empty
    }
  }
  def getAuditRec(old: Box[FamilyEventClone]): String = {
    getAuditXml(old).toString
  }


  def toXml(em: EntityManager): NodeSeq = {
    this.getEventDetail(em)
//    <_ lang={S.locale.getLanguage}>
      <fe id={id.toString} tag={tag}>
        {this.familydetails.toList match {
        case x :: xs => {
          x.toXml
        }
        case _ =>
      }}
      </fe>
//    </_>
  }


  def toGedcom(em: EntityManager, levelNumber: Int, lang: String): String = {
    this.getEventDetail(em)
    val txt: StringBuffer = new StringBuffer(<_>{levelNumber} {tag}</_>.text+"\n")
    txt.append(
      this.familydetails.toList match {
        case x :: xs => {
          x.toGedcom(levelNumber+1, lang)
        }
        case _ => ""
      }
    )
    txt.toString
  }

}


case class FamilyEventClone(tag:String, familyevent_id:String)
