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

public class Revoke extends AbstractMessageTransformer{

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
			
	    	//LinkedList lstPartnerInfo = (LinkedList) message.getInvocationProperty("partnerInfo");
	    	//ParameterMap queryParms = (ParameterMap)message.getInboundProperty("http.query.params");

	    	//CaseInsensitiveHashMap hash = (CaseInsensitiveHashMap) lstPartnerInfo.getFirst();
	    	
	    	String refreshToken = message.getInvocationProperty("refreshToken").toString();
	    	String clientId = message.getInvocationProperty("clientId").toString();
	    	String clientSecret = message.getInvocationProperty("clientSecret").toString();
	    	String revokeApi = message.getInvocationProperty("revokeApi").toString();

			url = new URL(revokeApi);
			Map<String,Object> params = new LinkedHashMap<>();
			params.put("client_id", clientId);
			params.put("client_secret", clientSecret);
			params.put("token", refreshToken);
 
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
			
			System.out.println(sb.toString());
			//FileUtils.writeStringToFile(new File("token/token.txt"), sb.toString());
        
			//now, get the access_token and refresh_token and save into session variable
			Map<String,String> map = new HashMap<String,String>();

			ObjectMapper mapper = new ObjectMapper();
			if (sb.length() > 0.) {
				map = mapper.readValue(sb.toString(), HashMap.class);
				System.out.println(map);
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
