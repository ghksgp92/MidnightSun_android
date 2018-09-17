package com.hyunju.jin.movie.custom;

import android.content.Context;
import android.support.v7.widget.AppCompatEditText;
import android.util.AttributeSet;

import com.hyunju.jin.movie.activity.posting.PostingEditListener;

public class PostingEditText extends AppCompatEditText {

    private PostingEditListener listener;


    public PostingEditText(Context context) {
        super(context);
    }

    public PostingEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PostingEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onSelectionChanged(int selStart, int selEnd) {
        //super.onSelectionChanged(selStart, selEnd);
        if(listener != null) {
            // View가 처음 액티비티에 그려질 때 onSelectionChanged 를 호출한다. 이 시점에는 리스너 연결을 하지 못한 상태이기 때문에 null 체크
           listener.checkCurrentCursorSpannable(selStart, selEnd);   // 현재 선택한 커서에 적용된 텍스트 스타일을 체크하여 화면에 표시하도록 한다.

        }
    }

    public PostingEditListener getListener() {
        return listener;
    }

    public void setListener(PostingEditListener listener) {
        this.listener = listener;
    }

}
