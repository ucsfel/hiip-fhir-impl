package edu.ucsf.hiip.fhir;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;

import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.mule.transformer.AbstractMessageTransformer;

import com.mulesoft.mule.transport.hl7.HL7Encoding;
import com.mulesoft.mule.transport.hl7.util.HL7FormatConverter;
//import com.mulesoft.mule.transport.hl7.util.XMLNSParser;










import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.model.DataTypeException;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v251.message.MFN_M02;
import ca.uhn.hl7v2.model.v251.segment.PRA;
import ca.uhn.hl7v2.model.v251.segment.STF;
import ca.uhn.hl7v2.model.v251.segment.PV1;
import ca.uhn.hl7v2.model.v251.segment.PID;
import ca.uhn.hl7v2.model.v251.segment.NK1;
import ca.uhn.hl7v2.model.v251.datatype.CWE;
import ca.uhn.hl7v2.model.v251.datatype.PLN;
import ca.uhn.hl7v2.model.v251.datatype.SPD;
import ca.uhn.hl7v2.model.v251.datatype.TS;
import ca.uhn.hl7v2.model.v251.datatype.XPN;
import ca.uhn.hl7v2.model.v251.datatype.DTM;
import ca.uhn.hl7v2.model.v251.datatype.XTN;
import ca.uhn.hl7v2.model.v251.datatype.XAD;
import ca.uhn.hl7v2.parser.CanonicalModelClassFactory;
import ca.uhn.hl7v2.parser.DefaultModelClassFactory;
import ca.uhn.hl7v2.parser.GenericParser;
import ca.uhn.hl7v2.parser.Parser;
import ca.uhn.hl7v2.parser.PipeParser;
import ca.uhn.hl7v2.validation.impl.NoValidation;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.hl7.fhir.instance.formats.JsonComposer;
import org.hl7.fhir.instance.formats.JsonParser;
import org.hl7.fhir.instance.model.CodeableConcept;
import org.hl7.fhir.instance.model.DateAndTime;
import org.hl7.fhir.instance.model.HumanName;
import org.hl7.fhir.instance.model.Identifier;
import org.hl7.fhir.instance.model.Practitioner;
import org.hl7.fhir.instance.model.Address;


public class MfnToPractitionerArray  extends AbstractMessageTransformer {
	//private Convert convert;
	
    private String hl7encoding;	/* ER7, XML or HAPI */
    
    private boolean enableValidation = false;
    
    public String getHl7encoding() {
        return hl7encoding;
    }

    public void setHl7encoding(String hl7encoding) {
        this.hl7encoding = hl7encoding;
    }
    
    public void setEnableValidation(boolean enableValidation) {
        this.enableValidation = enableValidation;
    }

    public boolean isEnableValidation() {
        return enableValidation;
    }
    
