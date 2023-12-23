package com.nibl.bot.plugins.updatepacklist;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;

import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.events.ModeEvent;
import org.pircbotx.hooks.events.NoticeEvent;
import org.pircbotx.User;
import org.pircbotx.dcc.SendChat;
import org.pircbotx.hooks.events.HalfOpEvent;
import org.pircbotx.hooks.events.IncomingFileTransferEvent;
import org.pircbotx.hooks.events.TopicEvent;

import com.nibl.bot.Bot;
import com.nibl.bot.service.Service;

public class UpdatePackList extends Service {
	
	final long _timeInSeconds = Long.parseLong(_myBot.getProperty("update_pack_list_time"));
	private HashSet<String> _acceptedSenders = new HashSet<String>();
	
	public static ConcurrentHashMap<Integer, AbstractDistroBot> _botsMap = new ConcurrentHashMap<Integer, AbstractDistroBot>();
	public static ConcurrentHashMap<String, Integer> _botsReverseMap = new ConcurrentHashMap<String, Integer>();
	
	private UpdatePackListDAO _updatePackListDAO;
	private Boolean _running = false;

	public UpdatePackListFunctions _updatePackListFunctions;
	String _adminHelp = null;
	
	public UpdatePackList(Bot myBot) {
		super(myBot);
		_updatePackListDAO = (UpdatePackListDAO) _myBot.getDAOFactory().getDAO("UpdatePackListDAO");
		_updatePackListFunctions = new UpdatePackListFunctions(myBot, this);
		
		_updatePackListDAO.registerUpdatePackList(this);
		
		_myBot.getLogger().info("Set bots offline");
		_updatePackListDAO.setBotsOffline();
		
		_myBot.getLogger().info("Get Bots from Database");
		_updatePackListDAO.getBotsFromDatabaseCreateBots();
		
	}
	
	@Override
	public void execute() {
		long start = 0;
		try {
			while(_status == Status.RUNNING){
				Thread.sleep(1000*20);
				long elapsedTimeMillis = System.currentTimeMillis()-start;
				float elapsedTimeSec = elapsedTimeMillis/1000F;
				if(elapsedTimeSec>=_timeInSeconds || start == 0){
					_running = true;
					update();
					_myBot.getLogger().info("sleeping " + _timeInSeconds + " seconds.");
					_running = false;
					start = System.currentTimeMillis();
				}
			}
		} catch (InterruptedException e) {
			_myBot.getLogger().error("Interrupted in Scheduler sleep");
		}
		_myBot.getLogger().info("Service " + getName() + " stopped.");
	}

	private void update() {
		try{
			_myBot.getLogger().info("Remove Bots Map References");
			_updatePackListFunctions.cancelAnnounceHandlers();
			
			_myBot.getLogger().info("Register New Bots");
			_updatePackListFunctions.registerNewAndOfflineBots();
			
			_myBot.getLogger().info("Get Packlist from Bots");
			_updatePackListFunctions.getPackListFromBots();
			
			_myBot.getLogger().info("Iteration complete");
		}catch(Exception e){
			_myBot.getLogger().error("Unknown Exception: " + e.toString());
		}
	}
	
	public ConcurrentHashMap<Integer, AbstractDistroBot> getBotsMap(){
		return _botsMap;
	}
	
	public ConcurrentHashMap<String, Integer> getReverseBotsMap(){
		return _botsReverseMap;
	}
	
