package com.hyunju.jin.movie.adapter.recommend;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.hyunju.jin.movie.R;
import com.hyunju.jin.movie.activity.recommend.RecommendMovieListener;
import com.hyunju.jin.movie.datamodel.Movie;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 추천영화 목록을 관리하는 Adapter
 */
public class RecommendMovieAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    // ViewType 정의
    private final int VIEW_TYPE_LOADING = 0;    // 다음페이지 로드중임을 나타내는 View
    private final int VIEW_TYPE_ITEM = 1;       // 하나의 영화를 나타내는 View

    private boolean isLoadingAdded = false;     // 다음 페이지를 로드중이라면 true

    private Context context;
    private ArrayList<Movie> recommend_movies;  // 추천 영화 목록
    private LayoutInflater layoutInflater;
    private RecommendMovieListener listener;

    public RecommendMovieAdapter(Context context, RecommendMovieListener listener){
        this.context = context;
        this.recommend_movies = new ArrayList<Movie>();
        this.layoutInflater = LayoutInflater.from(context);
        this.listener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType){
            case VIEW_TYPE_LOADING: // 다음페이지 로드중임을 나타내는 View
                View loading = layoutInflater.inflate(R.layout.item_loading, parent, false);
                LoadingViewHolder loadingViewHolder = new LoadingViewHolder(loading);
                return loadingViewHolder;

            case VIEW_TYPE_ITEM: // 하나의 영화를 나타내는 View
                View item = layoutInflater.inflate(R.layout.item_recommend_movie, parent, false);
                RecommendMovieViewHolder recommendMovieViewHolder = new RecommendMovieViewHolder(item);
                return recommendMovieViewHolder;
                default:
                    return null;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        switch (getItemViewType(position)){
            case VIEW_TYPE_LOADING:
                // 해야할 일 없음
                break;
            case VIEW_TYPE_ITEM:
                final RecommendMovieViewHolder recommendMovieVH = (RecommendMovieViewHolder) holder;
                Movie movie = recommend_movies.get(position);
                // 영화포스터 로드
                if(StringUtils.isNotEmpty(movie.getPoster())) {
                    Glide.with(context).load(movie.getPoster()).listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            // 영화 포스터 로드에 실패했을 경우
                            recommendMovieVH.img_movie_poster.setImageDrawable(null); //  포스터를 보여줄 ImageView 의 리소스 해제
                            recommendMovieVH.movie_progress.setVisibility(View.GONE); // 포스터 로드중임을 나타내는 프로그래스바 감춤
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            // 영화 포스터 로드가 완료되면
                            recommendMovieVH.movie_progress.setVisibility(View.GONE); // 포스터 로드중임을 나타내는 프로그래스바 감춤
                            return false;
                        }
                    }).into(recommendMovieVH.img_movie_poster);
                }else{
                    // 영화포스터가 없다면
                    recommendMovieVH.img_movie_poster.setImageDrawable(null); // 이거 말고 뭔가 포스터가 없다는 표시를 하는게 맞다고 봄
                    recommendMovieVH.movie_progress.setVisibility(View.GONE); // 포스터 로드중임을 나타내는 프로그래스바 감춤
                }

                recommendMovieVH.position = position;
                recommendMovieVH.tv_movie_title.setText(movie.getMovieTitle());
                recommendMovieVH.tv_expected_rating.setText(movie.getRatingValue()+"");
                if(movie.isExistWantToWatch()){
                    recommendMovieVH.img_want_to_watch.setImageResource(R.drawable.ic_menu_recommend_on);
                    recommendMovieVH.tv_want_to_watch.setTextColor(ResourcesCompat.getColor(context.getResources(), R.color.pointColor, null));
                }else {
                    recommendMovieVH.img_want_to_watch.setImageResource(R.drawable.ic_heart);
                    recommendMovieVH.tv_want_to_watch.setTextColor(ResourcesCompat.getColor(context.getResources(), R.color.defaultTextColor, null));
                }

                recommendMovieVH.tv_movie_desc.setText(movie.getGenre());

                // 개봉년도 | 제작국가 형태의 텍스트를 만든다.
                // 개봉년도를 구한다.
                String year = StringUtils.isNotEmpty(movie.getReleaseDate()) ? movie.getReleaseDate().split("\\.")[0] : "";
                //
                String productionCountry = movie.getProductionCountry();
                String concat = StringUtils.isNotEmpty(year) ? year + " | " + productionCountry : productionCountry;
                recommendMovieVH.tv_movie_year.setText(concat);

                break;
                default:
                    break;
        }
    }

    @Override
    public int getItemCount() {
        return recommend_movies == null ? 0 : recommend_movies.size();
    }

    @Override
    public int getItemViewType(int position) {
        return (position == recommend_movies.size() - 1 && isLoadingAdded) ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }

    /*
        추천영화 리스트를 관리하는 메서드
     */

    public void add(Movie movie) {
        recommend_movies.add(movie);

        // (참고) http://gogorchg.tistory.com/entry/Android-RecyclerView-Adpater-Refresh
        // RecyclerView 에 연결된 데이터가 업데이트 되면, 다양한 방법으로 이를 알릴 수 있다.
        notifyItemInserted(recommend_movies.size() - 1); // 위치를 -1 로 두는 이유는,
    }

    /**
     * ViewHolder 정의
     */

    public class RecommendMovieViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.img_movie_poster) ImageView img_movie_poster;
        @BindView(R.id.movie_progress) ProgressBar movie_progress; // 영화 포스터가 로드될때까지 표시되는 프로그래스바
        @BindView(R.id.tv_movie_year) TextView tv_movie_year; // 개봉연도 및 제작국가 ex) 2018 | 한국
        @BindView(R.id.tv_movie_title) TextView tv_movie_title;
        @BindView(R.id.tv_movie_desc) TextView tv_movie_desc;
        @BindView(R.id.tv_expected_rating) TextView tv_expected_rating; // 예상평점 ex) 3.5

        @BindView(R.id.img_movie_trailer) ImageView img_movie_trailer; // [예고편재생] 아이콘
        @BindView(R.id.tv_movie_trailer) TextView tv_movie_trailer; // [예고편재생] 텍스트
        @BindView(R.id.img_want_to_watch) ImageView img_want_to_watch; // [보고싶어요] 아이콘
        @BindView(R.id.tv_want_to_watch) TextView tv_want_to_watch; // [보고싶어요] 텍스트

        // 평가 -- 이미 본 영화가 추천됬을 수도 있으니 바로 평가하고 저장하도록 해.

        // 태그정보
        // 시청가능여부

        // 깔끔하게 잘 보여질 기능과 숨겨져있을 기능 나누기.

        int position;
        public RecommendMovieViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            this.position = 0;
        }

        /**
         * 영화 상세정보 페이지로 이동한다.
         */
        @OnClick(R.id.img_movie_poster)
        public void showMovieDetail() {

        }

        /**
         * [예고편재생] 클릭 시 영화 예고편을 재생한다.
         */
        @OnClick({R.id.img_movie_trailer, R.id.tv_movie_trailer})
        public void playMovieTrailer() {
            // 애니메이션 넣고 싶다.
            Toast.makeText(context, "예고편을 재생합니다.", Toast.LENGTH_SHORT).show();
        }

        /**
         * 영화를 '보고싶어요' 목록에 추가하거나 목록에서 삭제하는 메서드.
         */
        @OnClick({R.id.img_want_to_watch, R.id.tv_want_to_watch})
        public void addWantToWatch() {
            listener.addWantToWatch(position);  // 그냥 포지션 넣어도 되나?
        }
    }

    public class LoadingViewHolder extends RecyclerView.ViewHolder {

        public LoadingViewHolder(View itemView) {
            super(itemView);
        }
    }

    //////////////////////////////////////////////////////////

    public void addAll(List<Movie> addList) {
        for (Movie mc : addList) {
            add(mc);
        }
    }

    public void remove(Movie movie) {
        int position = recommend_movies.indexOf(movie);
        if (position > -1) {
            recommend_movies.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void clear() {
        isLoadingAdded = false;
        while (getItemCount() > 0) {
            remove(getItem(0));
        }
    }

    public boolean isEmpty() {
        return getItemCount() == 0;
    }

    /*
       페이징 처리와 관련된 메서드
    */

    public void addLoadingFooter() {
        isLoadingAdded = true;
        add(new Movie());
    }

    public void removeLoadingFooter() {
        isLoadingAdded = false;

        int position = recommend_movies.size() - 1;
        Movie item = getItem(position);

        if (item != null) {
            recommend_movies.remove(position);
            notifyItemRemoved(position);
        }
    }

    public Movie getItem(int position) {
        return recommend_movies.get(position);
    }

}
