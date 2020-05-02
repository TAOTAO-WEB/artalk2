/*
 * Copyright 2018 Google LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.edu.hdu.artalk2;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.ar.core.Anchor;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Session;
import com.google.ar.core.Trackable;
import com.google.ar.core.TrackingState;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.core.exceptions.UnavailableException;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.HitTestResult;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ViewRenderable;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import cn.edu.hdu.artalk2.dto.Message;
import cn.edu.hdu.artalk2.eventSource.MessageListUpdateListener;
import cn.edu.hdu.artalk2.eventSource.Messages;
import cn.edu.hdu.artalk2.service.GetMessageListService;
import cn.edu.hdu.artalk2.utils.ArUtils;
import cn.edu.hdu.artalk2.utils.OkHttpManager;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * ar扫描界面
 */
public class ArScanActivity extends AppCompatActivity {
  private static final int RC_PERMISSIONS = 0x123;
  private boolean cameraPermissionRequested;

  private GestureDetector gestureDetector;
  private Snackbar loadingMessageSnackbar = null;

  private ArSceneView arSceneView;

  private ViewRenderable audioCardRenderable;

  // True once scene is loaded
  private boolean hasFinishedLoading = false;

  // True once the scene has been placed.
  private boolean hasPlacedSolarSystem = false;


  // 是否正在构建ar留言图
  private boolean isDisplayingAudioCard = false;

  LocationClient mLocationClient;

  private GetMessageListService mService;
  private boolean mBound = false;


  // 请求网址
  private static final String POST_COORDINATE_URL = "http://47.112.174.246:3389/getMessage/";

  // message列表
  private Messages messages=new Messages();

  private static final String TAG = "ArScanActivity";
  /**
   * 位置变化监听器
   */
  class MyLocationListener extends BDAbstractLocationListener {
    @Override
    public void onReceiveLocation(BDLocation location) {

      if (location == null){
        return;
      }

      //获取纬度信息
      double latitude = location.getLatitude();
      //获取经度信息
      double longitude = location.getLongitude();
      //获取定位精度，默认值为0.0f
      float radius = location.getRadius();
      // 获取手机方向
      float direction = location.getDirection();
      //获取经纬度坐标类型，以LocationClientOption中设置过的坐标类型为准
      String coorType = location.getCoorType();
      //获取定位类型、定位错误返回码，具体信息可参照类参考中BDLocation类中的说明
      int errorCode = location.getLocType();

      String toast = "纬度："+latitude+"，经度："+longitude+"，精度:"+
              radius+", 方向:"+direction+",errorCode:"+errorCode;

      Log.i(TAG,toast);
      Toast.makeText(ArScanActivity.this,toast,Toast.LENGTH_SHORT).show();

      Map<String,String> map = new HashMap<>();
      map.put("cx",String.valueOf(Math.round(latitude)));
      map.put("cy",String.valueOf(Math.round(longitude)));
//    map.put("mId","10");

      // 发送获取消息列表请求
      OkHttpManager.getInstance().sendComplexFrom(POST_COORDINATE_URL, map,new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
          Log.e("posterror",e.getMessage());
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
          String res = response.body().string();

          Log.d("response",res);

          ObjectMapper mapper = new ObjectMapper();

          List<Message> messageList = mapper.readValue(res, new TypeReference<List<Message>>(){});

          messages.setMessageList(messageList);

          Log.d("messageList",messageList.toString());

        }
      });

    }
  }

  /** Defines callbacks for service binding, passed to bindService() */
  private ServiceConnection connection = new ServiceConnection() {

    @Override
    public void onServiceConnected(ComponentName className,
                                   IBinder service) {
      // We've bound to LocalService, cast the IBinder and get LocalService instance
      GetMessageListService.GetMessagesBinder binder = (GetMessageListService.GetMessagesBinder) service;
      mService = binder.getService();
      mBound = true;
    }

    @Override
    public void onServiceDisconnected(ComponentName arg0) {
      mBound = false;
    }
  };


  @Override
  @SuppressWarnings({"AndroidApiChecker", "FutureReturnValueIgnored"})
  // CompletableFuture requires api level 24
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (!ArUtils.checkIsSupportedDeviceOrFinish(this)) {
      // Not a supported device.
      return;
    }

    messages.setListUpdateListener(new MessageListUpdateListener() {
      @Override
      public void onUpdate(List<Message> list) {
        if (list.size()!=0 && !isDisplayingAudioCard){
          isDisplayingAudioCard = true;
          Log.d(TAG,"监听到有语音留言数据...");
          displayAr();
        }
        // 没有语音数据
        if (list.size() == 0){
          Log.d(TAG,"监听到没有语音留言数据...");
          isDisplayingAudioCard = false;
        }
      }
    });
    // 开启定位
    locationStart();

    setContentView(R.layout.activity_ar_scan);
    arSceneView = findViewById(R.id.ar_scene_view);


    // 跳转到放置留言页面
    ImageButton leaveBtn = findViewById(R.id.leave_message_btn2);
    leaveBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent intent = new Intent(ArScanActivity.this,LeaveMessageActivity.class);
        startActivity(intent);
      }
    });
  }

  /**
   * AR 留言卡片的展示
   */
  private void displayAr() {

    // Build a renderable from a 2D View.
    CompletableFuture<ViewRenderable> audioCardStage =
            ViewRenderable.builder().setView(this, R.layout.audio_card).build();
    CompletableFuture.allOf(
            audioCardStage)
            .handle(
                    (notUsed, throwable) -> {
                      // When you build a Renderable, Sceneform loads its resources in the background while
                      // returning a CompletableFuture. Call handle(), thenAccept(), or check isDone()
                      // before calling get().

                      if (throwable != null) {
                        ArUtils.displayError(this, "Unable to load renderable", throwable);
                        return null;
                      }

                      try {
                        audioCardRenderable = audioCardStage.get();
                        // Everything finished loading successfully.
                        hasFinishedLoading = true;

                      } catch (InterruptedException | ExecutionException ex) {
                        ArUtils.displayError(this, "Unable to load renderable", ex);
                      }

                      return null;
                    });

    // Set up a tap gesture detector.
    gestureDetector =
            new GestureDetector(
                    this,
                    new GestureDetector.SimpleOnGestureListener() {
                      @Override
                      public boolean onSingleTapUp(MotionEvent e) {
                        onSingleTap(e);
                        return true;
                      }

                      @Override
                      public boolean onDown(MotionEvent e) {
                        return true;
                      }
                    });

    // Set a touch listener on the Scene to listen for taps.
    arSceneView
            .getScene()
            .setOnTouchListener(
                    (HitTestResult hitTestResult, MotionEvent event) -> {
                      // If the solar system hasn't been placed yet, detect a tap and then check to see if
                      // the tap occurred on an ARCore plane to place the solar system.
                      if (!hasPlacedSolarSystem) {
                        return gestureDetector.onTouchEvent(event);
                      }

                      // Otherwise return false so that the touch event can propagate to the scene.
                      return false;
                    });

    // Set an update listener on the Scene that will hide the loading message once a Plane is
    // detected.
    arSceneView
            .getScene()
            .addOnUpdateListener(
                    frameTime -> {
                      if (loadingMessageSnackbar == null) {
                        return;
                      }

                      Frame frame = arSceneView.getArFrame();
                      if (frame == null) {
                        return;
                      }

                      if (frame.getCamera().getTrackingState() != TrackingState.TRACKING) {
                        return;
                      }

                      for (Plane plane : frame.getUpdatedTrackables(Plane.class)) {
                        if (plane.getTrackingState() == TrackingState.TRACKING) {
                          hideLoadingMessage();
                        }
                      }
                    });

    // Lastly request CAMERA permission which is required by ARCore.
    ArUtils.requestCameraPermission(this, RC_PERMISSIONS);

  }


