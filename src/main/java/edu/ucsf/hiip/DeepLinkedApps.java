package edu.ucsf.hiip;

import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.io.*;
import java.net.*;
import java.util.*;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.io.FileUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.mule.transformer.AbstractMessageTransformer;
import org.mule.util.CaseInsensitiveHashMap;

public class DeepLinkedApps extends AbstractMessageTransformer{

	private String BaseUrl = "";
	
    public Object transformMessage(MuleMessage message, String outputEncoding) throws TransformerException {
 		
    	String patId = message.getInvocationProperty("patientId");
    	String encryptedPatId = message.getInvocationProperty("encryptedPatientId");
    	String APeXuserId = message.getInvocationProperty("APeXUserId");
    	String hiipClientId = message.getInvocationProperty("hiipClientId");
    	String hiipClientSecret = message.getInvocationProperty("hiipClientSecret");
 	
    	LinkedList lstPartners = (LinkedList) message.getInvocationProperty("partners");
    	LinkedList lstUserIdentifiers = (LinkedList) message.getInvocationProperty("userIdentifiers");
    	//HashMap queryParms = (HashMap)message.getInboundProperty("http.query.params");
    	
    	//BaseUrl = message.getInboundProperty("http.scheme") + "://" + message.getInboundProperty("host") + "/";
    	//BaseUrl ="https://" + message.getInboundProperty("host") + "/clinical/taxi/0.1/";
    	BaseUrl = message.getInvocationProperty("hiipBaseURL");
		
    	StringBuffer sb = new StringBuffer("<SetSDEValues xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"urn:Custom-com:Clinical.2012.Services.Patient\"><ContextName>PATIENT</ContextName><EntityID>");
    	
    	sb.append(patId);  		//1-PatientID
    	//sb.append("Z3225");	//patientid
    	sb.append("</EntityID><EntityIDType>External</EntityIDType><UserID>");
    	sb.append(APeXuserId); 	  		//2-UserID
    	//sb.append("15269");	//userid

    	//Removed unnecessary elements (ContactId, ContactIDType,Comments,
    	//sb.append("</UserID><UserIDType>External</UserIDType><Source>HIIP Web Service</Source><ContactID></ContactID><ContactIDType></ContactIDType><SmartDataValues><Value><Comments><string xmlns=\"http://schemas.microsoft.com/2003/10/Serialization/Arrays\">Comment</string></Comments><SmartDataID>HIIP#002</SmartDataID><SmartDataIDType>SDI</SmartDataIDType><Values>");
    	sb.append("</UserID><UserIDType>External</UserIDType><Source>HIIP Web Service</Source><SmartDataValues><Value><SmartDataID>HIIP#002</SmartDataID><SmartDataIDType>SDI</SmartDataIDType><Values>");

    	StringBuffer sbLinkedApps = new StringBuffer();
    	//iterate through the partner list
    	for (Object hash : lstPartners){
			CaseInsensitiveHashMap partner = (CaseInsensitiveHashMap) hash;
    		//System.out.println(partner);
    		//check if it exists in userIdentifiers
    		boolean found = false;
    		CaseInsensitiveHashMap identifier = null;
    		
    		for (Object cihm : lstUserIdentifiers){
    			identifier = (CaseInsensitiveHashMap) cihm;
    			if (partner.containsValue(identifier.get("partnerCode"))){
    				//System.out.println("found partner:" + identifier.get("partnerCode"));
    				found = true;
    				break;
    			}
    		}
    		
			String partnerCode = partner.get("code").toString();
			String partnerName = partner.get("name").toString();
			
			Integer iActive = identifier.get("isActive") == null ? new Integer(0) : (Integer)identifier.get("isActive");
			Integer iOauth = (Integer)partner.get("isOAuthCompliant");
    		if (iOauth.intValue() == 1) {
				//System.out.println("oauth compliant");
    			if (found && iActive.intValue() == 1){
    				Object refreshToken = identifier.get("refreshToken");
    				if (null != refreshToken){
    					//send to deeplink
    					sbLinkedApps.append("<string xmlns=\"http://schemas.microsoft.com/2003/10/Serialization/Arrays\">").append(partnerName);
    					sbLinkedApps.append("|").append(partnerName).append("|");
    					//sbLinkedApps.append(BaseUrl).append("deeplink?0=").append(partnerCode).append("&amp;1=").append(message.getPayload());
    					//sbLinkedApps.append(BaseUrl).append("deeplink?0=").append(partnerCode).append("&amp;1=").append(encryptedPatId).append("&amp;2=").append(APeXuserId).append("&amp;3=").append(hiipClientId).append("&amp;4=").append(hiipClientSecret);
    					sbLinkedApps.append(BaseUrl).append("deeplink?0=").append(partnerCode).append("&amp;1=").append(patId).append("&amp;2=").append(APeXuserId).append("&amp;3=").append(hiipClientId).append("&amp;4=").append(hiipClientSecret);
    					sbLinkedApps.append("</string>");
   					
    				}
    				
    			}
       		}
    	}
    	
    	//add the "Add New" item
    	//sbLinkedApps.append("<string xmlns=\"http://schemas.microsoft.com/2003/10/Serialization/Arrays\">").append("Add New||");
    	//sbLinkedApps.append(BaseUrl).append("register?source=HYPERSPACE").append("&amp;patientId=").append(message.getPayload());
    	//sbLinkedApps.append("</string>");
    	
    	sb.append(sbLinkedApps.toString());
       	sb.append("</Values></Value></SmartDataValues></SetSDEValues>");
    	
    	//message.setInvocationProperty("deepLinkApps", deepLinkApps.toArray());
    	message.setPayload(sb.toString());
    	
        return message;
    	//return "<SetSDEValues xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"urn:Custom-com:Clinical.2012.Services.Patient\"><ContextName>PATIENT</ContextName><EntityID>Z3225</EntityID><EntityIDType>External</EntityIDType><UserID>15269</UserID><UserIDType>External</UserIDType><Source>HIIP Web Service</Source><ContactID></ContactID><ContactIDType></ContactIDType><SmartDataValues><Value><Comments><string xmlns=\"http://schemas.microsoft.com/2003/10/Serialization/Arrays\">Comment</string></Comments><SmartDataID>HIIP#002</SmartDataID><SmartDataIDType>SDI</SmartDataIDType><Values><string xmlns=\"http://schemas.microsoft.com/2003/10/Serialization/Arrays\">Value</string></Values></Value></SmartDataValues></SetSDEValues>";
    }
 }
