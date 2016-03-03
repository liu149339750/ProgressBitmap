package com.example.progressbitmap;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PaintDrawable;
import android.graphics.drawable.ScaleDrawable;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class MainActivity extends Activity {
	int progress = 10;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		ProgressImageView image = new ProgressImageView(this);
//		setContentView(image);
//		image.setBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.icon));
//		image.setProgress(1f);
		
		ImageView image = new ImageView(this);
		LinearLayout ll = new LinearLayout(this);
		ll.setOrientation(LinearLayout.VERTICAL);
		setContentView(ll);
		ll.addView(image);
		final ProgressDrawable pd = new ProgressDrawable(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher));
		image.setImageDrawable(pd);
		pd.startProgress();
		pd.useWaveAnimal(true);
		
		pd.setProgress(progress);
		Button button = new Button(this);
		ll.addView(button);
		button.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				progress = progress + 5;
				if(progress > 100){
					pd.startProgress();
					progress = 0;
				}
				pd.setProgress(progress);
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	
	class ProgressImageView extends ImageView{
		private Bitmap bitmap;
		private int colors[];
		int width;
		int height;
		Paint paint;
		public ProgressImageView(Context context) {
			super(context);
			paint = new Paint();
			
		}
		
		public void setBitmap(Bitmap bitmap){
			width = bitmap.getWidth();
			height = bitmap.getHeight();
			colors = new int[width * height];
			this.bitmap = bitmap;
			bitmap.getPixels(colors, 0, width, 0, 0, width, height);
//			gray(colors, colors.length);
		}
		
		public void setProgress(float p){
			progress(colors, p, bitmap);
		}
	
		@Override
		protected void onDraw(Canvas canvas) {
			if(colors != null)
			canvas.drawBitmap(colors, 0, width, 0, 0, width, height, true, paint);
			
			canvas.translate(100, 100);
			canvas.drawBitmap(bitmap, 0, 0, paint);
			
			canvas.save();
			canvas.translate(width, 0);
			canvas.scale(-1f, 1f);
			canvas.drawBitmap(bitmap, null, new Rect(100, 100, 100+width, 100+height), paint);
			canvas.restore();
		}
	}
	
	class ProgressDrawable extends Drawable{
		private Bitmap mOriginBm;
		private int mColors[];
		private int mWidth;
		private int mHeight;
		private Paint mPaint;
		private boolean mUseWaveAnimal;
		private Paint mAboveWavePaint;
		private Paint mBelowWavePaint;
		private Path mAbovePath;
		private Path mBelowPath;
		
	    /**
	     * wave length
	     */
	    private int x_zoom = 150;

	    /**
	     * wave crest
	     */
	    private final int y_zoom = 6;
	    private final float offset = 0.5f;
//	    private final float max_right = x_zoom * offset;

	    // wave animation
	    private float aboveOffset = 0.0f;
	    private float blowOffset = 4.0f;
	    private float animSpeed = 0.15f;
		
	    private float waveToTop;
	    
	    float p;
	    
		public ProgressDrawable(Bitmap bm){
			mPaint = new Paint();
			setBitmap(bm);
		}
		
		public void setBitmap(Bitmap bm){
			mWidth = bm.getWidth();
			mHeight = bm.getHeight();
			mColors = new int[mWidth * mHeight];
			mOriginBm = bm;
			bm.getPixels(mColors, 0, mWidth, 0, 0, mWidth, mHeight);
			setBounds(0, 0, mWidth, mHeight);
			x_zoom = mWidth/4;
		}
		
		public void startProgress(){
			gray(mColors, mColors.length);
			invalidateSelf();
		}
		
		public void setProgress(int progress){
			p = (float)progress/100;
//			progress(mColors, p, mOriginBm);
			waveToTop = (1 - p) * mHeight;
//			calculatePath();
			invalidateSelf();
		}
		
		public void useWaveAnimal(boolean anim){
			mUseWaveAnimal = anim;
			mAbovePath = new Path();
			mBelowPath = new Path();
			mAboveWavePaint = new Paint();
			mAboveWavePaint.setColor(Color.BLUE);
			mAboveWavePaint.setAlpha(77);
			mAboveWavePaint.setStyle(Style.FILL);
			
			mBelowWavePaint = new Paint();
			mBelowWavePaint.setColor(Color.rgb(0, 0, 77));
			mBelowWavePaint.setAlpha(77);
			mBelowWavePaint.setStyle(Style.FILL);
			
			if(anim){
				animalThread.start();
			} else
				animalThread.interrupt();
		}
		
		private Thread animalThread  = new Thread(new Runnable() {
			
			@Override
			public void run() {
				try{
					while(true){
						Thread.sleep(100);
						calculatePath();
						mHandler.sendEmptyMessage(0);
					}
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		});
		
		Handler mHandler = new Handler(new Handler.Callback() {
			
			@Override
			public boolean handleMessage(Message msg) {
				invalidateSelf();
				return false;
			}
		});
		
	    private void calculatePath() {
	    	mAbovePath.reset();
	        mBelowPath.reset();

	        getWaveOffset();

	        mAbovePath.moveTo(0, getIntrinsicHeight());
	        float i = 0;
	        
	        for (i = 0; x_zoom * i <= mWidth; i += offset) {
	        	mAbovePath.lineTo(x_zoom * i, (float) (y_zoom * Math.cos(i + aboveOffset)) + waveToTop);
	        }
	        mAbovePath.lineTo(mWidth, (float)(y_zoom * Math.cos((i - offset/2) + aboveOffset)) + waveToTop);
	        mAbovePath.lineTo(mWidth, mHeight);

	        mBelowPath.moveTo(0, mHeight);
	        for (i = 0; x_zoom * i <= mWidth; i += offset) {
	        	mBelowPath.lineTo((x_zoom * i), (float) (y_zoom * Math.cos(i + blowOffset)) + waveToTop);
	        }
	        mBelowPath.lineTo(mWidth, (float)(y_zoom * Math.cos((i - offset/2) + aboveOffset)) + waveToTop);
	        mBelowPath.lineTo(mWidth, mHeight);
	    }
	    
	    private void getWaveOffset(){
	        if(blowOffset > Float.MAX_VALUE - 100){
	            blowOffset = 0;
	        }else{
	            blowOffset += animSpeed;
	        }

	        if(aboveOffset > Float.MAX_VALUE - 100){
	            aboveOffset = 0;
	        }else{
	            aboveOffset += animSpeed;
	        }
	    }
		
		@Override
		public int getIntrinsicHeight() {
			return mHeight;
		}
		
		@Override
		public int getIntrinsicWidth() {
			return mWidth;
		}
		
		@Override
		public void draw(Canvas canvas) {
			
			if(mUseWaveAnimal){
				canvas.drawPath(mAbovePath, mAboveWavePaint);
				canvas.drawPath(mBelowPath, mBelowWavePaint);
			}
			canvas.drawBitmap(mOriginBm, 0, 0, mPaint);
			if(1 - p > 0)
				canvas.drawBitmap(mColors, 0, mWidth, 0, 0, mWidth, (int)(mHeight * (1 - p)), true, mPaint);
		}

		@Override
		public void setAlpha(int alpha) {
			if(mPaint.getAlpha() != alpha){
				mPaint.setAlpha(alpha);
				invalidateSelf();
			}
		}

		@Override
		public void setColorFilter(ColorFilter cf) {
			mPaint.setColorFilter(cf);
			invalidateSelf();
		}

		@Override
		public int getOpacity() {
			return PixelFormat.TRANSLUCENT;
		}
		
	}
	
	public native void progress(int colors[],float p, Bitmap bitmap);
	public native void gray(int colors[],int len);
	static {
		System.loadLibrary("ProgressBitmap");
	}
}
