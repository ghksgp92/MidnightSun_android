package com.hyunju.jin.movie.activity.gallery.nofolder;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.hyunju.jin.movie.R;
import com.hyunju.jin.movie.activity.gallery.GalleryFunction;
import com.hyunju.jin.movie.datamodel.Gallery;

import org.wysaid.view.ImageGLSurfaceView;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ImageFilterApplyAdapter extends RecyclerView.Adapter<ImageFilterApplyAdapter.ImageFilterApplyViewHolder>{

    Context context;
    ArrayList<Gallery> selectedList;
    ImageFilterListenter listenter;

    public ImageFilterApplyAdapter(Context context, ArrayList<Gallery> selectedList, ImageFilterListenter imageFilterListenter){
        this.context = context;
        this.selectedList = selectedList;
        this.listenter = imageFilterListenter;
    }

    @NonNull
    @Override
    public ImageFilterApplyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_filter_apply_image, parent, false);
        ImageFilterApplyViewHolder viewHolder = new ImageFilterApplyViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ImageFilterApplyViewHolder holder, int position) {

        final HashMap <String, String> media = selectedList.get(position).getMedia();
        holder.position = position;
        Glide.with(context).load(new File(media.get(GalleryFunction.KEY_PATH))).into(holder.img_photo_original);

        final ImageFilterApplyViewHolder holderCopy = holder;

        try {

            final Bitmap mBitmap = loadBackgroundBitmap(media.get(GalleryFunction.KEY_PATH));
            final String filterConfig = selectedList.get(position).getFilter().getFilter_config();

            // openGL을 이용한 필터 설정.
            holder.glSurfaceView_photo.setSurfaceCreatedCallback(new ImageGLSurfaceView.OnSurfaceCreatedCallback() {
                @Override
                public void surfaceCreated() {
                    holderCopy.glSurfaceView_photo.setImageBitmap(mBitmap);
                    holderCopy.glSurfaceView_photo.setFilterWithConfig(filterConfig);
                }
            });

        }catch (Exception e){
            Toast.makeText(context, "사진을 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show();

        }
    }


    /**
     * 지정한 패스의 파일을 읽어서 Bitmap을 리턴 (화면사이즈에 최대한 맞춰서 리스케일한다.)
     * 이게 꼭 필요한가?
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
    public int getItemCount() {
        if(selectedList == null){
            return 0;
        }
        return selectedList.size();
    }

    public class ImageFilterApplyViewHolder extends RecyclerView.ViewHolder{

        @BindView(R.id.img_photo_original) ImageView img_photo_original;
        @BindView(R.id.surface_image) ImageGLSurfaceView glSurfaceView_photo;

        int position;

        public ImageFilterApplyViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            this.position = 0;
        }


    }
}
