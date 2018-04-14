package com.nibl.bot.plugins.updatepacklist;

import java.io.File;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.nibl.bot.Bot;
import com.nibl.bot.BotExtend;
import com.nibl.bot.plugins.search.SearchDAO;

public abstract class AbstractDistroBot extends BotExtend {
	
	public static final String TYPE_HTTP = "HTTP";
	public static final String TYPE_XDCC = "XDCC";
	public static final String TYPE_JS = "JS";
	public static final String TYPE_NEW = "NEW";
	public static final String TYPE_NONE = "NONE";
	
	protected UpdatePackListDAO _updatePackListDAO;
	protected SearchDAO _searchDAO;
	protected PackParseFunctions _packParseFunctions;
	protected Timer _timer;
	
	private int _id;
	private String _name;
	private String _url = "";
	private String _type = "NOTICE";  //TODO make enum
	private int _statusId;
	private Timestamp _lastSeen;
	private Timestamp _lastProcessed;
	private List<Pack> _listing = new LinkedList<Pack>();
	private int _informative;
	private int _external;
	private String _owner;
	private int _parserId;
	private ScheduledExecutorService _executor = Executors.newScheduledThreadPool(1);
	private ScheduledFuture<?> _schedule = null;
	private int _numberOfAnnounces = 0;
	private boolean _newBot = false;
	
	private volatile boolean _modified = false;
	
	public AbstractDistroBot(Bot myBot, int id, String name, String url, String type, 
			int statusId, Timestamp lastSeen, Timestamp lastProcessed, List<Pack> listing, int informative, int external, String owner, int parserId)
	{
		super(myBot);
		_id = id;
		_name = name;
		_url = url;
		_type = type;
		_statusId = statusId;
		_lastSeen = lastSeen;
		_lastProcessed = lastProcessed;
		_listing = listing;
		_informative = informative;
		_external = external;
		_owner = owner;
		_parserId = parserId;
		if( null != _myBot ) {
    		_updatePackListDAO = (UpdatePackListDAO) _myBot.getDAOFactory().getDAO("UpdatePackListDAO");
    		_searchDAO = (SearchDAO) _myBot.getDAOFactory().getDAO("SearchDAO");
    		_packParseFunctions = new PackParseFunctions( _myBot );
		}
	}
	
	public abstract void getPackListFromBot();
	public abstract LinkedList<Pack> parseList();
	public abstract LinkedList<Pack> parseList(File file);
	
	protected void announceHandler(){
		try{
		if(_numberOfAnnounces<=4){
			cancelAnnounceHandler();
			_myBot.getLogger().trace("Started ReprocessAnnounce executor for: " + this.getName());
			_numberOfAnnounces++;
			_schedule = _executor.schedule(new ReprocessAnnounce(_myBot,_updatePackListDAO.getUpdatePackList(),this), 120, TimeUnit.SECONDS);
		}
		}catch(Exception e){
			_myBot.getLogger().error("Failed in announceHandler - NumAnnounces: " + _numberOfAnnounces + " for botname: " + this.getName(),e);
		}
	}
	
	public void cancelAnnounceHandler(){
		if(_schedule!=null){
			if(_schedule.cancel(true))
				_myBot.getLogger().trace("Cancelled ReprocessAnnounce for: " + this.getName());
		}
	}
	public void handleLatest(){
		announceHandler();
	}
	
	public void resetNumberOfAnnounces(){
		_numberOfAnnounces = 0;
	}
	
	public void addNewBotToDatabase()	{
		_updatePackListDAO.addNewBot(this);
	}
	
	public void updateDatabase() {
		_updatePackListDAO.updateListing(this);
	}


	public void setUpdatePackListDAO(UpdatePackListDAO updatePackListDAO) {
		_updatePackListDAO = updatePackListDAO;
	}

	public void addNewListing(Pack newPack){
		_listing.add(newPack);
		_modified = true;
	}

	public void setURL(String url){
		_url=url;
		_modified = true;
	}

	public boolean isModified(){
		return _modified;
	}
	
	public void setModified() {
		_modified = true;
	}
	
	public void setStatusId(int status) throws SQLException{
		_statusId = status;
		_updatePackListDAO.setBotStatus(this);
		_myBot.getLogger().trace("Set " + this.getName() + " " + ((status==0)?"Offline":"Online"));
	}
	
	public void killTimer(){
		if(_timer!=null)
			_timer.cancel();
	}
	
	public boolean isOnlne(){
		if(_statusId==0)
			return false;
		else
			return true;
	}

	public boolean isIn(Pack input){
		for(Pack pack : _listing)
			if(pack.getName().equals(input.getName()) && pack.getNumber()==input.getNumber() && pack.getSize().equals(input.getSize()))
				return true;
		
		return false;
	}
	
	public void setListing(List<Pack> listing){
		_listing.clear();
		_listing.addAll(listing);
		//_listing = listing;
		_modified = true;
	}
	
	public final int getId() {
		return _id;
	}
	
	public final void setId(int id) {
		_id = id;
	}
	
	public final boolean getIsNewBot(){
		return _newBot;
	}
	
	public final void setIsNewBot(boolean isNew){
		_newBot = isNew;
	}

	public final String getName() {
		return _name;
	}

	public final String getType() {
		return _type;
	}

	public final int getStatusId() {
		return _statusId;
	}
	
	public final String getURL() {
		return _url;
	}
	
	public final Timestamp getLastSeen() {
		return _lastSeen;
	}
	
	public final Timestamp getLastProcessed() {
		return _lastProcessed;
	}
	
	public final List<Pack> getListing() {
		return _listing;
	}
	
	public final int getInformative() {
		return _informative;
	}
	
	public final void setInformative(int informative) {
		_informative = informative;
	}
	
	public final boolean isInformative(){
		if(_informative==1){
			return true;
		}else{
			return false;
		}
	}

	public final int getExternal() {
		return _external;
	}
	
	public final void setExternal(int external){
		_external = external;
	}
	
	public final boolean isExternal(){
		if(_external==1){
			return true;
		}else{
			return false;
		}
	}
	
	public final String getOwner() {
		if( null == _owner ){
			return "unknown";
		} else {
			return _owner;
		}
	}
	
	public final void setOwner(String owner) {
		_owner = owner;
	}
	
	public final int getParserId() {
		return _parserId;
	}
	
	public final void setParserId(int parserId) throws SQLException {
		_parserId = parserId;
		_updatePackListDAO.setBotParser(this);
	}
	
	public final Bot getPircBotX(){
		return this._myBot;
	}

}
