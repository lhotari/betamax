package betamax.proxy.ssl;

import groovy.transform.InheritConstructors

import java.security.KeyStore

import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager

import org.apache.http.conn.ssl.SSLSocketFactory

@InheritConstructors
class DummySSLSocketFactory extends SSLSocketFactory {
	SSLContext sslContext = SSLContext.getInstance("TLS")
	private javax.net.ssl.SSLSocketFactory factory;
	
	public static DummySSLSocketFactory getInstance() {
		def trustStore = KeyStore.getInstance(KeyStore.defaultType)
		trustStore.load(null, null)
		new DummySSLSocketFactory(trustStore)
	}

	public DummySSLSocketFactory(KeyStore trustStore) {
		super(trustStore)
		sslContext.init(null, [new DummyX509TrustManager()] as TrustManager[], new java.security.SecureRandom())
		factory = sslContext.socketFactory
		setHostnameVerifier(new DummyHostNameVerifier())
	}
	
	public static javax.net.ssl.SSLSocketFactory getDefault() {
		return new DummySSLSocketFactory();
	}

	@Override
	Socket createSocket(Socket socket, String host, int port, boolean autoClose) {
		factory.createSocket(socket, host, port, autoClose)
	}

	@Override
	Socket createSocket() throws IOException {
		factory.createSocket()
	}
}