package com.nibl.bot.plugins.updatepacklist;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;

import org.pircbotx.Colors;
import org.pircbotx.dcc.SendChat;
import org.pircbotx.hooks.events.MessageEvent;

import com.nibl.bot.Bot;
import com.nibl.bot.dao.DataAccessObject;
import com.nibl.bot.plugins.latestpacks.LatestPacksDAO;

public class UpdatePackListDAO extends DataAccessObject {

	private final String _STATUS_TABLE = "updatepacklist_status";
	private final String _BOTS_TABLE = "updatepacklist_bots";
	private final String _PACKS_TABLE = "updatepacklist_packs";
	//private Pattern _boldMatcher = Pattern.compile("^(?:\\[\\cB?.*?\\cB?\\]|\\cB?.*?\\cB?)(?: \\cB?\\[.*\\]\\cB?)?(?: -|) \\cB?([^/]+?)\\cB?(?: -|) \\cB?/(?:MSG|msg) (\\S+) (?:XDCC SEND|xdcc send) (\\d+)\\cB?$");
	private UpdatePackList _updatePackList;

	public UpdatePackListDAO(Bot myBot) {
		super(myBot);
	}

	public void registerUpdatePackList(UpdatePackList updatePackList){
		_updatePackList = updatePackList;
	}
	
	@Override
	public void createTables() {
		if (tablesDoesNotExist()) {
			try {
				Statement statement = getConnection().createStatement();
				statement.execute("CREATE TABLE IF NOT EXISTS `" + _STATUS_TABLE + "` (`id` INT(2) NOT NULL,`description` VARCHAR(20) NOT NULL,PRIMARY KEY (`id`)) ENGINE=InnoDB;");
				statement.execute("TRUNCATE `" + _STATUS_TABLE + "`;");
				statement.execute("INSERT INTO `" + _STATUS_TABLE + "` (`id`,`description`) VALUES ('1','ONLINE'), ('0','OFFLINE');");
				statement.execute("CREATE TABLE IF NOT EXISTS `" + _BOTS_TABLE + "` (`id` INT(12) NOT NULL auto_increment,`name` VARCHAR(40) NOT NULL,`url` VARCHAR(200) NOT NULL,`type` VARCHAR(10) NOT NULL, `owner` VARCHAR(255), `status_id` INT(2) NOT NULL,`last_seen` DATETIME NOT NULL,`last_processed` DATETIME NOT NULL,`informative` tinyint(4) NOT NULL,PRIMARY KEY (`id`),UNIQUE (`name`),FOREIGN KEY (`status_id`) REFERENCES `updatepacklist_status` (`id`)) ENGINE=InnoDB;");
				statement.execute("CREATE TABLE IF NOT EXISTS `" + _PACKS_TABLE + "` (`id` INT(12) NOT NULL auto_increment,`bot_id` INT(12) NOT NULL,`number` INT(12) NOT NULL,`name` VARCHAR(300) NOT NULL,`size` VARCHAR(8) NOT NULL,`last_modified` DATETIME NOT NULL,PRIMARY KEY (`id`),UNIQUE (`bot_id`,`number`),FOREIGN KEY (`bot_id`) REFERENCES `updatepacklist_bots` (`id`) ON DELETE CASCADE ON UPDATE CASCADE) ENGINE=InnoDB;");
				_myBot.getLogger().info("created update pack list tables");
				statement.close();
			} catch (SQLException e) {
				_myBot.getLogger().error("createTables",e);
			}
		}
	}

	private boolean tablesDoesNotExist() {
		return !tableExists(_STATUS_TABLE) || !tableExists(_BOTS_TABLE) || !tableExists(_PACKS_TABLE);
	}

	@Override
	public void dropTables() {
		if (tableExists("bot_info")) {
			try {
				Statement statement = getConnection().createStatement();
				statement.execute("DROP TABLE `" + _PACKS_TABLE + "`;");
				statement.execute("DROP TABLE `" + _BOTS_TABLE + "`;");
				statement.execute("DROP TABLE `" + _STATUS_TABLE + "`;");
				_myBot.getLogger().info("deleted update pack list tables");
				statement.close();
			} catch (SQLException e) {
				_myBot.getLogger().error("dropTables",e);
			}
		}
	}