//  @Override
//  protected void onStart() {
//    super.onStart();
//    Intent intent = new Intent(this,GetMessageListService.class);
//    bindService(intent,connection, Context.BIND_AUTO_CREATE);
//
//    //todo 服务改成url请求
//    List<Message> list = mService.getMessageList();
//  }

  @Override
  protected void onResume() {
    super.onResume();
    if (arSceneView == null) {
      return;
    }

    if (arSceneView.getSession() == null) {
      // If the session wasn't created yet, don't resume rendering.
      // This can happen if ARCore needs to be updated or permissions are not granted yet.
      try {
        Config.LightEstimationMode lightEstimationMode =
            Config.LightEstimationMode.ENVIRONMENTAL_HDR;
        Session session =
            cameraPermissionRequested
                ? ArUtils.createArSessionWithInstallRequest(this, lightEstimationMode)
                : ArUtils.createArSessionNoInstallRequest(this, lightEstimationMode);
        if (session == null) {
          cameraPermissionRequested = ArUtils.hasCameraPermission(this);
          return;
        } else {
          arSceneView.setupSession(session);
        }
      } catch (UnavailableException e) {
        ArUtils.handleSessionException(this, e);
      }
    }

    try {
      arSceneView.resume();
    } catch (CameraNotAvailableException ex) {
      ArUtils.displayError(this, "Unable to get camera", ex);
      finish();
      return;
    }

    if (arSceneView.getSession() != null) {
      showLoadingMessage();
    }
  }

  @Override
  public void onPause() {
    super.onPause();
    if (arSceneView != null) {
      arSceneView.pause();
    }
  }

