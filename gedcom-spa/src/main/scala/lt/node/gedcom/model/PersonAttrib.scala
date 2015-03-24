package lt.node.gedcom.model

import javax.persistence._

import scala.collection.JavaConversions._
import _root_.scala.xml._

import _root_.net.liftweb._
import http.S
import common._

/* INDIVIDUAL_ATTRIBUTE_STRUCTURE: = [
  n  CAST <CASTE_NAME>   {1:1}                +1 <<EVENT_DETAIL>>  {0:1}  |
  n  DSCR <PHYSICAL_DESCRIPTION>   {1:1}      +1 <<EVENT_DETAIL>>  {0:1}  |
  n  EDUC <SCHOLASTIC_ACHIEVEMENT>   {1:1}    +1 <<EVENT_DETAIL>>  {0:1}  |
  n  IDNO <NATIONAL_ID_NUMBER>   {1:1}*       +1 <<EVENT_DETAIL>>  {0:1}  |
  n  NATI <NATIONAL_OR_TRIBAL_ORIGIN>   {1:1} +1 <<EVENT_DETAIL>>  {0:1}  |
  n  NCHI <COUNT_OF_CHILDREN>   {1:1}         +1 <<EVENT_DETAIL>>  {0:1}  |
  n  NMR <COUNT_OF_MARRIAGES>   {1:1}         +1 <<EVENT_DETAIL>>  {0:1}  |
  n  OCCU <OCCUPATION>   {1:1}                +1 <<EVENT_DETAIL>>  {0:1}  |
  n  PROP <POSSESSIONS>   {1:1}               +1 <<EVENT_DETAIL>>  {0:1}  |
  n  RELI <RELIGIOUS_AFFILIATION>   {1:1}     +1 <<EVENT_DETAIL>>  {0:1}  |
  n  RESI           {1:1}                     +1 <<EVENT_DETAIL>>  {0:1}  |
  n  SSN <SOCIAL_SECURITY_NUMBER>   {0:1}     +1 <<EVENT_DETAIL>>  {0:1}  |
  n  TITL <NOBILITY_TYPE_TITLE>  {1:1}        +1 <<EVENT_DETAIL>>  {0:1}  ]   */

@Entity
@Table(name = "personattrib")
class PersonAttrib extends MultiLang {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  var id: Long = _

  @Column(unique = false, nullable = false, length = 4)
  var tag: String = ""

  @Column(unique = false, nullable = true, length = 32000)
  var tagValue: String = ""

  @ManyToOne(fetch = FetchType.LAZY, optional = true)
  var personattrib: Person = _

  @OneToMany(mappedBy = "personattrib", targetEntity = classOf[EventDetail], cascade = Array(CascadeType.ALL))
  var attribdetails: java.util.Set[EventDetail] = new java.util.HashSet[EventDetail]()
  // !!!  suboptimal 1-to-1 implementation for PersonAttrib-EventDetail

  var submitter = ""

  def setSubmitter(u: User) = this.submitter = u.getSubmitter()

  def getAttribDetail(em: EntityManager) = {
  // AC08-3/vsh workaround for wrong 'attribdetails' setting
    val retrievedEntity: java.util.List[EventDetail] = em.createNamedQuery("findEventDetailByPersonAttrib").
      setParameter("personattrib", this).
      getResultList().asInstanceOf[java.util.List[EventDetail]]
    this.attribdetails = new java.util.HashSet[EventDetail](retrievedEntity)
  }

  override def toString(/*em: EntityManager*/) = "personattrinb:[" + id + "] " + tag + "; " +
    " attribdetail: { " + attribdetails.toString() + " }"


  def getClone(): PersonAttribClone = {
    PersonAttribClone (tag, tagValue, (if (personattrib==null) "0" else personattrib.id.toString))
  }

  def getAuditXml(old: Box[PersonAttribClone]): NodeSeq = {
    val lang = S.locale.getLanguage
    old match {
      case Full(x) =>
        AuditHelper.checkChanges(List(
          ("tag", "", tag, x.tag),
          ("tagValue", lang, tagValue, x.tagValue),
          ("personattrib_id", "", (if (personattrib==null) "0" else personattrib.id.toString), x.personattrib_id)))
      case Empty =>
        AuditHelper.checkAddFields(List(
          ("tag", "", tag),
          ("tagValue", lang, tagValue),
          ("personattrib_id", "", (if (personattrib==null) "0" else personattrib.id.toString))))
      case _ =>
        NodeSeq.Empty
    }
  }
  def getAuditRec(old: Box[PersonAttribClone]): String = {
    getAuditXml(old).toString
  }


  def toXml(em: EntityManager): NodeSeq = {
    this.getAttribDetail(em)
//    <_ lang={S.locale.getLanguage}>
      <pa id={id.toString} tag={tag}>
        <tagValue>{Unparsed(tagValue)}</tagValue>
        {this.attribdetails.toList match {
        case x :: xs => {
          x.toXml
        }
        case _ =>
      }}
      </pa>
//    </_>
  }


  def toGedcom(em: EntityManager, levelNumber: Int, lang: String): String = {
    this.getAttribDetail(em)
    val txt: StringBuffer = new StringBuffer(<_>{levelNumber} {tag}</_>.text/*+"\n"*/)
    //Unparsed(avoidNull(tagValue)).length > 0 match {
    txt.append{
      (avoidEmpty(tagValue)).length > 0 match {
        case true => /*txt.append*/(<_> {getLangText(tagValue, lang)}</_>).text+"\n"
        case _ => "\n"
      }
    }
    txt.append(
      this.attribdetails.toList match {
        case x :: xs => {
          x.toGedcom(levelNumber+1, lang)
        }
        case _ => """"""
      }
    )
    txt.toString
  }

}


case class PersonAttribClone (tag:String, tagValue:String, personattrib_id:String)
