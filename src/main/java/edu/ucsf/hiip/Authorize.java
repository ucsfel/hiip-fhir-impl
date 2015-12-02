package edu.ucsf.hiip;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HeaderElement;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.io.FileUtils;
import org.apache.http.message.BasicNameValuePair;
import org.codehaus.jackson.map.ObjectMapper;
import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
//import org.mule.module.http.internal.ParameterMap;
import org.mule.transformer.AbstractMessageTransformer;
import org.mule.util.CaseInsensitiveHashMap;

public class Authorize extends AbstractMessageTransformer{

	public Object transformMessage(MuleMessage message, String outputEncoding) throws TransformerException {
		   
		message = getAuthorizeData(message);
		
		return message;
	}
	
	private MuleMessage getAuthorizeData(MuleMessage message){
		try {
			
	    	LinkedList lstPartnerInfo = (LinkedList) message.getInvocationProperty("partnerInfo");
	    	HashMap queryParms = (HashMap)message.getInboundProperty("http.query.params");
			
	    	final HttpClient client = new HttpClient();	
	    	CaseInsensitiveHashMap hash = (CaseInsensitiveHashMap) lstPartnerInfo.getFirst();
	    	String authorizeApi = hash.get("authorizeApi").toString();
			final GetMethod method = new GetMethod(authorizeApi); //"https://app.box.com/api/oauth2/authorize");
	    	
	    	//String hiipPatientIdentifier = queryParms.get("patientId").toString();
	    	String hiipPatientIdentifier = queryParms.get("1").toString().trim();
	    	String hiipAPeXUserIdentifier = queryParms.get("2").toString().trim();
	    	String hiipClientId= queryParms.get("3").toString().trim();
	    	String hiipClientSecret = queryParms.get("4").toString().trim();
	    	String clientId = hash.get("clientId").toString();
	    	String code = hash.get("code").toString();
	    	String callbackUrl = hash.get("redirectUrl").toString();
	    	
	    	StringBuffer sb = new StringBuffer();
			sb.append("response_type=code")
			  .append("&client_id=").append(clientId) //ldhg34l5eitfkyhbankuqo94s3kbyp82")
			  .append("&redirect_uri=").append(callbackUrl)
			  //TODO: this is potentially the correlation identifier to pair the HIIP user with the oauth partner's user
			  .append("&state=").append(code).append(":").append(hiipPatientIdentifier).append(":").append(hiipAPeXUserIdentifier).append(":").append(hiipClientId).append(":").append(hiipClientSecret);//hiip_ldhg34l5eitfkyhbankuqo94s3kbyp82");
				
			method.setQueryString(sb.toString());
			client.executeMethod(method);
				
			String responseBody = method.getResponseBodyAsString();

			message.setPayload(responseBody);
				        	
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	        
		return message;
	}
}
