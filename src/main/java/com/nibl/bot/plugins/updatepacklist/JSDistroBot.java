package com.nibl.bot.plugins.updatepacklist;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.URL;
import java.net.URLConnection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.List;

import com.nibl.bot.Bot;
import com.nibl.bot.util.InputStreamConverter;

public class JSDistroBot extends AbstractDistroBot {
	
	public JSDistroBot(Bot bot, int id, String name, String url, String type, int statusId, Timestamp lastSeen, Timestamp lastProcessed, List<Pack> listing, int informative, int external, String owner, int parserId) {
		super(bot, id, name, url, type, statusId, lastSeen, lastProcessed, listing, informative, external, owner, parserId);
	}

	@Override
	public void getPackListFromBot() {
		_myBot.getLogger().info(_updatePackListDAO.getUpdatePackList().getPrefix() + " Get list from " + getName());
		try{
			List<Pack> fromWeb = parseList();
			if( _packParseFunctions.packListChanged(this, fromWeb) ){ // pack list has changed, so update it
				setListing( fromWeb );
				updateDatabase();
			}
		}catch(Exception e){
			_myBot.getLogger().error("Unable to parse " + getType() + ": " + getName(),e);
		}
	}
	
	@Override
	public LinkedList<Pack> parseList(File file){ return null; };
	
	@Override
	public LinkedList<Pack> parseList() {
		String url = this.getURL();		
		try {
			_myBot.getLogger().trace("Get buffered reader");
	        URL urlopen = new URL( getURL() );
			URLConnection urlConnection = urlopen.openConnection();
			urlConnection.setConnectTimeout(10000);
			urlConnection.setReadTimeout(10000);
			urlConnection.setRequestProperty("User-Agent", "ooinuzaBot");
			InputStream inputStream = urlConnection.getInputStream();
			RandomAccessFile in = InputStreamConverter.toRandomAccessFile( inputStream );
			_myBot.getLogger().trace("Finished Get buffered reader");
			
			LinkedList<Pack> output = _packParseFunctions.randomAccessFileToPacks(this, in);
			
			if( null != in) { in.close(); }
			
			if(output.size() == 0){
				_myBot.getLogger().error("No Packs: " + getName() + " @ " + getURL() + ". Setting offline.");
				try {
					this.setStatusId(0);
				} catch (SQLException e) {
					_myBot.getLogger().error("Failure setting bot offline: " + getName() + " @ " + getURL(), e);
				}
			} else {
				return output;
			}
			
    		_myBot.getLogger().trace("Finish Iterating packlist");
		} catch (Exception e) {
			_myBot.getLogger().error(getName() + " : " + getURL(), e);
		}
		
		return new LinkedList<Pack>();
	}
}