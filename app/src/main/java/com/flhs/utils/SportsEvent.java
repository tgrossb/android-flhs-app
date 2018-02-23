package com.flhs.utils;

public class SportsEvent extends EventObject {
    private String sport;
    private String visitingTeam;
    private String homeTeam;
    private String time;

    public SportsEvent(String sport, String visitingTeam, String homeTeam, String time){
        super(time, sport + "\n" + homeTeam + " vs " + visitingTeam);
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
