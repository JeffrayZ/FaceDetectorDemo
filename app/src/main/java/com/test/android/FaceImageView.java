package com.test.android;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.media.FaceDetector;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import java.util.PrimitiveIterator;

public class FaceImageView extends androidx.appcompat.widget.AppCompatImageView {
    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
    private FaceDetector.Face face;
    private PointF pointF = new PointF();
    private float mEyesDistance;

    public void setFace(FaceDetector.Face face) {
        this.face = face;
        invalidate();
    }

    public FaceImageView(Context context) {
        super(context);
    }

    public FaceImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public FaceImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(face != null){
            paint.setStrokeWidth(28);
            paint.setStyle(Paint.Style.STROKE);
            face.getMidPoint(pointF);
            mEyesDistance = face.eyesDistance();
            Log.e("FaceImageView",mEyesDistance+"");
            paint.setColor(Color.GREEN);
            canvas.drawRect(
                    (int)(pointF.x - mEyesDistance * 1.5),
                    (int)(pointF.y - mEyesDistance * 1.5),
                    (int)(pointF.x + mEyesDistance * 0.75),
                    (int)(pointF.y + mEyesDistance*4/3),
                    paint);
        }
    }
}
