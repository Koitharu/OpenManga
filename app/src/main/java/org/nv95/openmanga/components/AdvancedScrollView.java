package org.nv95.openmanga.components;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.ScrollView;

/**
 * Created by nv95 on 23.10.15.
 */
@Deprecated
public class AdvancedScrollView extends ScrollView {
  protected OnScrollListener onScrollListener;

  public AdvancedScrollView(Context context) {
    super(context);
  }

  public AdvancedScrollView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public AdvancedScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  public AdvancedScrollView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
  }

  @Override
  protected void onScrollChanged(int x, int y, int oldx, int oldy) {
    super.onScrollChanged(x, y, oldx, oldy);
    if (onScrollListener != null) {
      onScrollListener.OnScroll(this, x, y, oldx, oldy);
    }
  }

  /*
      @Override
      protected boolean overScrollBy(int deltaX, int deltaY, int scrollX, int scrollY, int scrollRangeX, int scrollRangeY, int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) {
          if ((scrollY != 0 || deltaY > 0) || onScrollListener == null) {
              return super.overScrollBy(deltaX, deltaY, scrollX, scrollY, scrollRangeX, scrollRangeY, maxOverScrollX, maxOverScrollY, isTouchEvent);
          }
          return onScrollListener.OnOverScroll(this, deltaX, deltaY) || super.overScrollBy(deltaX, deltaY, scrollX, scrollY, scrollRangeX, scrollRangeY, maxOverScrollX, maxOverScrollY, isTouchEvent);
      }
  */
  public OnScrollListener getOnScrollListener() {
    return onScrollListener;
  }

  public void setOnScrollListener(OnScrollListener onScrollListener) {
    this.onScrollListener = onScrollListener;
  }

  public interface OnScrollListener {
    void OnScroll(AdvancedScrollView scrollView, int x, int y, int oldx, int oldy);

    boolean OnOverScroll(AdvancedScrollView scrollView, int deltaX, int deltaY);
  }
}