	public void getBotsFromDatabaseCreateBots() {
		try {
			Statement stmt = getConnection().createStatement();
			ResultSet rs = getResultSetOfBotTable(stmt);
			while (rs.next()) {
				AbstractDistroBot distroBot = buildDistroBot(rs);
				_updatePackList.getBotsMap().put(distroBot.getId(), distroBot);
				_updatePackList.getReverseBotsMap().put(distroBot.getName().toLowerCase(), distroBot.getId());
				distroBot.setListing( getBotPackListFromDatabase(distroBot) );
			}
			rs.close();
			stmt.close();
		} catch (SQLException e) {
			_myBot.getLogger().error("getBotsFromDatabaseCreateBots",e);
		}
	}
	
	public AbstractDistroBot getBotFromDatabase(String botName){
		
		try {
			Statement stmt = getConnection().createStatement();
			String query = "select * from " + _BOTS_TABLE + " where LOWER(`name`) = LOWER('" + botName + "')";
			ResultSet rs = stmt.executeQuery(query);
			
			if( rs.next() ){
				return buildDistroBot(rs);
			}
			
		} catch (SQLException e) {
			_myBot.getLogger().error("Error when getting bot " + this.getClass().getName(), e);
		}
		
		return null;
		
	}
	
	private ResultSet getResultSetOfBotTable(Statement stmt) throws SQLException {
		String query = "select * from " + _BOTS_TABLE + " order by `id`;";
		ResultSet rs = stmt.executeQuery(query);
		return rs;
	}

	public void setBotStatus(AbstractDistroBot bot) throws SQLException {
		Statement stmt = getConnection().createStatement();
		String query = "update " + _BOTS_TABLE + " set status_id = " + bot.getStatusId() + " where id = " + bot.getId();
		stmt.executeUpdate(query);
	}
	
	public void setBotParser(AbstractDistroBot bot) throws SQLException {
		Statement stmt = getConnection().createStatement();
		String query = "update " + _BOTS_TABLE + " set parser_id = " + bot.getParserId() + " where id = " + bot.getId();
		stmt.executeUpdate(query);
	}
	
	public AbstractDistroBot buildDistroBot(ResultSet rs) throws SQLException {
		int id = rs.getInt("id");
		String name = rs.getString("name");
		String url = rs.getString("url");
		String type = rs.getString("type");
		int statusId = rs.getInt("status_id");
		Timestamp lastSeen = rs.getTimestamp("last_seen");
		Timestamp lastProcessed = rs.getTimestamp("last_processed");
		int informative = rs.getInt("informative");
		int external = rs.getInt("external");
		String owner = rs.getString("owner");
		int parserId = rs.getInt("parser_id");
		LinkedList<Pack> listing = new LinkedList<Pack>();
		return DistroBotFactory.create(_myBot, id, name, url, type, statusId, lastSeen, lastProcessed, listing, informative, external, owner,parserId);
	}
	
	public AbstractDistroBot buildDistroBot(int id, String name, String url, String type, int statusId, Timestamp lastSeen, Timestamp lastProcessed, int informative, int external, String owner, int parserId) throws SQLException {
		LinkedList<Pack> listing = new LinkedList<Pack>();
		return DistroBotFactory.create(_myBot, id, name, url, type, statusId, lastSeen, lastProcessed, listing, informative, external, owner, parserId);
	}

	public List<Pack> getBotPackListFromDatabase(AbstractDistroBot bot){
		LinkedList<Pack> listing = new LinkedList<Pack>();
		try {
			Statement stmt = getConnection().createStatement();
			try {

				ResultSet rs = stmt.executeQuery("SELECT * FROM updatepacklist_packs WHERE bot_id = " + bot.getId());
				while (rs.next()) {
					listing.add( new Pack(rs.getInt("bot_id"), rs.getInt("number"), bot.getName(), rs.getString("name"), rs.getString("size"), rs.getInt("episode_number"), rs.getTimestamp("last_modified")) );
				}
				rs.close();
				stmt.close();
			} catch (SQLException e) {
				_myBot.getLogger().error("getBotPackListFromDatabase: " + e.getMessage());
			}
		} catch (SQLException e) {
			_myBot.getLogger().error("getBotPackListFromDatabase: " + e.getMessage());
		}
		return listing;
	}
	
