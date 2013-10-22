package com.seatgeek;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.ListActivity;
import android.content.Intent;
import android.graphics.Typeface;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

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
import com.seatgeek.adapter.EventsAdapter.EndlessEvents;
import com.seatgeek.adapter.RecommendationsAdapter;
import com.seatgeek.model.Event;
import com.seatgeek.model.Recommendation;
import com.seatgeek.model.RecommendationResults;
import com.seatgeek.util.Constants;
import com.seatgeek.util.GsonRequest;
import com.seatgeek.util.TypefaceSpan;
import com.squareup.picasso.Picasso;

public class SimilarEventsActivity extends ListActivity implements EndlessEvents {
	
	private static SimpleDateFormat sdfDate = new SimpleDateFormat("MMM dd");
	private static SimpleDateFormat sdfDayTime = new SimpleDateFormat("EEE h:mm a");
	
	private RequestQueue mRequestQueue;
	
	private LocationClient mLocationClient;
	private Location mLastKnownLocation;

	private View mListHeader;
	private View mLoadingFooter;
	private RecommendationsAdapter mEventsAdapter;
	
	private TextView mEventDate;
	private TextView mEventTime;
	private TextView mEventVenue;
	private TextView mEventTitle;
	private TextView mEventShortTitle;
	private ImageView mPerformerPix;
	private TextView mListHeaderText;
	
	private static Typeface fontTypeFaceWhitneyMedium;
	private static Typeface fontTypeFaceWhitneySentinelSemibold;
	
