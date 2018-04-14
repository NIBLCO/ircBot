package com.nibl.bot.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.URL;
import java.net.URLConnection;
import java.security.cert.X509Certificate;

import com.nibl.bot.Bot;
import com.nibl.bot.BotExtend;
import com.sun.net.ssl.HostnameVerifier;
import com.sun.net.ssl.HttpsURLConnection;
import com.sun.net.ssl.SSLContext;
import com.sun.net.ssl.TrustManager;
import com.sun.net.ssl.X509TrustManager;


@SuppressWarnings("deprecation")
public class HTTPGet extends BotExtend {

	public HTTPGet(Bot myBot) {
		super(myBot);
        // Create a trust manager that does not validate certificate chains
        final TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
			
			@Override
			public boolean isServerTrusted(X509Certificate[] arg0) {
				return true;
			}
			
			@Override
			public boolean isClientTrusted(X509Certificate[] arg0) {
				return false;
			}
			
			@Override
			public X509Certificate[] getAcceptedIssuers() {
				return null;
			}
		}};

        HostnameVerifier hv = new HostnameVerifier() {
        	
			@Override
			public boolean verify(String arg0, String arg1) {
				return true;
			}
        };
        HttpsURLConnection.setDefaultHostnameVerifier(hv);
        
        try{
        	
	        // Install the all-trusting trust manager
	        final SSLContext sslContext = SSLContext.getInstance("SSL");
	        sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
	        HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
	        

	
	        // Create an ssl socket factory with our all-trusting manager
	        System.setProperty("sun.security.ssl.allowUnsafeRenegotiation", "true");
        
        } catch(Exception e){
        	e.printStackTrace();
        }
	}
	
	public RandomAccessFile getURLData(String url){
	    try {
	        URL urlopen = new URL(url);
			URLConnection urlConnection = urlopen.openConnection();
			urlConnection.setConnectTimeout(10000);
			urlConnection.setReadTimeout(10000);
			urlConnection.setRequestProperty("User-Agent", "ooinuzaBot");
			InputStream inputStream = urlConnection.getInputStream();
			return InputStreamConverter.toRandomAccessFile( inputStream );
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	    
	    return null;
	}
	
}