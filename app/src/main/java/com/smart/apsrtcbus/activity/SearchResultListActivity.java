package com.smart.apsrtcbus.activity;

import java.io.Serializable;
import java.util.ArrayList;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.smart.apsrtcbus.R;
import com.smart.apsrtcbus.adapter.SearchResultAdapter;
import com.smart.apsrtcbus.adapter.TabPagerAdapter;
import com.smart.apsrtcbus.utilities.AppUtils;
import com.smart.apsrtcbus.vo.SearchResultVO;

public class SearchResultListActivity extends ActionBarActivity{

	private AdView adView;
	private ArrayList<SearchResultVO> list = null;
	private TabPagerAdapter tabPagerAdapter;
	private ViewPager viewPager;
	private ActionBar actionBar;

	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setContentView(R.layout.search_results_list);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		list = this.getIntent().getParcelableArrayListExtra("SearchResults");
		if(savedInstanceState!=null && list==null)
		{
			list = savedInstanceState.getParcelableArrayList("List");
		}
		final ListView listView = (ListView) findViewById(R.id.list);
		SearchResultAdapter adapter = new SearchResultAdapter(this, list);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if(position>0)
				{
					Intent intent = new Intent(SearchResultListActivity.this, JourneyDetailsListActivity.class);
					intent.putExtra("SearchResultVO", (Serializable)listView.getAdapter().getItem(position));
					intent.putExtra("FROM", getIntent().getStringExtra("FROM"));
					intent.putExtra("TO", getIntent().getStringExtra("TO"));
					intent.putExtra("DATE",getIntent().getStringExtra("DATE"));
					intent.putExtra("SEARCH_URL", getIntent().getStringExtra("SEARCH_URL"));
					startActivity(intent);
				}
			}
		});
		TextView fromTextView = (TextView) findViewById(R.id.fromTextView);
		TextView toTextView = (TextView) findViewById(R.id.toTextView);
		TextView dateTextView = (TextView) findViewById(R.id.dateTextView);

        String fromTxt = getIntent().getStringExtra("FROM");
        fromTxt = fromTxt.split("-").length>1?fromTxt.split("-")[0]:fromTxt;
		fromTextView.setText(fromTxt);

        String toTxt = getIntent().getStringExtra("TO");
        toTxt = toTxt.split("-").length>1?toTxt.split("-")[0]:toTxt;
        toTextView.setText(toTxt);

		String dateStr = getIntent().getStringExtra("DATE");
		dateTextView.setText(AppUtils.getFormattedDate(dateStr));

		adView = (AdView)this.findViewById(R.id.adMobView1);
		AdRequest adRequest = new AdRequest.Builder().build();
		adView.loadAd(adRequest);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putParcelableArrayList("List", list);
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		if(savedInstanceState!=null)
		{
			list = savedInstanceState.getParcelableArrayList("List");
		}
		super.onRestoreInstanceState(savedInstanceState);
	}
}
