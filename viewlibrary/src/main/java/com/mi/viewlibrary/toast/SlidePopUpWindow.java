package com.mi.viewlibrary.toast;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.mi.viewlibrary.R;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

//http://weishu.me/2019/03/16/another-free-reflection-above-android-p/
//https://blog.csdn.net/fan7983377/article/details/79488903
//https://blog.csdn.net/qq_22706515/article/details/51244191
public class SlidePopUpWindow implements View.OnTouchListener {

    //显示的时长，应该可以多态设置，不过我懒不想再抽取了。
    private static final long SHOW_TIME = 2000;
    private boolean mIsShow;
    private final Toast mToast;
    private final View mToastView;
    private Object mTN;

    private static SlidePopUpWindow mIns;
    private WindowManager windowManagerRef;
    private WindowManager.LayoutParams mParams;
    private static final int MSG_SHOW = 1;
    private static final int MSG_HIDE = 2;
    //屏幕宽度
    private int mScreenWidth = 0;
    //toast高度单位为dp
    private static int mToastHight = 60;
    android.os.Handler mHandler = new android.os.Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_HIDE:
                    hideView();
                    break;
                case MSG_SHOW:
                    showView();
                    break;
            }
        }
    };
    //重置动画
    private ValueAnimator restoreAnimator;
    //消失动画
    private ValueAnimator dismissAnimator;

    private void showView() {
        AlertDialog alertDialog = new AlertDialog();
        alertDialog.show();
        if (!mIsShow) {//如果Toast没有显示，则开始加载显示
            mIsShow = true;
            mToast.show();//将其加载到windowManager上
            mToastView.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        windowManagerRef = (WindowManager) getField(mTN, "android.widget.Toast$TN", "mWM");
                        Log.e("zty", "window manager : " + windowManagerRef);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    private void hideView() {
        if (mIsShow) {
            mToast.cancel();
            mIsShow = false;
            mParams.x = 0;//重置状态
            mToastView.setAlpha(1.0f);
        }
    }

    private SlidePopUpWindow(Context context) {
        mIsShow = false;//记录当前Toast的内容是否已经在显示
        //加载自己的布局
        mToastView = View.inflate(context, R.layout.layout_toast, null);
        //设置touch监听
        mToastView.setOnTouchListener(this);
        //获取屏幕宽度
        mScreenWidth = context.getResources().getDisplayMetrics().widthPixels;
        //设置布局参数
        mToast = new Toast(context.getApplicationContext());
        mToast.setDuration(Toast.LENGTH_SHORT);
        mToast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 0);
        mToast.setView(mToastView);

        try {
            mTN = getField(mToast, "android.widget.Toast", "mTN");
            if (mTN != null) {
                Object paramsRef = getField(mTN, "android.widget.Toast$TN", "mParams");
                if (paramsRef instanceof WindowManager.LayoutParams) {
                    mParams = (WindowManager.LayoutParams) paramsRef;
                    mParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
                    mParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
                    mParams.format = PixelFormat.TRANSLUCENT;
                    mParams.windowAnimations = R.style.anim_view;//设置进入退出动画效果
                    //
                    mParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                            | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                            | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
                    mParams.gravity = Gravity.CENTER_HORIZONTAL | Gravity.TOP;
                    mParams.width = mScreenWidth;
                    //在Toast高度上加上状态栏的高度
                    mParams.height = dip2px(context, mToastHight);
                    mParams.y = 0;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Object getField(Object object, String className, String fieldName)
            throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        Method forName = Class.class.getDeclaredMethod("forName", String.class);
        Method getDeclaredField = Class.class.getDeclaredMethod("getDeclaredField", String.class);

        Class<?> clazz = (Class<?>) forName.invoke(null, className);
        Field field = (Field) getDeclaredField.invoke(clazz, fieldName);

        if (field != null) {
            field.setAccessible(true);
            return field.get(object);
        }
        return null;
    }

    private void setField(Object object, Object value, String className, String fieldName)
            throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        Method forName = Class.class.getDeclaredMethod("forName", String.class);
        Method getDeclaredField = Class.class.getDeclaredMethod("getDeclaredField", String.class);

        Class<?> clazz = (Class<?>) forName.invoke(null, className);
        Field field = (Field) getDeclaredField.invoke(clazz, fieldName);

        if (field != null) {
            field.setAccessible(true);
            field.set(object, value);
        }
    }

    public static SlidePopUpWindow getIns(Context context) {
        if (mIns == null) {
            mIns = new SlidePopUpWindow(context);
        }
        return mIns;
    }

    public void show() {
        //发送显示消息
        mHandler.sendEmptyMessage(MSG_SHOW);
        //取消之前的消息，重新发送，防止多次调用。
        mHandler.removeMessages(MSG_HIDE);
        mHandler.sendEmptyMessageDelayed(MSG_HIDE, SHOW_TIME);
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    //按下的位置
    int xdown = 0;
    //记录按下的时间，用于计算速度
    long downTime = 0;
    //记录第一次按下的x坐标
    int mFistDown = 0;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        //如果正在动画直接return
        if ((restoreAnimator != null && restoreAnimator.isRunning() || (dismissAnimator != null && dismissAnimator.isRunning()))) {
            return true;
        }
        //记录下点击的x点
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                xdown = (int) event.getRawX();
                mFistDown = xdown;
                downTime = System.currentTimeMillis();
                break;
            case MotionEvent.ACTION_MOVE:
                //计算滑动距离
                int moveX = (int) (event.getRawX() - xdown);
                //更新位置
                update(mParams.x + moveX, 0);
                //取消自动消失
                mHandler.removeMessages(MSG_HIDE);
                //更新按下的位置
                xdown = (int) event.getRawX();
                break;
            case MotionEvent.ACTION_UP:
                //计算速度
                long usetime = System.currentTimeMillis() - downTime;
                int speeed = (int) ((xdown - mFistDown) * 1000 / usetime);
                //根据位置设置相应的动画
                autoDismissOrRestore(speeed);
                break;
        }
        return true;
    }

    private void autoDismissOrRestore(int speeed) {
        //判断是关闭还是还原
        boolean needDismis = true;
        boolean leftToDismiss = true;
        //在低速情况下
        if (Math.abs(speeed) < 1500) {
            //如果X坐标大于屏幕的-1/2并且小于1/2。还原。并添加关闭事件
            if (mParams.x >= -(mScreenWidth / 2) && mParams.x <= (mScreenWidth / 2)) {
                needDismis = false;
            } else {
                //默认是从左边消失，如果x坐标大于屏幕的1/2则从右边消失
                if (mParams.x > mScreenWidth / 2) {
                    leftToDismiss = false;
                }
            }
        } else {
            //当速度大于消失速度时，自动消失。判断消失的方向。
            if (speeed > 0) {
                leftToDismiss = false;
            }
        }
        //根据状态创建动画。
        if (!needDismis) {
            createRestoreAnimator();
            restoreAnimator.start();
        } else {
            creatDismissAnimator(leftToDismiss);
            dismissAnimator.start();
        }

    }

    private boolean update(int x, int y) {
        if (mIsShow) {
            try {
                mParams.x = x;
                mParams.y = y;
                if (windowManagerRef != null) {
                    windowManagerRef.updateViewLayout(mToastView, mParams);
                    //修改透明度
                    float alpha = (float) (1.0 - 0.5 * Math.abs(mParams.x) * 2.0 / mScreenWidth);
                    mToastView.setAlpha(alpha);
                    return true;
                }
            } catch (Exception e) {//我们无法准确得知toast消失时间，所以这里catch住，防止崩溃
                e.printStackTrace();
            }
        }
        return false;
    }

    private void createRestoreAnimator() {
        if (restoreAnimator == null) {
            restoreAnimator = new ObjectAnimator().ofInt(mParams.x, 0);
            //根据移动距离的百分比设置相应的时长，如果时间一样长的话，在移动距离很短的时候会变的很慢。
            int duration = (int) (250 * Math.abs(mParams.x - 0) * 2.0 / mScreenWidth);
            restoreAnimator.setDuration(duration);
            restoreAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int value = (int) animation.getAnimatedValue();
                    update(value, 0);
                }
            });
            restoreAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mHandler.sendEmptyMessageDelayed(MSG_HIDE, 2000);
                    restoreAnimator = null;
                }
            });
        }
    }

    private void creatDismissAnimator(boolean left) {
        if (dismissAnimator == null) {
            //根据消失的方向设置目标值
            dismissAnimator = new ObjectAnimator().ofInt(mParams.x, left ? -mScreenWidth : mScreenWidth);
            dismissAnimator.setDuration(200);
            dismissAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int value = (int) animation.getAnimatedValue();
                    update(value, 0);
                }
            });
            dismissAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    //立即消失
                    mHandler.sendEmptyMessage(MSG_HIDE);
                    dismissAnimator = null;
                }
            });
        }
    }

}
