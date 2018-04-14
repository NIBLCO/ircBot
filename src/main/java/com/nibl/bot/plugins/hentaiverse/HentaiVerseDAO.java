package com.nibl.bot.plugins.hentaiverse;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import com.nibl.bot.Bot;
import com.nibl.bot.dao.DataAccessObject;

public class HentaiVerseDAO extends DataAccessObject {
	public String TABLE_HVITEMS = " hvitems.hvitems ";
	public String TABLE_HVEQUIPMENT = " hvitems.hentaiverse_equipment ";
	public String TABLE_HVEQUIPMENT_META = " hvitems.hentaiverse_equipment_meta ";
	public String TABLE_STAT_PRIORITY = " hvitems.stats_priority ";
	public String TABLE_EQUIP_SUFFIX = " hvitems.equip_suffix ";
	public String TABLE_EQUIP_PREFIX = " hvitems.equip_prefix ";
	public String TABLE_EQUIP_QUALITY = " hvitems.equip_qualities ";
	public String TABLE_EQUIP_TYPE = " hvitems.equip_types ";
	public String TABLE_EQUIP_SUB_TYPE = " hvitems.equip_sub_types ";
	public String TABLE_EQUIP_DAMAGE_TYPE = " hvitems.equip_damage_types ";
	public String TABLE_EQUIP_PROCS = " hvitems.equip_procs ";
	public String TABLE_HV_GIFT = "hvitems.hvgifts";
	public String TABLE_HV_GIFT_TYPE = "hvitems.hvgift_type";
	public String TABLE_HVMONSTERLIST = "hvmonsterlist.hvmonsterlist";
	public String TABLE_HVITEM_VERSION = "hvitems.hvitem_version";
	
	public HentaiVerseDAO(Bot bot) {
		super(bot);
	}
	
	@Override
	public void createTables() {

	}

	@Override
	public void dropTables() {
		
	}
	
	public void storeEquipment(Equipment item){
		try {
			Statement statement = getConnection().createStatement();
			StringBuilder qsb = new StringBuilder();
			qsb.append("INSERT INTO  " + TABLE_HVEQUIPMENT + " (eid, `key`, basepxp, level, `name`, `type`, subtype, suffix, prefix, quality,created_on) VALUES (");
			qsb.append("'" + item.getEID().trim() + "',");
			qsb.append("'" + item.getKEY().trim() + "',");
			qsb.append("'" + item.parsePXP0() + "',");
			if( null == item.getLevel() || // Catch if level was set incorrectly
				item.getLevel() == HentaiVerse.SOULBOUND ||
				item.getLevel().equals("") ){
				qsb.append("'-1',");
			} else {
				qsb.append("'" + item.getLevel() + "',");
			}
			qsb.append("'" + item.getTitle().trim() + "',");
			qsb.append("'" + item.getType() + "',");
			qsb.append("'" + item.getSubType() + "',");
			qsb.append("'" + item.getSuffix() + "',");
			qsb.append("'" + item.getPrefix() + "',");
			qsb.append("'" + item.getQuality() + "',");
			qsb.append("NOW())");

			statement.executeUpdate(qsb.toString(), Statement.RETURN_GENERATED_KEYS);
			Long recordID = null;
			ResultSet rs = statement.getGeneratedKeys();
			if (rs != null && rs.next()) {
				recordID = rs.getLong(1);
			}

			StringBuilder sb = new StringBuilder();
			sb.append("INSERT INTO " + TABLE_HVEQUIPMENT_META + " (equipmentID, `key`, `value`) VALUES ");
			for(EquipmentAttribute attribute : item.getAttributes().values()){
				sb.append("(" + recordID + ", '" + attribute.getTitle() + "', '" + attribute.getValue() + "'),");
			}
			
			if( item.isWeapon() ){
				ArrayList<EquipmentProc> procs = item.getProcs();
				int procCounter = 0;
				if( procs != null ){
					for( EquipmentProc proc : procs ){
						procCounter++;
						sb.append("(" + recordID + ", 'procTitle" + procCounter + "', '" + proc.getTitle() + "'),");
						if( proc.getChance() != null)
							sb.append("(" + recordID + ", 'procChance" + procCounter + "', '" + proc.getChance() + "'),");
						if( proc.getTurns() != null )
							sb.append("(" + recordID + ", 'procTurns" + procCounter + "', '" + proc.getTurns() + "'),");
						if( proc.getPoints() != null )
							sb.append("(" + recordID + ", 'procPoints" + procCounter + "', '" + proc.getPoints() + "'),");
						if( proc.getDot() != null )
							sb.append("(" + recordID + ", 'procDOT" + procCounter + "', '" + proc.getDot() + "'),");
						
					}
				}
				sb.append("(" + recordID + ", 'weaponDamage', '" + item.getWeaponDamage().getValue() + "'),");
				sb.append("(" + recordID + ", 'weaponDamageType', '" + item.getWeaponDamage().getTitle() + "'),");
			}
			
			String query = sb.toString();
			statement.execute(query.substring(0, query.length() -1 ));
			
			_myBot.getLogger().trace("Stored Equipment EID: " + item.getEID());
			statement.close();
		}catch (SQLException e){
			_myBot.getLogger().error("Failed saving eid " + item.getEID(), e);
		}
	}
	