     public Object transformMessage(MuleMessage message, String hl7Encoding) throws TransformerException {
			
		
    	//String payload = message.getPayload().toString();
    	//payload = payload.replace('\n', '\r');

    	hl7encoding = "ER7";  //TODO: verify if this is defined by previous component
    	
        Object encodedMessage = null;
        Message hapiMsg = null;
		try {
			encodedMessage = convertToDefaultEncoding(message.getPayload());
			hapiMsg = toHAPIMessage(encodedMessage, isEnableValidation());
        
			//convert the Generic parsed message to the "highest" version: 2.5.1           
			ca.uhn.hl7v2.model.v251.message.MFN_M02 mfnMsg = new ca.uhn.hl7v2.model.v251.message.MFN_M02();// hapiMsg;
			mfnMsg.parse(hapiMsg.toString());
	        System.out.println(mfnMsg.printStructure());//mfnMsg.getMSH().getMessageControlID().getValue());
	        
	        Practitioner practitioner = this.createPractitioner(mfnMsg.getMF_STAFF(0).getPRA(0), mfnMsg.getMF_STAFF(0).getSTF());
           	//TODO:create a bundle to include 
        	ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	        JsonComposer jsonComposer = new JsonComposer();
            jsonComposer.compose(outputStream, practitioner, true);
           			
            String jsonMsg = new String(outputStream.toByteArray());
	        message.setPayload(jsonMsg);

		} catch (HL7Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


    	return message;
    }
	private Message toHAPIMessage(Object hl7Message, boolean enableValidation) throws HL7Exception {

		Message response = null;

		if (hl7Message instanceof Message) {
			response = (Message)hl7Message;
		} else if (hl7Message instanceof java.lang.String) {
			String payload = hl7Message.toString();
			Parser p = null;

//			if (HL7FormatConverter.getHL7Encoding(hl7Message) == HL7Encoding.XML)
//				p = new XMLNSParser();
//			else
				p = new GenericParser();
			if (!enableValidation) {
				logger.debug("Validation disabled");
				p.setValidationContext(new NoValidation());
			}    
			response = p.parse(payload);
		}

		if (response == null)
			throw new HL7Exception("Unsupported message: " + hl7Message, HL7Exception.UNSUPPORTED_MESSAGE_TYPE);

		return response;
	}

    private Object convertToDefaultEncoding(Object transportMessage) throws Exception
    {
        //logger.info("*** ENCODING IS " + hl7encoding);
        
        if (transportMessage instanceof java.lang.String) {
            String hl7String = (String)transportMessage;

            //check if it is ER7 or XML
            if ((hl7String.indexOf("?xml") != -1) && HL7Encoding.XML.toString().equalsIgnoreCase(hl7encoding)) {
                return hl7String;
            } else if ((hl7String.trim().startsWith("MSH|")) && HL7Encoding.ER7.toString().equalsIgnoreCase(hl7encoding)) {
                return hl7String.replace('\n', '\r');
            } else {
                return HL7FormatConverter.toDefaultEncoding(hl7String, HL7Encoding.valueOf(hl7encoding), isEnableValidation());
            }
        }

        return transportMessage;
    }
    
	private Practitioner createPractitioner(PRA pra, STF stf) {
		Practitioner practitioner = new Practitioner();
		//identifier	PRA-6 (or PLN Practitioner ID Numbers)
		practitioner = Hl7v2ToFhir.createPractitionerIdentifiers(practitioner, Arrays.asList(pra.getPractitionerIDNumbers()));
		
		for (PLN pln : Arrays.asList(pra.getPractitionerIDNumbers())){
			Identifier id = practitioner.addIdentifier().setValueSimple(pln.getIDNumber().toString());
			id.setSystemSimple(pln.getTypeOfIDNumber().toString());
		}
				
		//name	XPN Components
		HumanName name = Hl7v2ToFhir.createHumanName(stf.getStaffName(0));
		practitioner.setName(name);
		
		//telecom	STF-10, ROL-12
		Hl7v2ToFhir.createPractitionerTelecom(practitioner, Arrays.asList(stf.getPhone()));
		
		//address	STF-11, ROL-11
		List<Address> addrs = Hl7v2ToFhir.createAddress(Arrays.asList(stf.getOfficeHomeAddressBirthplace()));
		practitioner.setAddress(addrs.get(0));
		
		//gender	STF-5
		CodeableConcept cc = new CodeableConcept();
		cc.setTextSimple(stf.getAdministrativeSex().toString());
		practitioner.setGender(cc);
		
		//birthDate	STF-6
		DateAndTime dat = null;
		try {
			String str = stf.getDateTimeOfBirth().getTime().getValue();
			if (str != null) {
				dat = DateAndTime.parseV3(str);
				practitioner.setBirthDateSimple(dat);
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		
		//photo	
		//organization	PRD-10 (-> 14) / PRA-9-Institution / PRT-8-Participation Organization for person participation
		//role			STF-18-job title / PRA-3-practitioner category / ROL-3-role-ROL
		Hl7v2ToFhir.createPractitionerCategory(practitioner, Arrays.asList(pra.getPractitionerCategory()));
		
		//specialty		PRA-5-specialty
		Hl7v2ToFhir.createPractitionerSpecialty(practitioner, Arrays.asList(pra.getSpecialty()));
		
		//period		PRA-5.4-date of certification
		//location	
		//qualification	CER?
		//       code	
		//       period	
		//       issuer	
		//communication	LAN-2
		
		return practitioner;
	}
}
