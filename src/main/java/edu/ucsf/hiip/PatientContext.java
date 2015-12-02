package edu.ucsf.hiip;

import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.io.*;
import java.net.*;
import java.util.*;

import org.apache.commons.io.FileUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.mule.transformer.AbstractMessageTransformer;

public class PatientContext extends AbstractMessageTransformer{

    public Object transformMessage(MuleMessage message, String outputEncoding) throws TransformerException {
 			
			
//    	try {
			//message.setPayload(URLEncoder.encode(message.getPayload().toString(), "UTF-8"));
			message = postFormData(message);
			
/*			
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
*/			
        return message;
    }
    
    private MuleMessage postFormData(MuleMessage message){
        URL url;
		try {
			url = new URL("https://view-api.box.com/1/document");
			Map<String,Object> params = new LinkedHashMap<>();
			params.put("url", "https://cloud.box.com/shared/static/4qhegqxubg8ox0uj5ys8.pdf");
			//params.put("url", "https://ucsf.box.com/shared/static/kog6x320c5sanv2zcctkegbtozbe7m8f.png");
			
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
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setRequestProperty("Authorization", "Token tXcJ8kYcHimawt3S9ajESI4u0Z9eLEBx");
			conn.setDoOutput(true);
			conn.getOutputStream().write(postDataBytes);

			Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
			StringBuffer sb = new StringBuffer();
			for (int c; (c = in.read()) >= 0; sb.append((char)c));
			
			
			System.out.println(sb.toString());
        
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
