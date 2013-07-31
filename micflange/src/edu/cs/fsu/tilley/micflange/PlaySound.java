package edu.cs.fsu.tilley.micflange;
/*
 * Mic Flange - A wav recorder that allows for time dilation by graph of dt
 * Author: Rick Tilley
 * Date: 7/31/2013
 * Module: Main activity
 * 
 * Description:
 * Graph dt/t and record, playback, and save to wav file.
 */

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class PlaySound extends Activity {

    private final int duration = 3; // seconds
    final int sampleRate = 8000;
    int numSamples = duration * sampleRate;
    private double sample[];
    private final double freqOfTone = 440; // hz
    public Recorder mic = new Recorder(this);
	Bitmap publishing;
	mozaic dt;
    public byte generatedSnd[] = null;
    public byte warpedSnd[] = null;
    public int warpedStop = 0;
    public byte newSnd[];
    final public int GRIDWIDTH = 20;
    Handler handler = new Handler();
    boolean doingsomething = false;
    boolean playing = false;
    boolean recording = false;
    private AsyncTask<Void, Double, Void> myplayer; 
    private AsyncTask<Void, Double, Void> myrecorder; 
    Thread thread;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_sound);
        dt = new mozaic(this);
        dt.loadpic(dt.blackbox());
    	ImageView pane = (ImageView) findViewById(R.id.imageView1);
		dt.gallery(0,0);

		Bitmap result1 = dt.makemozaic(GRIDWIDTH,GRIDWIDTH);
		BitmapDrawable result = new BitmapDrawable(result1);
		pane.setImageDrawable(result);

		 int width = result1.getWidth();
		    int height = result1.getHeight();
		 Log.i("BMPERROR", "width: "+width);
		 Log.i("BMPERROR", "height: "+height);
		
		LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) pane.getLayoutParams();
	    params.width = dt.dx*GRIDWIDTH;
	    params.height = dt.dy*GRIDWIDTH;
	    pane.setLayoutParams(params);
	    
		pane.setOnTouchListener(new OnTouchListener() {
			  @Override
			  public boolean onTouch(View v, MotionEvent event)
			  {
		//		  ImageView pane = (ImageView) findViewById(R.id.imageView1);
				  int touchX = (int) event.getX();
				  int touchY = (int) event.getY();
//				  Log.i("scale", Float.toString(dt.scale));
				  int x = (int) Math.round((dt.scale * touchX - dt.dx) / (dt.dx * dt.scale ));
				  int y = (int) Math.round((dt.scale * touchY - dt.dy) / (dt.dy * dt.scale));
				  Log.i("touched", Integer.toString(x) + ", " + Integer.toString(y));
				  dt.settile(x,y);
				  return true;
			  }
			});

        Button recbtn = (Button) findViewById(R.id.rec);
        recbtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				  if (!(doingsomething | recording))
				  {
					  doingsomething = true;  recording = true;
					  Button rec = (Button) findViewById(R.id.rec);
					  rec.setText("Stop Recording");
					  rec.setBackgroundResource(R.drawable.buttonpsh);
					  Toast.makeText(
							  getApplicationContext(), "Start talking", 
							  Toast.LENGTH_LONG).show();
					  myrecorder = new Recorder(PlaySound.this).execute();
				  }
				  else
					  if (recording)
					  {
						  myrecorder.cancel(true);
					  }
			}
        });
        Button maketone = (Button) findViewById(R.id.tone);
        maketone.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Log.i("click","clicked play");
 			  if (doingsomething)
			  {	  if (playing)
			      {
				  	myplayer.cancel(true);
 				    Log.i("playing","interrupt playback with stop");
			      }
			  }
				else
			  {
				if (generatedSnd != null)
				{
		        doingsomething = true;  playing = true;
				Button tone = (Button) findViewById(R.id.tone);
				tone.setBackgroundResource(R.drawable.buttonpsh);
				tone.setText("Playing");
				myplayer = new Player(PlaySound.this).execute();
				}
				else
				{
					Toast.makeText(
							getApplicationContext(), "You haven't recorded anything yet!", 
							Toast.LENGTH_LONG).show();
				}
			  }
			}
        });
  
        Button save = (Button) findViewById(R.id.save);
        save.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (warpedSnd != null & warpedStop > 0 & doingsomething == false)
				{ doingsomething = true;
				 if (saveWarped())  
				 {
/*
					 Toast.makeText(
						getApplicationContext(), "Saved "+getFilesDir()+"/wackytone.pcm", 
						Toast.LENGTH_LONG).show();
						*/
					 Log.i("save", "saved!");
			         Button save = (Button) findViewById(R.id.save);
					 save.setBackgroundResource(R.drawable.buttonreg);
				 }
				 else
				   Toast.makeText(
						getApplicationContext(), "Error saving file", 
						Toast.LENGTH_LONG).show();

				 doingsomething = false;
				}
				else
					Toast.makeText(
							getApplicationContext(), "Please record and play the audio you want to record.", 
							Toast.LENGTH_LONG).show();
			}
        });
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
    	menu.add("Help");
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch(item.getItemId())
        {
            case 0:
            	Intent intent = new Intent(PlaySound.this, helpscreen.class);
                startActivity(intent);
                  return true;
            default:
                  return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
    
    
    byte[] readyPCM(byte[] buf)
    {
//    	byte[] pcmdat = new byte[2*buf.length];
    	Log.i("readyPCM", "getting PCM ready");
/*    	for (int i = 0; i < buf.length-2; i++) {
    		if (i < pcmdat.length-2)
    		{
    			byte dVal = pcmdat[i];
    		  pcmdat[++i] = dVal;
    		  pcmdat[++i] = dVal;
    		}
    	}
    	Log.i("readyPCM", "getting PCM returned");
    	return pcmdat;
    	*/
    	Log.i("readyPCM", "size: "+(buf.length*2));
    	byte[] pcmdat = new byte[buf.length*2];
    	int j = 0;
    	for (int i = 0; i < (buf.length-3);)
    	{  byte x,y;
    	    x = buf[i++]; y = buf[i++];
    		pcmdat[j++] = x;
    		pcmdat[j++] = y;
    		pcmdat[j++] = x;
    		pcmdat[j++] = y;
    	}
    	Log.i("readyPCM","j="+j);
    	Log.i("readyPCM","i="+((buf.length-1)));
    	return pcmdat;
    }
    
    
    
    
    
    /*
WaveWriter writer = new WaveWriter("/sdcard","recOut.wav",sampleRate,android.media.AudioFormat.CHANNEL_CONFIGURATION_MONO,android.media.AudioFormat.ENCODING_PCM_16BIT);
try {
    writer.createWaveFile();
} catch (IOException e) {
    e.printStackTrace();
}
while(isRunning){
    try {
        Sample sample = queue.take();
        writer.write(sample.buffer, sample.bufferSize);
    } catch (IOException e) {
        //snip
    }
}
     */

private byte[] wavHead(int size, byte[] data)
{
  byte[] wav = new byte[44+size];
  long v;
  // 4 ChunkID 0x52494646 "RIFF"
  wav[0] = 0x52;  wav[1] = 0x49; wav[2] = 0x46; wav[3] = 0x46;
  // 4 ChunkSize = 4 + (8 + SubChunk1Size) + (8 + SubChunk2Size)
  v = 44+size;
  wav[7] = (byte) ((v & 0xff000000) >>> 24);
  wav[6] = (byte) ((v & 0x00ff0000) >>> 16);
  wav[5] = (byte) ((v & 0x0000ff00) >>> 8);
  wav[4] = (byte) (v & 0x000000ff);
  // 4 Format 0x57415645 "WAVE"
  v = 0x57415645;
  wav[8] = (byte) ((v & 0xff000000) >>> 24);
  wav[9] = (byte) ((v & 0x00ff0000) >>> 16);
  wav[10] = (byte) ((v & 0x0000ff00) >>> 8);
  wav[11] = (byte) (v & 0x000000ff);
// fmt
  // 4 Subchunk1ID 0x666d7420 "fmt "
  v = 0x666d7420;
  wav[12] = (byte) ((v & 0xff000000) >>> 24);
  wav[13] = (byte) ((v & 0x00ff0000) >>> 16);
  wav[14] = (byte) ((v & 0x0000ff00) >>> 8);
  wav[15] = (byte) (v & 0x000000ff);
  // 4 Subchunk1Size = 16 for PCM
  wav[16] = 16;  wav[17] = 0;  wav[18] = 0;  wav[19] = 0;
  // 2 AudioFormat PCM = 1
  wav[20] = 1;   wav[21] = 0;  
  // 2 NumChannels Mono = 1, Stereo = 2
  wav[22] = 1;  wav[23] = 0;
  // 4 SampleRate 8000, 44100, etc
  v = 8000;
  wav[27] = (byte) ((v & 0xff000000) >>> 24);
  wav[26] = (byte) ((v & 0x00ff0000) >>> 16);
  wav[25] = (byte) ((v & 0x0000ff00) >>> 8);
  wav[24] = (byte) (v & 0x000000ff);
  // 4 ByteRate = SampleRate * NumChannels * BitsPerSample/8
  v = 8000 * 1 * 2;
  wav[31] = (byte) ((v & 0xff000000) >>> 24);
  wav[30] = (byte) ((v & 0x00ff0000) >>> 16);
  wav[29] = (byte) ((v & 0x0000ff00) >>> 8);
  wav[28] = (byte) (v & 0x000000ff);
  // 2 BlockAlign = NumChannels * BitsPerSample/8
  wav[32] = 2;  wav[33] = 0;
  // 2 BitsPerSample = 8 or 16
  wav[34] = 16; wav[35] = 0;
// data
  // 4 Subchunk2ID 0x64617461 "data"
  v = 0x64617461;
  wav[36] = (byte) ((v & 0xff000000) >>> 24);
  wav[37] = (byte) ((v & 0x00ff0000) >>> 16);
  wav[38] = (byte) ((v & 0x0000ff00) >>> 8);
  wav[39] = (byte) (v & 0x000000ff);
  // 4 Subchunk2Size = NumSamples * NumChannels * BitsPerSample/8
  v = warpedStop * 2;
  wav[43] = (byte) ((v & 0xff000000) >>> 24);
  wav[42] = (byte) ((v & 0x00ff0000) >>> 16);
  wav[41] = (byte) ((v & 0x0000ff00) >>> 8);
  wav[40] = (byte) (v & 0x000000ff);
  for (int i = 0; i+1 < warpedStop; i += 2)
  {
	  wav[i+45] = data[i+1];
	  wav[i+44] = data[i];
  }
  for (int i = 0; i < 45; i += 4)
	  Log.i ("wav", "["+i+"] "+Integer.toHexString(wav[i])+"-"+Integer.toHexString(wav[i+1])+"-"+Integer.toHexString(wav[i+2])+"-"+Integer.toHexString(wav[i+3]));
  return wav;
}
    
	public boolean saveWarped(){
		//Bitmap image = BitmapFactory.decodeResource(this.getResources(), R.drawable.pic1);
		String description = "PCM created by the app Mic Flange.";
		//MediaStore.Images.Media.insertImage(getContentResolver(), publishing, "MosaicOfFriends" , description);
		File sndDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_RINGTONES)
								+ "/MicFlange/");
		sndDir.mkdirs();
		
		Calendar c = Calendar.getInstance();
		//System.out.println("Current time => " + c.getTime());
		SimpleDateFormat df = new SimpleDateFormat("MMMddyy");
		String date = df.format(c.getTime());
		//String date = new SimpleDateFormat("mmd").format(new Date());
		
		
		String fname = "Flange-"+ date +".wav";
		File file = new File (sndDir, fname);
		
		int i = 1;
		while (file.exists ()){
			file = new File (sndDir, "Flange-"+ date + "(" + i + ").wav");
			i++;
		}
		if (i != 1)
			fname = "Flange-"+ date + "(" + i + ").wav";
		try {
			   byte [] wavfile = wavHead(warpedStop, warpedSnd);
		       FileOutputStream out = new FileOutputStream(file);
		       out.write(wavfile, 0, warpedStop+44);
//		       publishing.compress(Bitmap.CompressFormat.JPEG, 90, out);
//		       out.flush();
		       out.close();
//		       addImageGallery(file); // Notifies database that an image has been added to the gallery directory
		       Toast.makeText(getApplicationContext(), "Image saved to " 
		    		   + Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_RINGTONES)
		    		   + "/MicFlange/",
					     Toast.LENGTH_SHORT).show();
	            TextView tv = (TextView) findViewById(R.id.textView1);
	            tv.setText(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_RINGTONES)+fname);
		} catch (Exception e) {
		       e.printStackTrace();
		       return false;
		}
       return true;
	}
    
}
