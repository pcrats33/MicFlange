package edu.cs.fsu.tilley.micflange;

/*
 * Mic Flange - A wav recorder that allows for time dilation by graph of dt
 * Author: Rick Tilley
 * Date: 7/31/2013
 * Module: Recorder
 * 
 * Description:
 * Handles recording of audio.
 */

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder.AudioSource;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

class Recorder extends AsyncTask<Void, Double, Void> {
	
	/**
	 * 
	 */
	private final PlaySound playSound;

	/**
	 * @param playSound
	 */
	Recorder(PlaySound playSound) {
		this.playSound = playSound;
	}


	protected byte[] fulltrack;  
    protected int recorderBufferSize;
    protected int lastoffs = 0;

	@Override
	protected Void doInBackground(Void... params) {
	    byte[] recordedAudioBuffer;
	    
	    	
	        recorderBufferSize = AudioRecord.getMinBufferSize(8000,
	                AudioFormat.CHANNEL_CONFIGURATION_MONO,
	                AudioFormat.ENCODING_PCM_16BIT) * 2;

	        AudioRecord recorder = new AudioRecord(AudioSource.DEFAULT, 8000,
	                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT,
	                recorderBufferSize);
	        final int tblocks = 4;
	        int looptime = 200;
	        
	    	fulltrack = new byte[recorderBufferSize*tblocks*looptime]; 
	    	Log.i("rec", "allocating"+(recorderBufferSize*tblocks*looptime));
	    try {
	        recorder.startRecording();
	        Log.i("mic","audio rec/playing");
	        int i;
	    	recordedAudioBuffer = new byte[recorderBufferSize];
	      while (looptime > 0 & !isCancelled())
	      { i = 0;
	        while (i < tblocks) {
	            recorder.read(recordedAudioBuffer, 0, recorderBufferSize);
	            System.arraycopy(recordedAudioBuffer, 0, fulltrack, lastoffs, recorderBufferSize);
	            lastoffs += recorderBufferSize;
	            ++i;
	          }
	        --looptime;
	      }

	    }
	     catch (Throwable t) {
	        Log.e("Error", "Initializing Audio Record Failed "+t.getLocalizedMessage());
	    }
	        
	        Log.i("mic", "done recording");
	        recorder.stop();
	        recorder.release();
	        return(null);
	}

	@Override
	protected void onPreExecute() {
	// Things to be done before execution of long running operation. For example showing ProgessDialog
        Button save = (Button) this.playSound.findViewById(R.id.save);
        save.setVisibility(View.GONE);
	}

	@Override
	protected void onPostExecute(Void unused) {
	// execution of result of Long time consuming operation
		this.playSound.generatedSnd = new byte[lastoffs];
		System.arraycopy(fulltrack, 0, this.playSound.generatedSnd, 0, lastoffs);
		Toast.makeText(
				this.playSound.getApplicationContext(), "Done Recording", 
				Toast.LENGTH_LONG).show();
        TextView tv = (TextView) this.playSound.findViewById(R.id.textView1);
        tv.setText(Integer.toString(this.playSound.generatedSnd.length/8000)+" seconds");
		Button rec = (Button) this.playSound.findViewById(R.id.rec);
		rec.setText("Record");
		rec.setBackgroundResource(R.drawable.buttonreg);
        this.playSound.doingsomething = false;  this.playSound.recording = false;
	}

	@Override
	protected void onCancelled(Void unused) {
	// execution of result of Long time consuming operation
		this.playSound.generatedSnd = new byte[lastoffs];
		System.arraycopy(fulltrack, 0, this.playSound.generatedSnd, 0, lastoffs);
		Toast.makeText(
				this.playSound.getApplicationContext(), "Recording Done", 
				Toast.LENGTH_LONG).show();
        TextView tv = (TextView) this.playSound.findViewById(R.id.textView1);
        tv.setText(Integer.toString(this.playSound.generatedSnd.length/8000)+" seconds");
		Button rec = (Button) this.playSound.findViewById(R.id.rec);
		rec.setText("Record");
		rec.setBackgroundResource(R.drawable.buttonreg);
        this.playSound.doingsomething = false;  this.playSound.recording = false;
	}
	
	
	@Override
	protected void onProgressUpdate(Double... item) {
	// Things to be done while execution of long running operation is in progress. For example updating ProgessDialog
	}
	
}