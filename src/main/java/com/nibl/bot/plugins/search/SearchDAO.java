package com.nibl.bot.plugins.search;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.pircbotx.Colors;

import com.nibl.bot.Bot;
import com.nibl.bot.dao.DataAccessObject;
import com.nibl.bot.plugins.updatepacklist.AbstractDistroBot;
import com.nibl.bot.plugins.updatepacklist.Pack;
import com.nibl.bot.plugins.updatepacklist.UpdatePackList;

public class SearchDAO extends DataAccessObject {
	//private final String _STATUS_TABLE = "updatepacklist_status";
	private final String _BOTS_TABLE = "updatepacklist_bots";
	private final String _PACKS_TABLE = "updatepacklist_packs";
	private final String _LIMIT = "15";
	Pattern epPattern = Pattern.compile(".*_(?:ep|episode)?(\\d{1,3})(?:v\\d+)?(?:-(?:ep|episode)?(\\d{1,3})(?:v\\d+)?)?(?:_|$)",
			Pattern.CASE_INSENSITIVE);
	Pattern dotPattern = Pattern.compile("((?<!\\d)\\.|\\.(?!\\d)| )");
	
	public SearchDAO(Bot myBot) {
		super(myBot);
	}

	@Override
	public void createTables() {}

	@Override
	public void dropTables() {}

