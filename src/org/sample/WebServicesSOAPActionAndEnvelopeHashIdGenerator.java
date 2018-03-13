/**
 * This sample is provided for use in customer applications to cache webservices responses
 * in WebSphere Application Server
 */
package org.sample;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletInputStream;

import com.ibm.websphere.servlet.cache.CacheConfig;
import com.ibm.websphere.servlet.cache.IdGenerator;
import com.ibm.websphere.servlet.cache.ServletCacheRequest;

@SuppressWarnings("deprecation")
public class WebServicesSOAPActionAndEnvelopeHashIdGenerator implements IdGenerator {

	private static final String CLASS = WebServicesSOAPActionAndEnvelopeHashIdGenerator.class.getName();
	private static final Logger LOG = Logger.getLogger(CLASS);

	private static final String ACTION = "action=";
	private static final MessageDigest hasher;

	static {
		try {
			hasher = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Cache policy that governs if a response is cached or not.
	 * 
	 * Returning null is like telling Dynacache to not worry about caching this
	 * response or looking in the cache for this response.
	 * 
	 * Returning a non-null id will result in dynacache caching the response if the
	 * id does not exist in the cache (CACHE MISS) or simply returning the cached
	 * response for that id from the cache.
	 */
	@Override
	public String getId(ServletCacheRequest request) {

		if (LOG.isLoggable(Level.FINER)) {
			LOG.entering(CLASS, "getId", request);
		}

		String cacheId = null;

		if (request.getMethod().equals("POST")) {

			String soapAction = getSoapAction(request);

			if (soapAction != null) {
				try {
					request.setGeneratingId(true); // ** DO NOT REMOVE... THIS IS NEEDED FOR PROPER PARSING OF RESPONSE

					byte[] buffer = new byte[1024];
					ServletInputStream in = request.getInputStream();
					ByteArrayOutputStream reqOS = new ByteArrayOutputStream();
					int length;
					while ((length = in.read(buffer)) != -1) {
						reqOS.write(buffer, 0, length);
					}
					byte[] reqContent = reqOS.toByteArray();

					byte[] reqHash = hasher.digest(reqContent);
					cacheId = Base64.getEncoder().encodeToString(reqHash);

					if (LOG.isLoggable(Level.FINEST)) {
						LOG.fine("Create hash of SOAPEnvelope: " + cacheId);
					}

				} catch (IOException e) {
					throw new RuntimeException(e);
				} finally {
					request.setGeneratingId(false); // ** DO NOT REMOVE... THIS IS NEEDED FOR PROPER PARSING OF RESPONSE
				}

				cacheId = "SOAPAction=" + soapAction + ":SOAPEnvelope=" + cacheId;
			}
		}

		if (LOG.isLoggable(Level.FINER)) {
			LOG.exiting(CLASS, "getId", cacheId);
		}

		return cacheId;
	}

	private String getSoapAction(ServletCacheRequest request) {
		if (LOG.isLoggable(Level.FINER)) {
			LOG.entering(CLASS, "getSoapAction", request);
		}

		String soapAction = request.getHeader("SOAPAction");
		if (soapAction == null) {
			// pull out the action from the http request content-type
			// Content-Type: application/soap+xml;charset=UTF-8;action="getAccount"
			String contentType = request.getContentType();

			if (LOG.isLoggable(Level.FINEST)) {
				LOG.fine("Retrieved contentType " + contentType);
			}

			// parse the content-type with the ; delimiter
			StringTokenizer strToken = new StringTokenizer(contentType, ";", false);
			while (strToken.hasMoreTokens()) {
				String token = strToken.nextToken();
				if (LOG.isLoggable(Level.FINEST)) {
					LOG.fine("Processing token " + token);
				}
				int index = token.indexOf(ACTION);
				if (index != -1) {
					soapAction = token.substring(index + ACTION.length());
				}
			}
		}

		if (LOG.isLoggable(Level.FINER)) {
			LOG.exiting(CLASS, "getSoapAction", soapAction);
		}

		return soapAction;
	}

	// Deprecated method ... do nothing
	@Override
	public int getSharingPolicy(ServletCacheRequest request) {
		// Dynacache runtime will not do anything
		return 0;
	}

	// Deprecated method ... do nothing
	@Override
	public void initialize(CacheConfig cc) {
		// Dynacache runtime will not do anything
	}
}
