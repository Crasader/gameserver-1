package com.xinqihd.sns.gameserver.chat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.badlogic.gdx.audio.transform.SoundTouch;


public class SoundTouchManager {

	private static final Logger logger = LoggerFactory.getLogger(SoundTouch.class);
	
	private boolean isSoundTouchAvailable = true;
	private SoundTouch soundTouch = null;
	private static SoundTouchManager instance = new SoundTouchManager();
	
	
	private SoundTouchManager() {
		try {
			soundTouch = new SoundTouch();
		} catch (Exception e) {
			isSoundTouchAvailable = false;
			logger.warn("SoundTouch library is disabled");
		}
	}
	
	public static SoundTouchManager getInstance() {
		return instance;
	}
	
	/**
	 * Since soundtouch is not thread-safe, the object 
	 * should be created everytime we use it.
	 * 
	 * @param voices
	 * @param frame
	 * @return
	 */
	public short[] transformVoice(short[] voices, int frame) {
		if ( isSoundTouchAvailable ) {
			SoundTouch soundTouch = new SoundTouch();
			soundTouch.setSampleRate(11025);
			soundTouch.setChannels(1);
			soundTouch.setPitch(1.0f);
			soundTouch.setTempoChange(20);
			soundTouch.putSamples(voices, 0, frame);
			soundTouch.receiveSamples(voices, 0, frame);
		}
		return voices;
	}
}
