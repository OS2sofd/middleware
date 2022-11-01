package dk.digst.digital.post.memolib.model;

import java.util.HashMap;
import java.util.Map;
import lombok.experimental.UtilityClass;

@UtilityClass
public class MeMoMessageClassDictionary {

    private final Map<String, Class<? extends MeMoClass>> meMoClasses = new HashMap<>();

    static {
        meMoClasses.put("Action", Action.class);
        meMoClasses.put("AdditionalContentData", AdditionalContentData.class);
        meMoClasses.put("AdditionalDocument", AdditionalDocument.class);
        meMoClasses.put("AdditionalReplyData", AdditionalReplyData.class);
        meMoClasses.put("Address", Address.class);
        meMoClasses.put("AddressPoint", AddressPoint.class);
        meMoClasses.put("AttentionData", AttentionData.class);
        meMoClasses.put("AttentionPerson", AttentionPerson.class);
        meMoClasses.put("CaseID", CaseId.class);
        meMoClasses.put("ContactInfo", ContactInfo.class);
        meMoClasses.put("ContactPoint", ContactPoint.class);
        meMoClasses.put("ContentData", ContentData.class);
        meMoClasses.put("ContentResponsible", ContentResponsible.class);
        meMoClasses.put("CPRdata", CprData.class);
        meMoClasses.put("CVRdata", CvrData.class);
        meMoClasses.put("Education", Education.class);
        meMoClasses.put("EID", EidData.class);
        meMoClasses.put("EMail", Email.class);
        meMoClasses.put("EntryPoint", EntryPoint.class);
        meMoClasses.put("File", File.class);
        meMoClasses.put("FORMdata", FormData.class);
        meMoClasses.put("ForwardData", ForwardData.class);
        meMoClasses.put("GeneratingSystem", GeneratingSystem.class);
        meMoClasses.put("GlobalLocationNumber", GlobalLocationNumber.class);
        meMoClasses.put("KLEdata", KleData.class);
        meMoClasses.put("MainDocument", MainDocument.class);
        meMoClasses.put("Message", Message.class);
        meMoClasses.put("MessageBody", MessageBody.class);
        meMoClasses.put("MessageHeader", MessageHeader.class);
        meMoClasses.put("MotorVehicle", MotorVehicle.class);
        meMoClasses.put("ProductionUnit", ProductionUnit.class);
        meMoClasses.put("PropertyNumber", PropertyNumber.class);
        meMoClasses.put("Recipient", Recipient.class);
        meMoClasses.put("ReplyData", ReplyData.class);
        meMoClasses.put("Reservation", Reservation.class);
        meMoClasses.put("Sender", Sender.class);
        meMoClasses.put("SEnumber", SeNumber.class);
        meMoClasses.put("SORdata", SorData.class);
        meMoClasses.put("TechnicalDocument", TechnicalDocument.class);
        meMoClasses.put("Telephone", Telephone.class);
        meMoClasses.put("UnstructuredAddress", UnstructuredAddress.class);
    }

    public boolean isMeMoClass(String elementName) {
        return meMoClasses.containsKey(elementName);
    }

    public Class<? extends MeMoClass> getMeMoClass(String elementName) {
        return meMoClasses.get(elementName);
    }
}
