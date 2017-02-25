package comm.example.master.mapdemo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewParent;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.clusterutil.clustering.Cluster;
import com.baidu.mapapi.clusterutil.clustering.ClusterItem;
import com.baidu.mapapi.clusterutil.clustering.ClusterManager;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.navi.BaiduMapNavigation;
import com.baidu.mapapi.navi.NaviParaOption;
import com.baidu.mapapi.overlayutil.WalkingRouteOverlay;
import com.baidu.mapapi.radar.RadarNearbyResult;
import com.baidu.mapapi.radar.RadarNearbySearchOption;
import com.baidu.mapapi.radar.RadarSearchError;
import com.baidu.mapapi.radar.RadarSearchListener;
import com.baidu.mapapi.radar.RadarSearchManager;
import com.baidu.mapapi.radar.RadarUploadInfo;
import com.baidu.mapapi.radar.RadarUploadInfoCallback;
import com.baidu.mapapi.search.route.BikingRouteResult;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.IndoorRouteResult;
import com.baidu.mapapi.search.route.MassTransitRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRouteLine;
import com.baidu.mapapi.search.route.WalkingRoutePlanOption;
import com.baidu.mapapi.search.route.WalkingRouteResult;
import com.baidu.mapapi.utils.route.RouteParaOption;
import comm.example.master.mapdemo.MyView;



import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements RadarUploadInfoCallback{
    private BaiduMap map;
    private MapView mapView;
    private String TAG="MapActivity";
    private LatLng point;
    private MyView linearLayout;
    private Marker marker1;
    private Marker marker2;
    private ViewPager vp;
    private MarkerOptions options;
    private MarkerOptions options2;
    private WalkingRouteOverlay overlay;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        initView();
        initData();

    }

    //31.3508810000,121.3787860000     31.3507690000,121.3782920000
    private void initData() {
        point=new LatLng(31.350287,121.378746);
        // 开启定位图层
        map.setMyLocationEnabled(true);
        // 构造定位数据
        MyLocationData locData = new MyLocationData.Builder()
                .accuracy(1)
                // 此处设置开发者获取到的方向信息，顺时针0-360
                .direction(100).latitude(point.latitude)
                .longitude(point.longitude).build();
        // 设置定位数据
        map.setMyLocationData(locData);
        // 设置定位图层的配置（定位模式，是否允许方向信息，用户自定义定位图标）
        MyLocationConfiguration config = new MyLocationConfiguration(MyLocationConfiguration.LocationMode.COMPASS, true, null);
        map.setMyLocationConfigeration(config);
        BitmapDescriptor bitmapDescriptor=BitmapDescriptorFactory.fromView(LayoutInflater.from(getApplicationContext()).inflate(R.layout.iv_marker,null));
        options = new MarkerOptions().position(new LatLng(31.351559,121.376814)).title("中影国际影院").icon(bitmapDescriptor).zIndex(0).period(5).animateType(MarkerOptions.MarkerAnimateType.grow);
        options2 = new MarkerOptions().position(new LatLng(31.348988,121.377016)).title("时代联华超市").icon(bitmapDescriptor).zIndex(0).period(5).animateType(MarkerOptions.MarkerAnimateType.grow);
        marker1 = (Marker) map.addOverlay(options);
        marker2 = (Marker) map.addOverlay(options2);
        final int height = getWindowManager().getDefaultDisplay().getHeight();
        final int width = getWindowManager().getDefaultDisplay().getWidth();
        map.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                String title = marker.getTitle();
                Log.e(TAG, "onMarkerClick: "+ title);
                if (title!=null){
                    if (overlay!=null){
                        overlay.removeFromMap();
                    }
                    LatLng latLng = map.getProjection().fromScreenLocation(new Point(width / 2, height / 2));
                    Log.e(TAG, "onMarkerClick: latitude:"+latLng.latitude+"longitude:"+latLng.longitude);
                    linearLayout.setVisibility(View.VISIBLE);
                    if (title.equals("中影国际影院")){
                        marker1.remove();
                        marker1 = (Marker) map.addOverlay(options);
                        vp.setCurrentItem(0);

                    }else {
                        marker2.remove();
                        marker2= (Marker) map.addOverlay(options2);
                        vp.setCurrentItem(1);

                    }
                    RoutePlanSearch routePlanSearch=RoutePlanSearch.newInstance();
                    WalkingRoutePlanOption routePlanOption=new WalkingRoutePlanOption();
                    routePlanOption.from(PlanNode.withLocation(latLng)).to(PlanNode.withLocation(marker.getPosition()));
                    routePlanSearch.walkingSearch(routePlanOption);
                    routePlanSearch.setOnGetRoutePlanResultListener(new OnGetRoutePlanResultListener() {
                        @Override
                        public void onGetWalkingRouteResult(WalkingRouteResult walkingRouteResult) {
                            Log.e(TAG, "onGetWalkingRouteResult: "+walkingRouteResult.getRouteLines().size());
                            WalkingRouteLine walkingRouteLine = walkingRouteResult.getRouteLines().get(0);
                            overlay = new WalkingRouteOverlay(map);
                            overlay.setData(walkingRouteLine);
                            overlay.zoomToSpan();
                            overlay.addToMap();
                        }

                        @Override
                        public void onGetTransitRouteResult(TransitRouteResult transitRouteResult) {

                        }

                        @Override
                        public void onGetMassTransitRouteResult(MassTransitRouteResult massTransitRouteResult) {

                        }

                        @Override
                        public void onGetDrivingRouteResult(DrivingRouteResult drivingRouteResult) {

                        }

                        @Override
                        public void onGetIndoorRouteResult(IndoorRouteResult indoorRouteResult) {

                        }

                        @Override
                        public void onGetBikingRouteResult(BikingRouteResult bikingRouteResult) {

                        }
                    });
                }
                return false;
            }
        });
        map.setOnMapClickListener(new BaiduMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                linearLayout.setVisibility(View.GONE);
                if (overlay!=null){
                    overlay.removeFromMap();
                }

            }

            @Override
            public boolean onMapPoiClick(MapPoi mapPoi) {
                return false;
            }
        });
        RadarSearchManager manager=RadarSearchManager.getInstance();
        manager.setUserID("userid");
        //设置自动上传的callback和时间间隔
        manager.startUploadAuto(this, 5000);

        //构造请求参数，其中centerPt是自己的位置坐标
        final RadarNearbySearchOption option = new RadarNearbySearchOption().centerPt(point).pageNum(1).radius(2000);
        //发起查询请求
        manager.nearbyInfoRequest(option);
        manager.addNearbyInfoListener(new RadarSearchListener() {
            @Override
            public void onGetNearbyInfoList(RadarNearbyResult radarNearbyResult, RadarSearchError radarSearchError) {
                if (radarNearbyResult!=null&&radarNearbyResult.infoList!=null&&radarNearbyResult.infoList.size()!=0){
                    String s = radarNearbyResult.infoList.toString();
                    Log.e(TAG, "onGetNearbyInfoList: "+s+"error:"+radarSearchError.toString());
                    MarkerOptions options=new MarkerOptions();
                    options.position(radarNearbyResult.infoList.get(0).pt);
                    options.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher));
                    map.addOverlay(options);
                }else {
                    Log.e(TAG, "onGetNearbyInfoList: failed");
                }

            }

            @Override
            public void onGetUploadState(RadarSearchError radarSearchError) {

            }

            @Override
            public void onGetClearInfoState(RadarSearchError radarSearchError) {

            }
        });
