package com.nibl.bot.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.URL;
import java.net.URLConnection;

import com.nibl.bot.Bot;
import com.nibl.bot.BotExtend;

public class HTTPGet extends BotExtend {

	public HTTPGet(Bot myBot) {
		super(myBot);
	}

	public RandomAccessFile getURLData(String url) {
		try {
			URL urlopen = new URL(url);
			URLConnection urlConnection = urlopen.openConnection();
			urlConnection.setConnectTimeout(10000);
			urlConnection.setReadTimeout(10000);
			urlConnection.setRequestProperty("User-Agent", "ooinuzaBot");
			InputStream inputStream = urlConnection.getInputStream();
			return InputStreamConverter.toRandomAccessFile(inputStream);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

}