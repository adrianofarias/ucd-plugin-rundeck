import javax.net.ssl.*

public class HttpHelper {

    def request
    def nullTrustManager = [
        checkClientTrusted: { chain, authType ->  },
        checkServerTrusted: { chain, authType ->  },
        getAcceptedIssuers: { null }
    ]

    def nullHostnameVerifier = [
        verify: { hostname, session -> true }
    ]

//    SSLContext sc = new SSLContext.getInstance("SSL")
//    sc.init(null, [nullTrustManager as X509TrustManager] as TrustManager[], null)
//    HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory())
//    HttpsURLConnection.setDefaultHostnameVerifier(nullHostnameVerifier as HostnameVerifier)

    public HttpHelper(String url, String method) {
	    request = new URL(url).openConnection();
	    switch(method) {
	      case "DELETE":
	        request.setRequestMethod("DELETE");
	      break
	      default:
	        request.setDoOutput(true);
	        request.setRequestMethod(method);
	      break
	    }
  	}

    public HttpHelper(String url) {
        request = new URL(url).openConnection()
        request.setRequestMethod("GET")
    }

    def getResponseText()   {
        def responseCode = request.getResponseCode()
        println responseCode
        if (responseCode.equals(200))   {
            return getURL.getInputStream().getText()
        } else  {
            return null
        }
    }
}