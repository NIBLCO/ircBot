package com.nibl.bot.plugins.updatepacklist;

import java.io.RandomAccessFile;
import java.util.LinkedList;

public class ParserDinoex extends AbstractParser {
	
	@Override
	public LinkedList<Pack> parse(AbstractDistroBot bot, RandomAccessFile in) throws Exception {
		
		LinkedList<Pack> output = new LinkedList<Pack>();
		
		if( null == bot || null == in ){
			bot.getPircBotX().getLogger().error("Bad buffer or bot, cannot parse packlist");
			return output;
		}
		
		in.seek(0);
		
		String text;
		while((text = in.readLine()) !=null){
			if(text.startsWith("<td class")){
				StringBuilder items = new StringBuilder();
				String number = removeBrackets(text);
				in.readLine();//get rid of the 'Gets'
				String size = removeBrackets(in.readLine());
			herroloop: while((text = in.readLine()) != null){
					if(text.equals("</tr>"))
						break;
					if(text.contains("class=\"content\""))
						items.append("<"); //prepare to for stripping
						while((text = in.readLine()) != null){
							if(text.contains("</span>") || text.contains("</td>")){
								items.append(text);
								break herroloop;
							}
							items.append(text);
						}
					items.append(text);
				}
				String name = removeBrackets(items.toString());
				Pack pack = PackParseFunctions.buildPack(bot, Integer.parseInt(number.trim().replaceAll("#", "")), name, size);
				output.add(pack);
			} 
			
		}
		return output;
		
	}
	
	private String removeBrackets(String input){
		String strippedSearchTerm = "";
		int open = 0;
		for(char t : input.toCharArray()){
			if(t=='<'){
				open++;
			}
			if(open<1){
				strippedSearchTerm = strippedSearchTerm + t;
			}
			if(t=='>'){
				open--;
			}
		}
		return strippedSearchTerm;
	}
	
}