	public List<AbstractDistroBot> getBotsInfo() {
		List<AbstractDistroBot> result = new LinkedList<AbstractDistroBot>();

		try {
			Statement stmt = getConnection().createStatement();
			try {

				ResultSet rs = getResultSetOfBotTable(stmt);
				while (rs.next()) {
					AbstractDistroBot distroBot = buildDistroBot(rs);
					result.add(distroBot);
				}
				rs.close();
				stmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return result;
	}

	public HashSet<String> getNoticeBotNames() {
		try {
			String query = "select name from " + _BOTS_TABLE + " where type='NOTICE';";
			ResultSet rs = getConnection().createStatement().executeQuery(query);
			HashSet<String> result = new HashSet<String>();
			while (rs.next()) {
				result.add(rs.getString(1));
			}
			rs.close();
			return result;
		} catch (SQLException e) {
			_myBot.getLogger().error("Error when getNoticeBots within " + this.getClass().getName(),e);
		}
		return new HashSet<String>();
	}

	public void updateListing(AbstractDistroBot bot) {
		PreparedStatement pstmt = null;
		try {
			pstmt = getConnection().prepareStatement("DELETE FROM " + _PACKS_TABLE + " WHERE bot_id=?;");
			pstmt.setInt(1, bot.getId());
			pstmt.execute();
			pstmt.close();
			
			insertAndUpdateListFor(bot);

		} catch (SQLException e) {
			_myBot.getLogger().error("UpdatePackListDAO BotName: " + bot.getName(),e);
			for (Pack pack : bot.getListing()) {
				_myBot.getLogger().error(pack.toString());
			}
		}
	}

	private void insertAndUpdateListFor(AbstractDistroBot bot) throws SQLException {
		/*
		int listSize = bot.getListing().size();
		if( listSize == 0 ){
			return;
		}
		
		PreparedStatement pstmt = null;
		try {
			StringBuilder query = new StringBuilder();
			query.append("INSERT INTO " + _PACKS_TABLE + " (bot_id, number, name, size, episode_number, last_modified) VALUES ");
			query.append("(?,?,?,?,?,?) ");
			query.append("ON DUPLICATE KEY UPDATE name=VALUES(`name`), size=VALUES(`size`), last_modified=VALUES(`last_modified`), episode_number=VALUES(`episode_number`)");
			
			pstmt = getConnection().prepareStatement(query.toString());
			int counter = 0;
			for(int i=0; i<listSize; i++) {
				Pack pack = bot.getListing().get(i);
				pstmt.setInt(1, pack.getBotId());
				pstmt.setInt(2, pack.getNumber());
				pstmt.setString(3, pack.getName());
				pstmt.setString(4, pack.getSize());
				pstmt.setInt(5, pack.getEpisodeNumber());
				pstmt.setTimestamp(6, pack.getLastModified());
				pstmt.addBatch();
				counter++;
				if( counter > 100  ){
					pstmt.executeBatch();
					counter = 0;
				}
			}
		} catch(Exception e){
			_logger.log(bot.getName() + " broke in insertAndUpdateListFor()");
		}
		
		
		pstmt.executeBatch();
		pstmt.close();
		*/
		
		int listSize = bot.getListing().size();
		if( listSize == 0 ){
			return;
		}
		
		Statement stmt = getConnection().createStatement();
		
		int counter = 0;
		StringBuilder sb = new StringBuilder();
		sb.append("INSERT INTO " + _PACKS_TABLE + " (bot_id, number, name, size, episode_number, last_modified) VALUES ");
		
		for(int i=0; i<listSize; i++) {
			Pack pack = bot.getListing().get(i);
			sb.append("(" + pack.getBotId() + "," + pack.getNumber() + ",'" + pack.getName().replaceAll("'", Matcher.quoteReplacement("\\'")) + "','" + pack.getSize() + "','" + pack.getEpisodeNumber() + "','" + pack.getLastModified() + "')");
			counter++;
			
			if(counter>1000){
				sb.append(" ON DUPLICATE KEY UPDATE name=VALUES(`name`), size=VALUES(`size`), last_modified=VALUES(`last_modified`)");
				try{
					
					stmt.executeUpdate(sb.toString());
				} catch(Exception e){
					_myBot.getLogger().error(bot.getName() + " broke in insertAndUpdateListFor()", e);
				}
				sb = new StringBuilder();
				sb.append("INSERT INTO " + _PACKS_TABLE + " (bot_id, number, name, size, episode_number, last_modified) VALUES ");
				counter = 0;
			} else {
				if (i != listSize - 1) {
					sb.append(",");
				}
			}
		}
		
		if( counter > 0 ){
			sb.append(" ON DUPLICATE KEY UPDATE name=VALUES(`name`), size=VALUES(`size`), last_modified=VALUES(`last_modified`);");
			
			try{
				stmt.executeUpdate(sb.toString());
			} catch(Exception e){
				_myBot.getLogger().error(bot.getName() + " broke in insertAndUpdateListFor()", e);
			}
		}
		
		stmt.close();
		
		/*
		int counter = 0;
		StringBuilder sb = new StringBuilder();
		sb.append("INSERT INTO " + _PACKS_TABLE + " (bot_id, number, name, size, episode_number, last_modified) VALUES ");
		for (Pack pack : bot.getListing()) {
			sb.append(",(" + pack.getBotId() + "," + pack.getNumber() + ",'" + pack.getName().replaceAll("'", Matcher.quoteReplacement("\\'")) + "','" + pack.getSize() + "','" + pack.getEpisodeNumber() + "','" + pack.getLastModified() + "')");
			counter++;
			if(counter>1000){
				sb.append(" ON DUPLICATE KEY UPDATE name=VALUES(`name`), size=VALUES(`size`), last_modified=VALUES(`last_modified`);");
				try{
					stmt.executeUpdate(sb.toString().replaceFirst("VALUES ,", "VALUES "));
				} catch(Exception e){
					_logger.log(bot.getName() + " broke in insertAndUpdateListFor()");
					_logger.log(sb.toString().replaceFirst("VALUES ,", "VALUES "));
				}
				sb = new StringBuilder();
				sb.append("INSERT INTO " + _PACKS_TABLE + " (bot_id, number, name, size, last_modified) VALUES ");
				counter = 0;
			}
		}
		if(!sb.toString().equals("INSERT INTO " + _PACKS_TABLE + " (bot_id, number, name, size, episode_number, last_modified) VALUES ")){
			sb.append(" ON DUPLICATE KEY UPDATE name=VALUES(`name`), size=VALUES(`size`), episode_number=VALUES(`episode_number`), last_modified=VALUES(`last_modified`);");
			try{
				stmt.executeUpdate(sb.toString().replaceFirst("VALUES ,", "VALUES "));
			} catch(Exception e){
				_logger.log(sb.toString().replaceFirst("VALUES ,", "VALUES "));
				_logger.log(bot.getName() + " broke in insertAndUpdateListFor()");
			}
		}
		stmt.close();*/
	}

	public void addNewBot(AbstractDistroBot bot) {
		PreparedStatement pstmt = null;
		try {
			pstmt = getConnection().prepareStatement("INSERT INTO " + _BOTS_TABLE + "(name,url,type,status_id,last_seen,last_processed,informative) " + "Values (?,?,?,?,?,?,0);");
			pstmt.setString(1, bot.getName());
			pstmt.setString(2, bot.getURL());
			pstmt.setString(3, bot.getType());
			pstmt.setInt(4, bot.getStatusId());
			pstmt.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
			pstmt.setTimestamp(6, new Timestamp(System.currentTimeMillis()));
			pstmt.executeUpdate();
			pstmt.close();
			
			pstmt = getConnection().prepareStatement("SELECT * FROM " + _BOTS_TABLE + " WHERE name = '" + bot.getName() + "';");
			ResultSet rs = pstmt.executeQuery();
			if(rs.next()){
				bot.setId(rs.getInt(1));
			}else{
				_myBot.getLogger().warn("Something went horribly wrong with addNewBot() for " + bot.getName());
			}
			rs.close();
			pstmt.close();
		} catch (SQLException e) {
			_myBot.getLogger().error("Error in UpdatePackListDAO.addNewBot() For: " + bot.getName() + " -- Duplicate Add... How?", e);
		}
	}

	public int updateBotURL(int id, String url) {
		int result = 0;
		PreparedStatement pstmt = null;
		try {
			String sql = "UPDATE " + _BOTS_TABLE + " SET url=? WHERE id=?;";
			pstmt = getConnection().prepareStatement(sql);
			pstmt.setString(1, url);
			pstmt.setInt(2, id);
			result = pstmt.executeUpdate();
			pstmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} 

		return result;
	}

	public int updateBotType(int id, String type) {
		int result = 0;
		PreparedStatement pstmt = null;
		try {
			String sql = "UPDATE " + _BOTS_TABLE + " SET type=? WHERE id=?;";
			pstmt = getConnection().prepareStatement(sql);
			pstmt.setString(1, type.toUpperCase());
			pstmt.setInt(2, id);
			result = pstmt.executeUpdate();
			pstmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public int updateBotInformative(int id, int inform) {
		int result = 0;
		PreparedStatement pstmt = null;
		try {
			String sql = "UPDATE " + _BOTS_TABLE + " SET informative=? WHERE id=?;";
			pstmt = getConnection().prepareStatement(sql);
			pstmt.setInt(1, inform);
			pstmt.setInt(2, id);
			result = pstmt.executeUpdate();
			pstmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} 

		return result;
	}
	
	public int updateBotBatch(int id, int batch) {
		int result = 0;
		PreparedStatement pstmt = null;
		try {
			String sql = "UPDATE " + _BOTS_TABLE + " SET batchenable=? WHERE id=?;";
			pstmt = getConnection().prepareStatement(sql);
			pstmt.setInt(1, batch);
			pstmt.setInt(2, id);
			result = pstmt.executeUpdate();
			pstmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} 

		return result;
	}

	public void deleteBot(int botID) {
		Statement stmt = null;
		try {
			stmt = getConnection().createStatement();
			stmt.executeUpdate("delete from " + _BOTS_TABLE + " where id = '" + botID + "'");
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void deleteBot(String botName) {
		Statement stmt = null;
		try {
			stmt = getConnection().createStatement();
			stmt.executeUpdate("delete from " + _BOTS_TABLE + " where LOWER(`name`) = '" + botName.toLowerCase() + "'");
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Set offline bots in database and memory
	 * @param notBots
	 */
	public void setOfflineBots(HashSet<String> offBots){
		try{
			for (String botname : offBots) {
				UpdatePackListFunctions.getBot(botname).setStatusId(0);
			}
		}catch(Exception e){
			_myBot.sendMessageFair("#" + _myBot.getProperty("admin_channel"), "setOfflineBots failed. Turning packlist module off.");
		}
	}

	/**
	 * Set online bots in database and memory
	 * @param yesBots
	 */
	public void setBotsOffline(){
		if(_updatePackList.getBotsMap().size()>0){
			StringBuilder sb = new StringBuilder();
			sb.append("UPDATE `updatepacklist_bots` set status_id = 0");
			try {
				Statement stmt = getConnection().createStatement();
				stmt.executeUpdate(sb.toString());
				stmt.close();
			} catch (SQLException e) {
				_myBot.getLogger().error("setOfflineBots Error: " + sb.toString(), e);
			}
		}
	}

	public void handelLatestPacks(MessageEvent message){
		if(!_updatePackList.isIterating()){
			UpdatePackListFunctions.getBot(message.getUser().getNick()).handleLatest();
		}
	}

	public void showLatestPacksForDCC(SendChat session, String args) throws IOException{
		
		LatestPacksDAO latestPacksDAO = new LatestPacksDAO(_myBot);
		int dispNumber = 10;
		if(!args.equals("")){
			dispNumber = Integer.parseInt(args);
		}

		LinkedList<Pack> corn = latestPacksDAO.getLatestPacks(dispNumber);
		for(Pack pack : corn){
			session.sendLine(Colors.BROWN + "[" + Colors.TEAL + pack.getSize() + Colors.BROWN + "] " + Colors.DARK_GRAY + pack.getName() + Colors.NORMAL + "  /MSG " + pack.getBotName() + " XDCC SEND " + pack.getNumber());
		}
	}
	
	public UpdatePackList getUpdatePackList(){
		return _updatePackList;
	}
	
	public void updateOwner(AbstractDistroBot bot, String botowner) throws SQLException{
		PreparedStatement stmt = null;
			try {
				
				String query = "UPDATE updatepacklist_bots SET `owner` = ? WHERE id = ?";
				stmt = getConnection().prepareStatement(query);
				
				stmt.setString(1, botowner);
				stmt.setInt(2, bot.getId());
				stmt.executeUpdate();
				
				bot.setOwner(botowner);
				
			} catch (SQLException e) {
				_myBot.getLogger().error("SQL error in BotOwner updateOwner()", e);
			} finally {
				if( null != stmt ){
					stmt.close();
				}
			}
	}
}
