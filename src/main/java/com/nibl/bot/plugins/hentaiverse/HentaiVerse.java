package com.nibl.bot.plugins.hentaiverse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

import net.htmlparser.jericho.Segment;
import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.TextExtractor;

import org.pircbotx.User;
import org.pircbotx.dcc.SendChat;
import org.pircbotx.hooks.events.HalfOpEvent;
import org.pircbotx.hooks.events.IncomingFileTransferEvent;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.events.ModeEvent;
import org.pircbotx.hooks.events.NoticeEvent;
import org.pircbotx.hooks.events.TopicEvent;

import com.nibl.bot.Bot;
import com.nibl.bot.service.Service;

public class HentaiVerse extends Service {

	String _adminHelp = null;
	public static HentaiVerseDAO _hentaiverseDAO;
	final long _timeInSeconds = Long.parseLong(_myBot.getProperty("hentaiverse_update_time"));
	final long _updateDelayMs = Long.parseLong(_myBot.getProperty("hentaiverse_delay"));
	public static int PLAYER_LEVEL;
	public static String SOULBOUND = "Soulbound";
	
	static TreeMap<String,Boolean> equipTypes = new TreeMap<String,Boolean>();
	static ArrayList<String> equipSubTypes = new ArrayList<String>();
	static ArrayList<String> equipQuality = new ArrayList<String>();
	static ArrayList<String> weaponProcs = new ArrayList<String>();
	static ArrayList<String> equipPrefix = new ArrayList<String>();
	static ArrayList<String> equipSuffix = new ArrayList<String>();
	static ArrayList<String> damageTypes = new ArrayList<String>();
	
	public HentaiVerse(Bot myBot) {
		super(myBot);
		_hentaiverseDAO = (HentaiVerseDAO) myBot.getDAOFactory().getDAO("HentaiVerseDAO");
		refreshDataStorage();
		_myBot.getLogger().info("Created equip type, subtype and quality vars");
	}
	
	public void refreshDataStorage(){
		
		/* Different types of equipment */
		equipTypes = _hentaiverseDAO.getEquipmentTypes();
		
		/* Set the subtypes. For now, this is the equipment 'slot' */
		equipSubTypes = _hentaiverseDAO.getEquipmentSubTypes();
		
		/* Set the various qualities equipment can have */
		equipQuality = _hentaiverseDAO.getEquipmentQuality();

		/* Set the various prefixes */
		equipPrefix = _hentaiverseDAO.getEquipmentPrefix();
		
		/* Set the various suffixes */
		equipSuffix = _hentaiverseDAO.getEquipmentSuffix();
		
		/* Set the various special ability procs */
		weaponProcs = _hentaiverseDAO.getEquipmentProcs();
		
		/* Set the various equipment damage types */
		damageTypes = _hentaiverseDAO.getEquipmentDamageTypes();
		
		HentaiVerse.PLAYER_LEVEL = Integer.parseInt(_myBot.getProperty("player_level"));
		
	}

	@Override
	public void execute() {
		long start = 0;
		try {
			while(_status == Status.RUNNING){
				Thread.sleep(1000*10);
				long elapsedTimeMillis = System.currentTimeMillis()-start;
				float elapsedTimeSec = elapsedTimeMillis/1000F;
				if(elapsedTimeSec>=_timeInSeconds || start == 0){
					update();
					start = System.currentTimeMillis();
				}
			}
		} catch (InterruptedException e) {
			_myBot.getLogger().error("Interrupted in Scheduler sleep");
		}
		_myBot.getLogger().info("Service " + getName() + " stopped.");
	}
	