	/**
	 * This method will need to accept a notice...figure out what the response is
	 * and then update the corresponding _botInfo entry
	 */
	@Override
	public void onNotice(NoticeEvent notice) {
		AbstractDistroBot noticedbot = UpdatePackListFunctions.getBot(notice.getUser().getNick());
				
		if(_acceptedSenders.contains(notice.getUser().getNick().toLowerCase()) && 
				UpdatePackListFunctions.getBot(notice.getUser().getNick()).getType() == "NEW") {
			// new bot notices
				String message = notice.getMessage().toLowerCase();
				if ((message.contains("http") || message.contains("www"))) {
					_myBot.sendMessageFairToAdmin("Attempting to add new HTTP bot; " + notice.getUser().getNick());
					_updatePackListFunctions.addNewBotWithHTTPListingToDatabase(notice);
					AbstractDistroBot httpBot = UpdatePackListFunctions.getBot(notice.getUser().getNick());
					if( null == httpBot ){
						_myBot.sendMessageFairToAdmin("Unable to add new HTTP bot; " + notice.getUser().getNick());
						return;
					} else if( httpBot.getListing().size() == 0 ){
						_myBot.sendMessageFairToAdmin("New HTTP bot added with incorrect URL");
						_myBot.sendMessageFairToAdmin("ID: " + httpBot.getId() + ", Nick: " + httpBot.getName() + ", URL: " + httpBot.getURL());
					} else if( httpBot.getListing().size() > 0 ){
						_myBot.sendMessageFairToAdmin("New HTTP bot added; " + httpBot.getName() + ", Packs: " + httpBot.getListing().size());
					}
					_updatePackListFunctions.getOwner(httpBot.getName());
				} else if (message.toLowerCase().contains("xdcc send") || message.toLowerCase().contains("!blist") || message.toLowerCase().contains("pack #1") || message.toLowerCase().contains("xdcc send -1")) {
					_myBot.sendMessageFairToAdmin("Attempting to add new XDCC bot; " + notice.getUser().getNick() + " (if no messages appear after this, the bot did not send a packlist)");
					_updatePackListFunctions.addNewBotWithXDCCListingToDatabase(notice);
					AbstractDistroBot xdccBot = UpdatePackListFunctions.getBot(notice.getUser().getNick());
					xdccBot.setIsNewBot(true);
					_updatePackListFunctions.getOwner(xdccBot.getName());
				} else {//no idea, add it to http listing and basically ignore it until somebody fixes
					_myBot.sendMessageFairToAdmin("Attempting to add new bot of unknown type (fix this bot); " + notice.getUser().getNick());
					getAcceptedSenders().remove( notice.getUser().getNick().toLowerCase() );
					_myBot.sendMessageFair(notice.getUser().getNick(), "XDCC STOP");
					_updatePackListFunctions.addNewBotWithHTTPListingToDatabase(notice);
					AbstractDistroBot httpBot = UpdatePackListFunctions.getBot(notice.getUser().getNick());
					_myBot.sendMessageFairToAdmin("ID: " + httpBot.getId() + ", Nick: " + httpBot.getName() + ", URL: " + httpBot.getURL());
					_updatePackListFunctions.getOwner(httpBot.getName());
				}
		} else if( notice.getMessage().toLowerCase().contains("owner for this bot is") 
				&& null != noticedbot ){
			
			// Update owner notices
			AbstractDistroBot bot = UpdatePackListFunctions.getBot(notice.getUser().getNick());
			
			if( null == bot ) {
				return;
			}
			
			String botOwner = notice.getMessage().replace("Owner for this bot is: ", "");
			if( botOwner.equals("(unknown)") ) {
				_myBot.getLogger().dccChatSend("Bot owner for " + notice.getUser().getNick() + " is UNKNOWN (not updated)", this.getAccessLevel());
			} else {
				_myBot.getLogger().dccChatSend("Bot owner for " + notice.getUser().getNick() + " is " + botOwner, this.getAccessLevel());
				try {
					_updatePackListDAO.updateOwner(bot, botOwner);
					_myBot.getLogger().dccChatSend("Bot owner for " + notice.getUser().getNick() + " updated", this.getAccessLevel());
				} catch (SQLException e) {
					_myBot.getLogger().dccChatSend("Bot owner for " + notice.getUser().getNick() + " failed to update.", this.getAccessLevel());
				}
			}
		} else if( null != noticedbot ){
			_myBot.getLogger().trace(noticedbot.getName() + " sent a notice: " + notice.getMessage());
		}
	}


	
	@Override
	public void onIncomingFileTransfer(IncomingFileTransferEvent event) {
		String nick = event.getUser().getNick();
		String filename = event.getSafeFilename();
		try{
			if (filename.substring(filename.length() - 4, filename.length()).equals(".txt")) {
				XdccDistroBot bot = (XdccDistroBot)UpdatePackListFunctions.getBot(nick);
				if ( bot != null ) {
					File file = new File(_myBot.getProperty("download_dir").replaceAll("---", Matcher.quoteReplacement("\\")) + nick.replaceAll("\\|", ""));
					event.accept(file); // blocking
					bot.processPackListFromBot(file);
				} else {
					_myBot.getLogger().error(event.getUser().getNick() + " is sending an invalid file: " + filename);
					_myBot.sendMessageFair(event.getUser().getNick(), "xdcc cancel");
				}
			} else {
				_myBot.getLogger().error(event.getUser().getNick() + " is sending an invalid file: " + filename);
				_myBot.sendMessageFair(event.getUser().getNick(), "xdcc cancel");
			}
		}catch (Exception e){
			_myBot.getLogger().error("I crapped out getting file (onIncomingFileTransfer): " + nick + " : " + e.toString());
			try{
				//transfer.close();
			} catch (Exception e2){ }
		}
	}
	
