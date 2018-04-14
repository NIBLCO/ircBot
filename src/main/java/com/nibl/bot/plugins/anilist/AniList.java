package com.nibl.bot.plugins.anilist;

import java.util.List;

public class AniList {
    
    public static final String CLIENT_ID = "jenga-ziddi";
    public static final String CLIENT_SECRET = "LL0jV7FSR1j0SKIlBRhiC0iMlzz4Pu";
    
    public static void main(String[] args) throws Exception {
        
        AniListApiService aniListApiService = new AniListApiService();

        // Acquire an accessToken from AniList.co
        AniListAccessToken accessToken = aniListApiService.acquireAccessToken();

        Season testSeason = new Season();
        testSeason.setTitle("Summer 2016");
        testSeason.setYear(2016);
        testSeason.setSeasonName("summer");
        int currentWeek = 8;
        
        List<Series> allSeries = aniListApiService.fetchAllSeries(accessToken, testSeason);

        for (Series series : allSeries) {
            // aniListApiService.updateSeries(accessToken, series, currentWeek);
            System.out.println( series.getTitle() );
        }
        
    }

}
