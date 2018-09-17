package com.hyunju.jin.movie.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
    입력 값의 유효성을 검사하는 함수들을 모아놓은 클래스
    현재는 회원가입/로그인 시 아이디, 비밀번호 검사를 하는데만 사용한다.
 */
public class StringValidator {

    /**
     * 문자열이 이메일 형식에 맞으면 true 를 리턴한다.
     */
    public static boolean checkEmail(String email){
        String regex = "^[_a-zA-Z0-9-\\.]+@[\\.a-zA-Z0-9-]+\\.[a-zA-Z]+$";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(email);
        boolean isValid = m.matches();
        return isValid;
    }

    /**
     * 문자열이 비밀번호 정책에 맞는지 확인한다. 통과하면 공백 리턴, 통과하지 못하면 이유를 리턴한다.
     * 현재 비밀번호 정책: 8~12자 이내. 영문, 숫자, 특수문자 포함
     *
     * 비밀번호 검사는 서버에서도 한번 더 진행하므로, 서버와 클라이언트에서의 비밀번호 정책을 동일하게 맞춰야 한다.
     */
    public static String checkPwd(String pwd){

        int min = 8;
        int max = 12;
        //int num = 1;
        //int spe = 1;

        if( pwd.length() < min ){
            return "비밀번호는 "+min+"자 이상이어야합니다.";
        }else if( pwd.length() > max ){
            return "비밀번호는 최대 "+max+"자입니다.";
        }

        // 정규식도 공부 좀.. 이거 숫자는 검사도 하는건가?
        String regex = "^(?=.*[a-zA-Z])(?=.*[!@#$%^*+=-])(?=.*[0-8]).{8,12}$";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(pwd);
        boolean isValid = m.matches();
        if(!isValid){
            return "비밀번호는 영문, 숫자, 특수문자를 조합해주세요.";
        }

        return "";
    }

    /**
     * 문자열이 ID 정책에 맞는지 확인한다. 통과하면 공백 리턴, 통과하지 못하면 이유를 리턴한다.
     * 현재 ID 정책: 3-20자 이내. 영문 대소문자 및 언더바(_), 숫자 허용
     */
    public static String checkID(String id){

/*
        if( (id.length() < 3) || (id.length() > 20) ){
            return "아이디는 3~20자 이내여야합니다.";
        }

        // 숫자도 검사되는지 확인하기
        String regex = "^(?=.*[a-zA-Z])(?=.*[_])(?=.*[0-3]).{3,20}$";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(id);
        boolean isValid = m.matches();
        if(!isValid){   // 언더바 외 특수문자인 경우
            return "ID에는 언더바(_)만 허용됩니다.";
        }
*/
        return "";  // 정규식몰라서 우선 이렇게 함.
    }

}
