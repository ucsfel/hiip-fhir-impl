package edu.ucsf.hiip;

import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.io.*;
import java.net.*;
import java.sql.Timestamp;
import java.util.*;

import org.apache.commons.io.FileUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
//import org.mule.module.http.internal.ParameterMap;
import org.mule.transformer.AbstractMessageTransformer;
import org.mule.util.CaseInsensitiveHashMap;

public class TokenStore extends AbstractMessageTransformer{

    public Object transformMessage(MuleMessage message, String outputEncoding) throws TransformerException {
 			
			
    	try {
			message.setPayload(URLEncoder.encode(message.getPayload().toString(), "UTF-8"));
			message = postFormData(message);
			
			
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
        return message;
    }
    
    private MuleMessage postFormData(MuleMessage message){
        URL url;
		try {
			
	    	LinkedList lstPartnerInfo = (LinkedList) message.getInvocationProperty("partnerInfo");
	    	HashMap queryParms = (HashMap)message.getInboundProperty("http.query.params");

	    	CaseInsensitiveHashMap hash = (CaseInsensitiveHashMap) lstPartnerInfo.getFirst();
	    	
	    	String authCode = queryParms.get("code").toString();
	    	String clientId = hash.get("clientId").toString();
	    	String clientSecret = hash.get("clientSecret").toString();
	    	String tokenApi = hash.get("tokenApi").toString();
	    	//String patientContextApi = hash.get("patientContextApi").toString();

			url = new URL(hash.get("tokenApi").toString());	//"https://www.box.com/api/oauth2/token");
			Map<String,Object> params = new LinkedHashMap<>();
			params.put("grant_type", "authorization_code");
			params.put("code",authCode);
			params.put("client_id", clientId);
			params.put("client_secret", clientSecret);
 
			StringBuilder postData = new StringBuilder();
			for (Map.Entry<String,Object> param : params.entrySet()) {
				if (postData.length() != 0) postData.append('&');
				postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
				postData.append('=');
				postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
			}
			byte[] postDataBytes = postData.toString().getBytes("UTF-8");

			HttpURLConnection conn = (HttpURLConnection)url.openConnection();
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
			conn.setDoOutput(true);
			conn.getOutputStream().write(postDataBytes);

			Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
			StringBuffer sb = new StringBuffer();
			for (int c; (c = in.read()) >= 0; sb.append((char)c));
			
			//System.out.println(sb.toString());
			//FileUtils.writeStringToFile(new File("token/token.txt"), sb.toString());
        
			//now, get the access_token and refresh_token and save into session variable
			Map<String,String> map = new HashMap<String,String>();

			ObjectMapper mapper = new ObjectMapper();
			map = mapper.readValue(sb.toString(), HashMap.class);
        
			message.setInvocationProperty("tokenApi", tokenApi);
			message.setInvocationProperty("accessToken", map.get("access_token"));
			message.setInvocationProperty("refreshToken", map.get("refresh_token"));
			
			Object expiresIn = map.get("expires_in");
			Timestamp ts = HiipUtils.CalcExpirationDatetime(expiresIn);

			message.setInvocationProperty("expiresIn", ts.toString());
			
			//NOTE: for deeplink sub-flow
			//message.setInvocationProperty("patientContextApi", patientContextApi);
			
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
