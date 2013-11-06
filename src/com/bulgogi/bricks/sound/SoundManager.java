package com.bulgogi.bricks.sound;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.Log;
import android.util.SparseIntArray;

import com.bulgogi.bricks.R;


public class SoundManager {

	static private SoundManager _instance;
	private static SoundPool mSoundPool; 
	private static SparseIntArray mSoundPoolMap; 
	private static AudioManager  mAudioManager;
	private static Context mContext;

	private SoundManager()
	{   
	}

	/**
	 * Requests the instance of the Sound Manager and creates it
	 * if it does not exist.
	 * 
	 * @return Returns the single instance of the SoundManager
	 */
	static synchronized public SoundManager getInstance() 
	{
		if (_instance == null) 
			_instance = new SoundManager();
		return _instance;
	}

	/**
	 * Initialises the storage for the sounds
	 * 
	 * @param theContext The Application context
	 */
	public void initSounds(Context theContext) 
	{ 
		mContext = theContext;

		if (mSoundPool == null)
			mSoundPool = new SoundPool(32, AudioManager.STREAM_MUSIC, 0);

		if (mSoundPoolMap == null)
			mSoundPoolMap = new SparseIntArray ();

		if (mAudioManager == null)
			mAudioManager = (AudioManager)mContext.getSystemService(Context.AUDIO_SERVICE); 	    
	} 

	/**
	 * Add a new Sound to the SoundPool
	 * 
	 * @param Index - The Sound Index for Retrieval
	 * @param SoundID - The Android ID for the Sound asset.
	 */
	public void addSound(int Index,int SoundID)
	{
		mSoundPoolMap.put(Index, mSoundPool.load(mContext, SoundID, 1));
	}

	/**
	 * Loads the various sound assets.
	 */
	public void loadSounds()
	{
		mSoundPoolMap.clear();
		mSoundPoolMap.put(1, mSoundPool.load(mContext, R.raw.t_1, 1));
		mSoundPoolMap.put(2, mSoundPool.load(mContext, R.raw.t_2, 1));
		mSoundPoolMap.put(3, mSoundPool.load(mContext, R.raw.t_3, 1));
		mSoundPoolMap.put(4, mSoundPool.load(mContext, R.raw.t_4, 1));	
		mSoundPoolMap.put(5, mSoundPool.load(mContext, R.raw.t_5, 1));
		mSoundPoolMap.put(6, mSoundPool.load(mContext, R.raw.t_6, 1));
		mSoundPoolMap.put(7, mSoundPool.load(mContext, R.raw.t_7, 1));
		mSoundPoolMap.put(8, mSoundPool.load(mContext, R.raw.t_8, 1));
		mSoundPoolMap.put(9, mSoundPool.load(mContext, R.raw.t_9, 1));
		mSoundPoolMap.put(10, mSoundPool.load(mContext, R.raw.t_10, 1));
		mSoundPoolMap.put(11, mSoundPool.load(mContext, R.raw.t_11, 1));
		mSoundPoolMap.put(12, mSoundPool.load(mContext, R.raw.t_12, 1));	
		mSoundPoolMap.put(13, mSoundPool.load(mContext, R.raw.t_13, 1));
		mSoundPoolMap.put(14, mSoundPool.load(mContext, R.raw.t_14, 1));
	}

	/**
	 * Plays a Sound
	 * 
	 * @param index - The Index of the Sound to be played
	 * @param speed - The Speed to play not, not currently used but included for compatibility
	 */
	public void playSound(int index,float speed) 
	{ 	 	
		if (mSoundPool == null) {
			return ;
		}
		float streamVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC); 
		streamVolume = streamVolume / mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

		if (mSoundPoolMap.get(index) != 0) {
			Log.e("test", "index:" + index);
			mSoundPool.play(mSoundPoolMap.get(index), streamVolume, streamVolume, 1, 0, speed);
		}
	}

	public void releaseSound() {
		mSoundPool.release();
		mSoundPool = null;
		mSoundPoolMap.clear();
		mAudioManager.unloadSoundEffects();
		_instance = null;
	}

	/**
	 * Stop a Sound
	 * @param index - index of the sound to be stopped
	 */
	public void stopSound(int index)
	{
		mSoundPool.stop(mSoundPoolMap.get(index));
	}

	public void stopAll() {
		for (int i=0; i < mSoundPoolMap.size(); i++) {
			int id = mSoundPoolMap.get(0);
			mSoundPool.stop(id);
		}
	}
}