	public boolean equipmentExists(Equipment item){
		try {
			String query = "SELECT eid FROM " + TABLE_HVEQUIPMENT + " WHERE eid = '" + item.getEID() + "'";
			Statement statement = getConnection().createStatement();
			ResultSet rs = statement.executeQuery(query);
			if(rs.next()){
				return true;
			}else{
				return false;
			}
		}catch (SQLException e){
			return true;
		}
	}
	
	public TreeMap<String, String> getHVItems(){
		TreeMap<String, String> hvItems = new TreeMap<String, String>();
		try {
			String query = "SELECT eid, `key` FROM " + TABLE_HVITEMS + " WHERE parsed = 0";
			Statement statement = getConnection().createStatement();
			ResultSet rs = statement.executeQuery(query);
			while( rs.next() ){
				hvItems.put(rs.getString("eid"), rs.getString("key"));
			}
		}catch (SQLException e){
			_myBot.sendNoticeFair("Jenga", "Failed getting all hvitems from db for processing.");
		}
		
		return hvItems;
	}
	
	public void setEquipmentProcessed(String eid){
		try {
			
			String query = "UPDATE " + TABLE_HVITEMS + " SET parsed = 1 WHERE eid = ?";
			PreparedStatement stmt = getConnection().prepareStatement(query);
			stmt.setString(1, eid);
			stmt.executeUpdate();
			
		}catch (SQLException e){
			_myBot.sendNoticeFair("Jenga", "Failed setting eid " + eid + " as parsed.");
		}
	}
	
	public LinkedHashMap<String, StatPriority> getStatPriority(){
		LinkedHashMap<String, StatPriority> statPriorities = new LinkedHashMap<String, StatPriority>();
		
		try {
			
			String query = "SELECT * FROM " + TABLE_STAT_PRIORITY + " ORDER BY priority ASC";
			Statement statement = getConnection().createStatement();
			ResultSet rs = statement.executeQuery(query);
			while( rs.next() ){
				statPriorities.put(rs.getString("name"), new StatPriority(rs.getString("name"), rs.getBoolean("newline"), rs.getString("color")));
			}
			
		}catch (SQLException e){
			_myBot.sendNoticeFair("Jenga", "Failed getting stat priorities.");
		}
		
		return statPriorities;
	}
	
	public ArrayList<String> getEquipmentSuffix(){
		ArrayList<String> equipmentSuffix = new ArrayList<String>();
		
		try {
			
			String query = "SELECT suffix FROM " + TABLE_EQUIP_SUFFIX;
			Statement statement = getConnection().createStatement();
			ResultSet rs = statement.executeQuery(query);
			while( rs.next() ){
				equipmentSuffix.add( rs.getString("suffix") );
			}
			
		}catch (SQLException e){
			_myBot.sendNoticeFair("Jenga", "Failed getting equipment suffixes.");
		}
		
		return equipmentSuffix;
	}
	
