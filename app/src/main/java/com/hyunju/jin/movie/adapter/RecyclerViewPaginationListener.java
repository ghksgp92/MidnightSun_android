package com.hyunju.jin.movie.adapter;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

/**
 * LinearLayoutManager 으로 만들어진 RecyclerView 에 대하여
 * 스크롤 시 페이징 처리를 하기 위해 커스텀한 Listener
 *
 * (참고한 링크)
 * https://github.com/Suleiman19/Android-Pagination-with-RecyclerView
 * https://blog.iamsuleiman.com/android-pagination-tutorial-getting-started-recyclerview/
 *
 * (주의) 이 리스너를 사용하는 RecyclerView 의 어댑터에는 아래 메소드들이 정의되어야 한다.
             public void add(Movie mc);
             public void addAll(List<Object> list);
             public void remove(Object city);
             public void clear();
             public boolean isEmpty();
             public void addLoadingFooter();
             public void removeLoadingFooter();
 */
public abstract class RecyclerViewPaginationListener extends RecyclerView.OnScrollListener {

    LinearLayoutManager layoutManager;

    public RecyclerViewPaginationListener(LinearLayoutManager layoutManager) {
        this.layoutManager = layoutManager;
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        int visibleItemCount = layoutManager.getChildCount();
        int totalItemCount = layoutManager.getItemCount();
        int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

        if (!isLoading() && !isLastPage()) { // 현재 로딩중인 페이지가 없고, 마지막 페이지가 아닐 때
            if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount && firstVisibleItemPosition >= 0) {
                // 스크롤 끝까지 내려갈 경우 다음페이지를 로드한다.
                loadMoreItems();
            }
        }
    }

    /**
     * 현재 로딩중인 페이지가 없고, 마지막 페이지가 아닐 때
     * 스크롤 끝까지 내려갈 경우 다음페이지를 로드한다.
     */
    protected abstract void loadMoreItems();

    /**
     * 전체 페이지 수를 리턴
     * @return
     */
    public abstract int getTotalPageCount();

    public abstract boolean isLastPage();

    public abstract boolean isLoading();
}