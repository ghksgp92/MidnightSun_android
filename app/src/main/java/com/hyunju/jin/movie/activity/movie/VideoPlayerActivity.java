package com.hyunju.jin.movie.activity.movie;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MergingMediaSource;
import com.google.android.exoplayer2.source.SingleSampleMediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.text.Cue;
import com.google.android.exoplayer2.text.TextOutput;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.android.exoplayer2.util.Util;
import com.google.gson.reflect.TypeToken;
import com.hyunju.jin.movie.R;
import com.hyunju.jin.movie.activity.SuperActivity;
import com.hyunju.jin.movie.datamodel.Movie;
import com.hyunju.jin.movie.utils.SharedPreferencesBuilder;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class VideoPlayerActivity extends SuperActivity {

    @BindView(R.id.video_view) PlayerView video_view; // PlayerView는 이전의 SimpleExoPlayerView.
    private SimpleExoPlayer player;

    private boolean playWhenReady = true;   // true면 재생. 단, 버퍼가 충분히 채워졌을때. false면 중지? 일시중지?

    private Movie playingVideo;

    private static final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();   // 적응형 스트리밍(HLS)을 하기 위해 네트워크 대역폭을 예측하는 객체 생성. 다운로드 속도를 기반으로 함.
    private static final long END_TIME = 1000 * 300 ; // 5분. 현재 재생시간이 전체시간-5분 이내면 영화를 다 봤다고 간주한다.

    private DataSource.Factory mediaDataSourceFactory;

    private long mPlaybackPosition;
    private int mCurrentWindow;
    private int mPlaybackState;

    public static final String DATA_KEY_PLAY_MOVIE = "playingVideo";    // 재생할 영화 정보 key
    public static final String DATA_KEY_PLAY_POSITION = "restorePosition";  // 이어보기 시작할 위치 key

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);
        ButterKnife.bind(this);

        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            playingVideo = (Movie) extras.getSerializable(DATA_KEY_PLAY_MOVIE);
            mPlaybackPosition = extras.getLong(DATA_KEY_PLAY_POSITION, 0);
        }

    }

    @Override
    protected void onStart() {
        super.onStart();

        // Util 클래스를 통해 현재 실행중인 소프트웨어의 SDK 버전이 24 이상인지 확인한다.
        if(Util.SDK_INT > 23){  // SDK 24부터는 onStart()에서 초기화를 해줘야하는 이유가?
            initializePlayer();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideSystemUi();
        if( (Util.SDK_INT <= 23 || player == null) ){   // SDK 23 이하에서는 onResume()에서 초기화 해줘야하는 이유는?
            initializePlayer();
        }
    }


    /**
     * 플레이어를 재생가능한 상태로 세팅하고 재생을 시작한다.
     */
    private void initializePlayer(){

        // Exoplayer를 사용하여 HLS 방식으로 영화를 스트리밍한다.

        if(player != null){
            // 다시 동영상을 재생할 경우 재생시간을 조정한다. 5초전으로 설정하는 이유는 로딩하는 도중 소리만 나오고 비디오는 멈춰있는 현상이 있기 때문에
            // 재생시간보다 조금 앞에서 다시 영상을 재생하도록 함.
            mPlaybackPosition = ( (mPlaybackPosition > 5000) ? (mPlaybackPosition - 5000) : 0 );
            player.seekTo(mPlaybackPosition);
            player.setPlayWhenReady(playWhenReady);  // 원래 재생 상태 복원
            return;

        }

        // 1. TrackSelector 생성
        // HLS 는 적응형 스트리밍이기 때문에 네트워크 상태를 감지할 수 있도록 BANDWIDTH_METER 를 이용한다.
        // 네트워크 속도는 다운로드 속도 기준이다.
        TrackSelection.Factory adaptiveTrackSelectionFactory = new AdaptiveTrackSelection.Factory(BANDWIDTH_METER);
        TrackSelector trackSelector = new DefaultTrackSelector(adaptiveTrackSelectionFactory);

        // 2. LoadControl 생성. 버퍼를 관리하는 역할을 한다
        LoadControl loadControl = new DefaultLoadControl();

        // 3. player 생성. DefaultRenderersFactory 는 비디오/오디오/자막 등의 렌더링 동기화를 한다.
        player = ExoPlayerFactory.newSimpleInstance(new DefaultRenderersFactory(this), trackSelector, loadControl);
        video_view.setPlayer(player);

        // Produces DataSource instances through which media data is loaded.
        DataSource.Factory mediaDataSourceFactory = new DefaultDataSourceFactory(this, Util.getUserAgent(this, "player"));

        Format subTitleFormat = Format.createTextSampleFormat(null, MimeTypes.TEXT_VTT, 0, null);
        Uri subTitleUri = Uri.parse("http://183.111.227.218/hls/HarryPotterAndTheSorcerersStone/HarryPotterAndTheSorcerersStone.vtt");
        MediaSource subtitleMediaSource = new SingleSampleMediaSource.Factory(mediaDataSourceFactory).createMediaSource(subTitleUri, subTitleFormat, C.TIME_UNSET);

        // This is the MediaSource representing the media to be played.
        Uri videoUri = Uri.parse("");
        if(playingVideo != null && StringUtils.isNotEmpty(playingVideo.getStreamingFile())){
            videoUri = Uri.parse("http://183.111.227.218/hls/"+playingVideo.getStreamingFile());
        }else{
            Toast.makeText(getContext(), "재생할 수 있는 동영상이 없습니다.", Toast.LENGTH_SHORT).show();
            finish();
        }

        // [킵] URL이 없을 때 처리. E/ExoPlayerImplInternal: Source error.
        // HLS MediaSource 생성. 미디어 로드를 담당하며 읽기 후에 재생할 수 있다(무슨말이야)
        MediaSource videoSource = new HlsMediaSource.Factory(mediaDataSourceFactory)
                .setAllowChunklessPreparation(true) // HLS 세그먼트를 다운로드하지 않는다?
                .createMediaSource(videoUri);
        MediaSource mediaSource = new MergingMediaSource(videoSource, subtitleMediaSource);


        TextOutput textOutput = new TextOutput() {
            @Override
            public void onCues(List<Cue> cues) {
                Log.e(TAG, "자막222");
                if (video_view.getSubtitleView() != null) {
                    Log.e(TAG, "자막");
                    video_view.getSubtitleView() .onCues(cues);
                }
            }
        };

        player.addTextOutput(textOutput);
        player.addListener(new Player.EventListener() {
            @Override
            public void onTimelineChanged(Timeline timeline, Object manifest, int reason) {
                // 소스의 지속 시간이 결정되면 호출
                Log.d(TAG, "onTimelineChanged()");
            }

            @Override
            public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
                //  소스 트랙이 변경되면 호출
                Log.d(TAG, "onTracksChanged()");
            }

            @Override
            public void onLoadingChanged(boolean isLoading) {
                //  소스가 로드를 시작하거나 중지 할 때 호출
                Log.d(TAG, "onLoadingChanged()");
            }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                /*
                플레이어 상태가 변경 될 때 호출
                가능한 플레이어 상태는 유휴, 준비, 버퍼링, 일시 중지, 재생, 중지, 완료, 오류 등입니다.
                 */
                Log.d(TAG, "onPlayerStateChanged() "+playbackState);

                switch (playbackState) {
                    case Player.STATE_IDLE: // 1
                        //mPlaybackState = PlaybackStatus.IDLE;
                        break;
                    case Player.STATE_BUFFERING: // 2
                        //mPlaybackState = PlaybackStatus.LOADING;
                        // 프로그래스바 띄우기
                        break;
                    case Player.STATE_READY: // 3
                        //mPlaybackState = playWhenReady ? PlaybackStatus.PLAYING : PlaybackStatus.PAUSED;
                        // 프로그래스 바 없애기
                        break;
                    case Player.STATE_ENDED: // 4
                        //mPlaybackState = PlaybackStatus.STOPPED;
                        break;
                    default:
                        //mPlaybackState = PlaybackStatus.IDLE;
                        break;
                }
            }

            @Override
            public void onRepeatModeChanged(int repeatMode) {
                Log.d(TAG, "onRepeatModeChanged() "+repeatMode);
            }

            @Override
            public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {
                Log.d(TAG, "onShuffleModeEnabledChanged()");
            }

            @Override
            public void onPlayerError(ExoPlaybackException error) {
                // 소스 파일을 재생할 때 오류가 발생하면 호출
                Log.d(TAG, "onPlayerError()");
            }

            @Override
            public void onPositionDiscontinuity(int reason) {
                // 소스가 어떤 위치를 찾으면 호출됩니다.
                Log.d(TAG, "onPositionDiscontinuity()");
            }

            @Override
            public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
                // 현재 소스 매개 변수가 변경되면 호출됩니다.
                // 여기서 말하는 매개변수는 뭐야?
                Log.d(TAG, "onPlaybackParametersChanged()");
            }

            @Override
            public void onSeekProcessed() {
                Log.d(TAG, "onSeekProcessed()");
            }
        });

        // Prepare the player with the source.
        player.prepare(mediaSource);
        player.seekTo(mPlaybackPosition);
        player.setPlayWhenReady(playWhenReady);  // 재생준비가 완료되면 바로 재생을 시작하도록 한다.

    }

    // @SuppressLint Lint 검사를 비활성화 한다.
    // lnlinedApi 는 이전 버전에서는 작동하지 않을 수도 있는 필드를 찾는 Lint 검사다.
    @SuppressLint("InlinedApi")
    private void hideSystemUi() {
        video_view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }

    /*
    // dash 예제임
    private MediaSource buildMediaSource(Uri uri) {
        DataSource.Factory dataSourceFactory = new DefaultHttpDataSourceFactory("ua", BANDWIDTH_METER); // ua는 무슨뜻?
        DashChunkSource.Factory dashChunkSourceFactory = new DefaultDashChunkSource.Factory(dataSourceFactory);
        return new DashMediaSource(uri, dataSourceFactory, dashChunkSourceFactory, null, null);
    }
    */

    @Override
    protected void onPause() {
        super.onPause();

        mPlaybackPosition = player.getCurrentPosition();    // 재생 위치 저장. 단위 milliseconds
        mCurrentWindow = player.getCurrentWindowIndex();    // 재생중인 윈도우의 인덱스? 이게 뭐고 왜 필요해?
        playWhenReady = player.getPlayWhenReady();          // 재생상태 저장

        player.setPlayWhenReady(false);                     // 재생중지

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // onPause() --> onSaveInstanceState() --> onStop() 순으로 호출된다.
    }

    @Override
    protected void onStop() {
        super.onStop();

        updateWatchingMovieData();
        /* // 왜 버전따라 다르게 처리해야하는 지 알 수 없다.
        if (Util.SDK_INT > 23) {
            //releasePlayer();
        }
        */
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(player != null) {
            player.release();
            player = null;
        }
    }

    /**
     * 이어보기 기능을 위해, 영화 시청 기록을 기기에 저장한다. (SharedPreferences)
     * (주의) 이어보기 정보는 기기에 저장된다. 따라서 하나의 기기에서 여러 사용자가 로그인할 경우 구분할 수 있는 방법이 필요하다.
     * SharedPreferences 에서 key 값이 loginUserCode+SharedPreferencesBuilder.KEY_WATCHING_MOVIE 이어야 한다!
     */
    private void updateWatchingMovieData(){

        SharedPreferences sharedPreferencesBuilder = SharedPreferencesBuilder.getSharedDefaultConfig(getContext());
        int loginUserCode = sharedPreferencesBuilder.getInt(SharedPreferencesBuilder.USR_LOGIN_USER_CODE, -1);  // 현재 로그인한 사용자 userCode 를 구하고

        String watchingMoieDataKey = loginUserCode+SharedPreferencesBuilder.KEY_WATCHING_MOVIE;

        String watchingMovieValue = sharedPreferencesBuilder.getString(watchingMoieDataKey, "");
        HashMap<Integer, Movie> watchingMovieList = new HashMap<Integer, Movie>();
        if(StringUtils.isNotEmpty(watchingMovieValue)){ // 이미 저장된 이어보기 기록이 있다면
            try {
                watchingMovieList.putAll( (HashMap<Integer, Movie>) gson.fromJson(watchingMovieValue, new TypeToken<HashMap<Integer, Movie>>() {}.getType()));

            }catch (Exception e){
                Log.e(TAG, "이어보기 기록 조회 실패");
            }
        }

        if( mPlaybackPosition > (player.getDuration() - END_TIME)  ) {
            // 현재 동영상 재생 시간이 전체 영상 시간 - END_TIME 을 넘었다면, 영화를 다 본것으로 간주한다.
            // 이전에 남아있을 수 있는 이어보기 정보를 삭제한다.
            // 영화마다 다 봤다고 할 수 있는 시간이 달라서.. 사실 이 방법은 다소 임시방편임.
            // 관리자 페이지에서 영화별로 시청완료시점을 저장해두고 그 데이터를 사용하는게 최선의 방법?

            watchingMovieList.remove(playingVideo.getMovieCode());
            // [킵] 다본 영화 목록에 추가하기

        }else{
            // 그렇지 않으면 아직 영화를 다 보지 않았다고 판단한다.
            // 영화 코드, 시청시간 정보를 저장한다.
            watchingMovieList.put(playingVideo.getMovieCode(),
                    new Movie(playingVideo.getMovieCode(), playingVideo.getMovieTitle(), playingVideo.getMovieTitle_en()
                                , playingVideo.getPoster(), playingVideo.getStreamingFile(), mPlaybackPosition, player.getDuration()));
        }

        String updateWatchingMovieValue = gson.toJson(watchingMovieList);
        SharedPreferencesBuilder.getSharedDefaultConfigEditor(getContext()).putString(watchingMoieDataKey, updateWatchingMovieValue).commit();

    }



}
