package com.ft.upp.servicenameprovider.mvp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONObject;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

public class LambdaFunctionHandler implements RequestHandler<Object, String> {

	@Override
	public String handleRequest(Object input, Context context) {
		context.getLogger().log("Input: " + input);

		String result = "default";
		try {
			result = provedeServiceNameList();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	public static String provedeServiceNameList() throws IOException {
		 URL url = new URL("https://upp-prod-publish-eu.ft.com/__health");
//		URL url = new URL("http://ft.com");
//		URL url = new URL("http://google.co.uk");
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
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
		
		String responseBody = content.toString();
		JSONObject obj = new JSONObject(responseBody);
		

		StringBuilder fullResponseBuilder = new StringBuilder();

		fullResponseBuilder.append(con.getResponseCode())
//			.append(" ")
			.append(con.getResponseMessage())
			.append("\n")
			.append(content.toString())
			.append("\n");

		return fullResponseBuilder.toString();
	}

}