	private void update() {
		int counter = 0;
		for( Map.Entry<String, String> entry : _hentaiverseDAO.getHVItems().entrySet() ){
		    String urlTxt = "https://hentaiverse.org/equip/" + entry.getKey() + "/" + entry.getValue();
			try {
				_myBot.getLogger().trace("Parsing " + urlTxt);
				URL myUrl = new URL(urlTxt);
				urlToEquipment(myUrl);
			} catch (Exception e) {
				_myBot.getLogger().trace(urlTxt + " " + e.getMessage());
			}
			
			_hentaiverseDAO.setEquipmentProcessed(entry.getKey());

			counter = counter%3;
			if(counter == 0){
				try {
					Thread.sleep(_updateDelayMs);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
		}
		
	}

	@Override
	public boolean needsIncomingFileTransfers() {
		return false;
	}

	@Override
	public boolean needsMessages() {
		return true;
	}

	@Override
	public boolean needsNotices() {
		return false;
	}
	
	@Override
	public void onMessage(MessageEvent message) {
		String url = message.getMessage();
		
		if( !url.startsWith("http") || !url.contains("hentaiverse.org") || !url.contains("equip") )
			return;
			
		try {
			URL myUrl = new URL(message.getMessage());
			Equipment item = urlToEquipment(myUrl);
			item.sendEquip(_myBot, message.getChannel());
		} catch (Exception e) {
			_myBot.sendMessageFair(message.getChannel().getName(), e.getMessage());
		}
	}
	
	public static String getHVURL(URL hvUrl) throws Exception{
		
		try {
			
			String htmlOutput = "";
			try {
				HttpURLConnection urlConn = (HttpURLConnection) hvUrl.openConnection();
				urlConn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:32.0) Gecko/20100101 Firefox/32.0");
				String myCookie = "ipb_member_id=940068; ipb_pass_hash=196b1ccdf0bb9e92ac7b6708876ce175";
				urlConn.setRequestProperty("Cookie", myCookie);

				urlConn.connect();

				int code = urlConn.getResponseCode();
				if( code != 200 ){
					urlConn.disconnect();
					throw new Exception("Hentaiverse returned HTTP code " + code);
				}

				BufferedReader br = new BufferedReader(
						new InputStreamReader(urlConn.getInputStream()));
				String inputLine = "";
				while ((inputLine = br.readLine()) != null) {
					htmlOutput += inputLine;
				}
				urlConn.disconnect();
				
				return htmlOutput;
				
			}catch(Exception e){
				throw new Exception("Failed connection to HV. Try again.");
			}
			
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
	}
	
	public static Equipment urlToEquipment(URL hvUrl) throws Exception{
		
		Equipment item = new Equipment();;
		Boolean lotteryItem = false;
		if( null != hvUrl.getQuery() ) {
		    lotteryItem = hvUrl.getQuery().contains("Bazaar");
		}
		
		try {
		    
		    if( !lotteryItem ){
                String[] urlAttrs = hvUrl.getPath().split("/");
                item.setEID(urlAttrs[2]);
                item.setKEY(urlAttrs[3]);
		    } else {
                item.setEID( Integer.toString( ( _hentaiverseDAO.getEIDForLottery(item) ) ) ); // use next lotto eid
                item.setKEY("lottery");
		    }
            
			String htmlOutput = getHVURL(hvUrl);
			
			if( htmlOutput.toLowerCase().equals("No such item".toLowerCase())){
				throw new Exception("No such item, EID: " + item.getEID() + ", KEY: " + item.getKEY());
			}
			
			try{
			    
				item.setSource(new Source(htmlOutput));
				
				if( lotteryItem ){
					item.setLotteryTitle();
					item.setLevel(_hentaiverseDAO.getMaxVersion().toString()); // use version as level
				}
				
			}catch(Exception e){
				throw new Exception("Failed to create equipment object. Stupid Jenga >.>");
			}
			
			try{
				if( lotteryItem ){ // Store lotto item only once (doing this because item.canStore() breaks on lotto items
					item.setIsNewLotteryItem( !HentaiVerse._hentaiverseDAO.equipmentExists(item) ); // If equipment doesn't exist, set is new
					if( item.getIsNewLotteryItem() ){
						_hentaiverseDAO.storeEquipment(item);
					}
				} else if( item.canStore() ){
					_hentaiverseDAO.storeEquipment(item);
				}
			}catch(Exception e){ }
			
			return item;
			
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
	}

	public static String extractValue(Segment input){
		TextExtractor valueExtractor = new TextExtractor(input);
		return valueExtractor.toString();
	}
	
	@Override
	public void onNotice(NoticeEvent notice) {}

	@Override
	public void admin(SendChat session, User user, String message) throws IOException {
		if (message.startsWith("plvl")) {
			try{
				int plvl = Integer.parseInt( message.substring("plvl ".length()) );
				HentaiVerse.PLAYER_LEVEL = plvl;
				session.sendLine("Plvl set to " + HentaiVerse.PLAYER_LEVEL + ". Be sure to set config as well.");
			}catch(Exception e){
				session.sendLine("Cound not parse plvl.");
			}
			
		}else if(message.equalsIgnoreCase("refresh")){
			refreshDataStorage();
			session.sendLine("Refreshed data storage.");
		}else if(message.equalsIgnoreCase("show plvl")){
			session.sendLine("Current plvl is: " + HentaiVerse.PLAYER_LEVEL);
		}
	}

	@Override
	public LinkedList<String> adminHelp() {
		LinkedList<String> help = new LinkedList<String>();
		help.add("help - Shows a list commands\r\n");
		help.add("plvl # - Sets the player level used in calculations\r\n");
		help.add("show plvl - Show the current player level\r\n");
		help.add("refresh - Refreshes type, subtype, quality, prefix, suffix and plvl. Make sure config is updated with proper plvl.\r\n");
		return help;
	}

	@Override
	public String getDescription() {
		return "Perform some action for specific channel messages.";
	}

	@Override
	public String getName() {
		return "hentaiverse";
	}

	@Override
	public boolean needsOnPart() {
		return false;
	}

	@Override
	public void onPart(String nick) {}

	@Override
	public boolean needsOnMode() {
		return false;
	}

	@Override
	public int getAccessLevel() {
		return 9;
	}

	@Override
	public int delay() {
		return 0;		
	}

	@Override
	public boolean needsOnHop() {
		return false;
	}

	@Override
	public void onHalfOp(HalfOpEvent event) {}

	@Override
	public void onMode(ModeEvent mode) {}

	@Override
	public boolean needsOnTopic() {
		return false;
	}

	@Override
	public void onTopic(TopicEvent topic) {}

	@Override
	public boolean needsOnJoin() {
		return false;
	}

	@Override
	public void onJoin(String nick) {

	}

	@Override
	public void onIncomingFileTransfer(IncomingFileTransferEvent transfer) {}
}
