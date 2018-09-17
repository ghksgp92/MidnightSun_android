package com.hyunju.jin.movie.network;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * HTTPS1.1로 웹 서버에 연결한다. Retrofit 라이브러리를 사용함.
 */

public class RetrofitClient {

    // 웹서버
    public static String WEB_SERVER = "http://183.111.227.218";
    public static String WEB_SERVER_PORT = ":80";

    // 토큰 관련 노드서버
    //public static String TOKEN_NODE_SERVER = "http://192.168.56.101";
    public static String TOKEN_NODE_SERVER = "http://183.111.227.218";
    public static String TOKEN_NODE_SERVER_PORT = ":3000";

    private static Retrofit retrofit = null;
    private static Retrofit retrofitToken = null;

    /**
     * retrofit 객체를 리턴한다.
     * 과도한 HTTP 커넥션이 생기는 것을 막기 위해 싱글톤 패턴으로 객체를 관리한다.
     * 모든 응답은 Gson 형태로 리턴받는다.
     * @return
     */
    public static Retrofit getWebServerClient(){
        if(retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(WEB_SERVER + WEB_SERVER_PORT)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }

        return retrofit;
    }

    /**
     * 사용자 회원가입, 마이페이지 등과 관련된 요청을 하는 HTTP 서비스 객체를 리턴한다.
     * @return
     */
    public static UserService getUserService(){
        if(retrofit == null){
            getWebServerClient();
        }
        return retrofit.create(UserService.class);
    }

    /**
     * 영화 정보 조회 등과 관련된 요청을 하는 HTTP 서비스 객체를 리턴한다.
     * @return
     */
    public static MovieService getMovieService(){
        if(retrofit == null){
            getWebServerClient();
        }
        return retrofit.create(MovieService.class);
    }

    /**
     * 포스팅 작성/수정, 포스팅 댓글 등록/수정 등과 관련된 요청을 하는 HTTP 서비스 객체를 리턴한다.
     * @return
     */
    public static PostingService getPostingService(){
        if(retrofit == null){
            getWebServerClient();
        }
        return retrofit.create(PostingService.class);
    }

    public static RecommendService getRecommendService(){
        if(retrofit == null){
            getWebServerClient();
        }
        return retrofit.create(RecommendService.class);
    }

    /**
     * retrofit 객체를 리턴한다.
     * 과도한 HTTP 커넥션이 생기는 것을 막기 위해 싱글톤 패턴으로 객체를 관리한다.
     * 모든 응답은 Gson 형태로 리턴받는다.
     * @return
     */
    public static Retrofit getWalletNodeServerClient(){
        if(retrofitToken == null) {
            retrofitToken = new Retrofit.Builder()
                    .baseUrl(TOKEN_NODE_SERVER + TOKEN_NODE_SERVER_PORT)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }

        return retrofit;
    }

    public static WalletNodeService getWalletNodeService(){
        if(retrofitToken == null){
            getWalletNodeServerClient();
        }
        return retrofitToken.create(WalletNodeService.class);
    }

    public static WalletService getWalletService(){
        if(retrofit == null){
            getWebServerClient();
        }
        return retrofit.create(WalletService.class);

    }
}
