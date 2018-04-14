package com.nibl.bot.plugins.redirect;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

import org.pircbotx.dcc.SendChat;

import com.nibl.bot.Bot;
import com.nibl.bot.dao.DataAccessObject;

public class RedirectDAO extends DataAccessObject {
	
	public RedirectDAO(Bot bot) {
		super(bot);
	}
	
	public HashMap<Integer,HashMap<String,Object>> getSearchable(){
		HashMap<Integer,HashMap<String,Object>> map = new HashMap<Integer,HashMap<String,Object>>();
		try {
			Statement stmt = getConnection().createStatement();
			ResultSet rs = stmt.executeQuery("SELECT id, seriesname, nextepisode FROM newestepisode WHERE active = 1");
			while( rs.next() ){
				HashMap<String,Object> attribs = new HashMap<String,Object>();
				attribs.put("seriesname", rs.getString("seriesname"));
				attribs.put("seriessearch", "%" + rs.getString("seriesname").replace(" ", "%") + "%");
				attribs.put("nextepisode", rs.getInt("nextepisode"));
				map.put(rs.getInt("id"), attribs);
			}
			rs.close();
			stmt.close();
		}catch (SQLException e){
			_myBot.getLogger().error("Failed getting searchable items in NewestEpisodeService",e);
		}
		
		return map;
	}
	
	public Integer searchNewestEpisode(String searchTerm, Integer episodeNumber){
		Integer latestepisode = -1;
		try {
			PreparedStatement pstmt = getConnection().prepareStatement("SELECT * FROM updatepacklist_packs WHERE `name` LIKE ? AND episode_number >= ? ORDER BY episode_number DESC LIMIT 1");
			pstmt.setString(1, searchTerm);
			pstmt.setInt(2, episodeNumber);
			ResultSet rs = pstmt.executeQuery();
			if( rs.next() ){
				latestepisode = rs.getInt("episode_number");
			}
			rs.close();
			pstmt.close();
		} catch (SQLException e) {
			_myBot.getLogger().error("Failed searching for newestEpisode NewestEpisodeService",e);
		}
		
		return latestepisode;
	}
	
	public void incrementNewestEpisode(Integer id, Integer latestEpisode){
		try {
			PreparedStatement pstmt = getConnection().prepareStatement("UPDATE newestepisode SET nextepisode = ? WHERE id = ?");
			pstmt.setInt(1, latestEpisode+1);
			pstmt.setInt(2, id);
			pstmt.executeUpdate();
			pstmt.close();
		} catch (SQLException e) {
			_myBot.getLogger().error("Failed updating newestEpisode NewestEpisodeService",e);
		}
	}
	
	public String getRedirectHash(String redirectValue) throws Exception{
        java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
        byte[] array = md.digest(redirectValue.getBytes());
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < array.length; ++i) {
          sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1,3));
        }
        return sb.toString();
	}
	
	public void createRedirect(String redirectValue, String hash){
		try {
			PreparedStatement pstmt = getConnection().prepareStatement("INSERT INTO redirect (`value`,`hash`) VALUES (?,?)", Statement.RETURN_GENERATED_KEYS);
			pstmt.setString(1, redirectValue);
			pstmt.setString(2, hash);
			pstmt.executeUpdate();
			pstmt.close();
		} catch (SQLException e) {
			_myBot.getLogger().error("Failed creating redirect " + redirectValue,e);
		}
	}
	
	public String getRedirect(String hashCode){
		
		String url = null;
		try {
			PreparedStatement pstmt = getConnection().prepareStatement("SELECT CONCAT('http://nibl.co.uk/r/',HEX(id)) AS url FROM redirect WHERE `hash` = ?");
			pstmt.setString(1, hashCode);
			ResultSet rs = pstmt.executeQuery();
            if(rs.next())
            {
                url = rs.getString("url");
            }
			pstmt.close();
		} catch (SQLException e) {
			_myBot.getLogger().error("Failed getting redirect " + hashCode,e);
		}
		
		return url;
	}
	
	public Boolean setList(ArrayList<String> list) {
		try {
			StringBuilder query = new StringBuilder();
			query.append("UPDATE newestepisode SET active = 0");
			PreparedStatement pstmt = getConnection().prepareStatement(query.toString());
			pstmt.execute();
			pstmt.close();
			
			query = new StringBuilder();
			query.append("INSERT INTO newestepisode (seriesname,nextepisode,active) VALUES ");
			query.append("(?,?,?) ");
			query.append("ON DUPLICATE KEY UPDATE active=1");
			
			PreparedStatement pstmt2 = getConnection().prepareStatement(query.toString());
			for(String series : list) {
				pstmt2.setString(1, series);
				pstmt2.setInt(2, 0);
				pstmt2.setInt(3, 1);
				pstmt2.addBatch();
			}
			pstmt2.executeBatch();
			pstmt2.close();
			return true;
		} catch(Exception e){
			_myBot.getLogger().error("Broke in setList ",e);
		}
		return false;
	}
	
	public void showCurrent(SendChat session) {
		try {
			StringBuilder query = new StringBuilder();
			query.append("SELECT * FROM newestepisode WHERE active = 1");
			PreparedStatement pstmt = getConnection().prepareStatement(query.toString());
			ResultSet rs = pstmt.executeQuery();
			while( rs.next() ){
				session.sendLine(rs.getString("seriesname") + " episode " + rs.getInt("nextepisode") );
			}
			pstmt.close();
		} catch(Exception e){
			_myBot.getLogger().error("Broke in setList",e);
		}
		
	}
	
	@Override
	public void createTables() {}

	@Override
	public void dropTables() {}
	
}
