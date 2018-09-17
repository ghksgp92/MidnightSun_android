package com.github.irshulx.custom;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.irshulx.Components.CustomEditText;
import com.github.irshulx.EditorCore;
import com.github.irshulx.R;
import com.github.irshulx.models.EditorControl;
import com.github.irshulx.models.EditorType;
import com.github.irshulx.models.RenderType;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.TimerTask;
import java.util.UUID;

/**
 * 포스팅 작성 화면에서 사진을 추가할 때 사용한다.
 * ImageExtensions 를 참고하여 커스텀함. 서버에 이미지를 올릴 수 있도록 images 에서 업로드된 이미지 파일 정보를 관리한다.
 */
public class ServerUploadImageExtensions {

    private EditorCore editorCore;
    private int editorImageLayout = R.layout.tmpl_image_view;       // 포스팅 내용에 추가된 이미지의 레이아웃 파일. 레이아웃이 바뀌면 insertImage() 메서드를 반드시 수정해야함.
    private HashMap<String, HashMap<String, String>> images;        // 포스팅에 추가된 이미지 파일 정보. key,

    public ServerUploadImageExtensions(EditorCore editorCore) {
        this.editorCore = editorCore;
        this.images = new HashMap<String, HashMap<String, String>>();
    }

    public HashMap<String, HashMap<String, String>> getImages() {
        return images;
    }

    public void setEditorImageLayout(int drawable) {
        this.editorImageLayout = drawable;
    }

    public void executeDownloadImageTask(String url, int index, String desc) {
        new ServerUploadImageExtensions.DownloadImageTask(index).execute(url, desc);
    }

    /**
     * Editor 에 사진을 추가하기 위해 갤러리를 연다.
     * 현재는 사용하지 않는 메소드다. 왜냐하면 다중선택 갤러리가 이 모듈에 포함된 것이 아니라 내 앱에 포함되었기 때문에 앱에서 처리한다.
     */
    public void openImageGallery() {
        Intent intent = new Intent();
        // Show only images, no videos or anything else
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        // Always show the chooser (if there are multiple options available)
        ((Activity) editorCore.getContext()).startActivityForResult(Intent.createChooser(intent, "Select an image"), editorCore.PICK_IMAGE_REQUEST);
    }

    /**
     * Editor 에 사진을 추가한다.
     * (주의) 이미지 레이아웃이 바뀌면 이 메서드 구현을 수정해야함.
     * @param image
     */
    public void insertImage(HashMap<String, String> image, int index) {

        // editorImageLayout 으로 설정된 레이아웃으로 View 를 만든다.
        final View childLayout = ((Activity) editorCore.getContext()).getLayoutInflater().inflate(this.editorImageLayout, null);
        ImageView imageView = (ImageView) childLayout.findViewById(R.id.imageView);     // 선택한 이미지
        final TextView lblStatus = (TextView) childLayout.findViewById(R.id.lblStatus); // '이미지 추가 중..' TextView
        CustomEditText desc = (CustomEditText)childLayout.findViewById(R.id.desc);
        Picasso.with(editorCore.getContext()).load(new File(image.get("path"))).into(imageView);    // 피카소 이미지 라이브러리를 이용해 이미지를 표시한다.\

        final String uuid = generateUUID(); // 이미지를 구분하기 위한 key 값을 만든다.
        if (index == -1) {  // 항상 -1이 넘어오는데, 그 외 값이 넘어오는 경우는 아직 파악 못함.
            index = editorCore.determineIndex(EditorType.img);
        }
        showNextInputHint(index);
        editorCore.getParentView().addView(childLayout, index);

        if (editorCore.isLastRow(childLayout)) {
            editorCore.getInputExtensions().insertEditText(index + 1, null, null);
        }
        EditorControl control = editorCore.createTag(EditorType.img);
        control.path = uuid; // set the imageId,so we can recognize later after upload
        childLayout.setTag(control);
        if(editorCore.getRenderType()== RenderType.Editor) {
            lblStatus.setVisibility(View.VISIBLE);
            BindEvents(childLayout);
            childLayout.findViewById(R.id.progress).setVisibility(View.VISIBLE);
            editorCore.getServerUploadImageListener().onUpload(image, uuid);
        }else {
            desc.setEnabled(false);
            lblStatus.setVisibility(View.GONE);
        }
    }

    /**
     * 뭔지 모르겠는데
     * @param index
     */
    private void showNextInputHint(int index) {
        View view = editorCore.getParentView().getChildAt(index);
        EditorType type = editorCore.getControlType(view);
        if (type != EditorType.INPUT)
            return;
        TextView tv = (TextView) view;
        tv.setHint(editorCore.placeHolder);
    }

