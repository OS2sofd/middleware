//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2018.11.13 at 11:43:37 AM CET 
//


package dk.kmd.opus;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import java.math.BigDecimal;


/**
 * This object contains factory methods for each
 * Java content interface and Java element interface
 * generated in the dk.kmd.opus package.
 * <p>An ObjectFactory allows you to programatically
 * construct new instances of the Java representation
 * for XML content. The Java representation of XML
 * content can consist of schema derived interfaces
 * and classes representing the binding of schema
 * type definitions, element declarations and model
 * groups.  Factory methods for each of these are
 * provided in this class.
 */
@XmlRegistry
public class ObjectFactory
{

    private final static QName _ZipCode_QNAME = new QName("", "zipCode");
    private final static QName _Country_QNAME = new QName("", "country");
    private final static QName _SeNr_QNAME = new QName("", "seNr");
    private final static QName _EndDate_QNAME = new QName("", "endDate");
    private final static QName _PostalCode_QNAME = new QName("", "postalCode");
    private final static QName _EanNr_QNAME = new QName("", "eanNr");
    private final static QName _PositionShort_QNAME = new QName("", "positionShort");
    private final static QName _OrgType_QNAME = new QName("", "orgType");
    private final static QName _Members_QNAME = new QName("", "members");
    private final static QName _IsManager_QNAME = new QName("", "isManager");
    private final static QName _OrgTypeTxt_QNAME = new QName("", "orgTypeTxt");
    private final static QName _AddressSupplement_QNAME = new QName("", "addressSupplement");
    private final static QName _EntryDate_QNAME = new QName("", "entryDate");
    private final static QName _CostCenter_QNAME = new QName("", "costCenter");
    private final static QName _InitialEntry_QNAME = new QName("", "initialEntry");
    private final static QName _CvrNr_QNAME = new QName("", "cvrNr");
    private final static QName _InvoiceRecipient_QNAME = new QName("", "invoiceRecipient");
    private final static QName _Numerator_QNAME = new QName("", "numerator");
    private final static QName _Denominator_QNAME = new QName("", "denominator");
    private final static QName _FirstName_QNAME = new QName("", "firstName");
    private final static QName _ParentOrgUnit_QNAME = new QName("", "parentOrgUnit");
    private final static QName _PayGradeText_QNAME = new QName("", "payGradeText");
    private final static QName _PhoneNumber_QNAME = new QName("", "phoneNumber");
    private final static QName _Position_QNAME = new QName("", "position");
    private final static QName _ShortName_QNAME = new QName("", "shortName");
    private final static QName _WorkContractText_QNAME = new QName("", "workContractText");
    private final static QName _StartDate_QNAME = new QName("", "startDate");
    private final static QName _LastName_QNAME = new QName("", "lastName");
    private final static QName _SubordinateLevel_QNAME = new QName("", "subordinateLevel");
    private final static QName _City_QNAME = new QName("", "city");
    private final static QName _ProductionNumber_QNAME = new QName("", "productionNumber");
    private final static QName _PNr_QNAME = new QName("", "pNr");
    private final static QName _Street_QNAME = new QName("", "street");
    private final static QName _EntryIntoGroup_QNAME = new QName("", "entryIntoGroup");
    private final static QName _WorkContract_QNAME = new QName("", "workContract");
    private final static QName _Email_QNAME = new QName("", "email");
    private final static QName _RoleText_QNAME = new QName("", "roleText");
    private final static QName _InvoiceLevel1Text_QNAME = new QName("", "invoiceLevel1Text");
    private final static QName _RoleId_QNAME = new QName("", "roleId");
    private final static QName _UserId_QNAME = new QName("", "userId");
    private final static QName _SuperiorLevel_QNAME = new QName("", "superiorLevel");
    private final static QName _PositionId_QNAME = new QName("", "positionId");
    private final static QName _LeaveDate_QNAME = new QName("", "leaveDate");
    private final static QName _OrgDaekning_QNAME = new QName("", "orgDaekning");
    private final static QName _WorkPhone_QNAME = new QName("", "workPhone");
    private final static QName _InvoiceLevel2Text_QNAME = new QName("", "invoiceLevel2Text");
    private final static QName _InvoiceLevel2_QNAME = new QName("", "invoiceLevel2");
    private final static QName _InvoiceLevel1_QNAME = new QName("", "invoiceLevel1");
    private final static QName _ArtText_QNAME = new QName("", "artText");
    private final static QName _LongName_QNAME = new QName("", "longName");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: dk.kmd.opus
     */
    public ObjectFactory()
    {
    }

