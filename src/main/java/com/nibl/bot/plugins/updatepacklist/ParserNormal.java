package com.nibl.bot.plugins.updatepacklist;

import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedList;
import java.util.List;
import com.nibl.bot.util.InputStreamConverter;

public class ParserNormal extends AbstractParser {
	
	public LinkedList<Pack> parse(AbstractDistroBot bot, RandomAccessFile in) throws Exception {
		
		PackParseFunctions packParseFunctions = new PackParseFunctions(bot.getPircBotX());
		
		LinkedList<Pack> output = new LinkedList<Pack>();
		
		if( null == bot || null == in ){
			bot.getPircBotX().getLogger().error("Bad buffer or bot, cannot parse packlist");
			return output;
		}
		
		in.seek(0);
		
		String line = "";
		while ( (line = in.readLine()) != null ) {
		    try{
    			String[] parsedLines = line.split("<br>"); // in case it's all in one
    			for (String parsedLine : parsedLines) {
    				line = parsedLine.replaceAll("", "");
    				if (line.trim().startsWith("#")) {
    					int beginIndex;
    					int endIndex;
    
    					beginIndex = line.indexOf('#') + 1;
    					endIndex = line.indexOf(' ', beginIndex);
    					int number = Integer.parseInt(line.substring(beginIndex, endIndex));
    
    					beginIndex = line.indexOf('[', endIndex) + 1;
    					endIndex = line.indexOf(']', beginIndex);
    					String size = line.substring(beginIndex, endIndex);
    
    					String name = line.substring(endIndex + 2);
    					
    					Pack pack = packParseFunctions.buildPack(bot, number, name, size);
    					output.add(pack);
    				}
    			}
            } catch(Exception e){
                // Can't parse the pack
                // System.out.println("Can't parse: " + line);
            }
		}
		
		return output;
	}
	
	public static void main(String[] args) throws Exception{
	    HttpDistroBot tempBot = new HttpDistroBot(null, 1, "tempBot", "https://gin.sadaharu.eu/Gin.txt", "HTTP", 1, null, null, null, 0, 0, "Jenga", 0);
	    
        URL urlopen = new URL( tempBot.getURL() );
		URLConnection urlConnection = urlopen.openConnection();
		urlConnection.setConnectTimeout(10000);
		urlConnection.setReadTimeout(10000);
		urlConnection.setRequestProperty("User-Agent", "ooinuzaBot");
		InputStream inputStream = urlConnection.getInputStream();
		RandomAccessFile in = InputStreamConverter.toRandomAccessFile( inputStream );
        
        ParserNormal parser = new ParserNormal();
        List<Pack> packs = parser.parse(tempBot, in);
        for( Pack pack : packs ){
            System.out.println( pack.toString() );
        }
	}
}
