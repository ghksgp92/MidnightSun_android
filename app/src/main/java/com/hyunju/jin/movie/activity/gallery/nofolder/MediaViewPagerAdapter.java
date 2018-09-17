package com.hyunju.jin.movie.activity.gallery.nofolder;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.hyunju.jin.movie.R;
import com.hyunju.jin.movie.activity.gallery.GalleryFunction;
import com.hyunju.jin.movie.datamodel.Gallery;

import org.wysaid.myUtils.ImageUtil;
import org.wysaid.myUtils.MsgUtil;
import org.wysaid.view.ImageGLSurfaceView;
import org.wysaid.view.VideoPlayerGLSurfaceView;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;

public class MediaViewPagerAdapter extends PagerAdapter{

    Context context;
    ArrayList<Gallery> selectedList;
    ImageFilterListenter listenter;
    LayoutInflater layoutInflater;

    public MediaViewPagerAdapter(Context context, ArrayList<Gallery> selectedList, ImageFilterListenter imageFilterListenter){
        this.context = context;
        this.selectedList = selectedList;
        this.listenter = imageFilterListenter;
        this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        if(selectedList == null){
            return 0;
        }
        return selectedList.size();
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        return POSITION_NONE;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }



    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, final int position) {

        final HashMap<String, String> media = selectedList.get(position).getMedia();

        if( media.get(GalleryFunction.KEY_MIME_TYPE).contains("video") ){ // 현재 비디오를 보고 있다면
            View view = layoutInflater.inflate(R.layout.item_filter_apply_video, null);
            final VideoPlayerGLSurfaceView surface_video = (VideoPlayerGLSurfaceView) view.findViewById(R.id.surface_video);

            final String filterConfig = selectedList.get(position).getFilter().getFilter_config();

            Uri video = Uri.fromFile(new File(media.get(GalleryFunction.KEY_PATH)));
            surface_video.setVideoUri(video, new VideoPlayerGLSurfaceView.PlayPreparedCallback() {
                @Override
                public void playPrepared(MediaPlayer player) {
                    surface_video.setFilterWithConfig(filterConfig);
                    player.start();
                }
            }, new VideoPlayerGLSurfaceView.PlayCompletionCallback() {
                @Override
                public void playComplete(MediaPlayer player) {
                    player.start();
                }

                @Override
                public boolean playFailed(MediaPlayer mp, int what, int extra) {
                    MsgUtil.toastMsg(context, String.format("Error occured! Stop playing, Err code: %d, %d", what, extra));
                    return true;
                }
            });

            ((ViewPager) container).addView(view);
            return view;


        }else { // 현재 이미지를 보고 있다면
            View view = layoutInflater.inflate(R.layout.item_filter_apply_image, null);

            RelativeLayout layout_media_view = (RelativeLayout) view.findViewById(R.id.layout_media_view);
            final ImageGLSurfaceView surface_image = (ImageGLSurfaceView) view.findViewById(R.id.surface_image);
            try {
                final Bitmap mBitmap = loadBackgroundBitmap(selectedList.get(position).getMedia().get(GalleryFunction.KEY_PATH));
                final int filterCode = selectedList.get(position).getFilter().getCode();
                final String filterConfig = selectedList.get(position).getFilter().getFilter_config();

                surface_image.setSurfaceCreatedCallback(new ImageGLSurfaceView.OnSurfaceCreatedCallback() {
                    @Override
                    public void surfaceCreated() {
                        surface_image.setImageBitmap(mBitmap);

                        // 이미지에 필터 적용과 필터 적용이 완료되면 수행할 작업을 매개변수로 전달한다.
                        surface_image.setFilterWithConfig(filterConfig, new ImageGLSurfaceView.QueryResultBitmapCallback() {

                            // 필터 적용이 완료되면 이미지를 저장한다.
                            @Override
                            public void get(Bitmap bmp) {

                                /*  (참고)
                                    현재 보고 있는 이미지에 필터가 적용됬다면 앱 임시 폴더에 필터가 적용된 이미지 파일을 생성한다.
                                    '현재 보고 있는 이미지'란 조건을 주지 않으면, 이미지의 필터가 변경될때 notifyDataSetChanged() 가 호출되어
                                    모든 이미지를 다시 그리게 되기 때문에 이전에 필터를 적용시킨 다른 이미지까지 파일을 생성하게 된다.
                                */
                                if( listenter.currentPosition() == position && filterCode != 0) {
                                    // currentPosition() 메소드를 통해 현재 보고 있는 이미지의 인덱스를 알 수 있다.
                                    // filterCode == 0 이면 원본이다.

                                    String s = ImageUtil.saveBitmapTemp(bmp);   // 앱의 임시폴더 경로에 이미지 파일 생성
                                    media.put(GalleryFunction.KEY_FILTER_PATH, s);  // 생성된 이미지 파일 경로를 저장
                                    context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + s)));
                                    // 안드로이드 OS에? 새로운 파일이 생성됬음을 알린다. 이 작업을 해야 갤러리 등에서 파일이 조회 된다.
                                }
                            }
                        });
                        /* (참고)
                            이렇게 되면 사용자가 '현재 보고있는 이미지'의 필터를 바꿀때마다 새로운 이미지 파일이 생성된다.
                            현재 이미지 필터를 사용하는 기능은 '사용자 프로필 사진 설정' 뿐이라
                            모든 이미지를 임시폴더에 생성하고 작업이 완료되면 임시폴더를 지우는 방식으로 코딩되어있다.

                            만약 필터가 적용된 사진이 삭제되지 않길 원한다면 예외처리가 필요하다!

                            이렇게 처리한 이유는
                            현재 이 이미지 필터 코드는 다중선택 갤러리를 고려하여 ViewPager 를 사용해 이미지를 보여주는데
                            ViewPager 안의 ImageGLSurfaceView 에 접근할 방법을 찾지 못했기 때문이다.
                            - VIewPager 는 자신의 페이지를 유동적으로 생성, 삭제한다. ?

                            (한계)
                            이 방법의 단점은 사용자 기기에 이미지를 저장할 용량이 없으면 필터 교체를 할 수 없다는 것.
                         */
                    }
                });
            } catch (Exception e) {
                Log.e("MediaViewPagerAdapter", "필터 적용 오류");
            }

            ((ViewPager) container).addView(view);
            return view;
        }
    }

    /**
     * 주석 상세히 좀 달 것. 이 메소드로 bitmap 변환을 하지 않으면 이미지가 안나온다.
     * @param imgFilePath
     * @return
     * @throws Exception
     * @throws OutOfMemoryError
     */
    public Bitmap loadBackgroundBitmap(String imgFilePath) throws Exception, OutOfMemoryError {

        File imageFile = new File(imgFilePath);
        if (!imageFile.exists()) {
            throw new FileNotFoundException("background-image file not found : " + imgFilePath);
        }

        // 폰의 화면 사이즈를 구한다.
        Display display = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        int displayWidth = display.getWidth();
        int displayHeight = display.getHeight();

        // 읽어들일 이미지의 사이즈를 구한다.
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imgFilePath, options);

        // 화면 사이즈에 가장 근접하는 이미지의 리스케일 사이즈를 구한다.
        // 리스케일의 사이즈는 짝수로 지정한다. (이미지 손실을 최소화하기 위함.)
        float widthScale = options.outWidth / displayWidth;
        float heightScale = options.outHeight / displayHeight;
        float scale = widthScale > heightScale ? widthScale : heightScale;

        if(scale >= 8) {
            options.inSampleSize = 8;
        } else if(scale >= 6) {
            options.inSampleSize = 6;
        } else if(scale >= 4) {
            options.inSampleSize = 4;
        } else if(scale >= 2) {
            options.inSampleSize = 2;
        } else {
            options.inSampleSize = 1;
        }
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(imgFilePath, options);
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        ViewPager viewPager = (ViewPager) container;
        View view = (View) object;

        HashMap<String, String> media = selectedList.get(position).getMedia();

        if( media.get(GalleryFunction.KEY_MIME_TYPE).contains("video") ){

            final VideoPlayerGLSurfaceView surface_video = (VideoPlayerGLSurfaceView) view.findViewById(R.id.surface_video);
            //surface_video.release();
            surface_video.getPlayer().release();
            //surface_video.onPause();
        }

        viewPager.removeView(view);
    }


}
