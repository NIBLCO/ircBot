package com.nibl.bot.plugins.anidb;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.zip.GZIPInputStream;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.nibl.bot.Bot;
import com.nibl.bot.dao.DataAccessObject;

public class AnidbDAO extends DataAccessObject {
	
	public AnidbDAO(Bot myBot) {
		super(myBot);
	}
	
	@Override
	public void createTables() {
		if (!tableExists("anidb")) {
			try {
				// ALTER TABLE ooinuza.anidb MODIFY COLUMN `title` VARCHAR(255)  CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL;
				Statement statement = getConnection().createStatement();
				statement.execute("CREATE TABLE IF NOT EXISTS `anidb` (`id` int(11) NOT NULL, `title` varchar(300) NOT NULL, `type` varchar(300), `lang` varchar(300)) ENGINE=InnoDB DEFAULT CHARSET=latin1;");
			} catch (SQLException e) {
				_myBot.getLogger().error("createTables",e);
			}
			_myBot.getLogger().info("Tables Created for Admin Users");
		}
		anidbDump();
	}

	public void anidbDump(){
		dropTables();

		if(_myBot.getProperty("anidb").equals("enabled")){
			_myBot.getLogger().info("AniDB Data dump");
			try {
				String sql = "INSERT INTO `anidb` (`id`, `title`, `type`, `lang`) VALUES (?,?,?,?)";
				PreparedStatement pstmt = getConnection().prepareStatement(sql);
				URLConnection urlConnection = (new URL(_myBot.getProperty("anidb_link"))).openConnection();
				urlConnection.setConnectTimeout(4000);urlConnection.setReadTimeout(4000);
				Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new GZIPInputStream(urlConnection.getInputStream()));
				doc.getDocumentElement().normalize();
				
				NodeList nodeList = doc.getElementsByTagName("anime");
				
				for (int s = 0; s < nodeList.getLength(); s++) {
	
					Node currentNode = nodeList.item(s);
	
					if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
						NodeList titleNodeList = ((Element)currentNode).getElementsByTagName("title");
						
						for(int i = 0; i<titleNodeList.getLength(); i++){
							Element element = (Element)titleNodeList.item(i);
							String type = element.getAttribute("type");
							String lang = element.getAttribute("xml:lang");
	
							if(type.matches("(syn|main|official)") && lang.matches("(en|x\\-jat)")){
								Integer aid = Integer.parseInt( element.getParentNode().getAttributes().getNamedItem("aid").getNodeValue() );
								String title = element.getFirstChild().getNodeValue();
								pstmt.setInt(1, aid);
								pstmt.setString(2, title);
								pstmt.setString(3, type);
								pstmt.setString(4, lang);
								pstmt.addBatch();
							}
						}
					}
				}
				pstmt.executeBatch();
			} catch (Exception e1) {
				_myBot.getLogger().error("anidbDump",e1);
			}
		}
	}

	public Object[] findAidTotalAndSeriesName(String term, Integer moveForward) throws SQLException{
		Statement statement = getConnection().createStatement();

		String sql = "select distinct id from anidb where title like '%" + term.trim().replaceAll(" ", "%") + "%' order by id;";
		ResultSet rs = statement.executeQuery(sql);
		int total = 0;
		
		for(int i = 0; i<=moveForward; i++){
			total++;
			if(!rs.next()){
				return null;
			}
		}
		int aid = rs.getInt(1);
		while(rs.next()){
			total++;
		}
		
		
		sql = "select title from anidb where id=" + aid + ";";
		rs = statement.executeQuery(sql);
		rs.next();
		String seriesName = rs.getString(1);
		return new Object[]{aid,total,seriesName};
	}
	
	public Document findSeries(Integer aid) throws MalformedURLException, IOException, SAXException, ParserConfigurationException{
		String url = "http://api.anidb.net:9001/httpapi?request=anime&client=chipchip&clientver=2&protover=1&aid=" + aid;
		URLConnection urlConnection = (new URL(url).openConnection());
		urlConnection.setConnectTimeout(4000);urlConnection.setReadTimeout(4000);
		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new GZIPInputStream(urlConnection.getInputStream()));
		doc.getDocumentElement().normalize();
		return doc;
	}
	
	@Override
	public void dropTables() {
		try {
			Statement statement = getConnection().createStatement();
			statement.execute("DELETE FROM `anidb`");
		} catch (SQLException e) {
			_myBot.getLogger().error("dropTables",e);
		}
	}
}