package edu.ucsf.hiip;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.mule.transformer.AbstractMessageTransformer;
import org.mule.util.CaseInsensitiveHashMap;
import org.apache.commons.io.FileUtils;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;


public class Location extends AbstractMessageTransformer{
	
    public Object transformMessage(MuleMessage message, String outputEncoding) throws TransformerException {
 		/*
 		 * {"Assigned":false,"DateInService":"2015-07-10T16:38:49.12Z","DisplayValue":"",
 		 * "LastLocationChange":"2015-10-23T22:13:08.27Z",
 		 * "LocationId":1380942,
 		 * "LocationDisplayValue":"Corridor 9C5 (A)",
 		 * "ModelNumber":"T2A",
 		 * "PreviousLocationChange":"2015-10-23T22:12:57.907Z",
 		 * "PreviousLocationId":1380943,"PreviousLocationDisplayValue":"Corridor 9C1 (A)","TagId":"14EB00000E5165","TagStatusId":1,"TagTypeId":3,"TimeLastSeen":"2015-10-23T22:13:20.587Z","_links":{"self":{"href":"/API/Tag/14EB00000E5165"},"updateTag":{"href":"/API/Tag?tagId=14EB00000E5165"},"createTag":{"href":"/API/Tag?tagId=14EB00000E5165"},"deactivate-tag":{"href":"/API/Tag/14EB00000E5165/Deactivate"},"tags":{"href":"/API/Tags"}},"_embedded":{}}
 		 * 
 		 */
		ObjectMapper mapper = new ObjectMapper();
		Map<String,String> map = new HashMap<String,String>();
		String tuple = "";
		try {
			Object loc = null;
			//System.out.println(message.getInboundProperty("http.method"));
			if (message.getInboundProperty("http.method") == "POST") {
				loc = message.getPayloadAsString();
			} else {
				map = mapper.readValue(message.getPayloadAsString(), HashMap.class);
				loc = map.get("LocationId");
			}
			
			//now read the resource file
		    /*File dir1 = new File (".");
		     File dir2 = new File ("..");
		     try {
		       System.out.println ("Current dir : " + dir1.getCanonicalPath());
		       System.out.println ("Parent  dir : " + dir2.getCanonicalPath());
		       }
		     catch(Exception e) {
		       e.printStackTrace();
		       }
		    */
			InputStream is = this.getClass().getResourceAsStream("/location-map.json");
			map = mapper.readValue(is, HashMap.class);
			tuple = map.get(loc.toString());
		    //System.out.println ("Tuple: " + tuple);
			
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (tuple != null){
			tuple = tuple.toString();
			String[] tokens = tuple.split("/");
		
			StringBuffer sb = new StringBuffer("<GetCensusByUnit2 xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"urn:Epic-com:Access.2014.Services.Patient\"><UnitID xsi:nil=\"false\">");
			sb.append(tokens[0])
			  .append("</UnitID><!--Optional--><UnitIDType xsi:nil=\"false\"></UnitIDType><!--Optional--><UserID xsi:nil=\"false\"></UserID><!--Optional--><UserIDType xsi:nil=\"false\"></UserIDType></GetCensusByUnit2>");
		
			message.setPayload(sb.toString());
			//create inbound property for "room and bed" and "unit"
			message.setInvocationProperty("roomAndBed", tokens[1] + " " + tokens[2]);
			//get the unit so the correct server can be targeted downstream: ICU9 or ICU13 or ...
			message.setInvocationProperty("unitNo", tokens[3]);
		} else {
			message.setPayload("");
			//create inbound property for "room and bed" and "unit"
			message.setInvocationProperty("roomAndBed", "");
			//get the unit so the correct server can be targeted downstream: ICU9 or ICU13 or ...
			message.setInvocationProperty("unitNo", "");
		}
    	
    	
        return message;
    }
}
