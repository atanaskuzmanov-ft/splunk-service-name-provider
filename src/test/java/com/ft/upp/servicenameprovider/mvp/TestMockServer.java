package com.ft.upp.servicenameprovider.mvp;

import java.io.FileReader;
import java.util.concurrent.TimeUnit;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.mockserver.client.server.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.matchers.TimeToLive;
import org.mockserver.matchers.Times;
import org.mockserver.model.Header;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;

public class TestMockServer {

	private ClientAndServer mockServer;

	@BeforeClass
	public void startServer() {		
		mockServer = ClientAndServer.startClientAndServer(1080);
//		createMockServerExpectation1();
	}

	@AfterClass
	public void stopServer() {
		mockServer.stop();
	}

	protected void createMockServerExpectation1(String reponseBody) {
//		new MockServerClient("127.0.0.1", 1080)

		
		mockServer
				.when(
						HttpRequest.request()
						.withMethod("GET")
						.withPath("/test1")
						.withHeader("\"Accept\", \"application/json\""), 
						Times.exactly(1), TimeToLive.exactly(TimeUnit.MINUTES, 1l))
				.respond(HttpResponse.response().withStatusCode(200)
						.withHeaders(
								new Header("Content-Type", "application/json; charset=utf-8"),
								new Header("Cache-Control", "public, max-age=86400"))
//						.withBody("{ message: 'incorrect username and password combination' }")
						.withBody(reponseBody)
						.withDelay(TimeUnit.SECONDS, 1));
	}

}
