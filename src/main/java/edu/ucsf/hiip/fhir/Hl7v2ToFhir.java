package edu.ucsf.hiip.fhir;

import java.util.ArrayList;
import java.util.List;

import ca.uhn.hl7v2.model.v251.datatype.IS;
import ca.uhn.hl7v2.model.v251.datatype.PLN;
import ca.uhn.hl7v2.model.v251.datatype.SPD;
import ca.uhn.hl7v2.model.v251.datatype.XPN;
import ca.uhn.hl7v2.model.v251.datatype.XTN;
import ca.uhn.hl7v2.model.v251.datatype.XAD;

import org.hl7.fhir.instance.model.CodeableConcept;
//import org.hl7.fhir.instance.model.ValueSet;
import org.hl7.fhir.instance.model.Practitioner;
import org.hl7.fhir.instance.model.Identifier;
import org.hl7.fhir.instance.model.HumanName;
import org.hl7.fhir.instance.model.Contact;
import org.hl7.fhir.instance.model.Contact.ContactSystem;
import org.hl7.fhir.instance.model.Address;



public class Hl7v2ToFhir {

	static public Practitioner createPractitionerIdentifiers(Practitioner practitioner, List<PLN> plns) {
		List<Identifier> identifiers = new ArrayList<Identifier>();
		
		for (PLN pln : plns) {
			Identifier id = practitioner.addIdentifier().setValueSimple(pln.getIDNumber().toString());
			id.setSystemSimple(pln.getTypeOfIDNumber().toString());
		}
		
		return practitioner;
	}
	
	static public HumanName createHumanName(XPN xpn) {
		HumanName humanName = new HumanName();
		humanName.addFamilySimple(xpn.getFamilyName().getFn1_Surname().toString());
		humanName.addGivenSimple(xpn.getGivenName().toString());
		
		return humanName;
	}
	
	static public void createPractitionerTelecom(Practitioner practitioner, List<XTN> xtns) {
		for (XTN xtn : xtns) {
			Contact contact = practitioner.addTelecom();
			contact.setValueSimple(xtn.getXtn1_TelephoneNumber().toString());
			ContactSystem cs = mapTelecommunicationType(xtn.getTelecommunicationEquipmentType().toString());
			contact.setSystemSimple(cs);
		}
	}
	
	static public ContactSystem mapTelecommunicationType(String type) {
		/*
		BP 		Beeper
		CP 		Cellular or Mobile Phone
		FX		Fax
		Internet		Internet Address
		MD		Modem
		PH		Telephone
		SAT		Satellite Phone
		TDD		Telecommunications Device for the Deaf
		TTY		Teletypewriter
		X.400	X.400 email address*/
		
		if (type.equalsIgnoreCase("PH")){
			return ContactSystem.phone;
		} else if (type.equalsIgnoreCase("FX")) {
			return ContactSystem.fax;
		} else if (type.equalsIgnoreCase("X.400")) {
			return ContactSystem.email;
		} else if (type.equalsIgnoreCase("Internet")) {
			return ContactSystem.url;
		} else if (type.equalsIgnoreCase("BP")) {
			CodeableConcept cc = new CodeableConcept();
			cc.setTextSimple("BP");
			//how to convert or add missing enums/constants
			return null;

		} else {
			return ContactSystem.phone;
		}
	}
	
	static public List<Address> createAddress(List<XAD> xads) {
		List<Address> addresses = new ArrayList<Address>();
		for (XAD xad : xads) {
			Address addr = new Address();
			
			addr.setTextSimple(xad.getStreetAddress().getStreetOrMailingAddress().toString());
			addr.setCitySimple(xad.getCity().toString());
			addr.setCountrySimple(xad.getCountry().getName());
			addr.setStateSimple(xad.getStateOrProvince().toString());
			addr.setZipSimple(xad.getZipOrPostalCode().toString());
			
			addresses.add(addr);			
		}
		
		return addresses;
	}
	
	static public void createPractitionerCategory(Practitioner practitioner, List<IS> iss) {
		for (IS is : iss) {
			CodeableConcept role = practitioner.addRole();
			role.setTextSimple(is.getValueOrEmpty());
		}
	}
	
	static public void createPractitionerSpecialty(Practitioner practitioner, List<SPD> spds){
		for (SPD spd : spds) {
			CodeableConcept specialty = practitioner.addSpecialty();
			specialty.setTextSimple(spd.getSpecialtyName().toString());
		}
	}
}
