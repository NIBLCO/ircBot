package com.nibl.bot.plugins.dildo;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;

import com.nibl.bot.Bot;
import com.nibl.bot.dao.DataAccessObject;

public class DildoDAO extends DataAccessObject {
	
	private static String wordSQL = "SELECT `value` FROM ( SELECT * FROM words WHERE `type` IN (1,2,3,4) ORDER BY RAND() ) AS a GROUP BY `type` ORDER BY `type`";
	private static String randomWordSQL = "SELECT `value` FROM words WHERE `type` = ? AND `value` != ? ORDER BY RAND() LIMIT 1";
	
	public DildoDAO(Bot bot) {
		super(bot);
	}

	@Override
	public void createTables() {}

	@Override
	public void dropTables() {}
	
	// [caller] [verb]s [randUser] `with` [randNumber] [adjective:?opt] dildo(s) of [adjective:?opt] [noun]
	
	// 1=>Noun, 2=>Verb, 3=>Adjective, 4=>Adverb
	public String getRandomDildo(String caller, String receiver){
		StringBuilder sb = new StringBuilder();
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			/*
			 * Setup random variables
			 */
			pstmt = getConnection().prepareStatement(wordSQL);
			rs = pstmt.executeQuery();
			
			rs.next();
			String noun = rs.getString("value");
			rs.next();
			String verb = rs.getString("value");
			rs.next();
			String adjective = rs.getString("value");
			rs.next();
			String adverb = rs.getString("value");			
			
			Random rand = new Random();
			int randomNum;
			if( Math.random() >= 0.8 ){
				randomNum = rand.nextInt((9999 - 1000) + 1) + 1000;
			} else {
				randomNum = rand.nextInt((100 - 1) + 1) + 1;
			}
			/*
			 * End setup random variables
			 */
			
			// Add Caller Name
			sb.append(caller); // Jenga
			sb.append(" ");
			
			// Randomize adverb
			if( Math.random() >= 0.6 ){
				pstmt = getConnection().prepareStatement(randomWordSQL);
				pstmt.setInt(1, Dildo.ADVERB);
				pstmt.setString(2, adverb);
				rs = pstmt.executeQuery();
				if(rs.next()) {
					sb.append(rs.getString("value"));// Roughly
					sb.append(" ");
				}
			}
			
			// Add verb
			sb.append(verb); // Insert(s)
			sb.append("s");
			sb.append(" ");
			
			// Add Receiver Name
			sb.append(receiver); // ooinuza
			sb.append(" ");
			
			// Add Text
			sb.append("with"); // with
			sb.append(" ");
			
			// Add Random Number
			sb.append(randomNum); // 1234
			sb.append(" ");
			
			// Add Adjective
			sb.append(adjective); // rusty
			sb.append(" ");
			
			// Add Text
			sb.append("dildo"); // dildo(s)
			if( randomNum > 1 ){
				sb.append("s");
			}
			sb.append(" ");
			sb.append("of");
			sb.append(" ");
			
			// Randomize Adjective
			if( Math.random() >= 0.6 ){
				pstmt = getConnection().prepareStatement(randomWordSQL);
				pstmt.setInt(1, Dildo.ADJECTIVE);
				pstmt.setString(2, adjective);
				rs = pstmt.executeQuery();
				if(rs.next()) {
					sb.append(rs.getString("value"));// dirty
					sb.append(" ");
				}
			}
			
			// Add Noun
			sb.append(noun); // Sewage
			
			rs.close();
			pstmt.close();
			
		} catch (SQLException e) {
			_myBot.getLogger().error("Error when getting random dildo",e);
		}
		
		return sb.toString();
	}

}