	/**
	 * This function will start a search and determine results.
	 * @param sender
	 * @param searchTerm
	 * @return LinkedList<String[]> which is array[]{sender, pack info}
	 */
	public LinkedList<String[]> search(String sender, String searchTerm) {
		try{
			UpdatePackList updatePackListService = (UpdatePackList)_myBot.getServiceFactory().getRegisteredService().get("UpdatePackList");
			HashMap<Integer,Pack> results = new HashMap<Integer,Pack>();
			Pattern digitPattern = Pattern.compile(".*" + "\\[\\d{1,3}-\\d{1,3}\\]" + ".*", Pattern.CASE_INSENSITIVE); //<-- do we need this extra digitPattern?
			//Pattern possibleCRCPattern = Pattern.compile("(\\[\\w{8}\\])");
			
			String regexOfEpisodeNumbers = "";
			//<--can we just match a digitPattern once and use find.group() ?
			if(digitPattern.matcher(searchTerm).matches()){//If the user is searching for a specific range of episode numbers [1-10]
				digitPattern = Pattern.compile("\\[\\d{1,3}-\\d{1,3}\\]");
				Matcher digitMatcher = digitPattern.matcher(searchTerm);
				digitMatcher.find(); //find start and end numbers if exist
				String remDigit = digitMatcher.group(); //<-- only use digitMatcher.group() ? no need for a new string
				String[] range = digitMatcher.group().replace("[", "").replace("]", "").split("-");
				int start = Integer.parseInt(removeLeadingZeros(range[0]));
				int end = Integer.parseInt(removeLeadingZeros(range[1]));
				if(end<start){//we don't want people searching for [20-2]
					LinkedList<String[]> tmp = new LinkedList<String[]>();
					tmp.add(new String[]{sender,Colors.RED + "Please search in increasing order!"});
					return tmp;
				} else if(end-start>=Integer.parseInt(_LIMIT)){ //we only want max 15 results.  No [1-90]
					end = start+14;
				}
				searchTerm = searchTerm.replace(remDigit, "");//remove start and end numbers from searchTerm
				StringBuilder sb = new StringBuilder();
				for(int j = start; j<end+1; j++){ //populate the regex number term
					sb.append(j + "|");
				}
				regexOfEpisodeNumbers = "(" + sb.toString().substring(0,sb.toString().length() - 1) + ")";//complete the regexOfEpisodeNumbers, we end up with (1|2|3|4|5|6|7)
			}
			/*
				Eventually add a loop searching for multiple 'NOTS'
				i.e. for m.find() set notTerm=(term1|term2|term3)
			*/
			String notTerm = ""; //find the search strings we don't want
			if(searchTerm.contains("!")){ //!search episode number !cakes <--we should end up with "cakes"
				Pattern notPattern = Pattern.compile("(\\!\\w+)");
				Matcher m = notPattern.matcher(searchTerm);
				if (m.find()) {
					searchTerm = searchTerm.replace(m.group(), ""); //Strip notTerm from search string
					notTerm = m.group().toLowerCase().replace("!", "");
				}
			}else{
				notTerm = "JJ4fvieiwieEEEEQQ2?";
			}
			
			String httpList = "http://nibl.co.uk/search?query=" + searchTerm.trim().replaceAll(" ", "+");//prepare httpList for first output
			
			//Create the searchTerm pattern for matching and sanatize input
			Pattern searchTermPattern = Pattern.compile(".*" + searchTerm.trim().replaceAll("(\\{|\\}|\\+|\\\\|\\[|\\]|\\(|\\)|\\^|\\$|\\.|\\?|\\||\\<|\\>|\\-|\\&)","\\\\$1").replaceAll("[\\* _]", ".*") + ".*", Pattern.CASE_INSENSITIVE);
			
			List<Object> randomBots = Arrays.asList(updatePackListService.getBotsMap().values().toArray());//shuffle all bots
			Collections.shuffle(randomBots);
			
			int packsFound = 0;
			for(Object bot : randomBots){ //for each bot
				for(Pack pack : ((AbstractDistroBot) bot).getListing()){//get each listing and iterate packs
					//START matching pack to searchTerm
					if(!pack.getName().toLowerCase().contains(notTerm) && searchTermPattern.matcher(pack.getName()).matches()){ //if pack matches the pattern and doesn't contain a 'NOT' string
						if(regexOfEpisodeNumbers!="") {//START matching pack to searchterm and epsiode numbers
						/*
							We have already found a pack that matches the search term, now we find the episode number and make sure it's not a movie
						*/
							Pattern wantMatch = Pattern.compile(".*" + regexOfEpisodeNumbers + ".*");
							String strippedPackName = removeBrackets(pack.getName()); 
							//Matcher matcher = wantMatch.matcher(strippedPackName);
							if(wantMatch.matcher(strippedPackName).matches() && !strippedPackName.toLowerCase().contains("movie")){//If the strippedPackName matches the search term, find episodes
								LinkedList<Integer> episodes = findEpisodeNumber(strippedPackName);
								if(!episodes.isEmpty()){
									Integer episode = episodes.pop();
									if(regexOfEpisodeNumbers.contains(episode.toString())){ //if we managed a hit
										results.put(episode, pack); //slap that pack down
										packsFound++;
										/*
										Eventually put this into its own function. It should take a LinkedList<Integer> and
										recompile the regexOfEpisodeNumbers.
										*/
										regexOfEpisodeNumbers = regexOfEpisodeNumbers.replaceFirst(episode.toString(), "").replace("||", "|").replace("(|", "(").replace("|)", ")");
										while(!episodes.isEmpty()){
											episode = episodes.pop();
											regexOfEpisodeNumbers = regexOfEpisodeNumbers.replaceFirst(episode.toString(), "").replace("||", "|").replace("(|", "(").replace("|)", ")");
										}
										if(regexOfEpisodeNumbers.equals("()")){//if all episodes found return result
											return populateResult(sender, httpList, results);
										}
										wantMatch = Pattern.compile(".*" + regexOfEpisodeNumbers + ".*");
									}
								}
							}//END matching pack to searchterm and epsiode numbers
						}else{//Matched the pack, so just add it and move to the next
							packsFound++;
							results.put(packsFound, pack);	
							if(packsFound>=Integer.parseInt(_LIMIT)){
								return populateResult(sender, httpList, results);
							}
						}
					}//END matching pack to searchTerm
				}
			}
		return populateResult(sender, httpList, results);
		}catch (Exception e){
			//service not running
		}
		return null;
	}
	
