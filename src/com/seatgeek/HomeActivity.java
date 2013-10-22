package com.seatgeek;

import java.util.List;

import android.app.ListActivity;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ListView;

import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;
import com.seatgeek.adapter.EventsAdapter;
import com.seatgeek.adapter.EventsAdapter.EndlessEvents;
import com.seatgeek.model.Event;
import com.seatgeek.model.EventResults;
import com.seatgeek.util.Constants;
import com.seatgeek.util.GsonRequest;
import com.seatgeek.util.TypefaceSpan;

public class HomeActivity extends ListActivity implements EndlessEvents {

	private RequestQueue mRequestQueue;
	
	private LocationClient mLocationClient;
	private Location mLastKnownLocation;
	
	private View mLoadingFooter;
	private EventsAdapter mEventsAdapter;
	
	private Response.Listener<EventResults> mVolleyCreateSuccessListener() {
	    return new Response.Listener<EventResults>() {
	        @Override
	        public void onResponse(EventResults eventsWrapper) {
	        	Log.e("SeatGeek", "onResponse()" + eventsWrapper.events.size());
	        	mLoadingFooter.setVisibility(View.GONE);
	        	initAdapter(eventsWrapper.events);
	        }
	    };
	}
	
	private Response.ErrorListener mVolleyCreateErrorListener() {
	    return new Response.ErrorListener() {
	        @Override
	        public void onErrorResponse(VolleyError error) {
	        	Log.e("SeatGeek", "onErrorResponse()");
	        	mLoadingFooter.setVisibility(View.GONE);
	        }
	    };
	}
	
	private ConnectionCallbacks mLocationClientConnectionCallbacks = new GooglePlayServicesClient.ConnectionCallbacks() {
		@Override
		public void onDisconnected() {
			Log.e("SeatGeek", "onDisconnected()");
		}
		
		@Override
		public void onConnected(Bundle connectionHint) {
			mLastKnownLocation = mLocationClient.getLastLocation();
			Log.e("SeatGeek", "onConnected() - last Known location:" + mLastKnownLocation.getLatitude() + "," + mLastKnownLocation.getLongitude());
			startRequest(1);
		}
	};
	
	private OnConnectionFailedListener mLocationClientConnectionFailedListener = new GooglePlayServicesClient.OnConnectionFailedListener() {
		@Override
		public void onConnectionFailed(ConnectionResult result) {
			Log.e("SeatGeek", "onConnectionFailed()");
		}
	};
	
	@Override
	protected void onStart() {
		super.onStart();
		this.mLocationClient.connect();
	}

	@Override
	protected void onStop() {
		super.onStop();
		this.mLocationClient.disconnect();
		this.mRequestQueue.cancelAll(this);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		setActionBarTitleWithCustomFont();
		
		this.mRequestQueue = Volley.newRequestQueue(this);
		this.mLocationClient = new LocationClient(this, mLocationClientConnectionCallbacks, mLocationClientConnectionFailedListener);
		
		this.mLoadingFooter = getLayoutInflater().inflate(R.layout.part_progress_bar_footer, null);
		this.getListView().addFooterView(mLoadingFooter);
	}
	
	@Override
	public void nextPage(int page) {
		startRequest(page);
	}
	
	private void startRequest(final int page) {
	    final GsonRequest<EventResults> jsObjRequest = new GsonRequest<EventResults>(
												        Method.GET,
												        getEventsURL(page),
												        EventResults.class, 
												        mVolleyCreateSuccessListener(),
												        mVolleyCreateErrorListener());
	    this.mRequestQueue.add(jsObjRequest);
	    this.mLoadingFooter.setVisibility(View.VISIBLE);
	}
	
	private void initAdapter(final List<Event> events) {
		if (mEventsAdapter == null) {
			mEventsAdapter = new EventsAdapter(this, 0, events);
			getListView().setAdapter(mEventsAdapter);
		} else {
			mEventsAdapter.addAll(events);
		}
	}
	
	private String getEventsURL(final int page) {
		final Uri.Builder builder = new Uri.Builder();
		
		builder.scheme(Constants.API_SERVER_SCHEME);
		builder.encodedAuthority(Constants.API_AUTHORITY);
		builder.appendEncodedPath(Constants.API_VERSION);
		builder.appendEncodedPath(Constants.API_EVENTS_EP);
		
		builder.appendQueryParameter(Constants.PARAM_LAT, String.valueOf(mLastKnownLocation.getLatitude()));
		builder.appendQueryParameter(Constants.PARAM_LONG, String.valueOf(mLastKnownLocation.getLongitude()));
		builder.appendQueryParameter(Constants.PARAM_PAGE, String.valueOf(page));
		builder.appendQueryParameter(Constants.PARAM_SORT, "score.desc");
		builder.appendQueryParameter(Constants.PARAM_PER_PAGE, "20");
		builder.appendQueryParameter(Constants.PARAM_TYPE, "concert");
		
		Log.e("SeatGeek", "URL"+builder.toString()+"]");
		return builder.toString();
	} 
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		final Event event = mEventsAdapter.getItem(position);
		final Intent intentToLaunch = new Intent(this, SimilarEventsActivity.class);
		intentToLaunch.putExtra("HomeActivity.Title", event.title);
		intentToLaunch.putExtra("HomeActivity.ShortTitle", event.shortTitle);
		intentToLaunch.putExtra("HomeActivity.Venue", event.venue.display_location);
		intentToLaunch.putExtra("HomeActivity.Date", event.datetimeLocal);
		intentToLaunch.putExtra("HomeActivity.Pid", event.performers.get(0).id);
		intentToLaunch.putExtra("HomeActivity.Pix", event.performers.get(0).images.banner);
		startActivity(intentToLaunch);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.home, menu);
		return true;
	}
	
	private void setActionBarTitleWithCustomFont() {
		final SpannableString s = new SpannableString(getString(R.string.app_name));
		s.setSpan(new TypefaceSpan(this, "sentinelsemibold"), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		getActionBar().setTitle(s);
	}
}