    private void hideInputHint(int index) {
        View view = editorCore.getParentView().getChildAt(index);
        EditorType type = editorCore.getControlType(view);
        if (type != EditorType.INPUT)
            return;

        String hint = editorCore.placeHolder;
        if (index > 0) {
            View prevView = editorCore.getParentView().getChildAt(index - 1);
            EditorType prevType = editorCore.getControlType(prevView);
            if (prevType == EditorType.INPUT)
                hint = null;
        }
        TextView tv = (TextView) view;
        tv.setHint(hint);
    }

    /**
     * yyyyMMddHHmmss 형식으로 key 값을 만든다. 이미지를 구분하는 키가 된다.
     * @return
     */
    public String generateUUID() {
        DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
        String sdt = df.format(new Date(System.currentTimeMillis()));
        UUID x = UUID.randomUUID();
        String[] y = x.toString().split("-");
        return y[y.length - 1] + sdt;
    }

    /*
      /used by the renderer to render the image from the Node
    */
    public void loadImage(String _path, String desc) {
        final View childLayout = ((Activity) editorCore.getContext()).getLayoutInflater().inflate(this.editorImageLayout, null);
        ImageView imageView = (ImageView) childLayout.findViewById(R.id.imageView);
        CustomEditText text = (CustomEditText) childLayout.findViewById(R.id.desc);
        if (TextUtils.isEmpty(desc)) {
            text.setVisibility(View.GONE);
        } else {
            text.setText(desc);
            text.setEnabled(false);
        }
        Picasso.with(this.editorCore.getContext()).load(_path).into(imageView);
        editorCore.getParentView().addView(childLayout);
    }


    public View findImageById(String imageId) {
        for (int i = 0; i < editorCore.getParentChildCount(); i++) {
            View view = editorCore.getParentView().getChildAt(i);
            EditorControl control = editorCore.getControlTag(view);
            if (!TextUtils.isEmpty(control.path) && control.path.equals(imageId))
                return view;
        }
        return null;
    }

    public void onPostUpload(HashMap<String, String> image, String imageId) {
        View view = findImageById(imageId);
        final TextView lblStatus = (TextView) view.findViewById(R.id.lblStatus);
        String filePath = image.get("path");
        lblStatus.setText(!TextUtils.isEmpty(filePath) ? "이미지 추가 완료" : "이미지 추가 실패");
        if (!TextUtils.isEmpty(filePath)) {
            EditorControl control = editorCore.createTag(EditorType.img);
            control.path = imageId;
            view.setTag(control);
            images.put(imageId, image);
            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    ((Activity) editorCore.getContext()).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // This code will always run on th UI thread, therefore is safe to modify UI elements.
                            lblStatus.setVisibility(View.GONE);
                        }
                    });
                }
            };
            new java.util.Timer().schedule(timerTask, 100); // 구지 딜레이 줄 필요 못느낀다.
        }
        view.findViewById(R.id.progress).setVisibility(View.GONE);
    }

    /*
      /used to fetch an image from internet and return a Bitmap equivalent
    */
    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        private int InsertIndex;
        private String subTitle;
        public DownloadImageTask(int index) {
            this.InsertIndex = index;
        }
        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            this.subTitle = urls[1];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }
        protected void onPostExecute(Bitmap result) {
            //insertImage(result, this.InsertIndex,subTitle);
        }
    }

    private void BindEvents(final View layout) {
        final ImageView imageView = (ImageView) layout.findViewById(R.id.imageView);
        final View btn_remove = layout.findViewById(R.id.btn_remove);

        // 이미지를 지운다.
        btn_remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int index = editorCore.getParentView().indexOfChild(layout);
                editorCore.getParentView().removeView(layout);
                hideInputHint(index);
                editorCore.getInputExtensions().setFocusToPrevious(index);

                EditorControl control = (EditorControl) layout.getTag();
                images.remove(control.path);
            }
        });

        imageView.setOnTouchListener(new View.OnTouchListener() {
            private Rect rect;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    imageView.setColorFilter(Color.argb(50, 0, 0, 0));
                    rect = new Rect(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    imageView.setColorFilter(Color.argb(0, 0, 0, 0));
                }
                if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    if (!rect.contains(v.getLeft() + (int) event.getX(), v.getTop() + (int) event.getY())) {
                        imageView.setColorFilter(Color.argb(0, 0, 0, 0));
                    }
                }
                return false;
            }
        });
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btn_remove.setVisibility(View.VISIBLE);
            }
        });
        imageView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                btn_remove.setVisibility(hasFocus ? View.VISIBLE : View.GONE);
            }
        });
    }
}