	private Response.Listener<RecommendationResults> mVolleyCreateSuccessListener() {
	    return new Response.Listener<RecommendationResults>() {
	        @Override
	        public void onResponse(RecommendationResults recommendationsWrapper) {
	        	Log.e("SeatGeek", "onResponse()" + recommendationsWrapper.recommendations.size());
	        	mLoadingFooter.setVisibility(View.GONE);
	        	List<Event> events = new ArrayList<Event>();
	        	for (Recommendation r : recommendationsWrapper.recommendations) {
	        		events.add(r.event);
	        	}
	        	initAdapter(events);
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
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		Log.e("SeatGeek", "onNewIntent()");
		mEventsAdapter.clear();
		populateViews();
		startRequest(1);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_item_similars);
		setActionBarTitleWithCustomFont();
		
		fontTypeFaceWhitneyMedium = Typeface.createFromAsset(getAssets(), "fonts/whitneymediumbas.ttf");
		fontTypeFaceWhitneySentinelSemibold = Typeface.createFromAsset(getAssets(), "fonts/sentinelsemibold.ttf");
		
		this.mRequestQueue = Volley.newRequestQueue(this);
		this.mLocationClient = new LocationClient(this, mLocationClientConnectionCallbacks, mLocationClientConnectionFailedListener);
		
		this.mListHeader = getLayoutInflater().inflate(R.layout.part_similars_header, null);
		mListHeaderText = (TextView) mListHeader.findViewById(R.id.header_text);
		mListHeaderText.setTypeface(fontTypeFaceWhitneySentinelSemibold);
		
		this.mLoadingFooter = getLayoutInflater().inflate(R.layout.part_progress_bar_footer, null);
		
		this.getListView().addHeaderView(mListHeader);
		this.getListView().addFooterView(mLoadingFooter);
		
		initViews();
		populateViews();
	}
	
	private void initViews() {
		this.mEventDate = (TextView) findViewById(R.id.txt_event_date);
		this.mEventDate.setTypeface(fontTypeFaceWhitneySentinelSemibold);
		
		this.mEventTime = (TextView) findViewById(R.id.txt_event_time);
		this.mEventTime.setTypeface(fontTypeFaceWhitneyMedium);
		
		this.mEventVenue = (TextView) findViewById(R.id.txt_event_location);
		this.mEventVenue.setTypeface(fontTypeFaceWhitneyMedium);
		
		this.mEventTitle = (TextView) findViewById(R.id.txt_event_title);
		this.mEventTitle.setTypeface(fontTypeFaceWhitneySentinelSemibold);
		
		this.mEventShortTitle = (TextView) findViewById(R.id.txt_event_main_title);
		this.mEventShortTitle.setTypeface(fontTypeFaceWhitneySentinelSemibold);
		
		this.mPerformerPix = (ImageView) findViewById(R.id.img_event_image);
	}
	
	private void populateViews() {
		final Intent intent = getIntent();
		final Date eventDate = (Date) intent.getSerializableExtra("HomeActivity.Date");
		final String eventTitle = intent.getStringExtra("HomeActivity.Title");
		final String eventVenue = intent.getStringExtra("HomeActivity.Venue");
		final String eventShortTitle = intent.getStringExtra("HomeActivity.ShortTitle");
		final String eventPerformerPix = intent.getStringExtra("HomeActivity.Pix");
		
		this.mEventTitle.setText(eventTitle);
		this.mEventShortTitle.setText(eventShortTitle);
		this.mEventVenue.setText(eventVenue);
		this.mEventDate.setText(sdfDate.format(eventDate));
		this.mEventTime.setText(sdfDayTime.format(eventDate));
		this.mListHeaderText.setText("Similar to " + eventShortTitle);
		
		Picasso.with(this).load(eventPerformerPix).into(mPerformerPix);
	} 
	
	@Override
	public void nextPage(int page) {
		startRequest(page);
	}
	
	private void startRequest(final int page) {
	    final GsonRequest<RecommendationResults> jsObjRequest = new GsonRequest<RecommendationResults>(
												        Method.GET,
												        getEventsURL(page),
												        RecommendationResults.class, 
												        mVolleyCreateSuccessListener(),
												        mVolleyCreateErrorListener());
	    this.mRequestQueue.add(jsObjRequest);
	    this.mLoadingFooter.setVisibility(View.VISIBLE);
	}
	
	private void initAdapter(final List<Event> events) {
		if (mEventsAdapter == null) {
			mEventsAdapter = new RecommendationsAdapter(this, 0, events);
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
		builder.appendEncodedPath(Constants.API_RECOMMENDATIONS_EP);
		
		builder.appendQueryParameter(Constants.PARAM_LAT, String.valueOf(mLastKnownLocation.getLatitude()));
		builder.appendQueryParameter(Constants.PARAM_LONG, String.valueOf(mLastKnownLocation.getLongitude()));
		builder.appendQueryParameter(Constants.PARAM_PAGE, String.valueOf(page));
		builder.appendQueryParameter(Constants.PARAM_PER_PAGE, "20");
		builder.appendQueryParameter(Constants.PARAM_CLIENT_ID, Constants.SG_CLIENT_ID);
		builder.appendQueryParameter(Constants.PARAM_PERFORMERS_ID, getIntent().getStringExtra("HomeActivity.Pid"));
		
		Log.e("SeatGeek", "URL"+builder.toString()+"]");
		return builder.toString();
	} 
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		final Event event = mEventsAdapter.getItem(position - getListView().getHeaderViewsCount());
		final Intent intentToLaunch = new Intent(this, SimilarEventsActivity.class);
		intentToLaunch.putExtra("HomeActivity.Title", event.title);
		intentToLaunch.putExtra("HomeActivity.ShortTitle", event.shortTitle);
		intentToLaunch.putExtra("HomeActivity.Venue", event.venue.display_location);
		intentToLaunch.putExtra("HomeActivity.Date", event.datetimeLocal);
		intentToLaunch.putExtra("HomeActivity.Pid", event.performers.get(0).id);
		intentToLaunch.putExtra("HomeActivity.Pix", event.performers.get(0).images.banner);
		setIntent(intentToLaunch);
		startActivity(intentToLaunch);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.home, menu);
		return true;
	}
	
	private void setActionBarTitleWithCustomFont() {
		final SpannableString s = new SpannableString("Artist");
		s.setSpan(new TypefaceSpan(this, "sentinelsemibold"), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		getActionBar().setTitle(s);
	}
}
