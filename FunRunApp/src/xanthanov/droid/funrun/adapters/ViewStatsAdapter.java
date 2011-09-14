package xanthanov.droid.funrun.adapters; 

import xanthanov.droid.funrun.db.OldRun; 
import xanthanov.droid.funrun.db.OldLeg; 
import xanthanov.droid.funrun.R; 
import xanthanov.droid.xantools.DroidUnits; 
import xanthanov.droid.gplace.GooglePlace; 

import java.util.Date; 
import java.text.SimpleDateFormat; 
import java.text.DateFormat; 
import java.util.List; 
import java.util.ArrayList; 

import android.widget.BaseAdapter; 
import android.widget.TextView; 
import android.widget.Button; 
import android.widget.ImageButton; 
import android.widget.AdapterView; 
import android.view.View; 
import android.view.ViewGroup; 
import android.widget.Gallery;
import android.view.LayoutInflater; 
import android.content.Context; 
import android.text.Spanned; 
import android.content.res.TypedArray; 
import android.content.res.Resources; 

public class ViewStatsAdapter extends BaseAdapter {
	
	private List<OldRun> oldRuns; 
	private Context context; 
	private LayoutInflater inflater; 
	private View[] galleryViews; 
	private Gallery theGallery; 
	private int galleryItemBackground; 
	private int currentPosition; 

	private final DateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy h:mm aa"); 

	private final static int VIEW_TYPE = 0; 

	public ViewStatsAdapter(Context c, List<OldRun> oldRuns, Gallery theGallery) {
		this.oldRuns = oldRuns;
		this.context = c; 
		//inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater = LayoutInflater.from(context); 
		galleryViews = new View[oldRuns.size()]; 
		this.theGallery = theGallery; 

		TypedArray a = context.obtainStyledAttributes(R.styleable.StatsGallery);

		galleryItemBackground = a.getResourceId(
                R.styleable.StatsGallery_android_galleryItemBackground, 0);
        a.recycle();

		setupButtons();
		setButtonsEnabledState(theGallery.getFirstVisiblePosition());  
		currentPosition = theGallery.getFirstVisiblePosition(); 

		theGallery.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView parent, View view, int position, long id) {
				setButtonsEnabledState(position); 
				currentPosition = position; 
			}

			@Override
			public void onNothingSelected(AdapterView parent) {

			}
		}); 
	}

	public int getCurrentPosition() {
		return currentPosition; 
	}

	public boolean isEmpty() {
		return oldRuns.size() <= 0; 
	}

	@Override
	public int getCount() {
		return oldRuns.size() >= 1 ? oldRuns.size() : 1; 
	}

	@Override
	public Object getItem(int position) {
		return (oldRuns.size() <= 0 ? null : oldRuns.get(position));
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public int getItemViewType(int position) {
		return VIEW_TYPE; 
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (oldRuns == null || oldRuns.size() <= 0) {
			return getEmptyView(); 
		}

		ViewGroup galleryView = (ViewGroup) inflater.inflate(R.layout.stats_one_view, null); 
		TextView runDateText = (TextView) galleryView.findViewById(R.id.viewRunsDate); 
		TextView totalPoints = (TextView) galleryView.findViewById(R.id.totalPoints); 
		TextView totalDistance = (TextView) galleryView.findViewById(R.id.totalDistance); 
		TextView totalTime = (TextView) galleryView.findViewById(R.id.totalTime); 
		TextView avgSpeed = (TextView) galleryView.findViewById(R.id.avgSpeed); 
		TextView placesVisited = (TextView) galleryView.findViewById(R.id.placesVisited); 

		Date runDate = oldRuns.get(position).getRunDate(); 
		Spanned dateSpanned = android.text.Html.fromHtml("<b>" + dateFormat.format(runDate)  + "</b>");

		runDateText.setText(dateSpanned); 
		long totalPointsLong = oldRuns.get(position).getTotalPoints(); 
		double totalDistMeters = oldRuns.get(position).getTotalRunDistance(); 
		long totalTimeMs = oldRuns.get(position).getTotalRunTime(); 

		java.text.NumberFormat nf = java.text.NumberFormat.getInstance(); 
		totalPoints.setText(nf.format(totalPointsLong)); 

		totalDistance.setText(DroidUnits.getDistanceStringV3(totalDistMeters));
		totalTime.setText(DroidUnits.msToStrV2(totalTimeMs)); 
		avgSpeed.setText(DroidUnits.getSpeedStringV2(totalTimeMs, totalDistMeters)); 

		String placesStr = ""; 

		List<String> places = oldRuns.get(position).getPlacesVisited(); 

		int numPlaces = places.size(); 
		for (int i = 0; i < numPlaces; i++) {
			String p = places.get(i); 
			placesStr += (p + "<br/><b>" + (oldRuns.get(position).get(i).gotToPlace() ? "(Got there)" : "(Attempted)" )  + "</b>" + (i < numPlaces - 1 ? "<br>" : "")); 
		}

		Spanned placesHtml = android.text.Html.fromHtml(placesStr); 

		placesVisited.setText(placesHtml); 

		//galleryView.setLayoutParams(new Gallery.LayoutParams(150, 100));
        galleryView.setBackgroundResource(galleryItemBackground);


		galleryViews[position] = galleryView; 
		return galleryView;
	}

	private void setupButtons() {
		ImageButton leftButton = (ImageButton) ((ViewGroup)theGallery.getParent()).findViewById(R.id.leftArrow); 
		ImageButton rightButton = (ImageButton) ((ViewGroup)theGallery.getParent()).findViewById(R.id.rightArrow); 


		leftButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				theGallery.setSelection(currentPosition - 1, true); 	
			}
		}); 	

		rightButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				theGallery.setSelection(currentPosition + 1, true); 	
			}
		}); 	
	}

	private void setButtonsEnabledState(int position) {
		ImageButton leftButton = (ImageButton) ((ViewGroup)theGallery.getParent()).findViewById(R.id.leftArrow); 
		ImageButton rightButton = (ImageButton) ((ViewGroup)theGallery.getParent()).findViewById(R.id.rightArrow); 

		if (position <= 0) {
			leftButton.setEnabled(false); 
		}
		else {
			leftButton.setEnabled(true); 
		}
		if (position >= getCount() - 1) {
			rightButton.setEnabled(false); 
		}
		else {
			rightButton.setEnabled(true); 
		}
	}

	private View getEmptyView() {
		ViewGroup emptyStatsView = (ViewGroup) inflater.inflate(R.layout.empty_stats, null); 
		//emptyStatsView.setLayoutParams(new Gallery.LayoutParams(150, 100));
        emptyStatsView.setBackgroundResource(galleryItemBackground);

		return emptyStatsView;
	}

	@Override 
	public int getViewTypeCount() {
		return 1; 
	}

	@Override
	public boolean hasStableIds() {
		return true; 
	}

}
