package com.hyunju.jin.movie.activity.user;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

import com.adroitandroid.chipcloud.ChipCloud;
import com.adroitandroid.chipcloud.ChipListener;
import com.hyunju.jin.movie.R;
import com.hyunju.jin.movie.activity.SuperActivity;
import com.hyunju.jin.movie.activity.recommend.MovieRatingInsertActivity;
import com.hyunju.jin.movie.network.ResponseData;
import com.hyunju.jin.movie.network.RetrofitClient;
import com.hyunju.jin.movie.network.UserService;
import com.hyunju.jin.movie.utils.DateFormatUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import belka.us.androidtoggleswitch.widgets.ToggleSwitch;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 회원가입 직후 사용자 성별, 연령대, 영화 선호도 등의 추가정보를 입력받는 화면.
 * 영화는 최소 15개의 평점을 입력받는다.
 * [다음에 하기]를 누를 경우, 사용자가 [추천영화]를 보기 위해서는 이 정보를 다시 입력해달라고 요청해야함.
 */
public class AdditionalInfoActivity extends SuperActivity{

    @BindView(R.id.toggle_sex) ToggleSwitch toggle_sex;     // 성별을 선택하는 Toggle
    @BindView(R.id.ed_birth_year) EditText ed_birth_year;   // 나이를 알기 위해 연도를 입력받는 EditText
    @BindView(R.id.spinner_birth_year) Spinner spinner_birth_year;
    ArrayAdapter<String> birthYearAdapter;
    ArrayList<String> birthYears;

    @BindView(R.id.chip_cloud_movie_genres) ChipCloud chip_cloud_movie_genres;
    String[] someStringArray;

    @BindView(R.id.img_next) ImageView img_next;    // 다음 버튼. 사용자가 필수정보를 모두 입력하면 활성화된다.
    boolean isClickableNext;                        // 다음 버튼 활성화 여부
    HashMap<Integer, Integer> selectedMovieGenres;

