package com.nibl.bot.plugins.request;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.PreparedStatement;

import org.pircbotx.Channel;
import org.pircbotx.User;

import com.nibl.bot.Bot;
import com.nibl.bot.dao.DataAccessObject;

/**
 * 
 * CREATE TABLE IF NOT EXISTS requests_status( id TINYINT NOT NULL
 * AUTO_INCREMENT PRIMARY KEY, status VARCHAR(20) );
 * 
 * INSERT INTO requests_status(id, status) VALUES('1','new') ON DUPLICATE KEY
 * UPDATE; INSERT INTO requests_status(status) VALUES('filling'); INSERT INTO
 * requests_status(status) VALUES('filled');
 * 
 * CREATE TABLE IF NOT EXISTS requests( id INT NOT NULL AUTO_INCREMENT PRIMARY
 * KEY, in_channel VARCHAR(20), made_by VARCHAR(20), made_date DATETIME, request
 * VARCHAR(100), status_id TINYINT, filled_by VARCHAR(20), filled_date DATETIME,
 * FOREIGN KEY (status_id) REFERENCES requests_status(id) ON DELETE CASCADE );
 * 
 * INSERT INTO requests(in_channel,made_by,status_id) VALUES('b','o','1');
 * INSERT INTO requests(in_channel,made_by,status_id) VALUES('b','o','2');
 * INSERT INTO requests(in_channel,made_by,status_id) VALUES('b','o','3');
 * INSERT INTO requests(in_channel,made_by,status_id) VALUES('b','o','4');
 * 
 * 
 * @author cwang
 * 
 */
public class RequestDAO extends DataAccessObject {
	
	public RequestDAO(Bot myBot) {
		super(myBot);
	}

	@Override
	public void createTables() {
		if (!tableExists("requests_status") || !tableExists("requests")) {
			try {
				Statement statement = getConnection().createStatement();
				statement.execute("CREATE TABLE IF NOT EXISTS requests_status( id TINYINT NOT NULL AUTO_INCREMENT PRIMARY KEY, status VARCHAR(20));");
				statement.execute("INSERT INTO requests_status(id, status) VALUES('1','new');");
				statement.execute("INSERT INTO requests_status(id, status) VALUES('2','filling');");
				statement.execute("INSERT INTO requests_status(id, status) VALUES('3','filled');");
				statement.execute("INSERT INTO requests_status(id, status) VALUES('4','deleted');");
				statement.execute("CREATE TABLE IF NOT EXISTS requests( id INT NOT NULL AUTO_INCREMENT PRIMARY KEY, in_channel VARCHAR(20), made_by VARCHAR(20), made_date DATETIME, request VARCHAR(100), status_id TINYINT, filled_by VARCHAR(20), filled_date DATETIME, FOREIGN KEY (status_id) REFERENCES requests_status(id) ON DELETE CASCADE );");
			} catch (SQLException e) {
				_myBot.getLogger().error("createTables",e);
			}
			System.out.println("Tables Created for Request");
		}		
	}

	@Override
	public void dropTables() {
		try {
			Statement statement = getConnection().createStatement();
			statement.execute("DROP TABLE IF EXISTS requests;");
			statement.execute("DROP TABLE IF EXISTS requests_status;");
		} catch (SQLException e) {
			_myBot.getLogger().error("dropTables",e);
		}
	}
	