	public ArrayList<String> getEquipmentDamageTypes(){
		ArrayList<String> equipmentDamageType = new ArrayList<String>();
		
		try {
			
			String query = "SELECT type FROM " + TABLE_EQUIP_DAMAGE_TYPE;
			Statement statement = getConnection().createStatement();
			ResultSet rs = statement.executeQuery(query);
			while( rs.next() ){
				equipmentDamageType.add( rs.getString("type") );
			}
			
		}catch (SQLException e){
			_myBot.sendNoticeFair("Jenga", "Failed getting equipment damage types.");
		}
		
		return equipmentDamageType;
	}
	
	public ArrayList<String> getEquipmentProcs(){
		ArrayList<String> equipmentProcs = new ArrayList<String>();
		
		try {
			
			String query = "SELECT proc FROM " + TABLE_EQUIP_PROCS;
			Statement statement = getConnection().createStatement();
			ResultSet rs = statement.executeQuery(query);
			while( rs.next() ){
				equipmentProcs.add( rs.getString("proc") );
			}
			
		}catch (SQLException e){
			_myBot.sendNoticeFair("Jenga", "Failed getting equipment procs.");
		}
		
		return equipmentProcs;
	}
	
	public ArrayList<String> getEquipmentPrefix(){
		ArrayList<String> equipmentPrefix = new ArrayList<String>();
		
		try {
			
			String query = "SELECT prefix FROM " + TABLE_EQUIP_PREFIX;
			Statement statement = getConnection().createStatement();
			ResultSet rs = statement.executeQuery(query);
			while( rs.next() ){
				equipmentPrefix.add( rs.getString("prefix") );
			}
			
		}catch (SQLException e){
			_myBot.sendNoticeFair("Jenga", "Failed getting equipment prefixes.");
		}
		
		return equipmentPrefix;
	}
	
	public ArrayList<String> getEquipmentQuality(){
		ArrayList<String> equipmentQuality = new ArrayList<String>();
		
		try {
			
			String query = "SELECT quality FROM " + TABLE_EQUIP_QUALITY;
			Statement statement = getConnection().createStatement();
			ResultSet rs = statement.executeQuery(query);
			while( rs.next() ){
				equipmentQuality.add( rs.getString("quality") );
			}
			
		}catch (SQLException e){
			_myBot.sendNoticeFair("Jenga", "Failed getting equipment qualities.");
		}
		
		return equipmentQuality;
	}
	
	public TreeMap<String,Boolean> getEquipmentTypes(){
		TreeMap<String,Boolean> equipmentTypes = new TreeMap<String,Boolean>();
		
		try {
			
			String query = "SELECT `type`, hassubtype FROM " + TABLE_EQUIP_TYPE;
			Statement statement = getConnection().createStatement();
			ResultSet rs = statement.executeQuery(query);
			while( rs.next() ){
				equipmentTypes.put(rs.getString("type"), rs.getBoolean("hassubtype") );
			}
			
		}catch (SQLException e){
			_myBot.sendNoticeFair("Jenga", "Failed getting equipment types.");
		}
		
		return equipmentTypes;
	}
	
	public ArrayList<String> getEquipmentSubTypes(){
		ArrayList<String> equipmentSubTypes = new ArrayList<String>();
		
		try {
			
			String query = "SELECT subtype FROM " + TABLE_EQUIP_SUB_TYPE;
			Statement statement = getConnection().createStatement();
			ResultSet rs = statement.executeQuery(query);
			while( rs.next() ){
				equipmentSubTypes.add( rs.getString("subtype") );
			}
			
		}catch (SQLException e){
			_myBot.sendNoticeFair("Jenga", "Failed getting equipment sub types.");
		}
		
		return equipmentSubTypes;
	}
	
