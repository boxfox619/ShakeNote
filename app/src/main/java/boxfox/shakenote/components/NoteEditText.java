package boxfox.shakenote.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.EditText;

/**
 * Created by Administrator on 2016-08-13.
 */
public class NoteEditText extends EditText {
    private Rect mRect;
    private Paint mPaint;

    public NoteEditText(Context context) {
        super(context);
        initPaint();
    }

    public NoteEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        initPaint();
    }

    public NoteEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initPaint();
    }

    private void initPaint() {
        mRect = new Rect();
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(0x80000000);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int count = getLineCount();
        int left = getLeft();
        int right = getRight();
        int paddingTop = getPaddingTop();
        int paddingBottom = getPaddingBottom();
        int paddingLeft = getPaddingLeft();
        int paddingRight = getPaddingRight();
        int height = getHeight();
        int lineHeight = getLineHeight();

        for (int i = 0; i < count; i++) {
            int baseline = lineHeight * (i+1) + paddingTop;
            canvas.drawLine(left+paddingLeft, baseline, right-paddingRight, baseline, mPaint);
        }

        count = (height-paddingTop-paddingBottom) / lineHeight;

        for (int i = 0; i < count; i++) {
            int baseline = lineHeight * (i+1) + paddingTop;
            canvas.drawLine(left+paddingLeft, baseline, right-paddingRight, baseline, mPaint);
        }

        super.onDraw(canvas);
    }
}