	public boolean add(Channel channel, User user, String request) {
			try {
				Statement stmt = getConnection().createStatement();
				ResultSet rs = stmt.executeQuery("select amount from nibl_money where user_name='" + user.getNick().toLowerCase() + "';");
				if(rs.next()){
					int amount = rs.getInt(1);
					if(amount>=300){
						stmt.executeUpdate("update nibl_money set amount=" + (amount-Request._requestAmount) + " where user_name='" + user + "';");
					}else{
						_myBot.sendMessageFair(channel, user.getNick() + ", You only have $" + amount);
						return false;
					}
				}else{
					_myBot.sendMessageFair(channel, "Try using the !money trigger to register");
					return false;
				}
				
				PreparedStatement pstmt = null;
				String query = "insert into requests(in_channel, made_by, made_date, request, status_id) values(?, ?, ?, ?, ?)";
				pstmt = getConnection().prepareStatement(query);
				pstmt.setString(1, channel.getName());
				pstmt.setString(2, user.getNick());
				pstmt.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
				pstmt.setString(4, request);
				pstmt.setInt(5, 1);
				pstmt.executeUpdate();
				pstmt.close();
				return true;
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return false;
	}

	public String getUnfilledRequests() {
		String query = "select id, made_by, request from requests where status_id=1 order by made_date asc;";
		ResultSet rs = null;
		StringBuilder sb = new StringBuilder();
		try {
			PreparedStatement pstmt = null;
			try {
				pstmt = getConnection().prepareStatement(query);
				rs = pstmt.executeQuery();
				while ((rs != null) && rs.next()) {
					int id = rs.getInt(1);
					String made_by = rs.getString(2);
					String request = rs.getString(3);
					sb.append(String.format("#%d - %s: %s", id, made_by, request) + "\r\n");
				}
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				pstmt.close();
			}
		} catch (SQLException e) { // catching the close
			e.printStackTrace();
		}

		return sb.toString();
	}
	
	public String getMyFills(User user) {
		String query = "select id, made_by, request from requests where status_id=2 and filled_by=? order by made_date asc;";
		ResultSet rs = null;
		StringBuilder sb = new StringBuilder();
		try {
			PreparedStatement pstmt = null;
			try {
				pstmt = getConnection().prepareStatement(query);
				pstmt.setString(1, user.getNick());
				rs = pstmt.executeQuery();
				while ((rs != null) && rs.next()) {
					int id = rs.getInt(1);
					String made_by = rs.getString(2);
					String request = rs.getString(3);
					sb.append(String.format("#%d - %s: %s", id, made_by, request) + "\r\n");
				}
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				pstmt.close();
			}
		} catch (SQLException e) { // catching the close
			e.printStackTrace();
		}

		return sb.toString();
	}

	public Boolean deleteRequest(RequestObj request, User user) {
		String query = "update requests set status_id=?, filled_by=?, filled_date=? where id=?;";
			try {
				PreparedStatement pstmt = getConnection().prepareStatement(query);
				pstmt.setInt(1, 4);
				pstmt.setString(2, user.getNick());
				pstmt.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
				pstmt.setInt(4, request.getID());
				pstmt.executeUpdate();
				pstmt.close();
				return true;
			} catch (SQLException e) {
				return false;
			}
	}
	
	public Boolean fillRequest(RequestObj request, String botname, User user) {
		String query = "update requests set status_id=?, filled_by=?, filled_date=? where id=?;";
			try {
				PreparedStatement pstmt = getConnection().prepareStatement(query);
				pstmt.setInt(1, 3);
				pstmt.setString(2, user.getNick());
				pstmt.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
				pstmt.setInt(4, request.getID());
				pstmt.executeUpdate();
				pstmt.close();
				return true;
			} catch (SQLException e) {
				return false;
			}
	}
	
	public Boolean fillingRequest(RequestObj request, User user) {
		String query = "update requests set status_id=?, filled_by=? where id=?;";
			try {
				PreparedStatement pstmt = getConnection().prepareStatement(query);
				pstmt.setInt(1, 2);
				pstmt.setString(2, user.getNick());
				pstmt.setInt(3, request.getID());
				pstmt.executeUpdate();
				pstmt.close();
				return true;
			} catch (SQLException e) {
				return false;
			}
	}
	
	public RequestObj getRequestObj(String id){
		try {
			PreparedStatement pstmt = null;
			String query = "SELECT * FROM requests WHERE id = ?";
			pstmt = getConnection().prepareStatement(query);
			pstmt.setString(1, id);
			
			ResultSet rs = pstmt.executeQuery();
			if(rs.next()){
				return new RequestObj(rs);
			} else {
				return null;
			}
		} catch (SQLException e) {
			_myBot.getLogger().error("Request Object Error",e);
			return null;
		}
	}
	
	public Integer getAccessLevel(User user){
		
		try {
			if(user.isVerified()){
				Statement statement = getConnection().createStatement();
				ResultSet rs = statement.executeQuery("select access from bot_admins where user_name = '" + user.getNick().toLowerCase() + "';");
				if(rs.next()){
					return rs.getInt(1);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return -1;
	}
}
