package com.linsaya.view_guaguaka.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import com.linsaya.view_guaguaka.R;

/**
 * Created by Administrator on 2017/2/22.
 */

public class Guaguaka extends View {

    //底部图层的变量
    private Path mPath;
    private Bitmap mBitmap;
    private Paint mOutterPaint;
    private Canvas mCanvas;
    private int mLastX;
    private int mLastY;

    //顶部图层的变量
    private Bitmap bitmap;
    private Paint mInnerPaint;
    private Rect mRect;
    private String mText;
    private int mTextSize;
    private int mTextColor = 0xff000000;

    //临时变量，控制
    private boolean isComplete = false;


    public Guaguaka(Context context) {
        this(context, null);

    }

    public Guaguaka(Context context, AttributeSet attrs) {
        this(context, attrs, 0);

    }

    public Guaguaka(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
        TypedArray typedArray = null;
        try {
            typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.Guaguaka, defStyleAttr, 0);
            int count = typedArray.getIndexCount();
            for (int i = 0; i < count; i++) {
                int attr = typedArray.getIndex(i);
                switch (attr) {
                    case R.styleable.Guaguaka_text:
                        mText = typedArray.getString(attr);

                        break;
                    case R.styleable.Guaguaka_textSize:
                        mTextSize = (int) typedArray.getDimension(attr, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                                22, getResources().getDisplayMetrics()));
                        break;
                    case R.styleable.Guaguaka_textColor:
                        mTextColor = typedArray.getColor(attr, 0x000000);
                        break;
                }
            }
        } finally {
            if (typedArray != null) {
                typedArray.recycle();
            }
        }
    }

    public void setText(String mText) {
        this.mText = mText;
        mInnerPaint.getTextBounds(mText, 0, mText.length(), mRect);
    }

    /**
     * 创建借口给外部调用。如弹出对话框通知用户相关信息
     */
    public interface OnCompleteListener {
        void onComplete();
    }

    public OnCompleteListener listener;

    public void setOnCompleteListener(OnCompleteListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int height = getMeasuredHeight();
        int width = getMeasuredWidth();
        //创建覆盖在中奖信息上面的图层
        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
        //初始化画笔
        initOutterPaint();
        initInnerPaint();
        //绘制刮刮卡边角的弧度
        mCanvas.drawRoundRect(new RectF(0, 0, width, height), 30, 30, mOutterPaint);
        //绘制刮刮卡中部的图片，
        mCanvas.drawBitmap(bitmap, null, new Rect(0, 0, width, height), null);

    }

    /**
     * 初始化描画中奖信息的画笔
     */
    private void initInnerPaint() {
        mInnerPaint.setColor(mTextColor);
        mInnerPaint.setStyle(Paint.Style.FILL);
        mInnerPaint.setTextSize(mTextSize);
        mInnerPaint.getTextBounds(mText, 0, mText.length(), mRect);
    }

    private void init() {
        mPath = new Path();
        mOutterPaint = new Paint();
        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.guagua);
        mInnerPaint = new Paint();
        mRect = new Rect();
        mText = "恭喜你中奖啦！";
        mTextSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                22, getResources().getDisplayMetrics());
    }

    /**
     * 初始化外部图层的画笔
     */
    private void initOutterPaint() {
        mOutterPaint.setColor(Color.parseColor("#c0c0c0"));
        //分别设置抗锯齿、防抖动、让线条变得圆滑
        mOutterPaint.setAntiAlias(true);
        mOutterPaint.setDither(true);
        mOutterPaint.setStrokeWidth(20);
        mOutterPaint.setStrokeJoin(Paint.Join.ROUND);
        mOutterPaint.setStrokeCap(Paint.Cap.ROUND);
        mOutterPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        int x = (int) event.getX();
        int y = (int) event.getY();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mLastX = (int) event.getX();
                mLastY = (int) event.getY();
                mPath.moveTo(mLastX, mLastY);
                break;
            case MotionEvent.ACTION_MOVE:
                int dx = Math.abs(mLastX - x);
                int dy = Math.abs(mLastY - y);
                if (dx > 3 || dy > 3) {
                    mPath.lineTo(x, y);
                }
                mLastX = x;
                mLastY = y;

                break;
            case MotionEvent.ACTION_UP:
                //讲运算放到子线程内，防止出现界面的阻塞
                new Thread(mrunable).start();
                break;
        }
        invalidate();

        return true;
    }

    //使用runable对象存放
    private Runnable mrunable = new Runnable() {
        @Override
        public void run() {
            int w = getWidth();
            int h = getHeight();
            float totalArea = w * h;
            float wipeArea = 0;
            Bitmap bitmap = mBitmap;
            //申请图片像素存放的数组，接收位图颜色值的数组
            int[] mPixels = new int[w * h];
            //获取图层信息的像素值
            bitmap.getPixels(mPixels, 0, w, 0, 0, w, h);
            //循环遍历每一个像素
            for (int x = 0; x < w; x++) {
                for (int y = 0; y < h; y++) {
                    int index = x + y * w;
                    if (mPixels[index] == 0) {
                        wipeArea++;
                    }
                }
            }
            //计算百分比，当前图层擦出超出50%时，显示中奖信息
            if (totalArea > 0 && wipeArea > 0) {
                int percent = (int) (wipeArea * 100 / totalArea);
                Log.e("tag", percent + "");
                if (percent > 50) {
                    isComplete = true;
                }
            }
        }
    };


    @Override
    protected void onDraw(Canvas canvas) {
        mOutterPaint.setStyle(Paint.Style.STROKE);
        //canvas.drawBitmap(bitmap,0,0,null);
        //绘制中奖信息，高度为控件一半加文字高度一半，宽度为控件宽度一半减去文字的一半
        canvas.drawText(mText, getWidth() / 2 - mRect.width() / 2, getHeight() / 2 + mRect.height() / 2, mInnerPaint);
        if (isComplete) {
            if (listener != null) {
                listener.onComplete();
            }
        }
        if (!isComplete) {
            drawPath();
            canvas.drawBitmap(mBitmap, 0, 0, null);
        }
        super.onDraw(canvas);
    }

    private void drawPath() {
        //此方法的作用为：后面画出的路径会覆盖前面已经绘制的图层
        mOutterPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
        mCanvas.drawPath(mPath, mOutterPaint);
    }
}
