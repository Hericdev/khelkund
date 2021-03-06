package com.appacitive.khelkund.adapters;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.appacitive.khelkund.R;
import com.appacitive.khelkund.infra.BusProvider;
import com.appacitive.khelkund.infra.KhelkundApplication;
import com.appacitive.khelkund.model.Player;
import com.appacitive.khelkund.model.PlayerType;
import com.appacitive.khelkund.infra.TeamHelper;
import com.appacitive.khelkund.model.events.EmptyPlayerCardClickedEvent;
import com.appacitive.khelkund.model.events.FilledPlayerCardClickedEvent;
import com.appacitive.khelkund.model.viewholders.EmptyCardViewHolder;
import com.appacitive.khelkund.model.viewholders.FilledCardViewHolder;
import com.appacitive.khelkund.model.viewholders.PlayerCardViewHolder;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by sathley on 3/26/2015.
 */
public class PlayerCardAdapter extends RecyclerView.Adapter<PlayerCardViewHolder> {
    private int mCount;
    private List<Player> mPlayers;
    private int mPlayerTypeDrawableId;
    private PlayerType mPlayerType;
    private String mCaptainId;


    public PlayerCardAdapter(List<Player> players, int count, int playerTypeDrawableId, PlayerType playerType, String captainId) {
//        this.mCount = count;
//        this.mPlayers = players;
//        this.mPlayerTypeDrawableId = playerTypeDrawableId;
//        this.mPlayerType = playerType;
//        this.mCaptainId = captainId;
        resetDetails(players, count, playerTypeDrawableId, playerType, captainId);
    }

    public void resetDetails(List<Player> players, int count, int playerTypeDrawableId, PlayerType playerType, String captainId) {
        this.mCount = count;
        this.mPlayers = players;
        this.mPlayerTypeDrawableId = playerTypeDrawableId;
        this.mPlayerType = playerType;
        this.mCaptainId = captainId;
    }

    @Override
    public PlayerCardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        PlayerCardViewHolder holder = null;
        switch (viewType) {
            case 0: {
                View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_player_card_empty, parent, false);
                holder = new EmptyCardViewHolder(itemView);
                break;
            }

            case 1: {
                View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_player_card_filled, parent, false);
                holder = new FilledCardViewHolder(itemView);
                break;
            }
        }

        return holder;
    }

    private View.OnClickListener autoSelectClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

        }
    };

    @Override
    public void onBindViewHolder(PlayerCardViewHolder holder, final int position) {
        int viewType = getItemViewType(position);
        final PlayerType playerType = this.mPlayerType;
        switch (viewType) {
            case 0: {
                CardView cardView = ((EmptyCardViewHolder) holder).cvEmptyCard;
                ImageView ivPlayerType = (ImageView) cardView.findViewById(R.id.iv_player_type);
                Picasso
                        .with(KhelkundApplication.getAppContext())
                        .load(mPlayerTypeDrawableId)
                        .into(ivPlayerType);
                cardView.setOnClickListener(null);
                cardView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        EmptyPlayerCardClickedEvent event = new EmptyPlayerCardClickedEvent();
                        event.playerType = playerType;
                        BusProvider.getInstance().post(event);
                    }
                });

                return;
            }

            case 1: {
                final Player player = mPlayers.get(position);
                ((FilledCardViewHolder) holder).PlayerId = player.getId();
                CardView cardView = ((FilledCardViewHolder) holder).cvFilledCard;
                TextView name = (TextView) cardView.findViewById(R.id.tv_card_name);
                name.setText(player.getDisplayName());
                ImageView star = (ImageView) cardView.findViewById(R.id.card_star);
                if (mCaptainId != null) {
                    if (mCaptainId.equals(player.getId()))
                        star.setVisibility(View.VISIBLE);
                    else star.setVisibility(View.INVISIBLE);
                } else star.setVisibility(View.INVISIBLE);

                cardView.setCardBackgroundColor(KhelkundApplication.getAppContext().getResources().getColor(TeamHelper.getTeamColor(player.getShortTeamName())));
                ImageView ivPlayerPhoto = (ImageView) cardView.findViewById(R.id.iv_player_photo);
                Picasso
                        .with(KhelkundApplication.getAppContext())
                        .load(player.getImageUrl())
                        .placeholder(R.drawable.demo)
                        .into(ivPlayerPhoto);
                cardView.setOnClickListener(null);
                cardView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        FilledPlayerCardClickedEvent event = new FilledPlayerCardClickedEvent();
                        event.playerType = playerType;
                        event.playerId = player.getId();
                        BusProvider.getInstance().post(event);
                    }
                });

                return;
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        // 0 represents empty card
        // 1 represents filled card
        if (position >= mPlayers.size()) {
            return 0;
        }
        return 1;
    }

    @Override
    public int getItemCount() {
        return mCount;
    }
}
