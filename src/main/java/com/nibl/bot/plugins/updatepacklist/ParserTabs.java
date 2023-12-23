package com.nibl.bot.plugins.updatepacklist;

import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedList;
import java.util.List;
import com.nibl.bot.util.InputStreamConverter;

public class ParserTabs extends AbstractParser {
    /**
     * Sample of this file taken from loke's packlist
     * https://gin.sadaharu.eu/Gin.txt
     */
	public LinkedList<Pack> parse(AbstractDistroBot bot, RandomAccessFile in) throws Exception {
		
		PackParseFunctions packParseFunctions = new PackParseFunctions(bot.getPircBotX());
		
		LinkedList<Pack> output = new LinkedList<Pack>();
		
		if( null == bot || null == in ){
			bot.getPircBotX().getLogger().error("Bad buffer or bot, cannot parse packlist");
			return output;
		}
		
		int failedParsePacks = 0;
		
		in.seek(0);
		
		String line = "";
		while ( (line = in.readLine()) != null ) {
			line = line.replaceAll("\t", " ");
			if (line.trim().startsWith("#")) {
			    try {
    			    // Parse out the pack number
    				int beginIndex = line.indexOf('#') + 1;
    				int endIndex = line.indexOf(' ', beginIndex);
    				
    				int number = Integer.parseInt(line.substring(beginIndex, endIndex));
    
    				// parse out the pack size
    				beginIndex = line.indexOf('[', endIndex) + 1;
    				endIndex = line.indexOf(']', beginIndex);
    				String size = line.substring(beginIndex, endIndex);
    				
    				// parse out the pack name
    				String name = line.substring(endIndex + 2);
    				
    				// Build the pack and store to list
    				Pack pack = packParseFunctions.buildPack(bot, number, name, size);
    				output.add(pack);
                } catch(Exception e){
                    // Can't parse the pack
                    // System.out.println("Can't parse: " + line);
                    failedParsePacks++;
                }
			}
		}
		
		if( output.size() > 0 && failedParsePacks > 0 ){
		    bot.getPircBotX().getLogger().error("Found " + output.size() + " packs, but failed to parse " + failedParsePacks + " packs for " + bot.getName());
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
        
        ParserTabs parser = new ParserTabs();
        List<Pack> packs = parser.parse(tempBot, in);
        for( Pack pack : packs ){
            System.out.println( pack.toString() );
        }
	}
}