	@Override
	public void admin(SendChat session, User user, String message) throws IOException, SQLException {
		if (message.equals("show all")) {
			_updatePackListFunctions.showAll(session);
		} else if (message.equals("show online")) {
			_updatePackListFunctions.showOnline(session);
		} else if(message.toLowerCase().startsWith("show latest")){
			String butts = message.substring("show latest".length()).trim();
			_updatePackListDAO.showLatestPacksForDCC(session,butts);
		} else if(message.startsWith("show ")){
			_updatePackListFunctions.showBot(session, message.replace("show ", ""));
		} else if (message.startsWith("find errors")) {
			_updatePackListFunctions.findErrorBots(session);
		} else if (message.startsWith("count")) {
			_updatePackListFunctions.countPacks(session, message.substring("count ".length()));
		} else if (message.startsWith("get list")) {
			_updatePackListFunctions.getList(session, message.substring("get list ".length()));
		} else if (message.startsWith("remake")) {
			_updatePackListFunctions.remake(session, message.substring("remake ".length()));
		} else if (message.startsWith("set url ")) {
			_updatePackListFunctions.setURL(session, message.substring("set url ".length()));
		} else if (message.startsWith("set type ")) {
			_updatePackListFunctions.setType(session, message.substring("set type ".length()));
		} else if (message.startsWith("set informative ")) {
			_updatePackListFunctions.setInformative(session, message.substring("set informative ".length()));
		} else if (message.startsWith("set batch ")) {
			_updatePackListFunctions.setBatch(session, message.substring("set batch ".length()));
		} else if (message.toLowerCase().startsWith("help")){
			this.getHelp(session);
		} else if(message.toLowerCase().startsWith("delete ")){
			_updatePackListFunctions.delete(session, message.replace("delete ", ""));
		} else if(message.toLowerCase().startsWith("hop ")){
			_updatePackListFunctions.hop(session, message.replace("hop ", ""));
		} else if (message.startsWith("get owner ")) {
			_updatePackListFunctions.getOwner(message.replace("get owner ", ""));
		} else if(message.startsWith("set owner ")){
			String[] params = message.replace("set owner ", "").split(" ");
			if( null == params || params.length != 2 ) {
				session.sendLine("Invalid parameters. set owner [id/name] [owner_name]");
			} else {
				_updatePackListFunctions.setOwner(params[0], params[1]);
			}
		} else if( message.equals("unknown owners") ) {
			_updatePackListFunctions.getUnknownOwners();
		} else if( message.equals("set all owners") ) {
			session.sendLine("This command has been disabled");
			//_updatePackListFunctions.setAllOwners();
		} else {
			session.sendLine("Unknown Command.");
		}
	}

