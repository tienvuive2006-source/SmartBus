package com.smartbus.booking.service.ai;

public class ChatContext {
    public String from;
    public String to;
    public String date;
    public String sort = "default";
    public String company = "all";
    public String busType = "all";
    public long lastUpdated = System.currentTimeMillis();
}
