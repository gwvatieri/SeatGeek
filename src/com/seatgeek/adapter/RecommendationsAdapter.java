package com.seatgeek.adapter;

import java.util.List;

import android.content.Context;

import com.seatgeek.SimilarEventsActivity;
import com.seatgeek.model.Event;

public class RecommendationsAdapter extends EventsAdapter {

	public RecommendationsAdapter(Context ctx, int resId, List<Event> events) {
		super(ctx, resId, events);
	}
	
	@Override
	protected void getNextPage() {
		((SimilarEventsActivity)context).nextPage(++mPage);
	}
	
}
