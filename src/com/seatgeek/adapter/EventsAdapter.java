package com.seatgeek.adapter;

import java.text.SimpleDateFormat;
import java.util.List;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.seatgeek.HomeActivity;
import com.seatgeek.R;
import com.seatgeek.model.Event;
import com.squareup.picasso.Picasso;

public class EventsAdapter extends ArrayAdapter<Event> {
	
	protected Context context;
	private static Typeface fontTypeFaceWhitneyMedium;
	private static Typeface fontTypeFaceWhitneySentinelSemibold;
	
	private static SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy");
	
	volatile protected int mPage = 1;
	volatile private int mLastViewed = 0;
	
	public interface EndlessEvents {
		public void nextPage(final int page);
	}
	
	public EventsAdapter(Context ctx, int resId, List<Event> events) {
		super(ctx, 0, events);
		context = ctx;
		fontTypeFaceWhitneyMedium = Typeface.createFromAsset(context.getAssets(), "fonts/whitneymediumbas.ttf");
		fontTypeFaceWhitneySentinelSemibold = Typeface.createFromAsset(context.getAssets(), "fonts/sentinelsemibold.ttf");
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		EventWrapper wrapper = null;
		
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(R.layout.list_item_event, null);
			wrapper = new EventWrapper(convertView);
			convertView.setTag(wrapper);
		} else {
			wrapper = (EventWrapper) convertView.getTag();
		}

		wrapper.populateFrom(getItem(position));
		
		if (position > mLastViewed && position == getCount()-1) {
			mLastViewed = position;
			getNextPage();
		}
		
		return convertView;
	} 
	
	protected void getNextPage() {
		((HomeActivity)context).nextPage(++mPage);
	}
	
	private static class EventWrapper {
		private Context ctx;
		private TextView title;
		private TextView location;
		private ImageView image;
		private TextView date;

		public EventWrapper(View v) {
			this.ctx = v.getContext();
			this.title = (TextView) v.findViewById(R.id.txt_event_title);
			this.title.setTypeface(fontTypeFaceWhitneySentinelSemibold);
			this.location = (TextView) v.findViewById(R.id.txt_event_location);
			this.location.setTypeface(fontTypeFaceWhitneyMedium);
			this.image = (ImageView) v.findViewById(R.id.img_event_image);
			this.date = (TextView) v.findViewById(R.id.txt_event_date);
			this.date.setTypeface(fontTypeFaceWhitneyMedium);
		}

		private void populateFrom(final Event event) {
			title.setText(event.title);
			location.setText(event.venue.display_location);
			date.setText(sdf.format(event.datetimeLocal));
			Picasso.with(ctx).load(event.performers.get(0).images.banner).into(image);
		}
	}
	
}