	public LinkedList<Integer> findEpisodeNumber(String episode) {
		Matcher matcher = dotPattern.matcher(episode);
		episode = matcher.replaceAll("_");
		LinkedList<Integer> output = new LinkedList<Integer>();

		Matcher epMatcher = epPattern.matcher(episode);
		if (epMatcher.find()) {
			try{
				output.add(Integer.parseInt(removeLeadingZeros(epMatcher.group(1))));
			}catch (Exception e){}
			try{
				output.add(Integer.parseInt(removeLeadingZeros(epMatcher.group(2))));
			}catch (Exception e){
				
			}

		}
		else {
		    //need a regex to match stuff like this.
			//1x01, S01E01, S01E01E02, S01E01-02, S01E01-E02
			//do different pattern for these
			//System.out.println("FAILED!");
		}
		
	return output;
}
	/**
	 * This method removes the Leading '0's of a string
	 * @param input
	 * @return
	 */
	private String removeLeadingZeros(String input){
		while(input.startsWith("0"))
			input = input.replaceFirst("0", "");
		return input;
	}
	/**
	 * This method removes all Brackets [ ] and ( )
	 * @param input
	 * @return
	 */
	public String removeBrackets(String input){
		String strippedSearchTerm = "";
		int open = 0;
		for(char t : input.toCharArray()){
			if(t=='[' || t=='('){
				open++;
			}
			if(open<1){
				strippedSearchTerm = strippedSearchTerm + t;
			}
			if(t==']' || t==')'){
				open--;
			}
		}
		return strippedSearchTerm;
	}
	
	/**
	 * This method will populate the Result Set in ascending order.
	 */
	private LinkedList<String[]> populateResult(String sender, String httpList, HashMap<Integer,Pack> input){
		LinkedList<String[]> output = new LinkedList<String[]>();
		if(input.isEmpty()){
			output.add(new String[]{sender,Colors.RED + "No Results Found."});
		}else{
			Map<Integer, Pack> sortedMap = new TreeMap<Integer, Pack>(input);
			output.addFirst(new String[]{sender,Colors.DARK_GRAY + "Sending Results... To stop results " + Colors.TEAL + "/MSG " +  _myBot.getBot().getNick() + " STOP " + Colors.DARK_GRAY + "- Full Listing at: " + Colors.TEAL + httpList});
			for(Pack pack : sortedMap.values()){
				output.add(new String[]{sender,Colors.BROWN + "[" + Colors.TEAL + pack.getSize() + Colors.BROWN + "] " + Colors.DARK_GRAY + pack.getName() + Colors.NORMAL + "  /MSG " + pack.getBotName() + " XDCC SEND " + pack.getNumber()});
			}
		}
		
		return output;
		
	}
	
	/**
	 * This method will search the database and format the returned results
	 * ready for website output.
	 * @param sender
	 * @param searchTerm
	 * @return LinkedList<String[]> for parsing in php
	 */
	public LinkedList<String[]> webSearch(String searchTerm) {
		try {
			LinkedList<String[]> result = new LinkedList<String[]>();
			String processedSearchTerm = "%"+searchTerm.replaceAll(" ", "%").replaceAll("'", "''")+"%"; 
			String query = "SELECT " + _BOTS_TABLE + ".name, " + _PACKS_TABLE + 
			".name, " + _PACKS_TABLE + ".number, " + _PACKS_TABLE + ".size FROM " +
			_PACKS_TABLE + " left join " + _BOTS_TABLE + " ON " + _PACKS_TABLE +
			".bot_id=" + _BOTS_TABLE + ".id WHERE " + _PACKS_TABLE + ".name LIKE '" +
			processedSearchTerm + "' ORDER BY last_modified DESC LIMIT " + _LIMIT + ";" ;
			Statement pstmt = getConnection().createStatement();
			ResultSet rs = pstmt.executeQuery(query);

			while (rs.next()) {
				//string[name, size, bot, packnum]
				result.add(new String[]{rs.getString(2), rs.getString(4), rs.getString(1), rs.getString(3)});
			}

			if(result.size()==0){
				result.add(new String[]{"No Results Found"});
			}
			rs.close();
			pstmt.close();
			return result;
		} catch (SQLException e) {
			return null;
		}
	}
}
