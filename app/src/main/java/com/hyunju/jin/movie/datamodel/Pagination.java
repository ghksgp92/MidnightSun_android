package com.hyunju.jin.movie.datamodel;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Pagination implements Serializable {

    @SerializedName("currentPage") private int currentPage;
    @SerializedName("totalPages") private int totalPages;
    @SerializedName("pageSize") private int pageSize;

    public Pagination(){
        this.currentPage = 1;
        this.pageSize = 15;
    }

    public Pagination(int totalPages){
        this.currentPage = 1;
        this.pageSize = 10;
        this.totalPages = totalPages;
    }

    /**
     * 다음 페이지가 존재하면 true 리턴
     * @return
     */
    public boolean checkNextPage(){
        return (currentPage <= getTotalPages()); // <= 이어야하는지 의문이다.
    }

    /**
     * 페이지 번호를 다음 페이지로 변경한다.
     */
    public void setNextPage(){
        this.currentPage += 1;
    }

    /*
        getter, setter 정의
     */

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }
}
