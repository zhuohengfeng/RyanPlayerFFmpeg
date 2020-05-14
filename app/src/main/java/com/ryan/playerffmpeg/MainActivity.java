package com.ryan.playerffmpeg;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ryan.playerffmpeg.base.BaseActivity;
import com.ryan.playerffmpeg.player.OnInitializedCallback;
import com.ryan.playerffmpeg.player.PlayerController;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends BaseActivity implements SeekBar.OnSeekBarChangeListener {

    private SurfaceView surfaceView;
    private PlayerController playerController;
    private SeekBar playerSeekBar;
    private TextView current_time_label;
    private TextView end_time_label;

    private float playTimeSeconds = 0.0f;
    private float bufferedTimeSeconds = 0.0f; // 换成的时间
    private float totalDuration = 0.0f; // 总的时间
    public static final int UPDATE_PLAY_VIEDO_TIME_FLAG = 1201;
    private Timer timer;
    private TimerTask timerTask;
    private SurfaceHolder surfaceHolder = null;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void initView() {
        // 播放进度条
        playerSeekBar = (SeekBar) findViewById(R.id.music_seek_bar);
        playerSeekBar.setOnSeekBarChangeListener(this);
        // 当前播放时间
        current_time_label = (TextView) findViewById(R.id.current_time_label);
        // 播放结束时间
        end_time_label = (TextView) findViewById(R.id.end_time_label);
        // 显示画面
        surfaceView = (SurfaceView) findViewById(R.id.gl_surface_view);
        surfaceView.getLayoutParams().height = getWindowManager().getDefaultDisplay().getWidth();
        SurfaceHolder mSurfaceHolder = surfaceView.getHolder();
        // 设置surface的callback
        mSurfaceHolder.addCallback(previewCallback);
        // 暂停
        findViewById(R.id.pause_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playerController.pause();
            }
        });
        // 播放
        findViewById(R.id.continue_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timer = new Timer();
                // 没100ms执行一次， 更新播放进度
                timerTask = new TimerTask() {
                    @Override
                    public void run() {
                        playTimeSeconds = playerController.getPlayProgress(); // 获取播放进度
                        bufferedTimeSeconds = playerController.getBufferedProgress(); //  获得缓冲进度 返回秒数（单位秒 但是小数点后有3位 精确到毫秒）
                        handler.sendEmptyMessage(UPDATE_PLAY_VIEDO_TIME_FLAG);
                    }
                };
                timer.schedule(timerTask, 500, 100);

                playerController.play(); // 开始播放
                timerStart();
            }
        });
    }

    @Override
    protected void initData() {
        // 启动定时器
        timerStart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Free the native renderer
        Log.i("problem", "playerController.stop()...");
        playerController.stopPlay();
        timerStop();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                finish();
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    protected void timerStop() {
        if (null != timerTask) {
            timerTask.cancel();
            timerTask = null;
        }
        if (null != timer) {
            timer.cancel();
            timer = null;
        }
    }

    protected void timerStart() {
        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                if(null != playerController) {
                    playTimeSeconds = playerController.getPlayProgress();
                    bufferedTimeSeconds = playerController.getBufferedProgress();
                    handler.sendEmptyMessage(UPDATE_PLAY_VIEDO_TIME_FLAG);
                }
            }
        };
        timer.schedule(timerTask, 500, 100);
    }

    private static final int PLAY_END_FLAG = 12330;
    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_PLAY_VIEDO_TIME_FLAG: // 播放时，更新UI，显示播放时间进度等
                    if (!isDragging) {
                        // 当前时间
                        String curtime = String.format("%02d:%02d", (int) playTimeSeconds / 60, (int) playTimeSeconds % 60);
                        // 总的时间
                        String totalTime = String.format("%02d:%02d", (int) totalDuration / 60, (int) totalDuration % 60);
                        current_time_label.setText(curtime);
                        end_time_label.setText(totalTime);
                        int progress = totalDuration == 0.0f ? 0 : (int) (playTimeSeconds * 100 / totalDuration);
                        int secondProgress = totalDuration == 0.0f ? 0 : (int) (bufferedTimeSeconds * 100 / totalDuration);
                        playerSeekBar.setProgress(progress);
                        playerSeekBar.setSecondaryProgress(secondProgress); // 一个进度条可以显示2个进度条！！！！！！
                    }
                    break;
                case PLAY_END_FLAG:
                    Toast.makeText(MainActivity.this, "播放结束了！", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }

    };

    boolean isFirst = true;

    private SurfaceHolder.Callback previewCallback = new SurfaceHolder.Callback() {

        // 1. surface创建
        public void surfaceCreated(SurfaceHolder holder) {
            surfaceHolder = holder;

            if (isFirst) {
                // 总的播放器native管理类
                playerController = new PlayerController() {

                    @Override
                    public void showLoadingDialog() {
                        super.showLoadingDialog();
                    }

                    @Override
                    public void hideLoadingDialog() {
                        super.hideLoadingDialog();
                    }

                    @Override
                    public void onCompletion() {
                        super.onCompletion();

                        playerController.pause();
                        timerTask.cancel();
                        timerTask = null;
                        timer.cancel();
                        timer = null;
                        playerSeekBar.setProgress(0);
                        playerSeekBar.setSecondaryProgress(0);
                        playerController.seekToPosition(0);
                    }

                    @Override
                    public void videoDecodeException() {
                        super.videoDecodeException();
                    }

                    @Override
                    public void viewStreamMetaCallback(final int width, final int height, float duration) {
                        super.viewStreamMetaCallback(width, height, duration);

                        MainActivity.this.totalDuration = duration;
                        handler.post(new Runnable() {

                            @Override
                            public void run() {
                                int screenWidth = getWindowManager().getDefaultDisplay().getWidth();
                                int drawHeight = (int) ((float) screenWidth / ((float) width / (float) height));
                                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) surfaceView.getLayoutParams();
                                params.height = drawHeight;
                                surfaceView.setLayoutParams(params);

                                playerController.resetRenderSize(0, 0, screenWidth, drawHeight);
                            }
                        });
                    }

                };
                //playerController.setUseMediaCodec(false); // 这里设置是否使用硬解码，还是使用软解码
                int width = getWindowManager().getDefaultDisplay().getWidth();
                //String path = "http://vfx.mtime.cn/Video/2019/02/04/mp4/190204084208765161.mp4";
                //String path = "rtmp://58.200.131.2:1935/livetv/hunantv";
                //String path = "http://live.hkstv.hk.lxdns.com/live/hks/playlist.m3u8";
                String path = "/sdcard/movies/big_buck_bunny.mp4";
                playerController.init(path, holder.getSurface(), width, width, new OnInitializedCallback() {
                    public void onInitialized(OnInitialStatus onInitialStatus) {
                        // TODO: do your work here
                        Log.i("problem", "onInitialized called");
                    }
                });

                isFirst = false;
            }
            else {
                playerController.onSurfaceCreated(holder.getSurface());
            }
        }

        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            playerController.resetRenderSize(0, 0, width, height);
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            playerController.onSurfaceDestroyed(holder.getSurface());
        }
    };

    /** 是否正在拖动，如果正在拖动的话，就不要在改变seekbar的位置 **/
    private boolean isDragging = false;

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        isDragging = true;
        playerController.beforeSeekCurrent();
        Log.i("problem", "onStartTrackingTouch");
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (isDragging) {
            float pro = seekBar.getProgress();
            float num = seekBar.getMax();
            float result = pro / num;

            seekCurrent(result * totalDuration);

//			Log.i("problem", "onProgressChanged "+result * totalDuration);
        }
        else {
            // 也可能在seek后会有个位置矫正，如果这个时候isDragging被置false了，也可能调到这里来。这个再考虑
//			Log.i("problem", "this change from play callback "+progress);
        }
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        isDragging = false;
//		float pro = seekBar.getProgress();
//		float num = seekBar.getMax();
//		float result = pro / num;
//		seekToPosition(result * totalDuration);

        playerController.afterSeekCurrent();

        Log.i("problem", "onStopTrackingTouch");
    }

    public void seekToPosition(float position) {
        Log.i("problem", "position:" + position);
        playerController.seekToPosition(position);
    }

    public void seekCurrent(float position) {
//		Log.i("problem", "position:" + position);
        playerController.seekCurrent(position);
    }

}