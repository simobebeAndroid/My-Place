package com.simiomobile.myplace.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.simiomobile.myplace.R;
import com.simiomobile.myplace.realm.Place;

import java.util.List;

import jp.wasabeef.glide.transformations.CropCircleTransformation;

/**
 * Created by Aor__Feyverly on 21/5/2560.
 */

public class PlacesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    final Context context;
    private OnItemClickListener onItemClickListener;
    private List<Place> placeList;

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_FOOTER = 1;
    private static final int TYPE_ITEM = 2;

    public PlacesAdapter(Context context, List<Place> placeList) {
        this.context = context;
        this.placeList = placeList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_FOOTER) {
            //Inflating footer view
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_footer, parent, false);
            return new FooterViewHolder(itemView);
        }
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_place, parent, false);
        return new CardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        if (viewHolder instanceof FooterViewHolder) {

        } else {
            // get the article
            final Place place = getItem(position);
            // cast the generic view holder to our specific oneo
            final CardViewHolder holder = (CardViewHolder) viewHolder;

            // set the title and the snippet
            holder.textAddress.setText(place.getName() + " " + place.getAddress());
            String url = place.getUrlLink();
            if (url.length() == 0) url = "URL Link.";
            holder.textUrl.setText(url);


            String urlImage = "http://maps.google.com/maps/api/staticmap?center=" + place.getLatitude() + ",+" + place.getLongitude() + "&zoom=16&size=80x80&markers=color:red%7Clabel:C%7C" + place.getLatitude() + ",+" + place.getLongitude() + "&sensor=false";
            Glide.with(context)
                    .load(urlImage)
                    .bitmapTransform(new CropCircleTransformation(context))
                    .into(holder.imagePlace);

            boolean isFavorite = place.isFavorite();
            if (isFavorite) {
                holder.imageFavorite.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.icon_favorite_active));
            } else {
                holder.imageFavorite.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.icon_favorite));
            }

            setupItemClick(holder, position);
        }

    }

    private Place getItem(int position) {
        if (placeList != null) {
            return placeList.get(position);
        } else {
            return null;
        }
    }

    public void updateItem(Place place){
        for (int i = 0; i < placeList.size();i++){
            if (place.getId().equals(placeList.get(i).getId())){
                Place place1 = new Place();
                place1.setId(place.getId());
                place1.setName(place.getName());
                place1.setAddress(place.getAddress());
                place1.setUrlLink(place.getUrlLink());
                place1.setLatitude(place.getLatitude());
                place1.setLongitude(place.getLongitude());
                place1.setMarkerLink(place.getMarkerLink());
                place1.setFavorite(place.isFavorite());
                placeList.set(i,place1);
                notifyDataSetChanged();
                break;
            }
        }
    }

    @Override
    public int getItemCount() {
        if (placeList != null) {
            return placeList.size() + 1;
        }
        return 0;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == getItemCount() - 1) {
            return TYPE_FOOTER;
        }
        return TYPE_ITEM;
    }

    private class CardViewHolder extends RecyclerView.ViewHolder {

        public CardView card;
        public ImageView imagePlace;
        public TextView textAddress;
        public TextView textUrl;
        public ImageView imageFavorite;


        public CardViewHolder(View itemView) {
            super(itemView);
            card = (CardView) itemView.findViewById(R.id.app_card_place);
            imagePlace = (ImageView) itemView.findViewById(R.id.app_iv_place);
            textAddress = (TextView) itemView.findViewById(R.id.app_tv_address);
            textUrl = (TextView) itemView.findViewById(R.id.app_tv_url);
            imageFavorite = (ImageView) itemView.findViewById(R.id.app_iv_favorite);

        }
    }

    private class FooterViewHolder extends RecyclerView.ViewHolder {

        public FooterViewHolder(View view) {
            super(view);

        }
    }

    private void setupItemClick(final CardViewHolder cardViewHolder, final int position) {
        cardViewHolder.card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onItemClickListener != null) {
                    onItemClickListener.onPlaceClick(getItem(position));
                }
            }
        });
        cardViewHolder.imageFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onItemClickListener != null) {
                    onItemClickListener.onFavoriteClick(getItem(position),position);
                }
            }
        });
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onPlaceClick(Place place);

        void onFavoriteClick(Place place,int position);
    }
}
