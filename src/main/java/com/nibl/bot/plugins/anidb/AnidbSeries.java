package com.nibl.bot.plugins.anidb;

import com.nibl.bot.Bot;

public class AnidbSeries {
		
	private Bot _myBot;
	private int _aid;
	private int _total;
	private int _numberInTotal;
	private String _seriesName;
	private String _type;
	private String _episodeCount;
	private String _startDate;
	private String _endDate;
	private String _rating;
	private String _tempRating;
	private String _description;
	
	public AnidbSeries(Bot myBot, Integer aid, Integer total, Integer numberInTotal, String seriesName, String type, String episodeCount, String startDate, String endDate, String rating, String tempRating, String description){
		_myBot = myBot;
		_aid = aid;
		_total = total;
		_numberInTotal = numberInTotal;
		_type = type;
		_episodeCount = episodeCount;
		_startDate = startDate;
		_endDate = endDate;
		_rating = rating;
		_tempRating = tempRating;
		_seriesName = seriesName;
		_description = description;
		
	}
	
	public void outputToChannel(String channel, String user){
		StringBuilder output = new StringBuilder();
		output.append("[" + _numberInTotal + "/" + _total + "] " + _seriesName + " : " ); //append information that will always be there

		if(!_type.equals(""))
			output.append(_type + ", ");
		if(!_episodeCount.equals(""))
			output.append( _episodeCount + " episodes, ");
		if(!_startDate.equals(""))
			output.append("Date: " + _startDate);
		if(!_endDate.equals(""))
			output.append(" to " + _endDate);
		if(!_rating.equals(""))
			output.append(", Rating: " + _rating);
		if(!_tempRating.equals(""))
			output.append(", Temp Rating: " + _tempRating);

		_myBot.sendMessageFair(channel, output.toString() + " - http://anidb.net/a" + _aid);
		
			StringBuilder sb = new StringBuilder();
			int counter = 0;
			for(String str : _description.split(" ")){
				if(!str.contains("http://"))
					sb.append(str + " ");
				if(sb.toString().length()>130){
					counter++;
					if(counter==3){
						_myBot.sendNoticeFair(user, sb.toString().replaceAll("\\r", " ") + "....");
						sb = new StringBuilder();
						break;
					}else{
						_myBot.sendNoticeFair(user, sb.toString().replaceAll("\\r", " "));
						sb = new StringBuilder();
					}
				}
			}
			
			if(sb.toString().length()>=1){
				_myBot.sendNoticeFair(user, sb.toString().replaceAll("\\r", " "));
			}

	}
	
	public Integer getAid(){
		return _aid;
	}
	
	
}
