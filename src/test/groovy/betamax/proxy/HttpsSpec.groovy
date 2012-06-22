package betamax.proxy

import betamax.proxy.jetty.SimpleServer
import betamax.proxy.ssl.DummyHostNameVerifier
import betamax.proxy.ssl.DummyJVMSSLSocketFactory
import betamax.proxy.ssl.DummySSLSocketFactory
import betamax.util.server.EchoHandler

import java.io.IOException;
import java.security.Security;
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.impl.conn.ProxySelectorRoutePlanner
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager
import org.eclipse.jetty.server.ssl.SslSelectChannelConnector
import org.junit.Rule
import betamax.*
import javax.net.ssl.*
import javax.servlet.GenericServlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse

import static org.apache.http.HttpHeaders.VIA
import static org.apache.http.HttpVersion.HTTP_1_1
import org.apache.http.conn.scheme.*
import org.apache.http.conn.ssl.*
import org.apache.http.params.*
import static org.apache.http.protocol.HTTP.UTF_8
import org.eclipse.jetty.server.*
import org.eclipse.jetty.servlet.ServletContextHandler

import spock.lang.*
import groovy.transform.InheritConstructors
import static org.apache.http.HttpStatus.SC_OK

@Issue("https://github.com/robfletcher/betamax/issues/34")
class HttpsSpec extends Specification {

	@Shared @AutoCleanup("deleteDir") File tapeRoot = new File(System.properties."java.io.tmpdir", "tapes")
	@Rule @AutoCleanup("ejectTape") Recorder recorder = new Recorder(tapeRoot: tapeRoot, sslSupport: true)
	@Shared @AutoCleanup("stop") SimpleServer httpsEndpoint = new SimpleSecureServer(5001)
	@Shared @AutoCleanup("stop") SimpleServer httpEndpoint = new SimpleServer()

	@Shared URI httpUri
	@Shared URI httpsUri

	HttpClient http

	def setupSpec() {
		def handler = new ServletContextHandler()
		handler.contextPath = "/"
		handler.addServlet(HelloServlet, "/*")


		httpEndpoint.start(EchoHandler)
		httpsEndpoint.start(handler)

		httpUri = httpEndpoint.url.toURI()
		httpsUri = httpsEndpoint.url.toURI()
	}

	def setup() {
		def params = new BasicHttpParams()
		HttpProtocolParams.setVersion(params, HTTP_1_1)
		HttpProtocolParams.setContentCharset(params, "UTF-8")

		def registry = new SchemeRegistry()
		registry.register new Scheme("http", PlainSocketFactory.socketFactory, 80)
		registry.register new Scheme("https", DummySSLSocketFactory.getInstance(), 443)

		def connectionManager = new ThreadSafeClientConnManager(params, registry)

		http = new DefaultHttpClient(connectionManager, params)
		http.routePlanner = new ProxySelectorRoutePlanner(http.connectionManager.schemeRegistry, ProxySelector.default)
	}

	@Betamax(tape = "https spec")
	@Unroll("proxy is selected for #scheme URIs")
	def "proxy is selected for all URIs"() {
		given:
		def proxySelector = ProxySelector.default

		expect:
		def proxy = proxySelector.select(uri).first()
		proxy.type() == Proxy.Type.HTTP
		proxy.address().toString() == "$recorder.proxyHost:${scheme == 'https' ? recorder.httpsProxyPort : recorder.proxyPort}"

		where:
		uri << [httpUri, httpsUri]
		scheme = uri.scheme
	}

	@Betamax(tape = "https spec", mode = TapeMode.WRITE_ONLY)
	def "proxy can intercept HTTP requests"() {
		when: "an HTTPS request is made"
		def response = http.execute(new HttpGet(httpEndpoint.url))

		then: "it is intercepted by the proxy"
		response.statusLine.statusCode == SC_OK
		response.getFirstHeader(VIA)?.value == "Betamax"
	}

	@Betamax(tape = "https spec", mode = TapeMode.WRITE_ONLY)
	def "proxy can intercept HTTPS requests"() {
		when: "an HTTPS request is made"
		def response = http.execute(new HttpGet(httpsEndpoint.url))
		def responseBytes = new ByteArrayOutputStream()
		response.entity.writeTo(responseBytes)
		def responseString = responseBytes.toString("UTF-8")

		then: "it is intercepted by the proxy"
		response.statusLine.statusCode == SC_OK
		response.getFirstHeader(VIA)?.value == "Betamax"
		responseString == 'Hello World!'
	}


	@Betamax(tape = "https spec", mode = TapeMode.WRITE_ONLY)
	def "https request gets proxied"() {
		when:
		def u = new java.net.URL(httpsEndpoint.url)
		def content = u.text
		then:
		content == "Hello World!"
	}
}


@InheritConstructors
class SimpleSecureServer extends SimpleServer {

	@Override
	String getUrl() {
		"https://$host:$port/"
	}

	@Override
	protected Server createServer(int port) {
		def server = super.createServer(port)

		def connector = new SslSelectChannelConnector()

		String keystore = new File("src/main/resources/keystore").absolutePath

		connector.port = port
		connector.keystore = keystore
		connector.password = "password"
		connector.keyPassword = "password"

		server.connectors = [connector]as Connector[]

		server
	}
}

class HelloServlet extends GenericServlet {

	@Override
	public void service(ServletRequest req, ServletResponse res)
	throws ServletException, IOException {
		HttpServletResponse response = res
		response.setContentType("text/plain")
		response.writer << "Hello World!"
	}
}
