package com.team23.stim.controller;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ResponseBody;

import org.json.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;

import com.team23.stim.client.OAuth2PlatformClientFactory;
import com.team23.stim.controller.QBOController;
import com.team23.stim.classes.Session;
import com.intuit.oauth2.client.OAuth2PlatformClient;
import com.intuit.oauth2.data.BearerTokenResponse;
import com.intuit.oauth2.exception.OAuthException;

@Controller
public class CallbackController {
    
	@Autowired
	OAuth2PlatformClientFactory factory;

    private static final Logger logger = Logger.getLogger(CallbackController.class);

    public String myRealmId = null;
    public String myAccessToken = null;
    public String myRefreshToken = null;

    @RequestMapping("/getHeaders")
    @CrossOrigin("http://localhost:3000")
    @ResponseBody
    public String returnSessionInfo()
    {
        JSONObject json = new JSONObject();
        json.put("access_token", myAccessToken);
        json.put("refresh_token", myRefreshToken);
        json.put("realm_id", myRealmId);

        //System.out.print(json.toString());
        return json.toString();
    }
    
    /**
     *  This is the redirect handler you configure in your app on developer.intuit.com
     *  The Authorization code has a short lifetime.
     *  Hence Unless a user action is quick and mandatory, proceed to exchange the Authorization Code for
     *  BearerToken
     *      
     * @param auth_code
     * @param state
     * @param realmId
     * @param session
     * @return
     */
    @RequestMapping("/oauth2redirect")
    public String callBackFromOAuth(@RequestParam("code") String authCode, @RequestParam("state") String state, @RequestParam(value = "realmId", required = false) String realmId, HttpSession session) {   
        logger.info("inside oauth2redirect of sample"  );
        try {
	        String csrfToken = (String) session.getAttribute("csrfToken");
	        if (csrfToken.equals(state)) {
	            session.setAttribute("realmId", realmId);
	            session.setAttribute("auth_code", authCode);
	
	            OAuth2PlatformClient client  = factory.getOAuth2PlatformClient();
	            String redirectUri = factory.getPropertyValue("OAuth2AppRedirectUri");
	            logger.info("inside oauth2redirect of sample -- redirectUri " + redirectUri  );
	            
	            BearerTokenResponse bearerTokenResponse = client.retrieveBearerTokens(authCode, redirectUri);
				 
	            session.setAttribute("access_token", bearerTokenResponse.getAccessToken());
	            session.setAttribute("refresh_token", bearerTokenResponse.getRefreshToken());
                
                myRealmId = realmId;
                myAccessToken = bearerTokenResponse.getAccessToken();
                myRefreshToken = bearerTokenResponse.getRefreshToken();
                System.out.println(myAccessToken);
                System.out.println(myRealmId);

                //QBOController.createSession(session);
	    
	            // Update your Data store here with user's AccessToken and RefreshToken along with the realmId

	            return "connected";
	        }
	        logger.info("csrf token mismatch " );
        } catch (OAuthException e) {
        	logger.error("Exception in callback handler ", e);
		} 
        return null;
    }


}
