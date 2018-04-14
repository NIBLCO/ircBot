package com.nibl.bot.plugins.anilist;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.nibl.bot.plugins.anilist.AniListAccessToken;
import com.nibl.bot.plugins.anilist.Episode;
import com.nibl.bot.plugins.anilist.Season;
import com.nibl.bot.plugins.anilist.Series;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class AniListApiService {
    private static final Logger logger = LoggerFactory.getLogger(AniListApiService.class);

    public AniListAccessToken acquireAccessToken() {
        String url = "https://anilist.co/api/auth/access_token";

        URL obj;
        try {
            obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            // set request params with my client info on AniList.co/settings/developer
            con.setRequestMethod("POST");
            con.setRequestProperty("User-Agent", "NIBL");
            con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

            // hard coded client credentials for now
            String urlParameters = "grant_type=client_credentials&client_id=" + AniList.CLIENT_ID + "&client_secret=" + AniList.CLIENT_SECRET;

            // Send post request
            con.setDoOutput(true);
            con.setDoInput(true);
            OutputStream os = con.getOutputStream();
            BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            wr.write(urlParameters);
            wr.flush();
            wr.close();
            os.close();

            int responseCode = con.getResponseCode();
            /*
            System.out.println("\nSending 'POST' request to URL : " + url);
            System.out.println("Post parameters : " + urlParameters);
            System.out.println("Response Code : " + responseCode);
            */
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder responseBuffer = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                responseBuffer.append(inputLine);
            }
            in.close();

            //print result
            //System.out.println(responseBuffer.toString());

            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(responseBuffer.toString(), AniListAccessToken.class);
        } catch (Exception e) {
            logger.error(e.toString());
        }

        return null;
    }


    public List<Series> fetchAllSeries(AniListAccessToken accessToken, Season season) {
        String url = "http://anilist.co/api/browse/anime?access_token=" + accessToken.getAccessToken()
                + "&year=" + season.getYear()
                + "&season=" + season.getSeasonName()
                + "&type=Tv"
                + "&airing_data=true"
                + "&full_page=true";

        try {
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            // optional default is GET
            con.setRequestMethod("GET");

            //add request header
            con.setRequestProperty("User-Agent", "NIBL");

            int responseCode = con.getResponseCode();
            /*
            System.out.println("\nSending 'GET' request to URL : " + url);
            System.out.println("Response Code : " + responseCode);
            */

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer responseBuffer = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                responseBuffer.append(inputLine);
            }
            in.close();

            //print result
            //System.out.println(responseBuffer.toString());

            // Set up the ObjectMapper to map an array of series into an array of objects.
            ObjectMapper mapper = new ObjectMapper();
            List<Series> seriesList = mapper.readValue(responseBuffer.toString(), new TypeReference<List<Series>>() {
            });

            for (Series series : seriesList) {
                series.setSeason(season);
            }

            return seriesList;
        } catch (Exception e)

        {
            logger.error(e.toString());
        }

        return null;
    }

    public Series fetchSeries(AniListAccessToken accessToken, int seriesId) {
        String url = "http://anilist.co/api/anime/" + seriesId + "?access_token=" + accessToken.getAccessToken();

        try {
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            // optional default is GET
            con.setRequestMethod("GET");

            //add request header
            con.setRequestProperty("User-Agent", "NIBL");

            int responseCode = con.getResponseCode();
            /*
            System.out.println("\nSending 'GET' request to URL : " + url);
            System.out.println("Response Code : " + responseCode);
            */
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer responseBuffer = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                responseBuffer.append(inputLine);
            }
            in.close();

            //print result
            //System.out.println(responseBuffer.toString());

            ObjectMapper mapper = new ObjectMapper();
            Series resultSeries = mapper.readValue(responseBuffer.toString(), Series.class);
            return resultSeries;
        } catch (Exception e)

        {
            logger.error(e.toString());
        }

        return null;
    }

    // Update a given series object with the latest data (or simply the full data) from AniList.
    // TODO: remove weekindex as a param, instead put it in a lookup from the config service
    public void updateSeries(AniListAccessToken accessToken, Series series, int weekIndex) {
        String url = "http://anilist.co/api/anime/" + series.getAniListId() + "?access_token=" + accessToken.getAccessToken();

        try {
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            // optional default is GET
            con.setRequestMethod("GET");

            //add request header
            con.setRequestProperty("User-Agent", "NIBL");

            int responseCode = con.getResponseCode();
            /*
            System.out.println("\nSending 'GET' request to URL : " + url);
            System.out.println("Response Code : " + responseCode);
            */
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer responseBuffer = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                responseBuffer.append(inputLine);
            }
            in.close();

            //print result
            //System.out.println(responseBuffer.toString());

            ObjectMapper mapper = new ObjectMapper();
            ObjectReader updater = mapper.readerForUpdating(series);
            updater.readValue(responseBuffer.toString());

            // append the next episode
            final JsonNode episodeResponce = mapper.readTree(responseBuffer.toString()).path("airing");
            Episode nextEpisode = mapper.treeToValue(episodeResponce, Episode.class);
            nextEpisode.setSeries(series);
            nextEpisode.setSeason(series.getSeason());
            nextEpisode.setWeekIndex(weekIndex);
            series.getEpisodeList().add(nextEpisode);
        } catch (Exception e)

        {
            logger.error(e.toString());
        }

    }
}
