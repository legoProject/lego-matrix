package com.bulgogi.bricks.controller;

import static com.googlecode.javacv.cpp.opencv_core.*;
import android.graphics.*;
import android.hardware.Camera;
import android.util.*;

import com.bulgogi.bricks.config.*;
import com.bulgogi.bricks.cv.*;
import com.bulgogi.bricks.detector.*;
import com.bulgogi.bricks.model.*;
import com.bulgogi.bricks.view.*;
import com.googlecode.javacv.cpp.opencv_core.CvScalar;
import com.googlecode.javacv.cpp.opencv_core.IplImage;

public class FrameCallback implements Camera.PreviewCallback {
	private final String TAG = FrameCallback.class.getSimpleName();
	
	private final CvScalar HSV_BLUE_MIN = new CvScalar(100, 100, 100, 0);
	private final CvScalar HSV_BLUE_MAX = new CvScalar(120, 255, 255, 0);
	private final CvScalar HSV_GREEN_MIN = new CvScalar(60, 30, 30, 0);
	private final CvScalar HSV_GREEN_MAX = new CvScalar(90, 255, 255, 0);
	
	private OverlayView mOverlayView;
	private Bitmap mFrameBitmap;
	private IplImage mFrameImage;
	private Plate mPlate;
	private Pattern mPattern;
	private SparseArray<Sequence> mSequences;
	private Sequence mPreviousSequence;
	private Sequence mCurrentSequence;
	
	public FrameCallback(OverlayView overlayView, Plate plate, Pattern pattern) {
		mOverlayView = overlayView;
		mPlate = plate;
		mPattern = pattern;
		
		mSequences = new SparseArray<Sequence>();
		mSequences.put(Constant.SEQUENCE_TYPE.BLUE.ordinal(), 
				new Sequence(new PlateDetector(HSV_BLUE_MIN, HSV_BLUE_MAX), new PatternDetector(HSV_BLUE_MIN, HSV_BLUE_MAX)));
		mSequences.put(Constant.SEQUENCE_TYPE.GREEN.ordinal(), 
				new Sequence(new PlateDetector(HSV_GREEN_MIN, HSV_GREEN_MAX), new PatternDetector(HSV_GREEN_MIN, HSV_GREEN_MAX)));
//		mSequences.put(mSequenceType.CYAN.ordinal(), 
//				new Sequence(new PlateDetector(HSV_GREEN_MIN, HSV_GREEN_MAX), new PatternDetector(HSV_GREEN_MIN, HSV_GREEN_MAX)));
	}
	
	@Override
	public void onPreviewFrame(final byte[] data, Camera camera) {
		try {
            Camera.Size size = camera.getParameters().getPreviewSize();
			if (mFrameBitmap == null) {
				mFrameBitmap = Bitmap.createBitmap(size.width, size.height, Bitmap.Config.ARGB_8888);
			}

			if (mFrameImage == null) {
				mFrameImage = IplImage.create(size.width, size.height, IPL_DEPTH_8U, 4);
			}
            
            processImage(data, size.width, size.height);
            update();
            
            camera.addCallbackBuffer(data);
        } catch (RuntimeException e) {
        		e.printStackTrace();
        }				
	}
	
	private void processImage(final byte[] data, int width, int height) {
		boolean mStopped = true;
		int[] argb = new int[width * height];
		OpenCV.decodeYUV420SP(argb, data, width, height);
		
		mFrameBitmap.setPixels(argb, 0, width, 0, 0, width, height);
		OpenCV.BitmapToIplImage(mFrameBitmap, mFrameImage);
		
		for (int i = 0; i < mSequences.size(); i++) {
			Sequence sequence = mSequences.get(i);
			sequence.getPlateDetector().process(mFrameImage, mPlate.getPreprocessedImage(), mPlate.getProcessedImage());
			if (sequence.isEnabled()) {
				mStopped = false;
				mCurrentSequence = sequence;
				if (mPreviousSequence != mCurrentSequence) {
					Log.e(TAG, "POST SWITCH! " + i);
					mPreviousSequence = mCurrentSequence;
					// POST SWITCH!
				}
				
				break;
			}
		}
		
		if (mStopped && mCurrentSequence != null) {
			// POST STOP!
			Log.e(TAG, "POST STOPPED! ");
			mCurrentSequence = null;
			mPreviousSequence = null;
		}
		
		
		if (mCurrentSequence != null) {
			mCurrentSequence.getPlateDetector().process(mFrameImage, mPlate.getPreprocessedImage(), mPlate.getProcessedImage());
			mPlate.iplImageToBitmap();

			mCurrentSequence.getPatternDetector().process(mPlate.getProcessedImage(), mPattern.getProcessedImage());
			mPattern.iplImageToBitmap();
		}
	}
	
	private void update() {
		mOverlayView.update(mPlate.getPreprocessedBimtap(), mPlate.getProcessedBitmap(), mPattern.getProcessedBitmap());		
	}
	
	public void cleanup() {
		if (mFrameImage != null) {
			mFrameImage.release();
			mFrameImage = null;
		}
	}
}
