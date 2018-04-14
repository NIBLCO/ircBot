package com.nibl.bot.plugins.updatepacklist;

import java.io.File;
import java.io.RandomAccessFile;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.List;

import com.nibl.bot.Bot;

public class XdccDistroBot extends AbstractDistroBot {
	
	public XdccDistroBot(Bot bot, int id, String name, String url, String type, int statusId, Timestamp lastSeen, Timestamp lastProcessed, List<Pack> listing, int informative, int external, String owner, int parserId) {
		super(bot, id, name, url, type, statusId, lastSeen, lastProcessed, listing, informative, external, owner, parserId);
	}

	@Override
	public void getPackListFromBot() {
		if(getURL().contains("NONE"))
			_myBot.sendMessageFair(getName(), "xdcc send -1");
		else
			_myBot.sendMessageFair(getName(), getURL());
		
		_myBot.getLogger().info(_updatePackListDAO.getUpdatePackList().getPrefix() + " Get list from " + getName());
	}
	
	public void processPackListFromBot(File file) {
		if( null == file || !file.exists() ) {
			_myBot.getLogger().warn("Unable to Process " + getName() + " : File does not exist.");
			return;
		}
		_myBot.getLogger().trace("Process " + getType() + ": " + getName());
		List<Pack> fromFile = parseList( file );
		if( !file.delete() ) {
			_myBot.getLogger().error(_updatePackListDAO.getUpdatePackList().getPrefix() + "Could not delete packlist file for: " + getName());
		}
		
		if( _packParseFunctions.packListChanged(this, fromFile) ){ // pack list has changed, so update it
			setListing( fromFile );
			updateDatabase();
		}
		
	}
	
	@Override
	public LinkedList<Pack> parseList(){ return null; };
	
	@Override
	public LinkedList<Pack> parseList(File file) {
		
		try {
			_myBot.getLogger().trace("Get buffered reader");
			RandomAccessFile in = new RandomAccessFile( file, "rw" );
			_myBot.getLogger().trace("Finished Get buffered reader");
			
			LinkedList<Pack> output = _packParseFunctions.randomAccessFileToPacks(this, in);
			
			if( null != in) { in.close(); }
    		_myBot.getLogger().trace("Finish Iterating packlist");
			
    		if (output.size() == 0) {
				_myBot.getLogger().error("No Packs: " + getName() + " @ " + getURL() + ". Setting offline.");
				try {
					this.setStatusId(0);
				} catch (SQLException e) {
					_myBot.getLogger().error("Failure setting bot offline: " + getName() + " @ " + getURL(), e);
				}
			} else {
				return output;
			}
			
		} catch (Exception e) {
			_myBot.getLogger().error(getName() + " : " + getURL(), e);
		}
		
		return new LinkedList<Pack>();
	}
}
