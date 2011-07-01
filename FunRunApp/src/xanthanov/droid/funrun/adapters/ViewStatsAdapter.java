package xanthanov.droid.funrun.adapters; 

import xanthanov.droid.funrun.persist.FunRunData; 
import xanthanov.droid.funrun.R; 
import xanthanov.droid.xantools.DroidTime; 

import android.widget.BaseAdapter; 
import android.widget.TextView; 
import android.view.View; 
import android.view.ViewGroup; 
import android.view.LayoutInflater; 
import android.content.Context; 
import android.text.Spanned; 

public class ViewStatsAdapter extends BaseAdapter {
	
	private FunRunData state; 
	private Context context; 
	private LayoutInflater inflater; 
	private View[] galleryViews; 

	private final static int TABLE_CHILD = 1; 
	private final static int DISTANCE_ROW = 0; 
	private final static int TIME_ROW = 1; 
	private final static int SPEED_ROW = 2; 
	private final static int PLACES_ROW = 3; 
	private final static int DATA_COL = 1; 

	private final static int VIEW_TYPE = 0; 

	public ViewStatsAdapter(Context c, FunRunData state) {
		this.state = state; 
		this.context = c; 
		inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		galleryViews = new View[state.size()]; 
	}

	@Override
	public int getCount() {
		return state.size(); 
	}

	@Override
	public Object getItem(int position) {
		return state.get(position);
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
		if (galleryViews[position] != null) {
			return galleryViews[position]; 
		}
		ViewGroup galleryView = (ViewGroup) inflater.inflate(R.id.statsGalleryView, null); 
		TextView totalDistance = (TextView) galleryView.findViewById(R.id.totalDistance); 
		TextView totalTime = (TextView) galleryView.findViewById(R.id.totalTime); 
		TextView avgSpeed = (TextView) galleryView.findViewById(R.id.avgSpeed); 
		TextView placesVisited = (TextView) galleryView.findViewById(R.id.placesVisited); 

		totalDistance.setText(DroidTime.getDistanceString(state.get(position).getDistanceSoFar())); 
		/*
		ViewGroup table = (ViewGroup) galleryView.getChildAt(TABLE_CHILD); 

		//Set the total distance TextView's data
		ViewGroup tableRow = (ViewGroup) table.getChildAt(DISTANCE_ROW); 
		TextView totalDistance = (TextView) tableRow.getChildAt(DATA_COL); 
		totalDistance.setText(DroidTime.getDistanceString(state.get(position).getDistanceSoFar())); 

		//Set the total time TextView's data
		TextView totalTime;
		TextView avgSpeed;  
	*/

		return galleryView;
	}

	@Override 
	public int getViewTypeCount() {
		return 1; 
	}

}
