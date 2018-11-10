package com.nibl.bot.plugins.latestpacks;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;

import com.nibl.bot.Bot;
import com.nibl.bot.dao.DataAccessObject;
import com.nibl.bot.plugins.updatepacklist.Pack;

public class LatestPacksDAO extends DataAccessObject{
	
	public LatestPacksDAO(Bot bot) {
		super(bot);
	}

	public LinkedList<Pack> getLatestPacks(int number){
		LinkedList<Pack> latest = new LinkedList<Pack>();
		try {
			Statement stmt = getConnection().createStatement();
			StringBuilder sb = new StringBuilder();
			sb.append("SELECT a.bot_id, b.name AS botName, a.number, a.name AS packName, a.size, a.sizekbits, a.episode_number, a.last_modified ");
			sb.append("FROM updatepacklist_packs AS a ");
			sb.append("INNER JOIN updatepacklist_bots AS b ON a.bot_id = b.id ");
			sb.append("ORDER BY a.last_modified DESC ");
			sb.append("LIMIT " + number);
			ResultSet rs = stmt.executeQuery(sb.toString());
			while(rs.next()){
				latest.add(new Pack(rs.getInt("bot_id"), rs.getInt("number"), rs.getString("botName"), rs.getString("packName"), rs.getString("size"), rs.getLong("sizekbits"), rs.getInt("episode_number"), rs.getTimestamp("last_modified")));
			}
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return latest;
	}
	
	@Override
	public void createTables() {}

	@Override
	public void dropTables() {

	}

}
