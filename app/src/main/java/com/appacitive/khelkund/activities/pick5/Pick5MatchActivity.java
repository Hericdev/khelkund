package com.appacitive.khelkund.activities.pick5;

import android.app.ActivityOptions;
import android.app.ProgressDialog;
import android.gesture.GestureOverlayView;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.appacitive.khelkund.R;
import com.appacitive.khelkund.fragments.Pick5EmptyFragment;
import com.appacitive.khelkund.fragments.Pick5FinishedReadonlyFragment;
import com.appacitive.khelkund.fragments.Pick5PlayFragment;
import com.appacitive.khelkund.infra.APCallback;
import com.appacitive.khelkund.infra.ConnectionManager;
import com.appacitive.khelkund.infra.Http;
import com.appacitive.khelkund.infra.SharedPreferencesManager;
import com.appacitive.khelkund.infra.SnackBarManager;
import com.appacitive.khelkund.infra.StorageManager;
import com.appacitive.khelkund.infra.Urls;
import com.appacitive.khelkund.infra.transforms.CircleTransform2;
import com.appacitive.khelkund.model.Pick5MatchDetails;
import com.appacitive.khelkund.model.TeamHelper;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class Pick5MatchActivity extends ActionBarActivity {

    private GestureDetector gestureDetector;

    private ProgressDialog mDialog;

    private Pick5MatchDetails mDetails;

    DateFormat df = new SimpleDateFormat("EEEE, dd MMMM yyyy hh:mm a");

    private static final int empty_background_color = Color.parseColor("#ffd3d3d3");

    @InjectView(R.id.pick5_next_match)
    public RelativeLayout mNext;

    @InjectView(R.id.pick5_previous_match)
    public RelativeLayout mPrevious;

    @InjectView(R.id.iv_pick5_play_home)
    public ImageView mHomeLogo;

    @InjectView(R.id.iv_pick5_play_away)
    public ImageView mAwayLogo;

    @InjectView(R.id.tv_pick5_play_hometeamname)
    public TextView mHomeName;

    @InjectView(R.id.tv_pick5_play_awayteamname)
    public TextView mAwayName;

    @InjectView(R.id.tv_pick5_play_date)
    public TextView mDate;

    private String mUserId;

    int enterAnimation, exitAnimation = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick5_match);
        ButterKnife.inject(this);
        ConnectionManager.checkNetworkConnectivity(this);
        String matchId = getIntent().getStringExtra("match_id");
        mUserId = SharedPreferencesManager.ReadUserId();
        fetchMatchDetails(mUserId, matchId);
        gestureDetector = new GestureDetector(
                new SwipeGestureDetector());
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (gestureDetector.onTouchEvent(event)) {
            return true;
        }
        return super.onTouchEvent(event);
    }

    private void onLeftSwipe() {
        onNextMatchClick();
    }

    private void onRightSwipe() {
        onPreviousMatchClick();
    }

    private void setupFragment() {

        // setup toolbar
        int matchNumber = 1 + new StorageManager().GetMatch(mDetails.getMatchDetails().getId()).getMatchNumber();
        Date date = mDetails.getMatchDetails().getStartDate();
        Calendar cal = Calendar.getInstance();
        cal.setTime(date); // sets calendar time/date
        cal.add(Calendar.HOUR_OF_DAY, 5);
        cal.add(Calendar.MINUTE, 30);
        date = cal.getTime();
        mDate.setText(String.format("MATCH %s  %s IST", matchNumber, df.format(date)));
        mHomeName.setText(mDetails.getMatchDetails().getHomeTeamShortName());
        mAwayName.setText(mDetails.getMatchDetails().getAwayTeamShortName());

        Picasso.with(this)
                .load(TeamHelper.getTeamLogo(mDetails.getMatchDetails().getHomeTeamShortName()))
                .fit().transform(new CircleTransform2(empty_background_color))
                .into(mHomeLogo);
        Picasso.with(this)
                .load(TeamHelper.getTeamLogo(mDetails.getMatchDetails().getAwayTeamShortName()))
                .fit().transform(new CircleTransform2(empty_background_color))
                .into(mAwayLogo);


        //  if match is currently in progress or finished and user did not create a team
        if ((mDetails.getMatchDetails().getMatchStatus() == 2 || mDetails.getMatchDetails().getMatchStatus() == 1) && mDetails.getPlayers().size() == 0) {
            Pick5EmptyFragment emptyPick5Fragment = new Pick5EmptyFragment();
            getSupportFragmentManager().beginTransaction().setCustomAnimations(enterAnimation, exitAnimation)
                    .replace(R.id.fragment_container, emptyPick5Fragment).commit();
        }

        //  if user created a team for this match and the match has either started or is finished
        if (mDetails.getPlayers().size() > 0 && (mDetails.getMatchDetails().getMatchStatus() == 1 || mDetails.getMatchDetails().getMatchStatus() == 2)) {
            Pick5FinishedReadonlyFragment readonlyFragment = Pick5FinishedReadonlyFragment.newInstance();
            getSupportFragmentManager().beginTransaction().setCustomAnimations(enterAnimation, exitAnimation)
                    .replace(R.id.fragment_container, readonlyFragment).commit();
        }

        //  if match has not yet started
        if (mDetails.getMatchDetails().getMatchStatus() == 0) {
            Pick5PlayFragment playPick5Fragment = Pick5PlayFragment.newInstance();
            getSupportFragmentManager().beginTransaction().setCustomAnimations(enterAnimation, exitAnimation)
                    .replace(R.id.fragment_container, playPick5Fragment).commit();

        }
    }

    public Pick5MatchDetails getMatchDetails() {
//        Pick5MatchDetails details = null;
//        try {
//            details = new Pick5MatchDetails(new JSONObject("{\n" +
//                    "    \"Error\": null,\n" +
//                    "    \"Status\": 0,\n" +
//                    "    \"AppPlayers\": null,\n" +
//                    "    \"AppTeamPoints\": 20,\n" +
//                    "    \"Id\": null,\n" +
//                    "    \"MatchDetails\": {\n" +
//                    "        \"AwayTeamName\": \"Mumbai Indians\",\n" +
//                    "        \"AwayTeamShortName\": \"MI\",\n" +
//                    "        \"HomeTeamName\": \"Kolkata Knight Riders\",\n" +
//                    "        \"HomeTeamShortName\": \"KKR\",\n" +
//                    "        \"Id\": \"88663888708633095\",\n" +
//                    "        \"MatchStatus\": 0,\n" +
//                    "        \"StartDate\": \"2015-04-08T14:30:00.0000000Z\",\n" +
//                    "        \"Venue\": \"Eden Gardens, Kolkata\"\n" +
//                    "    },\n" +
//                    "    \"PlayerMapping\": [\n" +
//                    "        {\n" +
//                    "            \"AppPlayerId\": \"88664645986025969\",\n" +
//                    "            \"UserPlayerId\": \"88664679846642055\"\n" +
//                    "        },\n" +
//                    "        {\n" +
//                    "            \"AppPlayerId\": \"88664639720784334\",\n" +
//                    "            \"UserPlayerId\": \"88664677801918830\"\n" +
//                    "        },\n" +
//                    "        {\n" +
//                    "            \"AppPlayerId\": \"88664633373753761\",\n" +
//                    "            \"UserPlayerId\": \"88664648848638213\"\n" +
//                    "        },\n" +
//                    "        {\n" +
//                    "            \"AppPlayerId\": \"88664665582862676\",\n" +
//                    "            \"UserPlayerId\": \"88664638362878404\"\n" +
//                    "        },\n" +
//                    "        {\n" +
//                    "            \"AppPlayerId\": \"88664644121657826\",\n" +
//                    "            \"UserPlayerId\": \"88664675724689769\"\n" +
//                    "        },\n" +
//                    "        {\n" +
//                    "            \"AppPlayerId\": \"88664679846642055\",\n" +
//                    "            \"UserPlayerId\": \"88664645986025969\"\n" +
//                    "        },\n" +
//                    "        {\n" +
//                    "            \"AppPlayerId\": \"88664633946276262\",\n" +
//                    "            \"UserPlayerId\": \"88664650026189068\"\n" +
//                    "        },\n" +
//                    "        {\n" +
//                    "            \"AppPlayerId\": \"88664633946276262\",\n" +
//                    "            \"UserPlayerId\": \"88664651433378070\"\n" +
//                    "        },\n" +
//                    "        {\n" +
//                    "            \"AppPlayerId\": \"88664679175553409\",\n" +
//                    "            \"UserPlayerId\": \"88664646607831542\"\n" +
//                    "        },\n" +
//                    "        {\n" +
//                    "            \"AppPlayerId\": \"88664662621684037\",\n" +
//                    "            \"UserPlayerId\": \"88664637316399551\"\n" +
//                    "        },\n" +
//                    "        {\n" +
//                    "            \"AppPlayerId\": \"88664670244831577\",\n" +
//                    "            \"UserPlayerId\": \"88664639033967049\"\n" +
//                    "        },\n" +
//                    "        {\n" +
//                    "            \"AppPlayerId\": \"88664654966030634\",\n" +
//                    "            \"UserPlayerId\": \"88664634552353195\"\n" +
//                    "        },\n" +
//                    "        {\n" +
//                    "            \"AppPlayerId\": \"88664654966030634\",\n" +
//                    "            \"UserPlayerId\": \"88664636612805050\"\n" +
//                    "        },\n" +
//                    "        {\n" +
//                    "            \"AppPlayerId\": \"88664635909210549\",\n" +
//                    "            \"UserPlayerId\": \"88664654966030634\"\n" +
//                    "        },\n" +
//                    "        {\n" +
//                    "            \"AppPlayerId\": \"88664638362878404\",\n" +
//                    "            \"UserPlayerId\": \"88664664977834318\"\n" +
//                    "        },\n" +
//                    "        {\n" +
//                    "            \"AppPlayerId\": \"88664641879802328\",\n" +
//                    "            \"UserPlayerId\": \"88664678554796408\"\n" +
//                    "        },\n" +
//                    "        {\n" +
//                    "            \"AppPlayerId\": \"88664662621684037\",\n" +
//                    "            \"UserPlayerId\": \"88664635140604336\"\n" +
//                    "        },\n" +
//                    "        {\n" +
//                    "            \"AppPlayerId\": \"88664639033967049\",\n" +
//                    "            \"UserPlayerId\": \"88664670244831577\"\n" +
//                    "        },\n" +
//                    "        {\n" +
//                    "            \"AppPlayerId\": \"88664674284994911\",\n" +
//                    "            \"UserPlayerId\": \"88664647883948288\"\n" +
//                    "        },\n" +
//                    "        {\n" +
//                    "            \"AppPlayerId\": \"88664648848638213\",\n" +
//                    "            \"UserPlayerId\": \"88664633373753761\"\n" +
//                    "        },\n" +
//                    "        {\n" +
//                    "            \"AppPlayerId\": \"88664637316399551\",\n" +
//                    "            \"UserPlayerId\": \"88664662621684037\"\n" +
//                    "        },\n" +
//                    "        {\n" +
//                    "            \"AppPlayerId\": \"88664642910552541\",\n" +
//                    "            \"UserPlayerId\": \"88664681073475986\"\n" +
//                    "        },\n" +
//                    "        {\n" +
//                    "            \"AppPlayerId\": \"88664654966030634\",\n" +
//                    "            \"UserPlayerId\": \"88664635909210549\"\n" +
//                    "        },\n" +
//                    "        {\n" +
//                    "            \"AppPlayerId\": \"88664678554796408\",\n" +
//                    "            \"UserPlayerId\": \"88664641879802328\"\n" +
//                    "        },\n" +
//                    "        {\n" +
//                    "            \"AppPlayerId\": \"88664682300309916\",\n" +
//                    "            \"UserPlayerId\": \"88664645396726252\"\n" +
//                    "        },\n" +
//                    "        {\n" +
//                    "            \"AppPlayerId\": \"88664650026189068\",\n" +
//                    "            \"UserPlayerId\": \"88664633946276262\"\n" +
//                    "        },\n" +
//                    "        {\n" +
//                    "            \"AppPlayerId\": \"88664632768725404\",\n" +
//                    "            \"UserPlayerId\": \"88664652021629211\"\n" +
//                    "        },\n" +
//                    "        {\n" +
//                    "            \"AppPlayerId\": \"88664633373753761\",\n" +
//                    "            \"UserPlayerId\": \"88664654018117925\"\n" +
//                    "        },\n" +
//                    "        {\n" +
//                    "            \"AppPlayerId\": \"88664647883948288\",\n" +
//                    "            \"UserPlayerId\": \"88664674284994911\"\n" +
//                    "        },\n" +
//                    "        {\n" +
//                    "            \"AppPlayerId\": \"88664644824203751\",\n" +
//                    "            \"UserPlayerId\": \"88664680451670413\"\n" +
//                    "        },\n" +
//                    "        {\n" +
//                    "            \"AppPlayerId\": \"88664632768725404\",\n" +
//                    "            \"UserPlayerId\": \"88664650664771857\"\n" +
//                    "        },\n" +
//                    "        {\n" +
//                    "            \"AppPlayerId\": \"88664681073475986\",\n" +
//                    "            \"UserPlayerId\": \"88664642910552541\"\n" +
//                    "        },\n" +
//                    "        {\n" +
//                    "            \"AppPlayerId\": \"88664645986025969\",\n" +
//                    "            \"UserPlayerId\": \"88664674939306340\"\n" +
//                    "        },\n" +
//                    "        {\n" +
//                    "            \"AppPlayerId\": \"88664645396726252\",\n" +
//                    "            \"UserPlayerId\": \"88664682300309916\"\n" +
//                    "        },\n" +
//                    "        {\n" +
//                    "            \"AppPlayerId\": \"88664646607831542\",\n" +
//                    "            \"UserPlayerId\": \"88664679175553409\"\n" +
//                    "        },\n" +
//                    "        {\n" +
//                    "            \"AppPlayerId\": \"88664641177256403\",\n" +
//                    "            \"UserPlayerId\": \"88664682922115489\"\n" +
//                    "        },\n" +
//                    "        {\n" +
//                    "            \"AppPlayerId\": \"88664682922115489\",\n" +
//                    "            \"UserPlayerId\": \"88664641177256403\"\n" +
//                    "        },\n" +
//                    "        {\n" +
//                    "            \"AppPlayerId\": \"88664680451670413\",\n" +
//                    "            \"UserPlayerId\": \"88664644824203751\"\n" +
//                    "        },\n" +
//                    "        {\n" +
//                    "            \"AppPlayerId\": \"88664652021629211\",\n" +
//                    "            \"UserPlayerId\": \"88664632768725404\"\n" +
//                    "        },\n" +
//                    "        {\n" +
//                    "            \"AppPlayerId\": \"88664681645998487\",\n" +
//                    "            \"UserPlayerId\": \"88664647212859899\"\n" +
//                    "        }\n" +
//                    "    ],\n" +
//                    "    \"Players\": null,\n" +
//                    "    \"Result\": 0,\n" +
//                    "    \"TeamPoints\": 10\n" +
//                    "}"));
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        List<String> myPlayerIds = new ArrayList<String>(){{
//            add("88664647212859899");add("88664632768725404");add("88664644824203751");add("88664638362878404");add("88664637316399551");
//        }};
//        List<String> aiPlayerIds = new ArrayList<String>(){{
//            add("88664681645998487");add("88664652021629211");add("88664680451670413");add("88664665582862676");add("88664662621684037");
//        }};
//        StorageManager manager = new StorageManager();
//        for(String myPlayerId : myPlayerIds)
//        {
//            details.getPlayers().add(manager.GetPlayer(myPlayerId));
//        }
//        for(String aiPlayerId : aiPlayerIds)
//        {
//            details.getAppPlayers().add(manager.GetPlayer(aiPlayerId));
//        }
//
//        return details;

        return mDetails;
    }

    private void fetchMatchDetails(String userId, String matchId) {
        Http http = new Http();
        mDialog = new ProgressDialog(this);
        mDialog.setMessage("Fetching match details");
        mDialog.show();
        http.get(Urls.Pick5Urls.getMatchesDetailsUrl(userId, matchId), new HashMap<String, String>(), new APCallback() {
            @Override
            public void success(JSONObject result) {
                mDialog.dismiss();
                if (result.optJSONObject("Error") != null) {
                    SnackBarManager.showError(result.optJSONObject("Error").optString("ErrorMessage"), Pick5MatchActivity.this);
                    return;
                }
                mDetails = new Pick5MatchDetails(result);
                mDetails = Pick5MatchActivity.this.getMatchDetails();
                setupFragment();
            }

            @Override
            public void failure(Exception e) {
                mDialog.dismiss();
                SnackBarManager.showError("Something went wrong.", Pick5MatchActivity.this);
            }
        });
    }

    @OnClick(R.id.pick5_next_match)
    public void onNextMatchClick() {
        StorageManager manager = new StorageManager();
        if(mDetails == null)
            return;
        String nextMatchId = manager.getNextMatchId(mDetails.getMatchDetails().getId());
        if (nextMatchId == null) {
            SnackBarManager.showMessage("No more matches", this);
            return;
        }
        enterAnimation = R.anim.slide_in_right;
        exitAnimation = R.anim.slide_out_left;
        fetchMatchDetails(mUserId, nextMatchId);
    }

    @OnClick(R.id.pick5_previous_match)
    public void onPreviousMatchClick() {
        StorageManager manager = new StorageManager();
        if(mDetails == null)
            return;
        String previousMatchId = manager.getPreviousMatchId(mDetails.getMatchDetails().getId());
        if (previousMatchId == null) {
            SnackBarManager.showMessage("No more matches", this);
            return;
        }
        enterAnimation = R.anim.slide_in_left;
        exitAnimation = R.anim.slide_out_right;
        fetchMatchDetails(mUserId, previousMatchId);
    }

    // Private class for gestures
    private class SwipeGestureDetector
            extends GestureDetector.SimpleOnGestureListener {
        // Swipe properties, you can change it to make the swipe
        // longer or shorter and speed
        private static final int SWIPE_MIN_DISTANCE = 120;
        private static final int SWIPE_MAX_OFF_PATH = 200;
        private static final int SWIPE_THRESHOLD_VELOCITY = 200;

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2,
                               float velocityX, float velocityY) {
            try {
                float diffAbs = Math.abs(e1.getY() - e2.getY());
                float diff = e1.getX() - e2.getX();

                if (diffAbs > SWIPE_MAX_OFF_PATH)
                    return false;

                // Left swipe
                if (diff > SWIPE_MIN_DISTANCE
                        && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    Pick5MatchActivity.this.onLeftSwipe();

                    // Right swipe
                } else if (-diff > SWIPE_MIN_DISTANCE
                        && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    Pick5MatchActivity.this.onRightSwipe();
                }
            } catch (Exception e) {

            }
            return false;
        }
    }
}
