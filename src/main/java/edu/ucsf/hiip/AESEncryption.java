package edu.ucsf.hiip;

import java.io.UnsupportedEncodingException;
import java.security.*;
import java.util.Arrays;
import java.util.HashMap;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.mule.transformer.AbstractMessageTransformer;

public class AESEncryption extends AbstractMessageTransformer{
	private static final String ALGO = "AES/CBC/PKCS5Padding";
	private static final String DEFAULT_KEY = "HmmdIFYHRDd9t2wujSHcEw==";
	//must be 16 bytes long
	private static final byte[] ivBytes = new byte[16];
	private static IvParameterSpec iv;
	
	private static final String UNICODE_FORMAT = "UTF8";
	//128-bit key requires 24 chars in base64
	//256-bit key requires 44 chars in base64
    private static byte[] keyValue;

    public Object transformMessage(MuleMessage message, String outputEncoding) throws TransformerException {

    	Arrays.fill(ivBytes, (byte)0x00);
    	iv = new IvParameterSpec(ivBytes);
    	
    	Object derivedKey = message.getInvocationProperty("derivedKey");
    	if (null == derivedKey){
    		keyValue = Base64.decodeBase64(DEFAULT_KEY.getBytes());
    	} else {
    		keyValue = Base64.decodeBase64(derivedKey.toString().getBytes());    		
    	}
       	SecretKeySpec skeySpec = new SecretKeySpec( keyValue, "AES" );
        
    	Cipher cipher = null;
		try {
			cipher = Cipher.getInstance(ALGO);
		} catch (NoSuchAlgorithmException e1) {
			e1.printStackTrace();
		} catch (NoSuchPaddingException e1) {
			e1.printStackTrace();
		}
    	    
    	try {    		   
   			cipher.init( Cipher.DECRYPT_MODE, skeySpec, iv );
		} catch (InvalidKeyException | InvalidAlgorithmParameterException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	    
    	byte[] original = null;
		try {
			//Object queryParm1 = message.getPayload().toString());	//"2zU3YSkvbamjrlE7WgBaKA==");
			//Object queryParm1 = message.getInvocationProperty("1");
			
			//HashMap queryParms = (HashMap)message.getInboundProperty("http.query.params");
			//Object queryParm1 = queryParms.get("1");
			
			Object queryParm1 = message.getInvocationProperty("encryptedPatientId");
			
			if (queryParm1 != null)
			{
				byte[] encString = Base64.decodeBase64( queryParm1.toString().getBytes() );		
				original = cipher.doFinal(encString );

				String clearText = "";
				try {
					clearText = new String(original, UNICODE_FORMAT).trim();
					//message.setInvocationProperty("patientId", clearText);
					message.setOutboundProperty("patientId", clearText);

				} catch (UnsupportedEncodingException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}

		} catch (IllegalBlockSizeException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (BadPaddingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		        
        return message;
    }
}
