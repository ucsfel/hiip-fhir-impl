package edu.ucsf.hiip.app;

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
//import org.mule.module.http.internal.ParameterMap;
import org.mule.transformer.AbstractMessageTransformer;
import org.mule.util.CaseInsensitiveHashMap;

public class Login extends AbstractMessageTransformer{

	private String BaseUrl = "http://localhost:8082/";
	
    public Object transformMessage(MuleMessage message, String outputEncoding) throws TransformerException {
 		
    	LinkedList lstPartners = (LinkedList) message.getInvocationProperty("partners");
    	LinkedList lstUserIdentifiers = (LinkedList) message.getInvocationProperty("userIdentifiers");
    	HashMap queryParms = (HashMap)message.getInboundProperty("http.query.params");
    	
    	BaseUrl = message.getInboundProperty("http.scheme") + "://" + message.getInboundProperty("host") + "/";
		   	
    	StringBuffer sb = new StringBuffer("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">");	
		
    	sb.append("<html xmlns=\"http://www.w3.org/1999/xhtml\">");
    	
    	sb.append("<head>");
    	sb.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />");
    	sb.append("<title>HIIP App</title>");
    	sb.append("<style>").append(addTableCss()).append("</style>");
    	
    	sb.append("<script src=\"http://ajax.googleapis.com/ajax/libs/jquery/1.11.2/jquery.min.js\"></script>")
    	.append("<script>$(document).ready(function(){$('tbody tr').hover(function() {$(this).addClass('odd');}, function() {$(this).removeClass('odd');});/*$(\"button\").click(function(){$(\"h1, h2, p\").addClass(\"blue\");$(\"div\").addClass(\"important\");});*/"
    			+ "$(\"#post\").click(function(){alert(\"hello!\");});"
    			+ "});</script>")
    	.append("<style>.important {font-weight: bold;font-size: xx-large;}.blue{color: blue;}</style>")    	
    	.append("</head>");
    	
    	sb.append("<body>");
    	
    	//sb.append("<h1>Heading 1</h1><h2>Heading 2</h2><p>This is a paragraph.</p><p>This is another paragraph.</p><div>This is some important text!</div><br><button>Add classes to elements</button><button id='post'>Post Data</button>");
    	//sb.append("<table><colgroup><col span=\"2\" style=\"background-color:gray\"><col style=\"background-color:white\"></colgroup><tr><th>Partner</th><th>Action</th><th>Revoke</th></tr>");
    	
    	sb.append("<table id=\"apps\">");
    	sb.append("<caption>").append("HIIP Applications").append("</caption>");
    	
    	sb.append("<thead>");
    	sb.append("<tr>");
    	sb.append("<th scope=\"col\" rowspan=\"2\">").append("Partner App").append("</th>");
    	sb.append("<th scope=\"col\" colspan=\"2\">").append("OAuth2 States for HIIP User: ").append(message.getPayload()).append("</th>");
    	sb.append("</tr>");
    	sb.append("<tr>");
    	sb.append("<th scope=\"col\">").append("Action").append("</th>");
    	sb.append("<th scope=\"col\">").append("Revoke").append("</th>");
    	sb.append("</tr>");
    	sb.append("</thead>");
    	
    	sb.append("<tbody>");
 	
    	//iterate through the partner list
    	for (Object hash : lstPartners){
			CaseInsensitiveHashMap partner = (CaseInsensitiveHashMap) hash;
    		System.out.println(partner);
    		//check if it exists in userIdentifiers
    		boolean found = false;
    		CaseInsensitiveHashMap identifier = null;
    		
    		for (Object cihm : lstUserIdentifiers){
    			identifier = (CaseInsensitiveHashMap) cihm;
    			if (partner.containsValue(identifier.get("partnerCode"))){
    				System.out.println("found partner:" + identifier.get("partnerCode"));
    				found = true;
    				break;
    			}
    		}
    		
			String partnerCode = partner.get("code").toString();
			sb.append("<tr><th scope=\"row\">").append(partnerCode).append("</th>");
			
			Integer iActive = identifier.get("isActive") == null ? new Integer(0) : (Integer)identifier.get("isActive");
			Integer iOauth = (Integer)partner.get("isOAuthCompliant");
    		if (iOauth.intValue() == 1) {
				System.out.println("oauth compliant");
    			if (found && iActive.intValue() == 1){
    				Object refreshToken = identifier.get("refreshToken");
    				if (null == refreshToken){
        				//send to authorize
    					sb.append("<td><a href=\"").append(BaseUrl).append("authorize?target=").append(partnerCode).append("&patientId=").append(message.getPayload()).append("\">");
    					sb.append("<div style=\"height:100%;width:100%\">authorize</div></a></td>");
    					
    					sb.append("<td>***</td>");
   					
    				} else {
    					//send to deeplink
    					sb.append("<td><a href=\"").append(BaseUrl).append("deeplink?target=").append(partnerCode).append("&patientId=").append(message.getPayload()).append("\">");
    					sb.append("<div style=\"height:100%;width:100%\">deeplink</div></a></td>");
        			
    					//the Revoke button  			
    					sb.append("<td><button onclick=\"location.href='")
    					  .append(BaseUrl).append("revoke?target=").append(partnerCode).append("&patientId=").append(message.getPayload()).append("'\">Revoke</button></td>");
    				}
    				
    				System.out.println("sb="+sb.toString());
        			
    			} else {
        			//send to authorize
    		    	String hiipPatientIdentifier = queryParms.get("patientId").toString();

        			System.out.println("partner not found:" + partner.get("code"));
        			sb.append("<td><a href=\"").append(BaseUrl).append("authorize?target=").append(partnerCode).append("&patientId=").append(hiipPatientIdentifier).append("\">");
        			sb.append("<div style=\"height:100%;width:100%\">authorize</div></a></td><td/>");
     			}
    			
    		} else {
				System.out.println("*not* oauth compliant");
    			sb.append("<td>not OAuth compliant</td><td>***</td>");
   		}
    		sb.append("</tr>");
    	}
    	
    	sb.append("</tbody>");
        sb.append("</table>")   	
          .append("</body></html>");
		
    	message.setPayload(sb.toString());
			
        return message;
    }
    
    private String addTableCss(){
    	StringBuffer sbTableCss = new StringBuffer("body {margin:0; padding:20px; font:13px \"Lucida Grande\", \"Lucida Sans Unicode\", Helvetica, Arial, sans-serif;}");
    	sbTableCss.append("/* ---- Some Resets ---- */p,table, caption, td, tr, th {margin:0;padding:0;font-weight:normal;}");
    	sbTableCss.append("/* ---- Paragraphs ---- */p {margin-bottom:15px;}"); 
    	sbTableCss.append("/* ---- Table ---- */table {border-collapse:collapse;margin-bottom:15px;width:90%;}");
    	sbTableCss.append("	caption {text-align:left;font-size:15px;padding-bottom:10px;}");
    	sbTableCss.append("	table td,table th {padding:5px;border:1px solid #fff;border-width:0 1px 1px 0;}");
    	sbTableCss.append("thead th {background:#91c5d4;}");
    	sbTableCss.append("thead th[colspan],thead th[rowspan] {background:#66a9bd;}");
    	sbTableCss.append("tbody th,tfoot th {text-align:left;background:#91c5d4;}");
    	sbTableCss.append("tbody td,tfoot td {text-align:center;background:#d5eaf0;}");
    	sbTableCss.append("tfoot th {background:#b0cc7f;}");
    	sbTableCss.append("tfoot td {background:#d7e1c5;font-weight:bold;}");
    	sbTableCss.append("tbody tr.odd td { background:#bcd9e1;}");
    	
    	return sbTableCss.toString();
    }
   
 }
