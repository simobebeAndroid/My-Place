package com.simiomobile.myplace.ui.fragment.main;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.simiomobile.myplace.R;
import com.simiomobile.myplace.adapter.PlacesAdapter;
import com.simiomobile.myplace.controller.BusController;
import com.simiomobile.myplace.controller.RealmController;
import com.simiomobile.myplace.model.FavoriteEventBus;
import com.simiomobile.myplace.realm.Place;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

public class FavoriteFragment extends Fragment {

    private PlacesAdapter mAdapter;
    private RecyclerView rvCard;
    private List<Place> mPlaceList;

    public FavoriteFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment FavoriteFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FavoriteFragment newInstance() {
        FavoriteFragment fragment = new FavoriteFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorite, container, false);
        // Inflate the layout for this fragment
        initInstant(view);
        initialData();
        initialListener();
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onResume() {
        super.onResume();
        BusController.getInstance().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        BusController.getInstance().unregister(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private void initInstant(View view) {
        rvCard = (RecyclerView) view
                .findViewById(R.id.app_rv_place);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rvCard.setLayoutManager(layoutManager);
    }

    private void initialData() {
        mPlaceList = new ArrayList<>();
        mAdapter = new PlacesAdapter(getContext(), mPlaceList);
        rvCard.setAdapter(mAdapter);
        RealmController.with(this).refresh();
        setRealmAdapter(RealmController.with(this).getFavoritePlaces());
    }

    private void initialListener() {
        mAdapter.setOnItemClickListener(new PlacesAdapter.OnItemClickListener() {
            @Override
            public void onPlaceClick(Place place) {

            }

            @Override
            public void onFavoriteClick(Place place,int position) {
                RealmController.getInstance().getRealm().beginTransaction();
                place.setFavorite(!place.isFavorite());
                RealmController.getInstance().getRealm().commitTransaction();
                mPlaceList.set(position,place);
                mAdapter.notifyDataSetChanged();
                RealmController.getInstance().savePlace(place);
                BusController.getInstance().postOnMain(new FavoriteEventBus(true,place));
            }
        });
    }

    public void setRealmAdapter(List<Place> places) {
        mPlaceList.clear();
        mPlaceList.addAll(places);
        mAdapter.notifyDataSetChanged();
    }
    /**
     * receiver of list place  changed
     **/
    @Subscribe
    public void onListPlaceChange(FavoriteEventBus result) {
        if (result != null && result.isRefresh()) {
            setRealmAdapter(RealmController.with(this).getFavoritePlaces());
        }
    }
}