	@Override
	public LinkedList<String> adminHelp() {
		LinkedList<String> help = new LinkedList<String>();
		help.add("help - Shows a list commands\r\n");
		help.add("current - Shows a list of the bots in memory\r\n");
		help.add("find errors - Search bots for any errors (invalid state, unhopped bots, no packs)\r\n");
		help.add("count [id/name] - Count packs found for a bot\r\n");
		help.add("get list [id/name] - Manually grab list for a bot\r\n");
		help.add("remake [id/name] - Trash and remake bot in memory\r\n");
		help.add("show all - Shows a list of the bots in database\r\n");
		help.add("show online - Shows all currently active bots\r\n");
		help.add("show [id/name] - Shows information about a bot\r\n");
		help.add("show latest - Shows information about the latests packs\r\n");
		help.add("set url [id/name] [address] - sets the address\r\n");
		help.add("set type [id/name] [type] - sets the type XDCC HTTP NOTICE HTML JS\r\n");
		help.add("set informative - don't process list of this bot\r\n");
		help.add("set batch - set batch download command on website\r\n");
		help.add("delete [id/name] - delete bot\r\n");
		help.add("get owner [id/name] - try to get the owner from a bot (xdcc owner)\r\n");
		help.add("set owner [id/name] [owner_name] - sets the owner of a bot\r\n");
		help.add("unknown owners - lists the bots with unknown owners\r\n");
		//help.add("set all owners - sets all owners for bots. This could overwrite manually created owners, so use sparingly.\r\n");
		return help;
	}
	
	@Override
	public String getDescription() {
		return "The update packlist service";
	}

	@Override
	public String getName() {
		return "packlist";
	}

	@Override
	public boolean needsMessages() {
		return true;
	}

	@Override
	public boolean needsNotices() {
		return true;
	}

	@Override
	public boolean needsIncomingFileTransfers() {
		return true;
	}

	@Override
	public void onMessage(MessageEvent message) {
		if(!isIterating()){
			if( UpdatePackListFunctions.getBot(message.getUser().getNick()) != null && !UpdatePackListFunctions.getBot(message.getUser().getNick()).isInformative() ){
				_updatePackListDAO.handelLatestPacks(message);
			}
		}
	}

	@Override
	public void onPart(String nick) {
		if(!isIterating()){
			if(_botsMap.containsKey(nick)){
				_updatePackListFunctions.getOfflineBots().add(nick);
				_updatePackListDAO.setOfflineBots(_updatePackListFunctions.getOfflineBots());
			}
		}
	}
	
	@Override
	public void onJoin(String nick) {}
	
	@Override
	public boolean needsOnPart() {
		return true;
	}

	@Override
	public boolean needsOnHop() {
		return true;
	}
	
	@Override
	public boolean needsOnMode() {
		return false;
	}
	
	@Override
	public int getAccessLevel() {
		return 8;
	}

	public boolean isIterating(){
		return _running;
	}
	
	public HashSet<String> getAcceptedSenders(){
		return _acceptedSenders;
	}
	
	public UpdatePackListDAO getMyDAO(){
		return _updatePackListDAO;
	}

	@Override
	public int delay() {
		return 5;		
	}

	@Override
	public void onMode(ModeEvent mode) {}

	@Override
	public void onHalfOp(HalfOpEvent event) {
		if( !isIterating() ){
			AbstractDistroBot curBot = UpdatePackListFunctions.getBot(event.getRecipient().getNick());
			if(curBot==null)
				return;
			try{
				if(event.isHalfOp()){
					curBot.setStatusId(1);
					curBot.getPackListFromBot(); 
				}else{
					curBot.setStatusId(0);
					curBot.getListing().clear();
				}
			}catch(Exception e){
				_myBot.sendMessageFair("#" + _myBot.getProperty("admin_channel"), "onHalfOp failed; Bot ID: " + curBot.getId());
			}
		}
	}

	@Override
	public boolean needsOnTopic() {
		return false;
	}

	@Override
	public void onTopic(TopicEvent topic) {}

	@Override
	public boolean needsOnJoin() {
		return true;
	}
}