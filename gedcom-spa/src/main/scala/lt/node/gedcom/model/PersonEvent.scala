package lt.node.gedcom.model

import javax.persistence._

import scala.collection.JavaConversions._
import _root_.scala.xml.NodeSeq

import _root_.net.liftweb._
import http.S
import common._

/**
INDIVIDUAL_EVENT_STRUCTURE: = [
n[ BIRT | CHR ] [Y|<NULL>]      { 1:1 }
+1 <<EVENT_DETAIL>>      { 0:1 }
+1 FAMC @<XREF:FAM>@      { 0:1 }
|
n  [ DEAT | BURI | CREM ] [Y|<NULL>]       { 1:1 }
+1 <<EVENT_DETAIL>>      { 0:1 }
|
n  ADOP [Y|<NULL>]      { 1:1 }
+1 <<EVENT_DETAIL>>      { 0:1 }
+1 FAMC @<XREF:FAM>@      { 0:1 }
+2 ADOP <ADOPTED_BY_WHICH_PARENT>      { 0:1 }
|
n  [ BAPM | BARM | BASM | BLES ] [Y|<NULL>]      { 1:1 }
+1 <<EVENT_DETAIL>>      { 0:1 }
|
n  [ CHRA | CONF | FCOM | ORDN ] [Y|<NULL>]      { 1:1 }
+1 <<EVENT_DETAIL>>      { 0:1 }
|
n  [ NATU | EMIG | IMMI ] [Y|<NULL>]      { 1:1 }
+1 <<EVENT_DETAIL>>      { 0:1 }
|
n  [ CENS | PROB | WILL] [Y|<NULL>]      { 1:1 }
+1 <<EVENT_DETAIL>>      { 0:1 }
|
n  [ GRAD | RETI ] [Y|<NULL>]      { 1:1 }
+1 <<EVENT_DETAIL>>      { 0:1 }
|
n  EVEN              { 1:1 }
+1 <<EVENT_DETAIL>>      { 0:1 }  ]
 */

@Entity
@Table(name = "personevent")
class PersonEvent {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  var id: Long = _

  @Column(unique = false, nullable = false, length = 4)
  var tag: String = ""
  // BIRT CHR   DEAT BURI CREM   ADOP   NATU EMIG IMMI  GRAD RETI  EVEN

  /**/
  // when tag is BIRT CHR ADOP
  @Column(unique = false, nullable = true)
  var familyId: Long = 0L

  // when tag is  ADOP; possible valuies: [ HUSB | WIFE | BOTH ]
  @Column(unique = false, nullable = true, length = 4)
  var adoptedBy: String = ""
  /**/

  @ManyToOne(fetch = FetchType.LAZY, optional = true)
  var personevent: Person = _

  @OneToMany(mappedBy = "personevent", targetEntity = classOf[EventDetail], cascade = Array(CascadeType.ALL))
  var eventdetails: java.util.Set[EventDetail] = new java.util.HashSet[EventDetail]()
  // !!!  suboptimal 1-to-1 implementation for PersonEvent-EventDetail

  var submitter = ""

  def setSubmitter(u: User) = this.submitter = u.getSubmitter()

  def getEventDetail(em: EntityManager) = {
    val retrievedEntity: java.util.List[EventDetail] = em.createNamedQuery("findEventDetailByPersonEvent").
      setParameter("personevent", this).
      getResultList().asInstanceOf[java.util.List[EventDetail]]
    this.eventdetails = new java.util.HashSet[EventDetail](retrievedEntity)
  }

  // AC07-2/vsh workaround for wrong 'eventdetails' setting

  override def toString(/*em: EntityManager*/) = "personevent:[" + id + "] " + tag + "; " +
    " eventdetail: { " + eventdetails.toString() + " }"
  //""


  def getClone(): PersonEventClone = {
    PersonEventClone (tag, familyId.toString, adoptedBy,  (if (personevent==null) "0" else personevent.id.toString))
  }

  def getAuditXml(old: Box[PersonEventClone]): NodeSeq = {
    val lang = S.locale.getLanguage
    old match {
      case Full(x) =>
        AuditHelper.checkChanges(List(
          ("tag", "", tag, x.tag),
          ("familyId", "", familyId.toString, x.familyId.toString),
          ("adoptedBy", "", adoptedBy, x.adoptedBy),
          ("personevent_id", "", (if (personevent==null) "0" else personevent.id.toString), x.personevent_id)))
      case Empty =>
        AuditHelper.checkAddFields(List(
          ("tag", "", tag),
          ("familyId", "", familyId.toString),
          ("adoptedBy", "", adoptedBy),
          ("personevent_id", "", (if (personevent==null) "0" else personevent.id.toString))))
      case _ =>
        NodeSeq.Empty
    }
  }
  def getAuditRec(old: Box[PersonEventClone]): String = {
    getAuditXml(old).toString
  }


  def toXml(em: EntityManager): NodeSeq = {
    this.getEventDetail(em)
//    <_ lang={S.locale.getLanguage}>
      <pe id={id.toString} tag={tag}>
        {this.eventdetails.toList match {
        case x :: xs => {
          x.toXml
        }
        case _ =>
      }}
      </pe>
//    </_>
  }


  def toGedcom(em: EntityManager, levelNumber: Int, lang: String): String = {
    this.getEventDetail(em)
    println(<_>===AAA===|{this.toString()}|===AAA===</_>.text)
    val txt: StringBuffer = new StringBuffer(<_>{levelNumber} {tag}</_>.text+"\n")
    txt.append(
      this.eventdetails.toList match {
        case x :: xs => {
          x.toGedcom(levelNumber+1, lang).toString()
        }
        case _ => """"""
      }
    )
    println(<_>===BBB===|{txt.toString}|===BBB===</_>.text)
    txt.toString
  }

}


case class PersonEventClone (tag:String, familyId:String, adoptedBy:String,  personevent_id:String)
