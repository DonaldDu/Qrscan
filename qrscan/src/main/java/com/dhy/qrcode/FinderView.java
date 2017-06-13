package com.dhy.qrcode;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;
import com.google.zxing.ResultPoint;


/**
 * This view is overlaid on top of the camera preview. It adds the viewfinder
 * rectangle and partial transparency outside it, as well as the laser scanner
 * animation and result points.
 */
public final class FinderView extends View implements IFinderView {
    private final Paint paint;
    private int backgroundColor, contentColor, strokeColor;
    private int lineAnimatorDuration;
    private float imageDataScale;
    /**
     * full size when init, then half size after onMeasure
     */
    private int contentWidth, contentHeight;
    private int left, top, cornerMargin;
    private BitmapDrawable corner, scanLine;
    private Rect contentRect, backgroundRect, strokeRect, scanLineRect, imageDataRect;

    /**
     * the content rect conter point
     */
    private Point p = new Point();

    public FinderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FinderView);
        backgroundColor = a.getColor(R.styleable.FinderView_backgroundColor, Color.BLUE);
        contentColor = a.getColor(R.styleable.FinderView_contentColor, Color.TRANSPARENT);
        strokeColor = a.getColor(R.styleable.FinderView_strokeColor, Color.YELLOW);
        contentWidth = a.getDimensionPixelOffset(R.styleable.FinderView_contentWidth, 0);
        contentHeight = a.getDimensionPixelOffset(R.styleable.FinderView_contentHeight, 0);
        top = a.getDimensionPixelOffset(R.styleable.FinderView_contentPaddingTop, 100);
        left = a.getDimensionPixelOffset(R.styleable.FinderView_contentPaddingLeft, 0);
        cornerMargin = a.getDimensionPixelOffset(R.styleable.FinderView_contentConerMargin, -5);
        lineAnimatorDuration = a.getInt(R.styleable.FinderView_lineAnimatorDuration, 2000);
        imageDataScale = a.getFloat(R.styleable.FinderView_imageDataScale, 1.5f);
        corner = (BitmapDrawable) a.getDrawable(R.styleable.FinderView_contentConer);
        scanLine = (BitmapDrawable) a.getDrawable(R.styleable.FinderView_contentLine);
        useContentSizeWhenZero(a);
        a.recycle();
        contentWidth = getEvenSize(contentWidth);
        contentHeight = getEvenSize(contentHeight);
    }

    private void useContentSizeWhenZero(TypedArray a) {
        if (contentWidth == 0 || contentHeight == 0) {
            int size = a.getDimensionPixelOffset(R.styleable.FinderView_contentSize, 300);
            contentWidth = contentWidth == 0 ? size : contentWidth;
            contentHeight = contentHeight == 0 ? size : contentHeight;
        }
    }

    private int getEvenSize(int size) {
        if (size % 2 == 1) {
            return ++size;
        }
        return size;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        initOnMeasure();
    }

    void initOnMeasure() {
        if (contentRect == null) {
            initBgAndContentRect();
            p.x = (contentRect.left + contentRect.right) / 2;
            p.y = (contentRect.top + contentRect.bottom) / 2;
            initStrokeRect();
            initScanLineRect();

            //half size
            contentWidth /= 2;
            contentHeight /= 2;
            initLineAnimation();
        }
    }

    void initBgAndContentRect() {
        contentRect = new Rect();
        contentRect.left = getEdgePadding(left, contentWidth, getMeasuredWidth());
        contentRect.top = getEdgePadding(top, contentHeight, getMeasuredHeight());
        contentRect.right = contentRect.left + contentWidth;
        contentRect.bottom = contentRect.top + contentHeight;

        backgroundRect = new Rect(0, 0, getMeasuredWidth(), getMeasuredHeight());
    }

    void initStrokeRect() {
        if (cornerMargin < 0 && strokeColor != Color.TRANSPARENT) {
            strokeRect = new Rect(contentRect.left + cornerMargin, contentRect.top + cornerMargin, contentRect.right - cornerMargin, contentRect.bottom - cornerMargin);
        }
    }

    void initScanLineRect() {
        if (scanLine != null) {
            int height = scanLine.getBitmap().getHeight();
            if (scanLine.getBitmap().getWidth() < contentWidth) {
                int scanLineLeft = (contentWidth - scanLine.getBitmap().getWidth()) / 2;
                scanLineRect = new Rect(contentRect.left + scanLineLeft, contentRect.top, contentRect.right - scanLineLeft, contentRect.top + height);
            } else {
                scanLineRect = new Rect(contentRect.left, contentRect.top, contentRect.right, contentRect.top + height);
            }
            scanLineRect.top = (contentRect.top + contentRect.bottom) / 2;
            scanLineRect.bottom = scanLineRect.top + height;
        }
    }

    private int getEdgePadding(int padding, int size, int all) {
        return padding != 0 ? padding : (all - size) / 2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        paint.setColor(backgroundColor);
        drawStroke(canvas, backgroundRect, contentRect, paint);//draw background

        paint.setColor(contentColor);
        canvas.drawRect(contentRect, paint);

        if (strokeRect != null) {//draw stroke
            paint.setColor(strokeColor);
            drawStroke(canvas, strokeRect, contentRect, paint);
        }

        if (corner != null) {//draw corner
            canvas.save();
            canvas.translate(p.x, p.y);
            canvas.drawBitmap(corner.getBitmap(), cornerMargin - contentWidth, cornerMargin - contentHeight, null);
            canvas.rotate(90);
            canvas.drawBitmap(corner.getBitmap(), cornerMargin - contentHeight, cornerMargin - contentWidth, null);
            canvas.rotate(90);
            canvas.drawBitmap(corner.getBitmap(), cornerMargin - contentWidth, cornerMargin - contentHeight, null);
            canvas.rotate(90);
            canvas.drawBitmap(corner.getBitmap(), cornerMargin - contentHeight, cornerMargin - contentWidth, null);
            canvas.restore();
        }

        //draw scan line
        if (scanLine != null) {
            canvas.drawBitmap(scanLine.getBitmap(), null, scanLineRect, null);
        }
    }

    /**
     * draw stroke between main and content
     */
    void drawStroke(Canvas canvas, Rect main, Rect content, Paint paint) {
        canvas.drawRect(main.left, main.top, main.right, content.top, paint);//top
        canvas.drawRect(main.left, content.top, content.left, content.bottom, paint);//left
        canvas.drawRect(content.right, content.top, main.right, content.bottom, paint);//right
        canvas.drawRect(main.left, content.bottom, main.right, main.bottom, paint);//bottom
    }

    @Override
    public void addPossibleResultPoint(ResultPoint point) {

    }

    @NonNull
    @Override
    public Rect getDecodeRetangle() {
        if (imageDataRect == null) {
            float scale = imageDataScale >= 1 ? imageDataScale - 1 : 0;
            int sw = (int) ((contentRect.right - contentRect.left) * scale / 2);
            int sh = (int) ((contentRect.bottom - contentRect.top) * scale / 2);
            imageDataRect = new Rect(contentRect.left - sw, contentRect.top - sh, contentRect.right + sw, contentRect.bottom + sh);
        }
        return imageDataRect;
    }

    void initLineAnimation() {
        if (scanLineRect == null || isInEditMode()) return;
        final int height = scanLineRect.bottom - scanLineRect.top;
        ObjectAnimator lineAnimator = ObjectAnimator.ofInt(this, "y", contentRect.top, contentRect.bottom - height);
        lineAnimator.setDuration(lineAnimatorDuration);
        lineAnimator.setRepeatCount(ValueAnimator.INFINITE);
        lineAnimator.setRepeatMode(ValueAnimator.REVERSE);
        lineAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                scanLineRect.top = (int) animation.getAnimatedValue();
                scanLineRect.bottom = scanLineRect.top + height;
                invalidate();
            }
        });
        lineAnimator.start();
    }
}
