package cn.edu.gdmec.s07150751.mycamera;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.SurfaceHolder.Callback;
import android.view.View.OnClickListener;
import android.widget.ImageView;

public class CameraActivity extends AppCompatActivity implements OnClickListener,Callback {

    private SurfaceView   mSurfaceView;
    private ImageView     mImageView;
    private SurfaceHolder mSurfaceHolder;
    private ImageView     shutter;
    private Camera        mCamera = null;
    private boolean       mPreviewRunning;
    private static final int MENU_START = 1;
    private static final int MENU_SENSOR = 2;
    private Bitmap bitmap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_camera);
        mSurfaceView = (SurfaceView) findViewById(R.id.camera);
        mImageView = (ImageView) findViewById(R.id.image);
        shutter= (ImageView) findViewById(R.id.shutter);

        shutter.setOnClickListener(this);
        mImageView.setVisibility(View.GONE);
        mSurfaceHolder = mSurfaceView.getHolder();

        mSurfaceHolder.addCallback(this);

        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);


    }

    public void onClick(View v) {


        if (mPreviewRunning) {

            shutter.setEnabled(false);

            mCamera.autoFocus(new AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean success, Camera camera) {

                    mCamera.takePicture(mShutterCallback,
                            null, mPictureCallback);
                }});
        }
    }

    PictureCallback mPictureCallback = new PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera1) {

            if (data != null) {
                saveAndShow(data);
            }
        }
    };

    ShutterCallback mShutterCallback = new ShutterCallback() {

        public void onShutter() {
            System.out.println("快照回调函数.....");
        }
    };

    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
        try{

            if (mPreviewRunning) {
                mCamera.stopPreview();
            }

            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
            mPreviewRunning = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void surfaceCreated(SurfaceHolder holder) {

        setCameraParams();
    }

    public  void  setCameraParams()
    {
        if(mCamera != null)
        {
            return ;
        }

        mCamera = Camera.open();

        Parameters params = mCamera.getParameters();

        params.setFocusMode(Parameters.FOCUS_MODE_AUTO);

        params.setPreviewFrameRate(3);

        params.setPreviewFormat(PixelFormat.YCbCr_422_SP);

        params.set("jpeg-quality",85);

        List<Size> list = params.getSupportedPictureSizes();
        Size size = list.get(0);
        int w = size.width;
        int h = size.height;

        params.setPictureSize(w, h);

        params.setFlashMode(Parameters.FLASH_MODE_AUTO);
    }


    public void surfaceDestroyed(SurfaceHolder holder) {
        if(mCamera!=null)
        {

            mCamera.stopPreview();
            mPreviewRunning = false;

            mCamera.release();
            mCamera = null;
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, MENU_START,0,  "重拍");
        menu.add(0, MENU_SENSOR,0, "打开相册");
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == MENU_START) {

            setRequestedOrientation(ActivityInfo.
                    SCREEN_ORIENTATION_PORTRAIT);
            setRequestedOrientation(ActivityInfo.
                    SCREEN_ORIENTATION_LANDSCAPE);
            return true;
        }else if(item.getItemId() == MENU_SENSOR)
        {
            Intent intent = new Intent(this,AlbumActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    public void  saveAndShow(byte[] data)
    {
        try
        {

            String imageId=System.currentTimeMillis()+"";

            String pathName = android.os.Environment.
                    getExternalStorageDirectory().getPath()
                    +"/com.demo.pr4";

            File file=new File(pathName);
            if(!file.exists())
            {
                file.mkdirs();
            }

            pathName+="/"+imageId+".jpeg";
            file=new File(pathName);
            if(!file.exists()){
                file.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(data);
            fos.close();
            AlbumActivity album=new AlbumActivity();


            bitmap = album.loadImage(pathName);


            mImageView.setImageBitmap(bitmap);
            mImageView.setVisibility(View.VISIBLE);
            mSurfaceView.setVisibility(View.GONE);

            if(mPreviewRunning) {
                mCamera.stopPreview();
                mPreviewRunning = false;
            }
            shutter.setEnabled(true);
        }catch(Exception e)
        {
            e.printStackTrace();
        }
    }
}
