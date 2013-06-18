package lt.node.gedcom.model

import _root_.javax.persistence._

/**
This class represents a user with login privileges on the website.
 */

@Entity
@Table(name = "tstperson")
class TstPerson {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(name = "personId")
  var id: Long = _

  @Column(unique = false, nullable = false, length = 120)
  var nameGivn: String = ""

  @Column(unique = false, nullable = false, length = 120)
  var nameSurn: String = ""

  @ManyToOne
  @JoinColumn(name = "addressId") // inverse = false
  var address: TstAddress = _

  override def toString() = "person[" + id + "] " + nameGivn + " " + nameSurn + " " + address.toString

}