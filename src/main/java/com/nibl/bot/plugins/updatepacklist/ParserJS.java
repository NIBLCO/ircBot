package com.nibl.bot.plugins.updatepacklist;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParserJS extends AbstractParser {
	
	private static Pattern _pattern = Pattern.compile("\\s*(?:p\\.k|packlist.packs)\\[(\\d*)\\]\\s*=\\s*\\{\\s*(?:b(?:ot|):\\s*\"(.*?)\"\\s*,\\s*|)n(?:umber|):\\s*(\\d+)\\s*,\\s*(?:g(?:ets|):\\s*(\\d+)\\s*,\\s*|)s(?:ize|):\\s*(\\d+)\\s*,\\s*(?:name|f):\\s*\"(.*?)\"\\s*\\}\\s*;\\s*", Pattern.MULTILINE | Pattern.DOTALL);
	
	public LinkedList<Pack> parse(AbstractDistroBot bot, RandomAccessFile in) 
			throws NumberFormatException, IOException{
		
		LinkedList<Pack> output = new LinkedList<Pack>();
		
		if( null == bot || null == in ){
			bot.getPircBotX().getLogger().error("Bad buffer or bot, cannot parse packlist");
			return output;
		}
		
		in.seek(0);
		
		String line = "";
		while ( (line = in.readLine()) != null ) {
			Matcher matcher;
			line = line.trim();

			matcher = _pattern.matcher(line);
			if( matcher.find() ) {
				output.add( PackParseFunctions.buildPack(bot, Integer.parseInt(matcher.group(3)), matcher.group(6), matcher.group(5) + 'M') );
			}
		}
		
		return output;
		
	}
	
}
