package edu.cs.fsu.tilley.micflange;
/*
 * Mic Flange - A wav recorder that allows for time dilation by graph of dt
 * Author: Rick Tilley
 * Date: 7/31/2013
 * Module: Player
 * 
 * Description:
 * Handles playback of sound, does flange scaling.
 */

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.Button;

class Player extends AsyncTask<Void, Double, Void> {

	/**
	 * 
	 */
	private final PlaySound playSound;
	private double laststatus = -1;

	/**
	 * @param playSound
	 */
	Player(PlaySound playSound) {
		this.playSound = playSound;
	}

	@Override
	protected Void doInBackground(Void... unused) {
		// play the sound
    	try {
        AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                this.playSound.sampleRate, AudioFormat.CHANNEL_CONFIGURATION_MONO,
                AudioFormat.ENCODING_PCM_16BIT, this.playSound.generatedSnd.length,
                AudioTrack.MODE_STREAM);
    	int BufferSize = AudioRecord.getMinBufferSize(8000,
                AudioFormat.CHANNEL_CONFIGURATION_MONO,
                AudioFormat.ENCODING_PCM_16BIT) * 2;
    	int offs = 0;
    	int endaddy = this.playSound.generatedSnd.length;
    	byte[] AudioBuffer = new byte[BufferSize];
		short x1 = 0;
		short x2 = 0;
		byte bla1 = 0;
		byte bla2 = 0;
		int time = 0;
		int timecnt = endaddy / 20;
		int timed = timecnt;
		int idx = 0;
		int dert;
		double dirrt;
		double t = 0;
		double voice;
		int tp = 0;
		int done = 2;
		int donet = done;
		int warpoff = 0;
		int maxW = endaddy*2;
		int juststop = 0;
		this.playSound.warpedSnd = new byte[maxW];

		Log.i("playing","endaddy = "+endaddy+" , timecnt = "+timecnt+" , BufferSize = "+BufferSize);

		audioTrack.play();
		// play whole sound clip loop:
    	while (done != 0 & !isCancelled())
    	{  
    		dert = this.playSound.dt.dt[time];
    		dirrt = Math.exp((double)dert / (double)this.playSound.GRIDWIDTH);
    		Log.i("playing","dert="+dert+" , dirrt="+dirrt);
    		// ***
    		// load a buffer in at a time, loop:
    		for (idx = 0; (idx+1 < BufferSize); idx += 2)
    		{
    		  double a, b;
    		  a = ((double)tp+1.0)-t;
    		  if (a > dirrt)
    		  {  a = dirrt; b = 0;
    		  }
    		  else
    		  {
    			  b = dirrt-a;
    			  if (idx+3 > BufferSize)
    			  {
    				  b = 0;
    			  }
    		  }
    		  x1 = (short) (((short)this.playSound.generatedSnd[offs] << 8) + (short)this.playSound.generatedSnd[offs+1]);
    		  if (b != 0 & offs+3 < endaddy)
    		  {
    		    x2 = (short) (((short)this.playSound.generatedSnd[offs+2] << 8) + (short)this.playSound.generatedSnd[offs+3]);
    		    voice = (a * (double)x1 + b * (double)x2) / dirrt;
    		    x1 = (short) voice;
  			  bla1 = (byte) ((x1 & 0xff00) >>> 8);
  			  bla2 = (byte) (x1 & 0x00ff);
    		  }
    		  else
    		  {
    			  bla1 = (byte) ((x1 & 0xff00) >>> 8);
    			  bla2 = (byte) (x1 & 0x00ff);
    		  }

    		  if (offs+1 < endaddy)
    		  {
    			bla1 = this.playSound.generatedSnd[offs];
    			bla2 = this.playSound.generatedSnd[offs+1];
    		  }
    		  AudioBuffer[idx] = bla1;
    		  AudioBuffer[idx+1] = bla2;
      		  t = t + dirrt;
    		  tp = (int)Math.floor(t);
    		  offs = (int) (tp*2.0);
    		  if (endaddy < offs+3)
    		  {
    			  t = 0;
        		  tp = 0;
        		  offs = 0;
        		  if (done == donet)
        			  juststop = warpoff-(BufferSize*1);
    			  done--;
    		  }
    		  else
    		    offs = tp*2;
    		}
    		if (idx < BufferSize)  // last buffer clear end
    		{ for (; idx+1 < BufferSize; idx++)
    		  {  AudioBuffer[idx] = bla1;
    			  AudioBuffer[idx+1] = bla2;
     		  }
    		  Log.i("playing","clear end of buffer");
    		} 
    		if (warpoff+BufferSize < maxW)
    		{
    		  System.arraycopy(AudioBuffer, 0, this.playSound.warpedSnd, warpoff, BufferSize);
    		  warpoff += BufferSize;
    		}
    		if (warpoff > maxW)
    		{
    		  byte[] temp = new byte[maxW+(20*BufferSize)];
    		  System.arraycopy(this.playSound.warpedSnd, 0, temp, 0, maxW);
    		  maxW += 20*BufferSize;
    		  this.playSound.warpedSnd = temp;
    		}
    		if (offs > timecnt)
    		{
    			do {
        			timecnt += timed;
    			    ++time;
    			    Log.i("playing","inc time="+time+" , timecnt="+timecnt);
    			} while (offs > timecnt);
    			if (time >= this.playSound.GRIDWIDTH)
    				time = 0;
    		}
    	}
		Log.i("playing","now feed "+warpoff+" bytes");
		offs = 0;
		while (offs+BufferSize < warpoff & !isCancelled())
		{
			System.arraycopy(this.playSound.warpedSnd, offs, AudioBuffer, 0, BufferSize);
			audioTrack.write(AudioBuffer, 0, BufferSize);
			offs += BufferSize;
		}
		this.playSound.warpedStop = juststop;
        this.playSound.numSamples = juststop/2;
        Log.i("playing","play loop starting: t::"+this.playSound.numSamples);
    	Log.i("playing", "rate: "+this.playSound.sampleRate);
        int x = this.playSound.numSamples;
        int xcnt = 0;
        int xdelay = BufferSize;

        do{                                                     // Montior playback to find when done
             if (audioTrack != null) 
             {
                 x = audioTrack.getPlaybackHeadPosition();
                 --xdelay;
               if (xdelay == 0)
                 {
                   publishProgress((double)x);
  //                 Log.i("playing", "status bar update");
                   xdelay = BufferSize;
                 }
                 if (x == 0)
                	 xcnt++;
             }
             else 
                 x = this.playSound.numSamples;   
        } while ((x<this.playSound.numSamples) & (xcnt < 50) & !isCancelled());
        this.playSound.playing = false;
        audioTrack.flush();
        if (audioTrack != null) audioTrack.release();           // Track play done. Release track.    	
        }
        catch (Exception e){
//            getApplicationContext().RunTimeError("Error: " + e);
        	Log.i("play error", "woops can't play");
        }
     	Log.i("playing", "done playing");
     	if (isCancelled())
     		Log.i("Playing","canceling play");
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
//		this.playSound.dt.onstatus(laststatus,-1,this.playSound.numSamples);
		this.playSound.doingsomething = false;  this.playSound.playing = false;
		Button tone = (Button) this.playSound.findViewById(R.id.tone);
		tone.setText("Tone");
		tone.setBackgroundResource(R.drawable.buttonreg);
        Button save = (Button) this.playSound.findViewById(R.id.save);
        save.setVisibility(0);
        save.setBackgroundResource(R.drawable.buttonsel);
	}

	@Override
	protected void onCancelled(Void unused) {
	// execution of result of Long time consuming operation
//		this.playSound.dt.onstatus(laststatus,-1,this.playSound.numSamples);
        this.playSound.doingsomething = false;  this.playSound.playing = false;
		Button tone = (Button) this.playSound.findViewById(R.id.tone);
		tone.setText("Tone");
		tone.setBackgroundResource(R.drawable.buttonreg);
        Button save = (Button) this.playSound.findViewById(R.id.save);
        save.setVisibility(0);
        save.setBackgroundResource(R.drawable.buttonsel);
	}
	
	
	@Override
	protected void onProgressUpdate(Double... item) {
	// Things to be done while execution of long running operation is in progress. For example updating ProgessDialog
//		this.playSound.dt.onstatus(item[0] - 1.0,item[0],this.playSound.numSamples);
		laststatus = item[0];
	}

// ****
}  // END ASYNC TASK Player