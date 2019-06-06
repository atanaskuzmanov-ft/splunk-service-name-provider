package com.ft.upp.servicenameprovider.mvp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

public class LambdaFunctionHandler implements RequestHandler<Object, String> {
	
	private static final String UPP_PROD_PUBLISH_EU_URL = "https://upp-prod-publish-eu.ft.com/__health";
	private static final String UPP_PROD_DELIVERY_EU_URL = "https://upp-prod-delivery-eu.ft.com/__health";

	@Override
	public String handleRequest(Object input, Context context) {
//		context.getLogger().log("Input: " + input + "\n");
		List<String> urls = new ArrayList<String>(Arrays.asList(UPP_PROD_PUBLISH_EU_URL, UPP_PROD_DELIVERY_EU_URL));
		String result = "";
		try {
			for(String u : urls)
				result = provedeServiceNameList(u);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	public static String provedeServiceNameList(String urlString) throws IOException {
		URL url;
		StringBuilder fullResponseBuilder = new StringBuilder();
		try {
//			url = new URL("https://upp-prod-publish-eu.ft.com/__health");
			url = new URL(urlString);

			HttpURLConnection con;

			con = (HttpURLConnection) url.openConnection();

			con.setRequestMethod("GET");

			con.setRequestProperty("Accept", "application/json");
			con.setConnectTimeout(5000);
			con.setReadTimeout(5000);
			con.setInstanceFollowRedirects(true);

			int status = con.getResponseCode();

			if (status == HttpURLConnection.HTTP_MOVED_TEMP || status == HttpURLConnection.HTTP_MOVED_PERM) {
				String location = con.getHeaderField("Location");
				URL newUrl = new URL(location);
				con = (HttpURLConnection) newUrl.openConnection();
				status = con.getResponseCode();
			}

			Reader streamReader = null;

			if (status > 299) {
				streamReader = new InputStreamReader(con.getErrorStream());
			} else {
				streamReader = new InputStreamReader(con.getInputStream());
			}

			BufferedReader in = new BufferedReader(streamReader);

			String inputLine;
			StringBuffer content = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
				content.append(inputLine);
			}
			in.close();

			con.disconnect();

			String serviceNames = extractServiceNames(content.toString());

			fullResponseBuilder.append(serviceNames).append("\n");

		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		}

		return fullResponseBuilder.toString();
	}

	public static String extractServiceNames(String responseBody) {
		JSONArray serviceNamesJSON = new JSONArray();

		JSONObject obj = new JSONObject(responseBody);
		JSONArray arr = obj.getJSONArray("checks");

		for (int i = 0; i < arr.length(); i++) {
			String serviceName = arr.getJSONObject(i).getString("name");
			serviceNamesJSON.put(serviceName);
		}

		String serviceNames = serviceNamesJSON.toString();
		System.out.println(serviceNames);

		return serviceNames;
	}
}