    /**
     * Create an instance of {@link Employee }
     */
    public Employee createEmployee()
    {
        return new Employee();
    }

    /**
     * Create an instance of {@link Cpr }
     */
    public Cpr createCpr()
    {
        return new Cpr();
    }

    /**
     * Create an instance of {@link Address }
     */
    public Address createAddress()
    {
        return new Address();
    }

    /**
     * Create an instance of {@link Function }
     */
    public Function createFunction()
    {
        return new Function();
    }

    /**
     * Create an instance of {@link OrgUnit }
     */
    public OrgUnit createOrgUnit()
    {
        return new OrgUnit();
    }

    /**
     * Create an instance of {@link Kmd }
     */
    public Kmd createKmd()
    {
        return new Kmd();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Short }{@code >}}
     */
    @XmlElementDecl(namespace = "", name = "zipCode")
    public JAXBElement<Short> createZipCode(Short value)
    {
        return new JAXBElement<Short>(_ZipCode_QNAME, Short.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     */
    @XmlElementDecl(namespace = "", name = "country")
    public JAXBElement<String> createCountry(String value)
    {
        return new JAXBElement<String>(_Country_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Integer }{@code >}}
     */
    @XmlElementDecl(namespace = "", name = "seNr")
    public JAXBElement<Integer> createSeNr(Integer value)
    {
        return new JAXBElement<Integer>(_SeNr_QNAME, Integer.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link XMLGregorianCalendar }{@code >}}
     */
    @XmlElementDecl(namespace = "", name = "endDate")
    public JAXBElement<XMLGregorianCalendar> createEndDate(XMLGregorianCalendar value)
    {
        return new JAXBElement<XMLGregorianCalendar>(_EndDate_QNAME, XMLGregorianCalendar.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     */
    @XmlElementDecl(namespace = "", name = "postalCode")
    public JAXBElement<String> createPostalCode(String value)
    {
        return new JAXBElement<String>(_PostalCode_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Long }{@code >}}
     */
    @XmlElementDecl(namespace = "", name = "eanNr")
    public JAXBElement<Long> createEanNr(Long value)
    {
        return new JAXBElement<Long>(_EanNr_QNAME, Long.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     */
    @XmlElementDecl(namespace = "", name = "positionShort")
    public JAXBElement<String> createPositionShort(String value)
    {
        return new JAXBElement<String>(_PositionShort_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Byte }{@code >}}
     */
    @XmlElementDecl(namespace = "", name = "orgType")
    public JAXBElement<Byte> createOrgType(Byte value)
    {
        return new JAXBElement<Byte>(_OrgType_QNAME, Byte.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Byte }{@code >}}
     */
    @XmlElementDecl(namespace = "", name = "members")
    public JAXBElement<Byte> createMembers(Byte value)
    {
        return new JAXBElement<Byte>(_Members_QNAME, Byte.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     */
    @XmlElementDecl(namespace = "", name = "isManager")
    public JAXBElement<Boolean> createIsManager(Boolean value)
    {
        return new JAXBElement<Boolean>(_IsManager_QNAME, Boolean.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     */
    @XmlElementDecl(namespace = "", name = "orgTypeTxt")
    public JAXBElement<String> createOrgTypeTxt(String value)
    {
        return new JAXBElement<String>(_OrgTypeTxt_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     */
    @XmlElementDecl(namespace = "", name = "addressSupplement")
    public JAXBElement<String> createAddressSupplement(String value)
    {
        return new JAXBElement<String>(_AddressSupplement_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     */
    @XmlElementDecl(namespace = "", name = "entryDate")
    public JAXBElement<String> createEntryDate(String value)
    {
        return new JAXBElement<String>(_EntryDate_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Long }{@code >}}
     */
    @XmlElementDecl(namespace = "", name = "costCenter")
    public JAXBElement<Long> createCostCenter(Long value)
    {
        return new JAXBElement<Long>(_CostCenter_QNAME, Long.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     */
    @XmlElementDecl(namespace = "", name = "initialEntry")
    public JAXBElement<String> createInitialEntry(String value)
    {
        return new JAXBElement<String>(_InitialEntry_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Integer }{@code >}}
     */
    @XmlElementDecl(namespace = "", name = "cvrNr")
    public JAXBElement<Integer> createCvrNr(Integer value)
    {
        return new JAXBElement<Integer>(_CvrNr_QNAME, Integer.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     */
    @XmlElementDecl(namespace = "", name = "invoiceRecipient")
    public JAXBElement<Boolean> createInvoiceRecipient(Boolean value)
    {
        return new JAXBElement<Boolean>(_InvoiceRecipient_QNAME, Boolean.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BigDecimal }{@code >}}
     */
    @XmlElementDecl(namespace = "", name = "numerator")
    public JAXBElement<BigDecimal> createNumerator(BigDecimal value)
    {
        return new JAXBElement<BigDecimal>(_Numerator_QNAME, BigDecimal.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BigDecimal }{@code >}}
     */
    @XmlElementDecl(namespace = "", name = "denominator")
    public JAXBElement<BigDecimal> createDenominator(BigDecimal value)
    {
        return new JAXBElement<BigDecimal>(_Denominator_QNAME, BigDecimal.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     */
    @XmlElementDecl(namespace = "", name = "firstName")
    public JAXBElement<String> createFirstName(String value)
    {
        return new JAXBElement<String>(_FirstName_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     */
    @XmlElementDecl(namespace = "", name = "parentOrgUnit")
    public JAXBElement<String> createParentOrgUnit(String value)
    {
        return new JAXBElement<String>(_ParentOrgUnit_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     */
    @XmlElementDecl(namespace = "", name = "payGradeText")
    public JAXBElement<String> createPayGradeText(String value)
    {
        return new JAXBElement<String>(_PayGradeText_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     */
    @XmlElementDecl(namespace = "", name = "phoneNumber")
    public JAXBElement<String> createPhoneNumber(String value)
    {
        return new JAXBElement<String>(_PhoneNumber_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     */
    @XmlElementDecl(namespace = "", name = "position")
    public JAXBElement<String> createPosition(String value)
    {
        return new JAXBElement<String>(_Position_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     */
    @XmlElementDecl(namespace = "", name = "shortName")
    public JAXBElement<String> createShortName(String value)
    {
        return new JAXBElement<String>(_ShortName_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     */
    @XmlElementDecl(namespace = "", name = "workContractText")
    public JAXBElement<String> createWorkContractText(String value)
    {
        return new JAXBElement<String>(_WorkContractText_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link XMLGregorianCalendar }{@code >}}
     */
    @XmlElementDecl(namespace = "", name = "startDate")
    public JAXBElement<XMLGregorianCalendar> createStartDate(XMLGregorianCalendar value)
    {
        return new JAXBElement<XMLGregorianCalendar>(_StartDate_QNAME, XMLGregorianCalendar.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     */
    @XmlElementDecl(namespace = "", name = "lastName")
    public JAXBElement<String> createLastName(String value)
    {
        return new JAXBElement<String>(_LastName_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Byte }{@code >}}
     */
    @XmlElementDecl(namespace = "", name = "subordinateLevel")
    public JAXBElement<Byte> createSubordinateLevel(Byte value)
    {
        return new JAXBElement<Byte>(_SubordinateLevel_QNAME, Byte.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     */
    @XmlElementDecl(namespace = "", name = "city")
    public JAXBElement<String> createCity(String value)
    {
        return new JAXBElement<String>(_City_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     */
    @XmlElementDecl(namespace = "", name = "productionNumber")
    public JAXBElement<String> createProductionNumber(String value)
    {
        return new JAXBElement<String>(_ProductionNumber_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Integer }{@code >}}
     */
    @XmlElementDecl(namespace = "", name = "pNr")
    public JAXBElement<Integer> createPNr(Integer value)
    {
        return new JAXBElement<Integer>(_PNr_QNAME, Integer.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     */
    @XmlElementDecl(namespace = "", name = "street")
    public JAXBElement<String> createStreet(String value)
    {
        return new JAXBElement<String>(_Street_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     */
    @XmlElementDecl(namespace = "", name = "entryIntoGroup")
    public JAXBElement<String> createEntryIntoGroup(String value)
    {
        return new JAXBElement<String>(_EntryIntoGroup_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     */
    @XmlElementDecl(namespace = "", name = "workContract")
    public JAXBElement<String> createWorkContract(String value)
    {
        return new JAXBElement<String>(_WorkContract_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     */
    @XmlElementDecl(namespace = "", name = "email")
    public JAXBElement<String> createEmail(String value)
    {
        return new JAXBElement<String>(_Email_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     */
    @XmlElementDecl(namespace = "", name = "roleText")
    public JAXBElement<String> createRoleText(String value)
    {
        return new JAXBElement<String>(_RoleText_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     */
    @XmlElementDecl(namespace = "", name = "invoiceLevel1Text")
    public JAXBElement<String> createInvoiceLevel1Text(String value)
    {
        return new JAXBElement<String>(_InvoiceLevel1Text_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Integer }{@code >}}
     */
    @XmlElementDecl(namespace = "", name = "roleId")
    public JAXBElement<Integer> createRoleId(Integer value)
    {
        return new JAXBElement<Integer>(_RoleId_QNAME, Integer.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     */
    @XmlElementDecl(namespace = "", name = "userId")
    public JAXBElement<String> createUserId(String value)
    {
        return new JAXBElement<String>(_UserId_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Byte }{@code >}}
     */
    @XmlElementDecl(namespace = "", name = "superiorLevel")
    public JAXBElement<Byte> createSuperiorLevel(Byte value)
    {
        return new JAXBElement<Byte>(_SuperiorLevel_QNAME, Byte.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Integer }{@code >}}
     */
    @XmlElementDecl(namespace = "", name = "positionId")
    public JAXBElement<Integer> createPositionId(Integer value)
    {
        return new JAXBElement<Integer>(_PositionId_QNAME, Integer.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     */
    @XmlElementDecl(namespace = "", name = "leaveDate")
    public JAXBElement<String> createLeaveDate(String value)
    {
        return new JAXBElement<String>(_LeaveDate_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     */
    @XmlElementDecl(namespace = "", name = "orgDaekning")
    public JAXBElement<String> createOrgDaekning(String value)
    {
        return new JAXBElement<String>(_OrgDaekning_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     */
    @XmlElementDecl(namespace = "", name = "workPhone")
    public JAXBElement<String> createWorkPhone(String value)
    {
        return new JAXBElement<String>(_WorkPhone_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     */
    @XmlElementDecl(namespace = "", name = "invoiceLevel2Text")
    public JAXBElement<String> createInvoiceLevel2Text(String value)
    {
        return new JAXBElement<String>(_InvoiceLevel2Text_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     */
    @XmlElementDecl(namespace = "", name = "invoiceLevel2")
    public JAXBElement<String> createInvoiceLevel2(String value)
    {
        return new JAXBElement<String>(_InvoiceLevel2_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     */
    @XmlElementDecl(namespace = "", name = "invoiceLevel1")
    public JAXBElement<String> createInvoiceLevel1(String value)
    {
        return new JAXBElement<String>(_InvoiceLevel1_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     */
    @XmlElementDecl(namespace = "", name = "artText")
    public JAXBElement<String> createArtText(String value)
    {
        return new JAXBElement<String>(_ArtText_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     */
    @XmlElementDecl(namespace = "", name = "longName")
    public JAXBElement<String> createLongName(String value)
    {
        return new JAXBElement<String>(_LongName_QNAME, String.class, null, value);
    }

}