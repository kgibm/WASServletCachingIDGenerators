# WAS Servlet Caching ID Generators

## ID Generators

### org.sample.WebServicesSOAPActionAndEnvelopeHashIdGenerator

As of March 2018, WAS traditional servlet caching does not support JAX-WS.
This is an example custom ID generator that allows JAX-WS to work with servlet caching.
This code requires Java 8 (due to the use of `java.util.Base64`.
For POST SOAP requests, it generates a cache ID of SOAPAction=${SOAPAction}:SOAPEnvelope=${Base64(SHA-256(SOAPEnvelope))}

Example WAR file WebContent/WEB-INF/cachespec.xml:

    <?xml version="1.0" ?>
    <!DOCTYPE cache SYSTEM "cachespec.dtd">
    <cache>
    	<cache-entry>
    		<class>webservice</class>
    		<name>/SampleJAXWSServiceName</name>
    		<sharing-policy>not-shared</sharing-policy>
    		<cache-id>
    			<timeout>60</timeout>
    			<priority>1</priority>
    			<idgenerator>org.sample.WebServicesSOAPActionAndEnvelopeHashIdGenerator</idgenerator>
    		</cache-id>
	    </cache-entry>
    </cache>

## Installing

This WASServletCachingIDGenerators.jar file must be bundled as a shared library and associated with each application server.

### Create a Shared Library

1. Environment > Shared Library
1. Select Scope: Cell=[...]
1. Click New...
1. Name = WASServletCachingIDGenerators
1. Classpath = ${PATH_TO}/WASServletCachingIDGenerators.jar
1. Click OK

### Associate Shared Library with Application Server
1. Servers > Server Types > WebSphere application servers > ${SERVER} > Server Infrastructure > Java and Process Management > Class loader
1. Click New...
1. Select Classes loaded with parent class loader first
1. Click OK
1. Click the class loader that was created
1. Click Shared library references
1. Click Add
1. Select WASServletCachingIDGenerators
1. Click OK
1. Save, synchronize, and restart

## Development

1. Window > Preferences > Java > Build Path > User Libraries > New
1. WAS_DEV
1. Click OK
1. Select WAS_DEV
1. Click Add External JARs
1. Select ${WAS}/dev/was_public.jar
1. Repeat again for ${WAS}/dev/JavaEE/j2ee.jar
1. Run build.xml to generate WASServletCachingIDGenerators.jar

## References

1. https://www.ibm.com/support/knowledgecenter/SSAW57_9.0.0/com.ibm.websphere.nd.multiplatform.doc/ae/rdyn_cachespec.html
1. https://wasdynacache.blogspot.com/2010/02/caching-webservices-responses-in.html