	public Integer getHVGiftType(String gift) throws SQLException{
		try {
			String query = "SELECT * FROM " + TABLE_HV_GIFT_TYPE + " WHERE `item` = ?";
			PreparedStatement stmt = getConnection().prepareStatement(query);
			stmt.setString(1, gift);
			ResultSet rs = stmt.executeQuery();
			if( rs.next() ){
				return rs.getInt("id");
			} else {
				rs.close();
				stmt.close();
				query = "INSERT INTO " + TABLE_HV_GIFT_TYPE + " (item) VALUES (?)";
				stmt = getConnection().prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
				stmt.setString(1, gift);
				stmt.executeUpdate();
				rs = stmt.getGeneratedKeys();
				if( rs.next() ) {
					return rs.getInt(1);
				} else {
					return -1;
				}
			}
		}catch (SQLException e){
			_myBot.sendNoticeFair("Jenga", "Failed hv gift type.");
			throw e;
		}
	}
	
	public Integer getMonsterIDByName(String monsterName) throws SQLException{
		try {
			_myBot.getLogger().trace("Get monsterID by name: " + monsterName);
			String query = "SELECT * FROM " + TABLE_HVMONSTERLIST + " WHERE monstername = ?";
			PreparedStatement stmt = getConnection().prepareStatement(query);
			stmt.setString(1, monsterName);
			ResultSet rs = stmt.executeQuery();
			if( rs.next() ){
				return rs.getInt("monsterid");
			} else {
				return -1;
			}
		}catch (SQLException e){
			_myBot.sendNoticeFair("Jenga", "Failed getting monsterid from hvmonsterlist.");
			throw e;
		}
	}
	
	public void storeHVGift(MonsterGift gift) throws SQLException{
		try {
			String query = "INSERT INTO " + TABLE_HV_GIFT + " (monsterid,`type`,quantity,created_on) VALUES (?,?,?,NOW())";
			PreparedStatement stmt = getConnection().prepareStatement(query);
			
			Integer monsterID = this.getMonsterIDByName( gift.getMonsterName() );
			
			Iterator<Entry<String, Integer>> it = gift.getGifts().entrySet().iterator();
			while(it.hasNext()){
                Map.Entry<String, Integer> pair = (Map.Entry<String, Integer>)it.next();
    			stmt.setInt(1, monsterID);
    			stmt.setInt(2, this.getHVGiftType( pair.getKey() ));
    			stmt.setInt(3, pair.getValue() );
    			stmt.addBatch();
			}
			stmt.executeBatch();
		}catch (SQLException e){
			_myBot.sendNoticeFair("Jenga", "Failed saving monster gift.");
			throw e;
		}
	}
	
	public void storeAllHVGifts(ArrayList<MonsterGift> gifts) throws SQLException{
		try{
			for(MonsterGift gift : gifts){
				this.storeHVGift(gift);
			}
		} catch(SQLException e){
			throw e;
		}
	}
	
	public Integer getMaxVersion() throws SQLException{
		try {
			String query = "SELECT MAX(`version`) as version FROM " + TABLE_HVITEM_VERSION;
			Statement stmt = getConnection().createStatement();
			ResultSet rs = stmt.executeQuery(query);
			if( rs.next() ){
				return rs.getInt("version");
			} else {
				return -1;
			}
		}catch (SQLException e){
			_myBot.sendNoticeFair("Jenga", "Failed getting max hvitem version.");
			throw e;
		}
	}
	
	public Integer getEIDForLottery(Equipment item) throws SQLException{
		try {
			
			String query = "SELECT * FROM hvitems.hentaiverse_equipment WHERE `name` = ? AND created_on BETWEEN DATE_SUB(NOW(),INTERVAL 1 DAY) AND NOW()";
			PreparedStatement pstmt = getConnection().prepareStatement(query);
			pstmt.setString(1, item.getTitle());
			ResultSet rs = pstmt.executeQuery();
			if( rs.next() ){
				return rs.getInt("eid");
			} else {
				rs.close();
				query = "SELECT MIN(eid)-1 AS eid FROM " + TABLE_HVEQUIPMENT + " WHERE `key` = 'lottery'";
				Statement stmt = getConnection().createStatement();
				rs = stmt.executeQuery(query);
				if( rs.next() ){
					return rs.getInt("eid");
				} else {
					return -1;
				}
			}
		}catch (SQLException e){
			_myBot.sendNoticeFair("Jenga", "Failed getting next eid for lottery.");
			throw e;
		}
	}
}
