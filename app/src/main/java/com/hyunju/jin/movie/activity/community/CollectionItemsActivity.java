package com.hyunju.jin.movie.activity.community;

import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.hyunju.jin.movie.R;
import com.hyunju.jin.movie.activity.SuperActivity;
import com.hyunju.jin.movie.activity.gallery.ItemDecorationGalleryColumns;
import com.hyunju.jin.movie.adapter.community.CollectionItemAdapter;
import com.hyunju.jin.movie.datamodel.Movie;
import com.hyunju.jin.movie.datamodel.MovieCollection;
import com.hyunju.jin.movie.network.MovieService;
import com.hyunju.jin.movie.network.RetrofitClient;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 *
 */
public class CollectionItemsActivity extends SuperActivity {

    public static final String DATA_KEY_COLLECTION = "collection";

    private MovieCollection movieCollection;

    @BindView(R.id.tv_collection_title) TextView tv_collection_title;
    @BindView(R.id.tv_collection_item_count) TextView tv_collection_item_count;

    // 리스트
    @BindView(R.id.recycler_collection_item) RecyclerView recycler_collection_item;
    ArrayList<Movie> collectionItemList;
    CollectionItemAdapter collectionItemAdapter;

    private MovieService movieService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection_items);
        ButterKnife.bind(this); // ButterKnife 라이브러리를 이용해 뷰를 참조한다. ?

        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            movieCollection = (MovieCollection) bundle.getSerializable(DATA_KEY_COLLECTION);
        }


        movieService = RetrofitClient.getMovieService();
        collectionItemList = new ArrayList<Movie>();
        collectionItemAdapter = new CollectionItemAdapter(getContext(), collectionItemList);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 3);
        recycler_collection_item.setLayoutManager(gridLayoutManager);
        recycler_collection_item.addItemDecoration(new ItemDecorationGalleryColumns(1, gridLayoutManager.getSpanCount()));
        recycler_collection_item.setAdapter(collectionItemAdapter);

        tv_collection_title.setText(movieCollection.getCollectionTitle());
        tv_collection_item_count.setText(movieCollection.getCollectionItemCount() + " movies");

        HashMap<String, String> data_movieCollectionItemGET = new HashMap<String, String>();
        data_movieCollectionItemGET.put("collectionCode", movieCollection.getCollectionCode()+"");
        Call<ArrayList<Movie>> call_movieCollectionItemGET = movieService.getMovieList("movieCollectionItemGET", data_movieCollectionItemGET);
        call_movieCollectionItemGET.enqueue(new Callback<ArrayList<Movie>>() {
            @Override
            public void onResponse(Call<ArrayList<Movie>> call, Response<ArrayList<Movie>> response) {
                if(response.isSuccessful()){
                    collectionItemList.clear();
                    collectionItemList.addAll(response.body());
                    collectionItemAdapter.notifyDataSetChanged();
                    return;
                }
                Log.e(TAG, "컬렉션 아이템 조회 실패");
                Toast.makeText(getContext(), "다시 시도해주세요.", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onFailure(Call<ArrayList<Movie>> call, Throwable t) {
                Log.e(TAG, "컬렉션 아이템 조회 실패 (onFailure)");
                Toast.makeText(getContext(), "다시 시도해주세요.", Toast.LENGTH_SHORT).show();

            }
        });

    }

}
