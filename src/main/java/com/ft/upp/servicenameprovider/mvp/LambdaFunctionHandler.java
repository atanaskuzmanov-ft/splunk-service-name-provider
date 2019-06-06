package com.ft.upp.servicenameprovider.mvp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
		Set<String> uniqueServiceNames = new HashSet<String>();
//		String result = "";
		JSONArray serviceNamesJSON = new JSONArray();
		try {
			for(String u : urls) {
//				result += provedeServiceNameList(u);
				for(String s : provedeServiceNameList(u))
				uniqueServiceNames.add(s);
			}
			serviceNamesJSON.put(uniqueServiceNames);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return serviceNamesJSON.toString();
	}

	public static List<String> provedeServiceNameList(String urlString) throws IOException {
		URL url;
//		StringBuilder fullResponseBuilder = new StringBuilder();
		List<String> serviceNamesList = new ArrayList<String>();
		try {
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

//			String serviceNames = extractServiceNames(content.toString());
			
			serviceNamesList = extractServiceNames(content.toString());

//			fullResponseBuilder.append(serviceNames).append("\n");

		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		}

//		return fullResponseBuilder.toString();
		return serviceNamesList;
	}

	public static List<String> extractServiceNames(String responseBody) {
//		JSONArray serviceNamesJSON = new JSONArray();
		 List<String> serviceNames = new ArrayList<String>();

		JSONObject obj = new JSONObject(responseBody);
		JSONArray arr = obj.getJSONArray("checks");

		for (int i = 0; i < arr.length(); i++) {
			String serviceName = arr.getJSONObject(i).getString("name");
//			serviceNamesJSON.put(serviceName);
			serviceNames.add(serviceName);
		}

//		String serviceNames = serviceNamesJSON.toString();
//		System.out.println(serviceNames);

		return serviceNames;
	}
}