//  @Override
//  protected void onStop() {
//    super.onStop();
//    unbindService(connection);
//    mBound = false;
//  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    if (arSceneView != null) {
      arSceneView.destroy();
    }
  }

  @Override
  public void onRequestPermissionsResult(
      int requestCode, @NonNull String[] permissions, @NonNull int[] results) {
    if (!ArUtils.hasCameraPermission(this)) {
      if (!ArUtils.shouldShowRequestPermissionRationale(this)) {
        // Permission denied with checking "Do not ask again".
        ArUtils.launchPermissionSettings(this);
      } else {
        Toast.makeText(
                this, "Camera permission is needed to run this application", Toast.LENGTH_LONG)
            .show();
      }
      finish();
    }
  }

  @Override
  public void onWindowFocusChanged(boolean hasFocus) {
    super.onWindowFocusChanged(hasFocus);
    if (hasFocus) {
      // Standard Android full-screen functionality.
      getWindow()
          .getDecorView()
          .setSystemUiVisibility(
              View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                  | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                  | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                  | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                  | View.SYSTEM_UI_FLAG_FULLSCREEN
                  | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
      getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
  }

  private void onSingleTap(MotionEvent tap) {
    if (!hasFinishedLoading) {
      // We can't do anything yet.
      return;
    }

    Frame frame = arSceneView.getArFrame();
    if (frame != null) {
      if (!hasPlacedSolarSystem && tryPlaceSolarSystem(tap, frame)) {
        hasPlacedSolarSystem = true;
      }
    }
  }

  private boolean tryPlaceSolarSystem(MotionEvent tap, Frame frame) {
    if (tap != null && frame.getCamera().getTrackingState() == TrackingState.TRACKING) {
      for (HitResult hit : frame.hitTest(tap)) {
        Trackable trackable = hit.getTrackable();
        if (trackable instanceof Plane && ((Plane) trackable).isPoseInPolygon(hit.getHitPose())) {
          // Create the Anchor.
          Anchor anchor = hit.createAnchor();
          AnchorNode anchorNode = new AnchorNode(anchor);
          anchorNode.setParent(arSceneView.getScene());
          Node solarSystem = createSolarSystem();
          anchorNode.addChild(solarSystem);
          return true;
        }
      }
    }

    return false;
  }

  private Node createSolarSystem() {
    Node base = new Node();
    Node audioCardNode = new Node();
    audioCardNode.setParent(base);
    audioCardNode.setRenderable(audioCardRenderable);
    audioCardNode.setLocalPosition(new Vector3(0.0f,0.1f,0.0f));

    View audioCardView = audioCardRenderable.getView();

    audioCardView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        // 跳转到读取语音界面
        Log.d(TAG,"你点击了语音条");
        Message m = messages.getMessageList().get(0);

        Intent intent = new Intent(ArScanActivity.this,ReadActivity.class);
        intent.putExtra("msgId",m.getMsId());
        startActivity(intent);
      }
    });
    return base;
  }


  private void showLoadingMessage() {
    if (loadingMessageSnackbar != null && loadingMessageSnackbar.isShownOrQueued()) {
      return;
    }

    loadingMessageSnackbar =
        Snackbar.make(
            ArScanActivity.this.findViewById(android.R.id.content),
            R.string.plane_finding,
            Snackbar.LENGTH_INDEFINITE);
    loadingMessageSnackbar.getView().setBackgroundColor(0xbf323232);
    loadingMessageSnackbar.show();
  }

  private void hideLoadingMessage() {
    if (loadingMessageSnackbar == null) {
      return;
    }

    loadingMessageSnackbar.dismiss();
    loadingMessageSnackbar = null;
  }

  /**
   * 开启定位功能
   */
  private void locationStart() {
    //定位初始化
    mLocationClient = new LocationClient(getApplicationContext());

    //通过LocationClientOption设置LocationClient相关参数
    LocationClientOption option = new LocationClientOption();
    option.setOpenGps(true); // 打开gps
    option.setCoorType("bd09ll"); // 设置坐标类型
    option.setScanSpan(5000);// 设置发起连续定位请求的间隔需要大于等于1000ms才是有效的
    option.setNeedDeviceDirect(true); // 设置是否需要设备方向结果
    //可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
    option.setLocationNotify(false);
    option.setIgnoreKillProcess(false);

    //设置打开自动回调位置模式，该开关打开后，
    // 期间只要定位SDK检测到位置变化就会主动回调给开发者，
    // 该模式下开发者无需再关心定位间隔是多少，
    // 定位SDK本身发现位置变化就会及时回调给开发者
//    option.setOpenAutoNotifyMode();

    //　参数含义：
    // minTimeInterval:3000
    // minDistance: 1
    // locSensitivity:
//    option.setOpenAutoNotifyMode(3000,2,LocationClientOption.LOC_SENSITIVITY_HIGHT);


    //设置locationClientOption
    mLocationClient.setLocOption(option);

    //注册LocationListener监听器
    MyLocationListener myLocationListener = new MyLocationListener();
    mLocationClient.registerLocationListener(myLocationListener);
    //开启地图定位图层
    mLocationClient.start();
  }


}
