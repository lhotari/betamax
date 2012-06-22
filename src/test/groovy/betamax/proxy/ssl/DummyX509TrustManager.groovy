package betamax.proxy.ssl;

import groovy.transform.InheritConstructors

import java.security.KeyStore

import javax.net.ssl.SSLContext
import javax.net.ssl.SSLException
import javax.net.ssl.SSLSession
import javax.net.ssl.SSLSocket
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import java.security.cert.X509Certificate

import org.apache.http.conn.ssl.SSLSocketFactory
import org.apache.http.conn.ssl.X509HostnameVerifier

class DummyX509TrustManager implements X509TrustManager {
	void checkClientTrusted(X509Certificate[] chain, String authType) {
	}

	void checkServerTrusted(X509Certificate[] chain, String authType) {
	}

	X509Certificate[] getAcceptedIssuers() {
		null
	}
}






