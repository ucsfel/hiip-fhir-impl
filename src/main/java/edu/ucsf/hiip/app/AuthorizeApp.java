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
import org.mule.module.http.internal.ParameterMap;
import org.mule.transformer.AbstractMessageTransformer;
import org.mule.util.CaseInsensitiveHashMap;

public class AuthorizeApp extends AbstractMessageTransformer{

	private String BaseUrl = "http://localhost:8082/";
	
    public Object transformMessage(MuleMessage message, String outputEncoding) throws TransformerException {
 		
    	String clientId = message.getInvocationProperty("client_id");
    	String redirectUri = message.getInvocationProperty("redirect_uri");
    	String state = message.getInvocationProperty("state");
    	//ParameterMap queryParms = (ParameterMap)message.getInboundProperty("http.query.params");
    	BaseUrl = message.getInboundProperty("http.scheme") + "://" + message.getInboundProperty("host") + "/";
		   	
    	StringBuffer sb = new StringBuffer("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">");
    	sb.append("<html xmlns=\"http://www.w3.org/1999/xhtml\">");
    	
    	sb.append("<head>");
    	sb.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />");
    	sb.append("<title>HIIP App</title>");
    	sb.append("<style>").append(addTableCss()).append("</style>");
    	
    	sb.append("<script src=\"http://ajax.googleapis.com/ajax/libs/jquery/1.11.2/jquery.min.js\"></script>")
    	.append("<script>function submitform(){alert('hello');document.myform2.submit();}</script>")
    	//.append("<script>$(document).ready(function(){$('tbody tr').hover(function() {$(this).addClass('odd');}, function() {$(this).removeClass('odd');$(\"button\").click(function(){alert('hello!');$.post(\"http://localhost:9081/authorize?from=HIIP\",{name: \"Donald Duck\",city: \"Duckburg\"}, function(data, status){alert(\"Data: \" + data + \"\nStatus: \" + status); }); });"
    	//		+ "$(\"#post\").click(function(){alert(\"hello!\");});"
    	//		+ "});</script>")
    	.append("<style>.important {font-weight: bold;font-size: xx-large;}.blue{color: blue;}</style>")    	
    	.append("</head>");
    	
    	sb.append("<body>");
    	
    	sb.append("<form name='myform2' action='").append(BaseUrl).append("authorize/callback?from=HIIP")
    	  .append("&client_id=").append(clientId)
    	  .append("&redirect_uri=").append(redirectUri)
    	  .append("&state=").append(state)
    	  .append("' method='POST'><div>");
    	sb.append("<table>");
    	sb.append("<tr>");
    	sb.append("<td>Username: <input type='text' name='username' value='123456789XYZ'></td>");
    	sb.append("<td>Password: <input type='password' name='password' value='123456789'></td>");
    	sb.append("</tr>");
    	sb.append("<tr><td>SCOPE</td><td>GRANT</td></tr>");
    	
    	ListIterator<CaseInsensitiveHashMap> lstScope = ((LinkedList<CaseInsensitiveHashMap>) message.getPayload()).listIterator();
    	while (lstScope.hasNext()){
    		CaseInsensitiveHashMap hash = lstScope.next();
    		int id = Integer.valueOf(hash.get("id").toString());
    		int mode = Integer.valueOf(hash.get("mode").toString());
    		String code = hash.get("code").toString();
    		String desc = hash.get("description").toString();
    		
        	sb.append("<tr>");
        	sb.append("<td><label for='").append(id).append("'>").append(desc).append("</label></td>");
        	sb.append("<td><input type='checkbox' name='").append(id).append("' value='").append(mode).append("' checked></td>");
        	sb.append("</tr>");
    		
    		//.out.println(hash);
    	}
    	sb.append("<tr><td colspan='2'/></tr>");
    	sb.append("<tr><td><input type='submit' value='Login and Authorize'></td><td/></tr>");
    	//sb.append("<br>");
     	//sb.append("<button onclick=\"href='javascript:submitform()'\">Post Data2</button>");
      	//sb.append("<button onclick=\"location.href='http://localhost:9081/authorize?from=HIIP'\">href</button>");
    	sb.append("</table>");
   	sb.append("</div></form>");

        sb.append("</body></html>");
		
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
