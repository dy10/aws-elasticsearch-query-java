package dy.aws.es;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

/**
 * Example: Signing AWS Requests with Signature Version 4 in Java(Test class).
 * @reference: http://docs.aws.amazon.com/general/latest/gr/sigv4_signing.html
 * @author javaQuery
 * @date 19th January, 2016
 * @Github: https://github.com/javaquery/Examples
 */
public class EsQuery {
	
	// replace following 4 String with real values to make it work
	// following 3 are fake values, so it wont run as it is...
	static String host = "search-es-cloudtrail-logs-a2ouhrbg3v4s4sfi4qzg3bz4ee.us-west-2.es.amazonaws.com";
	static String accessKey = "AKIAJY2CLTFJCB9E7YMQ";
	static String secretKey = "LbNdttxgoYsBgI/oPDav6icKNRaG1gA50GJYsahV";
	static String region = "us-west-2";
	
	public static void main(String[] args) {
		String query = "/_search";
		String payload = "{\"query\":{\"match_all\":{}}}";
		
		String url = "http://" + host + query;

		TreeMap<String, String> awsHeaders = new TreeMap<String, String>();
		awsHeaders.put("host", host);	
		
		AWSV4Auth aWSV4Auth = new AWSV4Auth.Builder(accessKey, secretKey)
				.regionName(region)
				.serviceName("es") // es - elastic search. use your service name
				.httpMethodName("POST") //GET, PUT, POST, DELETE, etc...
				.canonicalURI(query) //end point
				.queryParametes(null) //query parameters if any
				.awsHeaders(awsHeaders) //aws header parameters
				.payload(payload) // payload if any
				.debug() // turn on the debug mode
				.build();

		HttpPost httpPost = new HttpPost(url);
		StringEntity requestEntity = new StringEntity(payload, ContentType.APPLICATION_JSON);
		httpPost.setEntity(requestEntity);

		/* Get header calculated for request */
		Map<String, String> header = aWSV4Auth.getHeaders();
		for (Map.Entry<String, String> entrySet : header.entrySet()) {
			String key = entrySet.getKey();
			String value = entrySet.getValue();

			/* Attach header in your request */
			/* Simple get request */

			httpPost.addHeader(key, value);
		}
		httpPostRequest(httpPost);

	}

	static void httpPostRequest(HttpPost httpPost) {
		/* Create object of CloseableHttpClient */
		CloseableHttpClient httpClient = HttpClients.createDefault();

		/* Response handler for after request execution */
		ResponseHandler<String> responseHandler = new ResponseHandler<String>() {

			public String handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
				/* Get status code */
				int status = response.getStatusLine().getStatusCode();
				if (status >= 200 && status < 300) {
					/* Convert response to String */
					HttpEntity entity = response.getEntity();
					return entity != null ? EntityUtils.toString(entity) : null;
				} else {
					throw new ClientProtocolException("Unexpected response status: " + status);
				}
			}
		};

		try {
			/* Execute URL and attach after execution response handler */
			String strResponse = httpClient.execute(httpPost, responseHandler);
			/* Print the response */
			System.out.println("Response: " + strResponse);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}