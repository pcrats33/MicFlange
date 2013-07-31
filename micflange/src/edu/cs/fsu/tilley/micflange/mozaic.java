package edu.cs.fsu.tilley.micflange;

/*
 * Mic Flange - A wav recorder that allows for time dilation by graph of dt
 * Author: Rick Tilley
 * Date: 7/31/2013
 * Module: Mozaic
 * 
 * Description:
 * Handles graphing for the dt/t graph.
 */


import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

/*
     * class Mozaic mod to fit programs task
     * this class has been changed dramatically, will not work in its original fashion
     */
    class mozaic {

		/**
		 * 
		 */
		private final PlaySound playSound;

		protected class tiles {
			Bitmap profpic;
			String name;
			tiles(Bitmap x)
			{
				profpic = x;
				name = "Mr X";
			}
			// datestamp needed
			// name of friend or id
			// optional tag or separate class for tags
		}
		
		ArrayList<tiles> grid;
		int [][] smiley;
		int [] dt;
		int gridi;
		int dx, dy;
		int facewidth, faceheight;
		int SCREENx, SCREENy;
		float scale;
		int [] galpicid;
		int galpicidn;
		int touchidx;
		private int halfpt;
		boolean drawingmode;
		// *** Change maxprofiles for testing or speed
		public static final int MAXPROFILES = 512;
		// *******************************************

		mozaic(PlaySound playSound)
		{   this.playSound = playSound;
		int MAXdx = 255;
			int MINdx = 0;
			galpicidn = 0;
			touchidx = -1;
			gridi = 0;
			drawingmode = false;
			grid = new ArrayList<tiles>();
			smiley = null;
			scale = this.playSound.getBaseContext().getResources().getDisplayMetrics().density;
			float tilesize = 16.0f;
			dx = Math.round(tilesize * scale);
			dy = Math.round(tilesize * scale);
			Log.i("initMoz", "making mozaic");
			Log.i("tilesize", Float.toString(tilesize));
			Log.i("dx/dy", Integer.toString(dx)+"/"+Integer.toString(dy));
			// MAX checks for renderstamps to fit
			if (dx > MAXdx) {  dx = dy = MAXdx;	}
			else
				if (dx < MINdx) { dx = dy = MINdx;}
			
			facewidth = 0;
			faceheight = 0;
			Display display = this.playSound.getWindowManager().getDefaultDisplay();
			Point size = new Point();
			display.getSize(size);
			SCREENx = size.x;
			SCREENy = size.y;
			galpicid = null;

		}

		private int getValidId(int idoff)
		{
			int id = 200+idoff;
			// Returns a valid id that isn't in use
			View v = this.playSound.findViewById(id);  
			while (v != null){  
				v = this.playSound.findViewById(++id);  
			}	
			return id;
		} 
		
		public void gallery()
		{  
			gallery(!drawingmode);
		}
		
		public void gallery(boolean on)
		{   LinearLayout gal = (LinearLayout) this.playSound.findViewById(R.id.gallery);
/*
		  Button btn = (Button) findViewById(R.id.painteron);
			if (on)
			{  touchidx = 1;
				btn.setText("Erase Mode");
				gal.setVisibility(View.VISIBLE);
				drawingmode = true;
			}
			else
			{	
				btn.setText("Draw Mode");
				touchidx = -1;
				gal.setVisibility(View.VISIBLE);
				drawingmode = false;
			}
			*/
		}
		
		public void gallery(int bx, int by)
		{
			LinearLayout gal = (LinearLayout) this.playSound.findViewById(R.id.gallery);
			Iterator<tiles> it = grid.iterator();
			ImageView tile;
			tiles friend;
			int id;
			if (galpicid != null)
			{
				for (int i = 0; i < galpicidn; i++)
				{	tile = (ImageView) this.playSound.findViewById(galpicid[i]);
					if (tile != null)
					  gal.removeView(tile);
				}
				galpicid = null;
				galpicidn = 0;
			}
			galpicid = new int[grid.size()+1];
			LinearLayout.LayoutParams lparams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
			lparams.setMargins(2,2,2,2);
			// make empty box
			
			// make chosen palette
			tile = new ImageView(this.playSound.getApplicationContext());
			galpicid[galpicidn++] = id = getValidId(galpicidn);
			tile.setId(id);
			tile.setLayoutParams(lparams);
			tile.setImageBitmap(blackbox());
				// maybe make it look different
			gal.addView(tile);
			palettelistener(tile);
			while (it.hasNext())
			{
				friend = it.next();
				tile = new ImageView(this.playSound.getApplicationContext());
				tile.setImageBitmap(friend.profpic);
				galpicid[galpicidn++] = id = getValidId(galpicidn);
				tile.setId(id);
				tile.setLayoutParams(lparams);
				gal.addView(tile);
				palettelistener(tile);
			}
			
		}
		
// add listener to each profile pic thumbnail
private void palettelistener(ImageView tile)
{
		tile.setOnTouchListener(new OnTouchListener() {
			  @Override
			  public boolean onTouch(View v, MotionEvent event)
			  {
				  ImageView pane = (ImageView) v;
				  int i = pane.getId();
				  touchidx = -1;
				  for (int j = 0; j < galpicid.length; j++)
					  if (galpicid[j] == i)
					  {
						 touchidx = j;
						 ImageView image = (ImageView) mozaic.this.playSound.findViewById(R.id.ppal);
						 if (j > 0 & j <= grid.size())
							 image.setImageBitmap((grid.get(j-1)).profpic);
						 else
							 image.setImageBitmap(blackbox());
					  }
				  /*
				  Button btn = (Button) findViewById(R.id.painteron);
				  btn.setText("Erase Mode");
*/
				  return true;
			  }
			});
}
		
		
		public void loadpic(Bitmap x)
		{
			// pass jpg not bitmap get it into a bitmap shrink also.
			tiles thumb = new tiles(Bitmap.createScaledBitmap(x,  dx,  dy,  true));
			grid.add(thumb);
			gridi++;
			
		}
		
		private void cleart(int x, int y)
		{
			ImageView image = (ImageView) this.playSound.findViewById(R.id.imageView1);
			Bitmap bigpic = ((BitmapDrawable)image.getDrawable()).getBitmap();
			Canvas panorama = new Canvas(bigpic);

			Paint clearit = new Paint();
			if (y == halfpt)
			  clearit.setColor(Color.LTGRAY);
			else			
			  clearit.setXfermode(new PorterDuffXfermode(Mode.CLEAR));
			panorama.drawRect(x*dx, y*dy, (x+1)*dx, (y+1)*dy, clearit);

			 int width = bigpic.getWidth();
			    int height = bigpic.getHeight();
			 Log.i("BRUSHERROR", "width: "+width);
			 Log.i("BRUSHERROR", "height: "+height);

			
			image.setImageBitmap(bigpic);
			smiley[x][y] = 0;
		}
		
		public void settile(int x, int y)
		{
//		Log.i("settile", "set to "+Integer.toString(touchidx));
//		Log.i("settile","uh"+Integer.toString(facewidth));
			if (x >= 0 & x < facewidth & y >= 0 & y < faceheight)
			{
				ImageView image = (ImageView) this.playSound.findViewById(R.id.imageView1);
				Bitmap bigpic = ((BitmapDrawable)image.getDrawable()).getBitmap();
				Canvas panorama = new Canvas(bigpic);
				int oldy = -1;
				for (int j = 0; j < faceheight; j++)
					if (smiley[x][j] != 0)
					{  cleart(x,j);
						oldy = j;
					}
				smiley[x][y] = 1;
				dt[x] = halfpt-y;
				drawtile(panorama, x, y);
				/*
				if (touchidx > 0 & touchidx <= grid.size())
				{	smiley[x][y] = touchidx;
					drawtile(panorama, x, y);

					int width = bigpic.getWidth();
					    int height = bigpic.getHeight();
					 Log.i("BRUSHERROR", "width: "+width);
					 Log.i("BRUSHERROR", "height: "+height);

					 image.setImageBitmap(bigpic);
				}
				else
				{	Paint clearit = new Paint();
					clearit.setXfermode(new PorterDuffXfermode(Mode.CLEAR));
					panorama.drawRect(x*dx, y*dy, (x+1)*dx, (y+1)*dy, clearit);

					 int width = bigpic.getWidth();
					    int height = bigpic.getHeight();
					 Log.i("BRUSHERROR", "width: "+width);
					 Log.i("BRUSHERROR", "height: "+height);

					
					image.setImageBitmap(bigpic);
					smiley[x][y] = 0;
				}
				*/
			}
		}
		
		Bitmap blackbox()
		{
			Paint myPaint = new Paint(); 
			myPaint.setColor(Color.BLACK);
			Bitmap bbox = Bitmap.createBitmap(dx,dy,Bitmap.Config.ARGB_8888);
			Canvas splat = new Canvas(bbox);
			splat.drawRect(0, 0,dx,dy,myPaint);
			return bbox;
		}

		public void makeblock()
		{
			if (facewidth > 0 & faceheight > 0)
				makeblock(facewidth,faceheight);
		}
		
		public void makeblock(int width, int height)
		{  
			 int count = 1;
			drawingmode = true;
			smiley = new int [width][height];
			facewidth = width; faceheight = height;
			for (int y = 0; y < width; y++)
			{  
				for (int x = 0; x < height; x++)
				{  
					smiley[x][y] = count++;
					if (count > galpicidn)
						count = 1;
				}
			}
		}
		
		public void makeflatline(int width, int height)
		{  
			drawingmode = true;
			smiley = new int [width][height];
			dt = new int[height];
			facewidth = width; faceheight = height;
			halfpt = height/2;
			for (int y = 0; y < height; y++)
			{  
				for (int x = 0; x < width; x++)
				{  
					smiley[x][y] = 0;
				}
			}
			for (int x = 0; x < width; x++)
			{
				smiley[x][halfpt] = 1;
				dt[x] = 0;
			}
			
			touchidx = 1;
		}
		
		
		public void makesmiley()
		{
			if (facewidth > 0 & faceheight > 0)
				makesmiley(facewidth,faceheight);
		}
		
		public void makesmiley(int width, int height)
		{  
		     int roll = 1;
			 double rth;
			 int count = 1;
			 Random radical = new Random();
			 int widthx = width*dx;
			 int heightx = height*dy;
			 int rx = -1*widthx/2;
			 int ry = -1*heightx/2;	 
			 double r = (widthx*widthx/4.0)*0.8;
			 
			ImageView image = (ImageView) this.playSound.findViewById(R.id.imageView1);
			Bitmap bigpic = ((BitmapDrawable)image.getDrawable()).getBitmap();
			Canvas panorama = new Canvas(bigpic);
			Paint clearpaint = new Paint();
			clearpaint.setXfermode(new PorterDuffXfermode(Mode.CLEAR));

			Toast.makeText(this.playSound.getApplicationContext(), "Cookie Cutter Working", Toast.LENGTH_LONG).show();
				
			facewidth = width; faceheight = height;
			for (int y = 0; y < widthx; y++, ry++)
			{   rx = -1*widthx/2;
				for (int x = 0; x < heightx; x++, rx++)
				{  
					//roll = radical.nextInt(800);
				//	smiley[x][y] = radical.nextInt(435);		// for all
					// circle
					rth = rx*rx+ry*ry;
					if (rth < r)
					{  // test left eye
						int zx = x-(3*widthx/10);
						int zy = y-(heightx/3);
					   rth = zx*zx+zy*zy;
					   if (rth > r/16)
					   {  // right eye
						   zx = x-(7*widthx/10);
						   rth = zx*zx+zy*zy;
						   if (rth > r/16)
						   {  rth = (rx*rx+ry*ry) - (4*r/7);
						      rth = rth * rth;
						   	  if (rth > r*r/128 | ry < 0)
						   		  roll = 1; // dummy call, don't draw on smiley
						   	  else
						   		  panorama.drawPoint(x,y,clearpaint);
						   }
						   else
							   panorama.drawPoint(x,y,clearpaint);
					   }
					   else
						   panorama.drawPoint(x, y, clearpaint);
					}
					else
						panorama.drawPoint(x, y, clearpaint);
				}
			}
			image.setImageBitmap(bigpic);
			Log.i("cookie", "smiley has been cut");

		}

		public void maketriangle(int width, int height)
		{  
			boolean inside = false;
			 int widthx = width*dx;
			 int heightx = height*dy;
			 int rx = -1*widthx/2;
			 int ry = -1*heightx/2;	 
			 double m[] = {0.0,0.0,0.0};
			 double b[] = {0.0,0.0,0.0};
			 
			 // triangle slopes of 3 sides
			 m[0] = -1 * (heightx/2.0) / (widthx/4.0);
			 m[1] = -m[0];	m[2] = 0;
			 b[0] = b[1] = -1*heightx/2.0;
			 b[2] = -b[0];
			 
			 Log.i("dims", Double.toString(m[0])+"+"+Double.toString(b[0]));
			 Log.i("dims", Double.toString(m[1])+"+"+Double.toString(b[1]));
			 Log.i("dims", Double.toString(m[2])+"+"+Double.toString(b[2]));
			 
			ImageView image = (ImageView) this.playSound.findViewById(R.id.imageView1);
			Bitmap bigpic = ((BitmapDrawable)image.getDrawable()).getBitmap();
			Canvas panorama = new Canvas(bigpic);
			Paint clearpaint = new Paint();
			clearpaint.setXfermode(new PorterDuffXfermode(Mode.CLEAR));

			Toast.makeText(this.playSound.getApplicationContext(), "Cookie Cutter Working", Toast.LENGTH_LONG).show();
				
			facewidth = width; faceheight = height;
			for (int y = 0; y < widthx; y++, ry++)
			{   rx = -1*widthx/2;
				for (int x = 0; x < heightx; x++, rx++)
				{  
					if ((ry > (m[0]*rx+b[0]))
					  & (ry > (m[1]*rx+b[1]))
					  & (ry < b[2]))
						inside = true;
					
					if (!inside)
						panorama.drawPoint(x,y,clearpaint);
					else
						inside = false;
				}
			}
			image.setImageBitmap(bigpic);
			Log.i("cookie", "triangle has been cut");

		}
	
		class dumbPair
		{
			public	double first,second;
			dumbPair()
			{
				
			}
		}

		protected dumbPair toPolar(dumbPair cart, dumbPair p)
		{
			p.first = Math.sqrt(cart.first*cart.first+cart.second*cart.second);
			p.second = Math.tan(cart.second/cart.first);
			return p;
		}

		protected dumbPair toCart(dumbPair polar, dumbPair c)
		{
			c.first = polar.first * Math.cos(polar.second);
			c.second = polar.first * Math.sin(polar.second);
			return c;
		}
		
		
/*		
		protected Pair<Double, Double> toPolar(Pair<Double, Double> cart)
		{
			Pair <Double, Double> p;
			double r, th;
			r = Math.sqrt(cart.first*cart.first+cart.second*cart.second);
			th = Math.tan(cart.second/cart.first);
			p = Pair.create(r, th);
			return p;
		}

		protected Pair<Double, Double> toCart(Pair<Double, Double> polar)
		{
			Pair <Double, Double> c;
			double x, y;
			x = polar.first * Math.cos(polar.second);
			y = polar.first * Math.sin(polar.second);
			c = Pair.create(x,y);
			return c;
		}
	*/	
		
		public void makesun(int width, int height)
		{  
			boolean inside = false;
			 int widthx = width*dx;
			 int heightx = height*dy;
			 int rx = -1*widthx/2;
			 int ry = -1*heightx/2;	 
			 dumbPair cart = new dumbPair();
			 dumbPair polar = new dumbPair();
			 double r;
			 double th;
			 double scale = 2.0/widthx;
			 double compval;
			 
			ImageView image = (ImageView) this.playSound.findViewById(R.id.imageView1);
			Bitmap bigpic = ((BitmapDrawable)image.getDrawable()).getBitmap();
			Canvas panorama = new Canvas(bigpic);
			Paint clearpaint = new Paint();
			clearpaint.setXfermode(new PorterDuffXfermode(Mode.CLEAR));

			Toast.makeText(this.playSound.getApplicationContext(), "Cookie Cutter Working", Toast.LENGTH_LONG).show();
				
			facewidth = width; faceheight = height;
			for (int y = 0; y < widthx; y++, ry++)
			{   rx = -1*widthx/2;
				for (int x = 0; x < heightx; x++, rx++)
				{  
					cart.first = (double)rx;  cart.second = (double)ry;
					polar = toPolar(cart, polar);
					r = polar.first * scale; th = polar.second;
					compval = Math.sin(th*10.0);
					if (r < (0.9+(compval/10.0)))
					 inside = true;
					
					if (!inside)
						panorama.drawPoint(x,y,clearpaint);
					else
						inside = false;
				}
			}
			image.setImageBitmap(bigpic);
			Log.i("cookie", "moon has been cut");

		}		

		public void makeflower(int width, int height)
		{  
			boolean inside = false;
			 int widthx = width*dx;
			 int heightx = height*dy;
			 int rx = -1*widthx/2;
			 int ry = -1*heightx/2;	 
			 dumbPair cart = new dumbPair();
			 dumbPair polar = new dumbPair();
			 double r;
			 double th;
			 double scale = 2.0/widthx;
			 double compval;
			 
			ImageView image = (ImageView) this.playSound.findViewById(R.id.imageView1);
			Bitmap bigpic = ((BitmapDrawable)image.getDrawable()).getBitmap();
			Canvas panorama = new Canvas(bigpic);
			Paint clearpaint = new Paint();
			clearpaint.setXfermode(new PorterDuffXfermode(Mode.CLEAR));

			Toast.makeText(this.playSound.getApplicationContext(), "Cookie Cutter Working", Toast.LENGTH_LONG).show();
				
			facewidth = width; faceheight = height;
			for (int y = 0; y < widthx; y++, ry++)
			{   rx = -1*widthx/2;
				for (int x = 0; x < heightx; x++, rx++)
				{  
					cart.first = (double)rx;  cart.second = (double)ry;
					polar = toPolar(cart, polar);
					r = polar.first * scale; th = polar.second;
					compval = Math.sin(th) * Math.cos(th);
					if (r < (0.4+compval))
					 inside = true;
					
					if (!inside)
						panorama.drawPoint(x,y,clearpaint);
					else
						inside = false;
				}
			}
			image.setImageBitmap(bigpic);
			Log.i("cookie", "moon has been cut");

		}		
		
		
		private void drawtile(Canvas portr, int x, int y)
		{
			Rect src, dest;
//			int drawidx = smiley[x][y] - 1;
			int drawidx = 0;
			if (drawidx >= 0 & drawidx < grid.size() )
			{
				tiles t = grid.get(drawidx);
				src = new Rect(0,0,dx,dy);
				dest = new Rect(src);
				dest.offset(x*dx, y*dy);
		Log.i("brush", "touched ("+x*dx+", "+y*dy+")");
				portr.drawBitmap(t.profpic,  src,  dest, null);
			}
			else
				if (drawidx == -1)
				{
					// clear the block
				}
		}
		public Bitmap redraw()
		{
			if (facewidth > 0 & faceheight > 0)
			{
				Iterator<tiles> it = grid.iterator();
				Bitmap bigpic = Bitmap.createBitmap(facewidth*dx, faceheight*dy, Bitmap.Config.ARGB_8888);
/*
				Paint myPaint = new Paint(); 
				myPaint.setColor(Color.RED);
//				Bitmap bbox = Bitmap.createBitmap(dx,dy,Bitmap.Config.ARGB_8888);
				Canvas splat = new Canvas(bigpic);
				splat.drawRect(0, 0,facewidth*dx,faceheight*dy,myPaint);				
*/				
				Log.i("drawing bitmap", "("+(facewidth*dx)+", "+(faceheight*dy)+")");
				Canvas panorama = new Canvas(bigpic);
				Rect src, dest;
				for (int x = 0; x < facewidth; x++)
					for (int y = 0; y < faceheight; y++)
					{   
						if (smiley[x][y] != 0)
						{		
							if (!it.hasNext())
									it = grid.iterator();
							if (it.hasNext())
							{
								tiles t = it.next();
								src = new Rect(0,0,dx,dy);
								dest = new Rect(src);
								dest.offset(x*dx, y*dy);
								panorama.drawBitmap(t.profpic,  src,  dest, null);
							}
						}
					}
				this.playSound.publishing = bigpic;
				return bigpic;
			}
			else
				return null;
		}
		
		public Bitmap makemozaic(int width, int height)
		{	
			if (drawingmode == false | smiley == null)
				makeflatline(width,height);
		//		makeblock(width, height);		// init blocks first time
			facewidth = width;
			faceheight = height;
			return redraw();
		}
		
		public void cookie(int cidx)
		{  
		  if (facewidth > 0 & faceheight > 0)
		  {
				Log.i("cookie", "which cutter?");
			switch (cidx)
			{
			  case 1: makesmiley(facewidth, faceheight); break;
			  case 2: makesun(facewidth, faceheight); break;
			  case 3: maketriangle(facewidth, faceheight); break;
			  case 4: makeflower(facewidth, faceheight); break;
			  default: break;
			}
		  }
		}
	
	private void renderstamp(Bitmap curtain, int tilex, int tiley, int width, int height)
	{
		int pixel1 = Color.rgb(3, 17, 5);
		int pixel2 = Color.rgb(tiley, 13, 23);
		int pixelp = Color.rgb(width, height, tilex);
		curtain.setPixel(0,0,pixelp);
		curtain.setPixel(1,0,pixel1);
		curtain.setPixel(2,0,pixel2);
	}

	private boolean loadstamp(Bitmap curtain)
	{
		int pixelp = curtain.getPixel(0, 0);
		int pixel1 = curtain.getPixel(1, 0);
		int pixel2 = curtain.getPixel(2, 0);
		if (Color.red(pixel1) == 3 && Color.green(pixel1) == 17 && Color.blue(pixel1) == 5 
				&& Color.green(pixel2) == 13 && Color.blue(pixel2) == 23)
		{
			dx = Color.blue(pixelp);
			dy = Color.red(pixel2);
			facewidth = Color.red(pixelp);
			faceheight = Color.green(pixelp);
			return true;
		}
		else
			return false;
		
	}
	
	// draw status line, pos = -1 erases old only.
	public void onstatus(double old, double pos, double max)
	{
		int linex, lineo;
		int boxx, boxo;
		int spot;
		lineo = (int) ((old/max) * (double)(facewidth*dx));
		linex = (int) ((pos/max) * (double)(facewidth*dx));
		boxo = lineo / dx;
		boxx = linex / dx;
		ImageView image = (ImageView) this.playSound.findViewById(R.id.imageView1);
		Bitmap bigpic = ((BitmapDrawable)image.getDrawable()).getBitmap();
		Canvas panorama = new Canvas(bigpic);
		Paint blackpaint = new Paint();
		blackpaint.setColor(Color.BLACK);
		Paint bluepaint = new Paint();
		bluepaint.setColor(Color.BLUE);
		Paint clearpaint = new Paint();
		clearpaint.setXfermode(new PorterDuffXfermode(Mode.CLEAR));
		if (lineo != linex)
		{
			if (old >= 0 | pos == -1)
			{
				// erase line
				spot = halfpt - dt[boxo];
				panorama.drawLine(lineo, 0, lineo, (spot*dy)-1, clearpaint);
				panorama.drawLine(lineo, spot*dy, lineo,((spot+1)*dy)-1, blackpaint);
				panorama.drawLine(lineo, (spot+1)*dy, lineo,faceheight*dy, blackpaint);
			}
			// draw line
			spot = halfpt - dt[boxx];
			panorama.drawLine(linex, 0, linex, (spot*dy)-1, bluepaint);
			panorama.drawLine(linex, spot*dy, linex,((spot+1)*dy)-1, clearpaint);
			panorama.drawLine(linex, (spot+1)*dy, linex,faceheight*dy, bluepaint);
		}
//		image.setImageBitmap(bigpic);
	}
	
// *** end mozaic class	
  }