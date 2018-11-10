package com.nibl.bot.plugins.updatepacklist;

import java.io.RandomAccessFile;
import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

import com.nibl.bot.Bot;
import com.nibl.bot.BotExtend;
import com.nibl.bot.plugins.search.SearchDAO;

public class PackParseFunctions extends BotExtend {
	
	public enum SizeMult {
		SIZE_NULL("",0d),
		SIZE_KB("K",1d),
		SIZE_MB("M",Math.pow(1024, 2)),
		SIZE_GB("G",Math.pow(1024, 3)),
		SIZE_TB("T",Math.pow(1024, 4)),
		SIZE_PB("P",Math.pow(1024, 5));
	    
		String text;
		Double mul;
		SizeMult(String text, Double mul) {
			this.text = text;
			this.mul = mul;
		}
		public SizeMult getSizeMultByText(String text){
			for( SizeMult sizeMult : SizeMult.values() ) {
				if( sizeMult.text.toLowerCase().equals(text.toLowerCase()) ){
					return sizeMult;
				}
			}
			return SizeMult.SIZE_NULL;
		}
	};
	
	public enum Parser {
		PARSER_NULL(0,null),
		PARSER_NORMAL(PARSER_NORMAL_ID, new ParserNormal()), 
		PARSER_DINOEX(PARSER_DINOEX_ID, new ParserDinoex()), 
		PARSER_JS(PARSER_JS_ID, new ParserJS()),
	    PARSER_TABS(PARSER_TABS_ID, new ParserTabs());
	    
		Integer _parserId;
		AbstractParser _parser;
		Parser(Integer parserId, AbstractParser parser) {
			_parserId = parserId;
			_parser = parser;
		}
		public Parser getParserById(Integer Id){
			for( Parser parser : Parser.values() ) {
				if( parser._parserId == Id ){
					return parser;
				}
			}
			return Parser.PARSER_NULL;
		}
	};
	
	public static final int PARSER_NULL_ID = 0;
	public static final int PARSER_NORMAL_ID = 1;
	public static final int PARSER_DINOEX_ID = 2;
	public static final int PARSER_JS_ID = 3;
	public static final int PARSER_TABS_ID = 4;
	
	public SearchDAO _searchDAO;
	
	public PackParseFunctions(Bot myBot){
		super(myBot);
		_searchDAO = (SearchDAO) _myBot.getDAOFactory().getDAO("SearchDAO");
	}
	
	public Pack buildPack(AbstractDistroBot bot, int packnumber, String packname, String packsize)
	{
		
		Long sizeKBits = 0L;
		try{
			packsize = packsize.trim();
			String sizeDesc = packsize.substring(packsize.length() -1 , packsize.length());
			SizeMult sizeMult = SizeMult.SIZE_NULL.getSizeMultByText(sizeDesc);
			sizeKBits = (long) (Double.parseDouble(packsize.replace(sizeDesc, "").replaceAll("<", "")) * sizeMult.mul);
		}catch(Exception e) {
			_myBot.getLogger().error("Failed to calculate size with " + packsize, e);
		}
		
		return new Pack(bot.getId(), packnumber, bot.getName(), packname, packsize, sizeKBits, -1, new Timestamp(System.currentTimeMillis()));
	}
	
	/*
	 * It is sometimes unknown which parser to use.
	 * If no parser is known, each parser will be tried until there is a success.
	 * If the parser fails (future packlist changes), all parsers will be re-tried
	 */
	public LinkedList<Pack> randomAccessFileToPacks(AbstractDistroBot bot, RandomAccessFile in) 
			throws Exception {
		
		LinkedList<Pack> output = new LinkedList<Pack>();
		try{
			output = Parser.PARSER_NULL.getParserById( bot.getParserId() )._parser.parse(bot, in);
			if( output.size() == 0 ) {
				_myBot.getLogger().warn("Not able to find packs using declared parser. Attempting others...");
			} else {
				return output;
			}
		} catch(Exception e){ 
			// New bots with parser unknown will throw an error here. Ignore it and allow testing of other parsers.
		} 
		
		for( Parser parser : Parser.values() ) {
			if( parser._parserId == Parser.PARSER_NULL._parserId ||
				parser._parserId == bot.getParserId() ) {
				continue;
			}
			output = parser._parser.parse(bot, in);
			if( output.size() > 0 ) {
				_myBot.getLogger().info("Found packs using parser " + parser._parserId + ".");
				bot.setParserId( parser._parserId );
				return output;
			}
		}
		_myBot.getLogger().error("No packs found after trying all parsers");
		return output;
	}
	
	public Boolean packListChanged(AbstractDistroBot bot, List<Pack> newData) {
		
		_myBot.getLogger().trace("Start Compare and update database");
		Boolean update = false;
		//packs have been removed or added
		if( bot.getListing().size() != newData.size() ){
			update = true;
		}
		
		// packs can be missing, locked, or non-contiguous
		// So, instead of using old listing, use a listing of <packNumber> = Pack when comparing data
		TreeMap<Integer, Pack> map = new TreeMap<Integer,Pack>();
		for(Pack pack : bot.getListing() ){
			map.put(pack.getNumber(), pack);
		}
		
		for (int i = 0; i < newData.size(); i++) { // For each piece of new data
			Pack comparePack = map.get(newData.get(i).getNumber());
			// If pack exists in current listing and has not changed, update new pack attributes
			if ( null != comparePack && 
				 comparePack.getName().equals( newData.get(i).getName() ) &&   
				 comparePack.getNumber() == newData.get(i).getNumber() && 
				 comparePack.getSize().equals( newData.get(i).getSize() ) &&
				 comparePack.getSizeKBits().equals( newData.get(i).getSizeKBits() )
				){
				
				newData.get(i).setLastModified( comparePack.getLastModified() );
				newData.get(i).setEpisodeNumber( comparePack.getEpisodeNumber() );
				
			} else {
				// Something has changed, so update db
				update = true;
				
				// Attempt to get episodeNumber
	        	int episodeNumber = -1;
				LinkedList<Integer> episodes = _searchDAO.findEpisodeNumber( _searchDAO.removeBrackets(newData.get(i).getName()) );
				if(!episodes.isEmpty()){
					episodeNumber = episodes.pop();
				}
				
				newData.get(i).setEpisodeNumber(episodeNumber);	
			}
		}
		
		return update;
		
	}
	
}
