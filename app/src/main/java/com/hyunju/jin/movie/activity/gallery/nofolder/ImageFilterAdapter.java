package com.hyunju.jin.movie.activity.gallery.nofolder;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.hyunju.jin.movie.R;
import com.hyunju.jin.movie.datamodel.ImageFilter;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ImageFilterAdapter extends RecyclerView.Adapter<ImageFilterAdapter.ImageFilterViewHolder>{

    Context context;
    ArrayList<ImageFilter> imageFilters;
    ImageFilterListenter listenter;

    public ImageFilterAdapter(Context context, ArrayList<ImageFilter> imageFilters, ImageFilterListenter imageFilterListenter){
        this.context = context;
        this.imageFilters = imageFilters;
        this.listenter = imageFilterListenter;
    }

    @NonNull
    @Override
    public ImageFilterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_image_filter_preview, parent, false);
        ImageFilterViewHolder viewHolder = new ImageFilterViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ImageFilterViewHolder holder, int position) {
        holder.position = position;
        ImageFilter filter = imageFilters.get(position);
        Glide.with(context).load(filter.getFilter_preview_drawable()).into(holder.img_filter_preview);
        holder.tv_filter_name.setText(filter.getFilter_name()+"");

    }

    @Override
    public int getItemCount() {
       if(imageFilters == null){
           return 0;
       }
       return imageFilters.size();
    }

    public class ImageFilterViewHolder extends RecyclerView.ViewHolder{

        @BindView(R.id.img_filter_preview) ImageView img_filter_preview;
        @BindView(R.id.tv_filter_name) TextView tv_filter_name;
        int position;

        public ImageFilterViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            position = 0;
        }

        @OnClick(R.id.layout_img_filter)
        public void selectFilter(){
            listenter.chagneFilter(imageFilters.get(position).getCode(), imageFilters.get(position).getFilter_config());
        }
    }
}
