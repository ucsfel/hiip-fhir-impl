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
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.io.FileUtils;
import org.codehaus.jackson.map.ObjectMapper;

//import com.firebase.*;
//import com.firebase.security.*;
//import com.firebase.security.token.*;

import org.apache.commons.codec.digest.DigestUtils;


public class HiipUtils {
	
	public static HashMap<String, String> GetPartnerInfo(String partnerCode){
		HashMap<String, String> hash = new HashMap<String, String>();
		
		
		return hash;
	}
	
	public static String LookupAccessToken(String hiipUser, String partner){
		String accessToken = null;
		//NOTE: this may have to call into RefreshToken if the access token is already expired
		HashMap<String, String> partnerInfo = GetPartnerInfo("");
		//RefreshToken("", "", "", "");
		return accessToken;
	}

	public static void RegisterUser(String partner, String identifier, HashMap<String, String> userInfo){
		
	}
	
	public static Map<String,String> RefreshToken(String tokenUrl, String clientId, String clientSecret, String refreshToken) throws IOException{
		
		Map<String,String> map = new HashMap<String,String>();
		URL url;
		try {
			url = new URL(tokenUrl);
			Map<String,Object> params = new LinkedHashMap<>();
			params.put("grant_type", "refresh_token");
			params.put("refresh_token", refreshToken);
			//params.put("required_token", refreshToken);
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
			
			//now, get the access_token, refresh_token and expires_in; save into flowVars variable
			ObjectMapper mapper = new ObjectMapper();
			map = mapper.readValue(sb.toString(), HashMap.class);

			//System.out.println(sb.toString());
			FileUtils.writeStringToFile(new File("token/token.txt"), sb.toString());
                
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

		return map;
	}
	
	public static Timestamp CalcExpirationDatetime(Object expiresIn){

		//calculate the expiration datetime
		Calendar cal = Calendar.getInstance();
		//String expiresIn = map.get("expires_in");
		int expiration = ((Integer)expiresIn).intValue() * 1000;
		
		cal.add(Calendar.MILLISECOND, expiration);
		
		Timestamp ts = new Timestamp(cal.getTimeInMillis());			
		
		return ts;
	}
	public static String GenerateAccessToken(String userId) {
		Map<String, Object> authPayload = new HashMap<String, Object>();
		authPayload.put("uid", "1");	//will this need to be dynamic?
		authPayload.put("some", userId);
		authPayload.put("data", Calendar.getInstance().getTimeInMillis());
/*
		TokenGenerator tokenGenerator = new TokenGenerator("hiip_secret");
		String token = tokenGenerator.createToken(authPayload);
*/
		String token = "";
		return token.substring(token.length() - 43);
		//"n7z471r1iPovWDE43vxAppYOEjSAhKNn"
	}
	
	public static String HashPassword(String password){
		return "";
		//return DigestUtils.sha256Hex(password);
	}
	
	public static Connection JdbcConnection(){
		Connection conn = null;
		try
	    {
	      Class.forName("com.mysql.jdbc.Driver").newInstance();
	      String url = "jdbc:mysql://localhost/HIIP";
	      conn = DriverManager.getConnection(url, "hiipuser", "hiip123");
	      //doTests();
	      //conn.close();
	    }
	    catch (ClassNotFoundException ex) {System.err.println(ex.getMessage());}
	    catch (IllegalAccessException ex) {System.err.println(ex.getMessage());}
	    catch (InstantiationException ex) {System.err.println(ex.getMessage());}
	    catch (SQLException ex)           {System.err.println(ex.getMessage());}		
		
	      return conn;

	}
}
