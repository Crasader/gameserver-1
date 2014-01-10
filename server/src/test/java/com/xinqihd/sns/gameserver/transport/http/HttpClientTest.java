package com.xinqihd.sns.gameserver.transport.http;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class HttpClientTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() throws Exception {
		String host = "192.168.0.77";
		//String host = "charge.babywar.xinqihd.com";
		//URL url = new URL("http://charge.babywar.xinqihd.com/dangle");
		URL url = new URL("http://"+host+":8080/dangle");
		HttpURLConnection conn = (HttpURLConnection)url.openConnection();
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		conn.setReadTimeout(5000);
		conn.setRequestProperty("Host", host);
		conn.setDoInput(true);
		conn.setDoOutput(true);
		OutputStream os = conn.getOutputStream();
		os.write("result=1&uip=1".getBytes());
		os.flush();
		os.close();
		InputStream is = conn.getInputStream();
		BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
		String line = null;
		while ((line = reader.readLine()) != null) {
			System.out.println(line);
		}
		reader.close();
		int responseCode = conn.getResponseCode();
		System.out.println(responseCode);
	}

}
