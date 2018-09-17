package com.hyunju.jin.movie.activity.entertainment;

import android.support.annotation.Nullable;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.hyunju.jin.movie.R;
import com.hyunju.jin.movie.activity.SuperActivity;
import com.wikitude.architect.ArchitectStartupConfiguration;
import com.wikitude.architect.ArchitectView;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Wikitude SDK (JavaScript) 를 사용하여 영화 포스터 위에 예고편을 띄워주는 AR 카메라 화면
 *
 * 실제로 AR 이벤트를 구현하는 부분은 Javascript로, 예제 코드는 https://github.com/Wikitude/wikitude-sdk-samples 에서 제공함.
 * OpenGL 제한 때문에 Android Emulator에서 Wikitude SDK 를 사용할 수는 없다.
 * SDK 최소 버전은 16 이라고 하는데, 실제로 wikitudesdk.arr 을 까보면 최소버전은 19 다.
 */
public class ARPosterActivity extends SuperActivity {

    @BindView(R.id.architect_view) ArchitectView architect_view; // 카메라 View 역할을 하며 센서 이벤트를 처리한다.
    // architect_view 에서 인식한 화면에 구현되는 AR 이벤트는 Javascript 로 구현된다.
    // assets/wikitude 와 libs/wikitudesdk.arr 을 사용함.

    /*
        wikitude 문서에서 참고할만한 코드 그냥 적어둠
        1) architectView.callJavascript(newData('" + poiDataAsJson +")')
        1-2) 안드로이드 네이티브에서 javascript 로 데이터 전달하기. 그럴일이 뭐가 있지? 아. 화면 클릭하면 뭐 다른 화면으로 이동한다던지 그런거.
        Communication between JavaScript and Android Native (Java) 로 검색하기.

     */

    private static final String arDefinitionsPath = "wikitude/wikitude.json";
    private List<ARCategory> categories;
    private static final String SAMPLES_ROOT = "wikitude/";
    private String arExperience;    // 현재 사용중인 AR 의 index.html 경로

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ar_poster);
        ButterKnife.bind(this);

        // assets/wikitude 하위에 작성한 AR 기능을 사용할 수 있도록 불러온다.
        final String json = ARJsonParser.loadStringFromAssets(this, arDefinitionsPath);
        categories = ARJsonParser.getCategoriesFromJsonString(json);

        // 이 작업은 왜 하는걸까?
        int supportedFeaturesForDevice = ArchitectView.getSupportedFeaturesForDevice(this);
        for (Iterator<ARCategory> catIt = categories.iterator(); catIt.hasNext(); ) {
            final ARCategory category = catIt.next();

            for (Iterator<ARData> dataIt = category.getSamples().iterator(); dataIt.hasNext(); ) {
                final ARData data = dataIt.next();

                int arFeatures = data.getArFeatures();
                if ((arFeatures & supportedFeaturesForDevice) != arFeatures) {
                    dataIt.remove();
                }
            }
            if (category.getSamples().size() == 0) {
                catIt.remove();
            }
        }


        startARCamera();

    }

    /**
     * AR 카메라 환경을 설정하고 카메라를 시작한다.
     */
    private void startARCamera(){

        // ArchitectView.isDeviceSupported(Context context)
        // 현재 디바이스에서 하드웨어가 지원되는지 확인한다.

        // 런타임 전에 카메라 권한이 있는지 확인한다.
        // architectView.onCreate( config ).

        final ARData ARData = categories.get(0).getSamples().get(0);
        //final String[] permissions = PermissionUtil.getPermissionsForArFeatures(ARData.getArFeatures());
        arExperience = ARData.getPath();

        final ArchitectStartupConfiguration config = new ArchitectStartupConfiguration();
        config.setLicenseKey(WikitudeSDKConstants.WIKITUDE_SDK_KEY_STARTUP); // StartUP 라이센스
        config.setCameraPosition(ARData.getCameraPosition());       // The default camera is the first camera available for the system.
        config.setCameraResolution(ARData.getCameraResolution());   // The default resolution is 640x480.
        config.setCameraFocusMode(ARData.getCameraFocusMode());     // The default focus mode is continuous focusing.
        config.setCamera2Enabled(ARData.isCamera2Enabled());        // The camera2 api is disabled by default (old camera api is used).
        config.setFeatures(ARData.getArFeatures());                 // This tells the ArchitectView which AR-features it is going to use, the deault is all of them.
        architect_view.onCreate(config);

    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        architect_view.onPostCreate();  // 왜 여기서 호출하는걸까?

        try {
            /*
             * Loads the AR-Experience, it may be a relative path from assets,
             * an absolute path (file://) or a server url.
             *
             * To get notified once the AR-Experience is fully loaded,
             * an ArchitectWorldLoadedListener can be registered.
             */
            architect_view.load(SAMPLES_ROOT + arExperience);
            // 서버에 있는 html 을 요청할 수도 있으나 문제는 인수를 전달할 수 없다.. 이거 너무한데?
        } catch (IOException e) {
            Toast.makeText(this, "Could not load AR experience.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Exception while loading arExperience " + arExperience + ".", e);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        architect_view.onResume(); // 액티비티 생명주기에 맞춰 architect_view 의 생명주기 처리
    }

    @Override
    protected void onPause() {
        super.onPause();
        architect_view.onPause(); // 액티비티 생명주기에 맞춰 architect_view 의 생명주기 처리
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // architect_view 의 인스턴스에서 캐쉬된 모든 데이터를 삭제한다.
        architect_view.clearCache();    // 반드시 onDestroy() 전에 호출되야함.
        architect_view.onDestroy();  // 액티비티 생명주기에 맞춰 architect_view 의 생명주기 처리
    }
}
