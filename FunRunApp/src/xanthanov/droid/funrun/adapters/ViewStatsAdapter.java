package xanthanov.droid.funrun.adapters; 

import xanthanov.droid.funrun.persist.FunRunData; 
import xanthanov.droid.funrun.R; 
import xanthanov.droid.xantools.DroidTime; 

import java.util.Date; 
import java.text.SimpleDateFormat; 
import java.text.DateFormat; 

import android.widget.BaseAdapter; 
import android.widget.TextView; 
import android.view.View; 
import android.view.ViewGroup; 
import android.widget.Gallery;
import android.view.LayoutInflater; 
import android.content.Context; 
import android.text.Spanned; 
import android.content.res.TypedArray; 
import android.content.res.Resources; 

public class ViewStatsAdapter extends BaseAdapter {
	
	private FunRunData state; 
	private Context context; 
	private LayoutInflater inflater; 
	private View[] galleryViews; 
	private Gallery theGallery; 
	private int galleryItemBackground; 

	private final DateFormat dateFormat = DateFormat.getDateInstance(); 

	private final static int VIEW_TYPE = 0; 

	public ViewStatsAdapter(Context c, FunRunData state, Gallery theGallery) {
		this.state = state; 
		this.context = c; 
		//inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater = LayoutInflater.from(context); 
		galleryViews = new View[state.size()]; 
		this.theGallery = theGallery; 

		TypedArray a = context.obtainStyledAttributes(R.styleable.StatsGallery);

		galleryItemBackground = a.getResourceId(
                R.styleable.StatsGallery_android_galleryItemBackground, 0);
        a.recycle();
	}

	@Override
	public int getCount() {
		return Math.max(state.size(), 1); 
	}

	@Override
	public Object getItem(int position) {
		return (state.size() <= 0 ? null : state.get(position));
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
		if (state == null || state.size() <= 0) {
			return getEmptyView(); 
		}

		if (galleryViews[position] != null) {
			return galleryViews[position]; 
		}
		ViewGroup galleryView = (ViewGroup) inflater.inflate(R.layout.stats_one_view, null); 
		TextView runDateText = (TextView) galleryView.findViewById(R.id.viewRunsDate); 
		TextView totalDistance = (TextView) galleryView.findViewById(R.id.totalDistance); 
		TextView totalTime = (TextView) galleryView.findViewById(R.id.totalTime); 
		TextView avgSpeed = (TextView) galleryView.findViewById(R.id.avgSpeed); 
		TextView placesVisited = (TextView) galleryView.findViewById(R.id.placesVisited); 

		Date runDate = state.get(position).getDate(); 
		Spanned dateSpanned = android.text.Html.fromHtml("<b>" + dateFormat.format(runDate)  + "</b>");

		runDateText.setText(dateSpanned); 
		totalDistance.setText(DroidTime.getDistanceString(state.get(position).getDistanceSoFar())); 
		totalTime.setText(DroidTime.msToStr(state.get(position).totalTime())) ; 

		//galleryView.setLayoutParams(new Gallery.LayoutParams(150, 100));
        galleryView.setBackgroundResource(galleryItemBackground);

		galleryViews[position] = galleryView; 
		return galleryView;
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
