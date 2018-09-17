package com.hyunju.jin.movie.custom;

import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.StyleSpan;

public class PostingEditTextWatcher implements TextWatcher{

    private boolean isFormatting = false;
    SpannableString commentSpannable;

    public PostingEditTextWatcher(){

    }

    public PostingEditTextWatcher(SpannableString commentSpannable){
        this.commentSpannable = commentSpannable;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if (isFormatting) {
            return;
        }

        isFormatting = true;

        String text = s.toString();

        for (StyleSpan styleSpan : s.getSpans(0, s.length(), StyleSpan.class)) {
            s.removeSpan(styleSpan);
        }
/*
        String textToBold = Strings.splitAtFirstLineBreak(text)[0];
        StyleSpan styleSpan = new StyleSpan(Typeface.BOLD);
        int start = text.indexOf(textToBold);
        int end = start + textToBold.length();
        editable.setSpan(styleSpan, start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE);*/

        isFormatting = false;

        commentSpannable = new SpannableString(s);  // 입력한 내용이 바뀔때마다 Spannable 객체를 업데이트 한다.
    }
}
