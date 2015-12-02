package edu.ucsf.hiip.security.oauth2;
/*
import org.mule.modules.oauth2.provider.Constants.RequestGrantType;
import org.mule.modules.oauth2.provider.Utils;
import org.mule.modules.oauth2.provider.client.Client;
import org.mule.modules.oauth2.provider.client.ClientRegistration;
import org.mule.modules.oauth2.provider.client.ClientType;

//import com.mulesoft.common.agent.util.Utils;

public class HiipInitializer {
    public static final String BOOKSTORE_CLIENT_ID = "e7aaf348-f08a-11e1-9237-96c6dd6a022f";
    public static final String BOOKSTORE_CLIENT_SECRET = "ee9acaa2-f08a-11e1-bc20-96c6dd6a022f";
 
    private ClientRegistration clientRegistration;
 
    public void initialize()
    {
        final Client hiipClient = new Client(BOOKSTORE_CLIENT_ID);
        hiipClient.setSecret(BOOKSTORE_CLIENT_SECRET);
        hiipClient.setType(ClientType.CONFIDENTIAL);
        hiipClient.setClientName("Mule Bookstore");
        hiipClient.setDescription("Mule-powered On-line Bookstore");
        hiipClient.getAuthorizedGrantTypes().add(RequestGrantType.AUTHORIZATION_CODE);
        hiipClient.getRedirectUris().add("http://localhost*");
        hiipClient.getScopes().addAll( Utils.tokenize("READ_PROFILE READ_APPTS READ_CLINICAL") );
 
        clientRegistration.addClient(hiipClient);
    }
 
    public void setClientRegistration(final ClientRegistration clientRegistration)
    {
        this.clientRegistration = clientRegistration;
    }
}
*/