//         // 定义点聚合管理类ClusterManager
//        ClusterManager<MyItem> mClusterManager = new ClusterManager<MyItem>(this, map);
//        // 添加Marker点
//        List<MyItem> items = new ArrayList<MyItem>();
//        items.add(new MyItem(marker1.getPosition()));
//        items.add(new MyItem(marker2.getPosition()));
//        mClusterManager.addItems(items);
//        // 设置地图监听，当地图状态发生改变时，进行点聚合运算
//        map.setOnMapStatusChangeListener(mClusterManager);

    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mapView.onDestroy();
    }
    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mapView.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mapView.onPause();
    }

    @Override
    public RadarUploadInfo onUploadInfoCallback() {
        RadarUploadInfo info=new RadarUploadInfo();
        info.pt=point;
        info.comments="test";
        return info;
    }

    private void initView() {
        vp = (ViewPager) findViewById(R.id.vp_main);
        initVp();
        mapView = (MapView) findViewById(R.id.map_view);
        mapView.setZOrderMediaOverlay(false);
        map = mapView.getMap();
        map.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        linearLayout = (MyView) findViewById(R.id.linear_map);
        map.setOnMapLoadedCallback(new BaiduMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                MapStatus status=new MapStatus.Builder().zoom(20).build();
                map.animateMapStatus(MapStatusUpdateFactory.newMapStatus(status));
            }
        });
    }

    private void initVp() {
        final List<Fragment> fragments = new ArrayList<>();
        InfoFragment info=new InfoFragment();
        info.setTitle("中影国际影院");
        fragments.add(info);
        InfoFragment info2=new InfoFragment();
        info2.setTitle("时代联华超市");
        fragments.add(info2);
        vp.setAdapter(new MyAdapter(getSupportFragmentManager(),fragments));
        vp.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                Log.e(TAG, "onPageScrolled: "+position);
            }

            @Override
            public void onPageSelected(int position) {
                InfoFragment fragment = (InfoFragment) fragments.get(position);
                String title = fragment.getTitle();
                Log.e(TAG, "onPageSelected: "+title);
                if (position==0){
                    marker1.remove();
                    marker1= (Marker) map.addOverlay(options);
                }else {
                    marker2.remove();
                    marker2= (Marker) map.addOverlay(options2);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }
    class MyAdapter extends FragmentPagerAdapter{
        private List<Fragment> fragments;
        public MyAdapter(FragmentManager fm) {
            super(fm);
        }

        public MyAdapter(FragmentManager fm, List<Fragment> fragments) {
            super(fm);
            this.fragments = fragments;
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }
    }
    /**
     * 每个Marker点，包含Marker点坐标以及图标
     */
    public class MyItem implements ClusterItem {
        private final LatLng mPosition;

        public MyItem(LatLng latLng) {
            mPosition = latLng;
        }

        @Override
        public LatLng getPosition() {
            return mPosition;
        }

        @Override
        public BitmapDescriptor getBitmapDescriptor() {
            return BitmapDescriptorFactory
                    .fromResource(R.mipmap.ic_launcher);
        }
    }


}
