package com.ft.upp.servicenameprovider.mvp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;

import com.amazonaws.services.lambda.runtime.Context;

public class LambdaFunctionHandlerTest {

	private static Object input;
	private static final File UPDE_HEALTH_GOOD = new File("src/test/resources/upp-prod-delivery-eu-health-good.json");
	private TestMockServer ts;

	@BeforeClass
	public static void createInput() throws IOException {
		input = null;
	}

	@AfterClass
	public static void cleanUp() {
	}

	@BeforeEach
	public void init() {
	}

	public void startServer() {
		ts = new TestMockServer();
		ts.startServer();
	}

	public void stopServer() {
		ts.stopServer();
	}

	private Context createContext() {
		TestContext ctx = new TestContext();
		ctx.setFunctionName("Provide a list of service names.");
		return ctx;
	}

	@Test
	public void testLambdaFunctionHandler() throws IOException {
		FileReader fr = new FileReader(UPDE_HEALTH_GOOD);
		BufferedReader reader = new BufferedReader(fr);
		String fileContents = reader.lines().collect(Collectors.joining(System.lineSeparator()));

		startServer();
		ts.createMockServerExpectation1(fileContents);
		fr.close();

		List<String> expected = new ArrayList<String>(Arrays.asList("annotations-rw-neo4j", "api-policy-component", "body-validation-service", "cms-notifier"));
		List<String> result = LambdaFunctionHandler.provideServiceNameList("http://127.0.0.1:1080/test1");

		Assert.assertEquals(expected, result);

		stopServer();
	}
}