    UserService userService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_additional_info);
        ButterKnife.bind(this);
        initialize();
    }

    private void initialize(){

        /*// 생년월일 스피너 생성
        birthYears = createBirthYears();
        birthYearAdapter = new ArrayAdapter<>(getContext(), R.layout.support_simple_spinner_dropdown_item, birthYears);
        spinner_birth_year.setAdapter(birthYearAdapter);*/

        userService = RetrofitClient.getUserService();
        selectedMovieGenres = new HashMap<Integer, Integer>();

        isClickableNext = false;
        someStringArray = createMovieGenres();
        new ChipCloud.Configure()
                .chipCloud(chip_cloud_movie_genres)
                .selectedColor(ResourcesCompat.getColor(getResources(), R.color.pointColor, null))
                .selectedFontColor(ResourcesCompat.getColor(getResources(), R.color.backgroundColor, null))
                .deselectedColor(ResourcesCompat.getColor(getResources(), R.color.backgroundLightColor, null))
                .deselectedFontColor(ResourcesCompat.getColor(getResources(), R.color.defaultTextColor, null))
                .selectTransitionMS(500)
                .deselectTransitionMS(250)
                .labels(someStringArray)
                .mode(ChipCloud.Mode.MULTI)
                .allCaps(false)
                .gravity(ChipCloud.Gravity.LEFT)
                .textSize(getResources().getDimensionPixelSize(R.dimen.default_textsize))
                .verticalSpacing(getResources().getDimensionPixelSize(R.dimen.vertical_spacing))
                .minHorizontalSpacing(getResources().getDimensionPixelSize(R.dimen.min_horizontal_spacing))
                .chipListener(new ChipListener() {
                    @Override
                    public void chipSelected(int index) {

                        // selectedMovieGenres.put(장르코드, list에서 인덱스)
                        selectedMovieGenres.put((index+1), index);  // 판타지 장르를 선택한 경우 index 1 이다.
                    }
                    @Override
                    public void chipDeselected(int index) {
                        // selectedMovieGenres.put(장르코드, list에서 인덱스)
                        selectedMovieGenres.remove((index+1));  // 판타지 장르를 선택한 경우 index 1 이다.
                    }
                })
                .build();

        ed_birth_year.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                toggleClickableNext();
            }
        });
    }

    /**
     * 선호 장르를 체크할 수 있도록 전체 영화장르 목록을 리턴한다.
     * @return
     */
    private String[] createMovieGenres(){
        ArrayList<String> list = new ArrayList<String>();

        // 주의!
        // list에 넣는 순서는 서버 DB genre 테이블에 있는 장르 코드 순서와 일치해야함.

        // list.add("테스트용"); // 테스트용 데이터는 넣지 않는다.
        list.add("드라마");    // 여기서부터 장르코드 1 (= 인덱스 +1)
        list.add("판타지");    // 장르코드 2
        list.add("서부");      // 장르코드 3
        list.add("공포");
        list.add("로맨스");
        list.add("모험");
        list.add("스릴러");
        list.add("느와르");
        list.add("컬트");
        list.add("다큐멘터리");
        list.add("코미디");
        list.add("가족");
        list.add("미스터리");
        list.add("전쟁");
        list.add("애니메이션");
        list.add("범죄");
        list.add("뮤지컬");
        list.add("SF");
        list.add("액션");
        list.add("무협");
        list.add("에로");
        list.add("서스펜스");
        list.add("서사");
        list.add("블랙코미디");
        list.add("실험");
        list.add("영화카툰");
        list.add("영화음악");   // 장르코드 27
        list.add("영화패러디포스터"); // 장르코드 28

        return list.toArray(new String[list.size()]);
    }

    /**
     * 올해부터 100년 전까지 생년월일 목록을 생성하여 리턴한다.
     * 현재 사용되지 않음.
     */
    private ArrayList<String> createBirthYears(){

        ArrayList<String> list = new ArrayList<String>();
        Calendar today = Calendar.getInstance();
        int year = Integer.parseInt(DateFormatUtils.getyyyy(today));

        for(int end=(year - 100) ; year >= end; year--){
            list.add(year + "");
        }

        return list;
    }

    /**
     * 필수 입력 정보가 모두 입력되면 다음 버튼을 활성화 시키는 메소드
     */
    private void toggleClickableNext(){
        int sex = toggle_sex.getCheckedTogglePosition();
        String birthYear = ed_birth_year.getText().toString();
        // 연도 확인을 어떻게 할까?
        if(selectedMovieGenres.size() < 0){
            img_next.setImageResource(R.drawable.ic_next);
            isClickableNext = false;

            return;
        }

        img_next.setImageResource(R.drawable.ic_next_on);
        isClickableNext = true;

    }

    @OnClick(R.id.img_next)
    public void complete(){

        if(isClickableNext){

            // 서버에 사용자가 입력한 정보를 저장한다.
            HashMap<String, String> data_additionalInfoUPDATE = new HashMap<String, String>();
            data_additionalInfoUPDATE.put("userCode", loginUser.getUserCode()+"");
            data_additionalInfoUPDATE.put("sex", ( toggle_sex.getCheckedTogglePosition() == 1 ? "F" : "M" ));   // 0: 남자, 1: 여자
            data_additionalInfoUPDATE.put("birthYear", ed_birth_year.getText().toString());
            data_additionalInfoUPDATE.put("favoriteGenre", ed_birth_year.getText().toString());

            Call<ResponseData> call_additionalInfoUPDATE = userService.post("additionalInfoUPDATE", data_additionalInfoUPDATE);
            call_additionalInfoUPDATE.enqueue(new Callback<ResponseData>() {
                @Override
                public void onResponse(Call<ResponseData> call, Response<ResponseData> response) {
                    if(response.isSuccessful()){
                        Log.i(TAG, "getTodayMovieRanking");

                        // 사용자가 선택한 '선호장르' 정보를 담아 영화 평점 입력 액티비티를 호출한다.
                        Intent intent = new Intent(getContext(), MovieRatingInsertActivity.class);
                        intent.putExtra(MovieRatingInsertActivity.KEY_CALL_ACTIVITY, MovieRatingInsertActivity.DATA_CALL_ACTIVITY_SIGN_UP);
                        intent.putExtra(MovieRatingInsertActivity.KEY_TASTE_GENRE, selectedMovieGenres);
                        startActivity(intent);
                        finish();
                        return;
                    }
                    Log.e(TAG, "complete");
                }

                @Override
                public void onFailure(Call<ResponseData> call, Throwable t) {
                    Log.e(TAG, "complete (onFailure)");
                }
            });

        }
    }

}
