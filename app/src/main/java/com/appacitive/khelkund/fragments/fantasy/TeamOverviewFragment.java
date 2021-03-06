package com.appacitive.khelkund.fragments.fantasy;


import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.appacitive.khelkund.R;
import com.appacitive.khelkund.activities.misc.LoginActivity;
import com.appacitive.khelkund.infra.KhelkundApplication;
import com.appacitive.khelkund.infra.SharedPreferencesManager;
import com.appacitive.khelkund.infra.StorageManager;
import com.appacitive.khelkund.model.Team;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ValueFormatter;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class TeamOverviewFragment extends Fragment {

    @InjectView(R.id.chart)
    public LineChart mChart;

    @InjectView(R.id.cv_overview_emblem)
    public CardView mCardEmblem;

    @InjectView(R.id.cv_overview_points)
    public CardView mCardPoints;

    @InjectView(R.id.cv_overview_rank)
    public CardView mCardRank;

    @InjectView(R.id.cv_overview_previous_points)
    public CardView mCardPreviousPoints;

    @InjectView(R.id.tv_overview_points)
    public TextView mTotalPoints;

    @InjectView(R.id.tv_overview_previous_points)
    public TextView mPreviousMatchPoints;

    @InjectView(R.id.tv_overview_stat_previous_points_label)
    public TextView mPreviousMatch;

    @InjectView(R.id.tv_overview_rank)
    public TextView mRank;

    @InjectView(R.id.iv_overview_emblem)
    public ImageView mEmblem;

    private Team mTeam;

    public TeamOverviewFragment() {
        // Required empty public constructor
    }

    public static TeamOverviewFragment newInstance() {
        return new TeamOverviewFragment();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView =  inflater.inflate(R.layout.fragment_team_overview, container, false);
        ButterKnife.inject(this, rootView);
        String userId = SharedPreferencesManager.ReadUserId();
        if (userId == null) {
            Intent loginIntent = new Intent(getActivity(), LoginActivity.class);
            startActivity(loginIntent);
            getActivity().finish();
        }
        StorageManager storageManager = new StorageManager();
        mTeam = storageManager.GetTeam(userId);
        ((ActionBarActivity)getActivity()).getSupportActionBar().setTitle(mTeam.getName());
        int bitmapId  = KhelkundApplication.getAppContext().getResources().getIdentifier(mTeam.getImageName(), "drawable", KhelkundApplication.getAppContext().getPackageName());
        if(bitmapId > 0)
            Picasso.with(getActivity()).load(bitmapId).into(mEmblem);
        mTotalPoints.setText(String.valueOf(mTeam.getTotalPoints()));
        if(mTeam.getTeamHistory().size() != 0)
        {
            mPreviousMatchPoints.setText(String.valueOf(mTeam.getTeamHistory().last().getPoints()));
            String[] previousMatchTeams = mTeam.getTeamHistory().last().getOpposition().split("-");
            mPreviousMatch.setText(String.format("%s vs %s", previousMatchTeams[0], previousMatchTeams[1]));
        }
        mRank.setText(String.valueOf(mTeam.getRank()));

        YoYo.with(Techniques.FadeInLeft).duration(700).playOn(mCardEmblem);
        YoYo.with(Techniques.FadeInLeft).duration(700).playOn(mCardRank);
        YoYo.with(Techniques.FadeInLeft).duration(700).playOn(mCardPoints);
        YoYo.with(Techniques.FadeInLeft).duration(700).playOn(mCardPreviousPoints);

        initGraph();
        showOverlayTutorial();
        return rootView;
    }

    private void initGraph()
    {
        XAxis xAxis = mChart.getXAxis();
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);
        xAxis.setAvoidFirstLastClipping(true);
        xAxis.setDrawLabels(true);
        xAxis.setTypeface(Typeface.DEFAULT_BOLD);
        xAxis.setAdjustXLabels(true);

        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setDrawGridLines(false);
        rightAxis.setDrawAxisLine(false);
        rightAxis.setEnabled(false);
        rightAxis.setStartAtZero(false);

        YAxis leftAxis = mChart.getAxisLeft();leftAxis.setLabelCount(5);
        leftAxis.setDrawGridLines(false);
        leftAxis.setDrawAxisLine(false);
        leftAxis.setStartAtZero(false);
        leftAxis.setSpaceBottom(10f);
        leftAxis.setSpaceTop(10f);
        leftAxis.setDrawLabels(true);
        leftAxis.setTypeface(Typeface.DEFAULT_BOLD);
        leftAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf(Math.round(value));
            }
        });

        ArrayList<String> oppositions = new ArrayList<String>();
        ArrayList<Entry> entries = new ArrayList<Entry>();
        for(int i = 0 ; i < mTeam.getTeamHistory().size(); i++)
        {
            oppositions.add(mTeam.getTeamHistory().get(i).getOpposition());
            entries.add(new Entry(Float.valueOf(mTeam.getTeamHistory().get(i).getPoints()), i));
        }

        final LineDataSet dataSet = new LineDataSet(entries, "points");
        dataSet.setValueTextSize(9);

        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf(Math.round(value));
            }
        });
        dataSet.setColor(getResources().getColor(R.color.accent));
        dataSet.setCircleSize(5);
        dataSet.setCircleColorHole(getResources().getColor(R.color.accent));
        dataSet.setCircleColor(getResources().getColor(R.color.accent));
        dataSet.setLineWidth(3);
        LineData lineData = new LineData(oppositions, new ArrayList<LineDataSet>(){{add(dataSet);}});

        mChart.setData(lineData);
        mChart.setDescription("");
        mChart.animateX(2000);
    }

    private void showOverlayTutorial() {

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                new ShowcaseView.Builder(getActivity())
                        .setTarget(ViewTarget.NONE)
                        .setContentText("Swipe left to view your squad")
                        .setContentTitle("Your team details appear here")
                        .hideOnTouchOutside()
                        .singleShot(11)
                        .build().hideButton();
            }
        };
        new Handler().postDelayed(runnable, 500);
    }

    @Override public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }


}
