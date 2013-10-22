package com.seatgeek.model;

import java.util.Date;
import java.util.List;

public class Event {
	public String type;
	public Venue venue;
	public String title;
	public boolean dateTbd;
	public String shortTitle;
	public Date datetimeUtc;
	public Date datetimeLocal;
	public List<Performer> performers;
}
