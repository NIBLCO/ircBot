package com.nibl.bot.plugins.dildo;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;

import com.nibl.bot.Bot;
import com.nibl.bot.dao.DataAccessObject;

public class DildoDAO extends DataAccessObject {

    private static String randomWordSQL = "SELECT `value` FROM words WHERE `type` = ? ORDER BY RAND() LIMIT 1";
    private static String randomAlternateWord = "SELECT `value` FROM words WHERE `type` = ? AND `value` != ? ORDER BY RAND() LIMIT 1";

    public DildoDAO(Bot bot) {
        super(bot);
    }

    @Override
    public void createTables() {
    }

    @Override
    public void dropTables() {
    }

    private String getRandomWord(Integer type) throws SQLException {
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = getConnection().prepareStatement(randomWordSQL);
            pstmt.setInt(1, type);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("value");
            } else {
                throw new SQLException("Probably a db error");
            }
        } finally {
            rs.close();
            pstmt.close();
        }

    }

    private String getRandomAlternateWord(String value, Integer type) throws SQLException {
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = getConnection().prepareStatement(randomAlternateWord);
            pstmt.setInt(1, type);
            pstmt.setString(2, value);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("value");
            } else {
                throw new SQLException("Probably a db error");
            }
        } finally {
            rs.close();
            pstmt.close();
        }

    }

    // [caller] [verb]s [randUser] `with` [randNumber] [adjective:?opt] dildo(s) of [adjective:?opt] [noun]

    // 1=>Noun, 2=>Verb, 3=>Adjective, 4=>Adverb
    public String getRandomDildo(String caller, String receiver) {
        StringBuilder sb = new StringBuilder();
        try {

            String noun = getRandomWord(Dildo.NOUN);
            String verb = getRandomWord(Dildo.VERB);
            String adjective = getRandomWord(Dildo.ADJECTIVE);
            String adverb = getRandomWord(Dildo.ADVERB);

            Random rand = new Random();
            int randomNum;
            if (Math.random() >= 0.8) {
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

            sb.append(adverb);// Roughly
            sb.append(" ");

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
            if (randomNum > 1) {
                sb.append("s");
            }
            sb.append(" ");
            sb.append("of");
            sb.append(" ");

            // Add Adjective
            sb.append(getRandomAlternateWord(adjective, Dildo.ADJECTIVE)); // rusty
            sb.append(" ");

            // Add Noun
            sb.append(noun); // Sewage

        } catch (SQLException e) {
            _myBot.getLogger().error("Error when getting random dildo", e);
        }

        return sb.toString();
    }

}
