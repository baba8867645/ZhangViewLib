package com.mi.zhangviewlib;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.mi.viewlibrary.toast.SlidePopUpWindow;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.text).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });
    }

    public void show(View view) {
        SlidePopUpWindow.getIns(getApplicationContext()).show();
//        toast();
//        final View popView = View.inflate(this, com.mi.viewlibrary.R.layout.layout_toast, null);
//        popView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Log.e("zty", "click");
//            }
//        });
//        final Toast toast = new Toast(getApplicationContext());
//        toast.setGravity(Gravity.TOP, 0, 0);
//        toast.setView(popView);
//        toast.setDuration(Toast.LENGTH_LONG);
//        toast.show();
//        popView.post(new Runnable() {
//            @Override
//            public void run() {
//                WindowManager.LayoutParams params = (WindowManager.LayoutParams) popView.getLayoutParams();
//                params.flags = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
//                        | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
//                popView.setLayoutParams(params);
//                Log.e("zty", "layout params : " + popView.getParent());
//            }
//        });
//        Log.e("zty", "show");
    }

    private void toast() {
        LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.layout_toast, null);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("zty", "click");
            }
        });
        Toast mToast = Toast.makeText(getApplicationContext(), "", Toast.LENGTH_LONG);
        mToast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 0);
        mToast.setView(view);

        try {
            Object mTN;
            mTN = getField(mToast, "android.widget.Toast", "mTN");
            if (mTN != null) {
                Object mParams = getField(mTN, "android.widget.Toast$TN", "mParams");
                if (mParams != null
                        && mParams instanceof WindowManager.LayoutParams) {
                    WindowManager.LayoutParams params = (WindowManager.LayoutParams) mParams;
                    params.flags = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                            | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
                    params.width = WindowManager.LayoutParams.MATCH_PARENT;
                    params.height = WindowManager.LayoutParams.WRAP_CONTENT;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        mToast.show();
    }

    private static Object getField(Object object, String className, String fieldName)
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
}
