package com.rks.musicx.ui.adapters;

import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.rks.musicx.R;
import com.rks.musicx.data.model.Artist;
import com.rks.musicx.data.network.ArtistArtwork;
import com.rks.musicx.misc.utils.ArtworkUtils;
import com.rks.musicx.misc.utils.Extras;
import com.rks.musicx.misc.utils.Helper;
import com.rks.musicx.misc.utils.palette;
import com.rks.musicx.misc.widgets.CircleImageView;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


/*
 * Created by Coolalien on 6/28/2016.
 */

public class ArtistListAdapter extends BaseRecyclerViewAdapter<Artist, ArtistListAdapter.ArtistViewHolder> implements FastScrollRecyclerView.SectionedAdapter {

    private int layoutID;
    private Interpolator interpolator = new LinearInterpolator();
    private int lastpos = -1;
    private int duration = 300;
    private ValueAnimator colorAnimation;

    public ArtistListAdapter(@NonNull Context context) {
        super(context);
    }

    public void setLayoutID(int layoutID) {
        this.layoutID = layoutID;
    }

    @Override
    public ArtistViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(layoutID, parent, false);
        return new ArtistViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ArtistViewHolder holder, int position) {
        Artist artists = getItem(position);
        if (layoutID == R.layout.item_grid_view) {
            int pos = holder.getAdapterPosition();
            if (lastpos < pos) {
                for (Animator animator : Helper.getAnimator(holder.backgroundColor)) {
                    animator.setDuration(duration).start();
                    animator.setInterpolator(interpolator);
                }
            }
            holder.ArtistName.setText(getContext().getResources().getQuantityString(R.plurals.albums_count, artists.getAlbumCount(), artists.getAlbumCount()));
            holder.AlbumCount.setText(artists.getName());
            if (!Extras.getInstance().saveData()) {
                ArtistArtwork artistArtwork = new ArtistArtwork(getContext(), artists.getName());
                artistArtwork.execute();
            }
            String artistImagePath = new Helper(getContext()).loadArtistImage(artists.getName());
            File file = new File(artistImagePath);
            if (file.exists()) {
                ArtworkUtils.ArtworkLoaderPalette(getContext(), file.getAbsolutePath(), holder.ArtistsArtwork, new palette() {
                    @Override
                    public void palettework(Palette palette) {
                        final int[] colors = Helper.getAvailableColor(getContext(),palette);
                        holder.backgroundColor.setBackgroundColor(colors[0]);
                        holder.ArtistName.setTextColor(ContextCompat.getColor(getContext(), R.color.text_transparent));
                        holder.AlbumCount.setTextColor(ContextCompat.getColor(getContext(), R.color.text_transparent2));
                        animateViews(holder, colors[0]);
                    }
                });
            } else {
                holder.ArtistsArtwork.setImageResource(R.mipmap.ic_launcher);
            }
            holder.menu.setVisibility(View.GONE);
        }
        if (layoutID == R.layout.item_list_view) {
            holder.ArtistListName.setText(artists.getName());
            holder.ArtistListAlbumCount.setText(getContext().getResources().getQuantityString(R.plurals.albums_count, artists.getAlbumCount(), artists.getAlbumCount()));
            if (!Extras.getInstance().saveData()) {
                ArtistArtwork artistArtwork = new ArtistArtwork(getContext(), artists.getName());
                artistArtwork.execute();
            }
            String artistImagePath = new Helper(getContext()).loadArtistImage(artists.getName());
            File file = new File(artistImagePath);
            if (file.exists()) {
                ArtworkUtils.ArtworkNetworkLoader(getContext(), artists.getName(), file.getAbsolutePath(), holder.ArtistListArtwork);
            } else {
                holder.ArtistListArtwork.setImageResource(R.mipmap.ic_launcher);
            }
            if (Extras.getInstance().mPreferences.getBoolean("dark_theme", false)) {
                holder.ArtistListName.setTextColor(Color.WHITE);
                holder.ArtistListAlbumCount.setTextColor(ContextCompat.getColor(getContext(), R.color.darkthemeTextColor));
            }
        }

    }

    @NonNull
    @Override
    public String getSectionName(int position) {
        return getItem(position).getName().substring(0, 1);
    }

    public void setFilter(List<Artist> artistList) {
        data = new ArrayList<>();
        data.addAll(artistList);
        notifyDataSetChanged();
    }

    private void animateViews(ArtistViewHolder artistViewHolder, int colorBg) {
        colorAnimation = setAnimator(0xffe5e5e5,
                colorBg);
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                artistViewHolder.backgroundColor.setBackgroundColor((Integer) animator.getAnimatedValue());
            }

        });
        colorAnimation.start();
    }

    private ValueAnimator setAnimator(int colorFrom, int colorTo) {
        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
        colorAnimation.setDuration(duration);
        return colorAnimation;
    }

    public class ArtistViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView ArtistName, ArtistListAlbumCount, AlbumCount, ArtistListName;
        private ImageView ArtistsArtwork;
        private LinearLayout backgroundColor;
        private CircleImageView ArtistListArtwork;
        private ImageButton menu;

        public ArtistViewHolder(View itemView) {
            super(itemView);
            if (layoutID == R.layout.item_grid_view) {
                ArtistsArtwork = (ImageView) itemView.findViewById(R.id.album_artwork);
                AlbumCount = (TextView) itemView.findViewById(R.id.album_name);
                ArtistName = (TextView) itemView.findViewById(R.id.artist_name);
                menu = (ImageButton) itemView.findViewById(R.id.menu_button);
                backgroundColor = (LinearLayout) itemView.findViewById(R.id.backgroundColor);
                ArtistsArtwork.setOnClickListener(this);
                itemView.setOnClickListener(this);
                itemView.findViewById(R.id.item_view).setOnClickListener(this);
            }
            if (layoutID == R.layout.item_list_view) {
                ArtistListArtwork = (CircleImageView) itemView.findViewById(R.id.album_listartwork);
                ArtistListName = (TextView) itemView.findViewById(R.id.listalbumname);
                ArtistListAlbumCount = (TextView) itemView.findViewById(R.id.listartistname);
                ArtistListArtwork.setOnClickListener(this);
                itemView.findViewById(R.id.item_view).setOnClickListener(this);
                itemView.setOnClickListener(this);
            }
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            triggerOnItemClickListener(position, v);
        }
    }
}
