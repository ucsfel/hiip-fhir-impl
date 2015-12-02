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
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HeaderElement;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.io.FileUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
//import org.mule.module.http.internal.ParameterMap;
import org.mule.transformer.AbstractMessageTransformer;
import org.mule.util.CaseInsensitiveHashMap;

public class DeepLink extends AbstractMessageTransformer{

	public Object transformMessage(MuleMessage message, String outputEncoding) throws TransformerException {
		   
		message = getLinkData(message);
		
		return message;
	}
	
	private MuleMessage getLinkData(MuleMessage message){
		try {
			
			final HttpClient client = new HttpClient();			
			GetMethod method = null;

			boolean success = false;
			int retry = 0;
			Map<String,String> map = null;
			do {
				/*String tokens = FileUtils.readFileToString(new File("token/token.txt"));
				//now, get the access_token and refresh_token and save into session variable
				Map<String,String> map = new HashMap<String,String>();

				ObjectMapper mapper = new ObjectMapper();
				map = mapper.readValue(tokens, HashMap.class);

				//final PostMethod method = new PostMethod("https://www.box.com/api/2.0/folders/0");
				System.out.println(map.get("access_token"));
				*/
				String host = message.getInvocationProperty("patientContextApi");
				// contains ? and ends with =
				if (host.contains("?") && host.endsWith("=")){
					host = host.concat(message.getInvocationProperty("accessToken").toString());
				}
				
				//store the host in flowvar to be accessed by the redirect in the flow
				message.setInvocationProperty("partnerPatientContextLandingPage", host);
								
				method = new GetMethod(host);
				method.addRequestHeader("Authorization", "Bearer " + message.getInvocationProperty("accessToken"));
				client.executeMethod(method);
				
				success = true;
								
				//get the www-authenticate header value
				Header[] authHeaders = method.getResponseHeaders();
				for (Header hdr : authHeaders){
					if (hdr.getName().equalsIgnoreCase("WWW-Authenticate")){
						//Bearer realm="Service", error="invalid_token", error_description="The access token provided is invalid."
						for (HeaderElement el: hdr.getElements()){
							if (el.getName().equalsIgnoreCase("error")){
								if (el.getValue().equalsIgnoreCase("invalid_token")){
									success = false;
									//refresh the token
									try{
									map = HiipUtils.RefreshToken(message.getInvocationProperty("tokenApi").toString(), 
											message.getInvocationProperty("clientId").toString(), 
											message.getInvocationProperty("clientSecret").toString(),
											message.getInvocationProperty("refreshToken").toString());
									
									if (!map.isEmpty()) {
										message.setInvocationProperty("accessToken", map.get("access_token"));
										message.setInvocationProperty("refreshToken", map.get("refresh_token"));
										Object expiresIn = map.get("expires_in");
										Timestamp ts = HiipUtils.CalcExpirationDatetime(expiresIn);
										message.setInvocationProperty("expiresIn", ts.toString());
									}

									} catch (IOException e){}
									retry++;
								}
								break;
							}
						}
						break;
					}
				}
				
				String responseBody = method.getResponseBodyAsString();
				message.setPayload(responseBody);
				
			} while (!success && retry < 10);
			
			if (success && retry > 0){
				System.out.println("token refreshed!");
				message.setInvocationProperty("refresh", true);
				String responseBody = method.getResponseBodyAsString();
				message.setPayload(responseBody);
			}
			
				        	
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
