package com.badlogic.gdx.audio;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.badlogic.gdx.audio.transform.SoundTouch;
import com.badlogic.gdx.audio.transform.WavFile;
import com.xinqihd.sns.gameserver.chat.SoundTouchManager;

public class SoundTouchTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testProcessWav() {
		short[] wav = readWave("test.wav");

		int maxSamples = 90000;
		SoundTouchManager.getInstance().transformVoice(wav, maxSamples);

		writeWav("test_p.wav", wav, maxSamples);

		fail("Not yet implemented");
	}

	public static short[] readWave(String fileName) {
		try {
			// Open the wav file specified as the first argument
			WavFile wavFile = WavFile.openWavFile(new File(fileName));

			// Display information about the wav file
			wavFile.display();

			// Get the number of audio channels in the wav file
			int numChannels = wavFile.getNumChannels();

			int totalFrame = (int)(wavFile.getFramesRemaining());

			// Create a buffer of 100 frames
			int[] buffer = new int[totalFrame * numChannels];
			// Read frames into buffer
			int framesRead = wavFile.readFrames(buffer, totalFrame);

			// Close the wavFile
			wavFile.close();

			short[] frames = new short[buffer.length];
			for ( int i=0; i<frames.length; i++ ) {
				frames[i] = (short)buffer[i];
			}
			return frames;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void writeWav(String fileName, short[] buffer, int frameToWrite) {
		try {
			int sampleRate = 11025; // Samples per second
			double duration = 5.0; // Seconds

			// Calculate the number of frames required for specified duration
			long numFrames = (long) (duration * sampleRate);

			// Create a wav file with the name specified as the first argument
			WavFile wavFile = WavFile.newWavFile(new File(fileName), 2, numFrames, 16,
					sampleRate);

			// Initialise a local frame counter
			long frameCounter = 0;

		  // Write the buffer
			int[] wav = new int[buffer.length];
			for ( int i=0; i<buffer.length; i++ ) {
				wav[i] = buffer[i];
			}
			wavFile.writeFrames(wav, frameToWrite);

			// Close the wavFile
			wavFile.close();
		} catch (Exception e) {
			System.err.println(e);
		}
	}

}
