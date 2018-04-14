package com.nibl.bot.plugins.anilist;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Series have a title, start and end dates and the season in which they aired.
 *
 * @author Brandon Risberg
 * @since 2/16/2016
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Series {

    private int seriesId;

    @JsonProperty("id")
    private int aniListId;

    @JsonProperty("title_english")
    private String title;

    @JsonProperty("total_episodes")
    private int episodeCount;

    @JsonProperty("start_date")
    private Date startDate;

    @JsonProperty("end_date")
    private Date endDate;

    private Season season;

    private List<Episode> episodeList;

    public Series() {
        episodeList = new ArrayList<Episode>();
    }

    public int getSeriesId() {
        return seriesId;
    }

    public void setSeriesId(int seriesId) {
        this.seriesId = seriesId;
    }

    public int getAniListId() {
        return aniListId;
    }

    public void setAniListId(int aniListId) {
        this.aniListId = aniListId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getEpisodeCount() {
        return episodeCount;
    }

    public void setEpisodeCount(int episodeCount) {
        this.episodeCount = episodeCount;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Season getSeason() {
        return season;
    }

    public void setSeason(Season season) {
        this.season = season;
    }

    public List<Episode> getEpisodeList() {
        return episodeList;
    }

    public void setEpisodeList(List<Episode> episodeList) {
        this.episodeList = episodeList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Series)) return false;

        Series series = (Series) o;

        return title.equals(series.title);

    }

    @Override
    public int hashCode() {
        return title.hashCode();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("Series[title=");
        sb.append(title);
        sb.append("]");

        return sb.toString();
    }
}