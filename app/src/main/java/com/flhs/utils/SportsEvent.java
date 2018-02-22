package com.flhs.utils;

public class SportsEvent {
    private String sport;
    private String visitingTeam;
    private String homeTeam;
    private String time;

    public SportsEvent(String sport, String visitingTeam, String homeTeam, String time){
        this.sport = sport;
        this.visitingTeam = visitingTeam;
        this.homeTeam = homeTeam;
        this.time = time;
    }

    @Override
    public String toString(){
        return time + "\n" + sport + "\n" + homeTeam + " vs " + visitingTeam;
    }
}
