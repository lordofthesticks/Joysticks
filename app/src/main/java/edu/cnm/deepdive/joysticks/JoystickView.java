package edu.cnm.deepdive.joysticks;

import android.content.Context;
//import android.graphics.AvoidXfermode;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

public class JoystickView extends SurfaceView implements SurfaceHolder.Callback, View.OnTouchListener {

  private float centerX;
  private float centerY;
  private float baseRadius;
  private float hatRadius;
  private JoystickListener joystickCallback;
  private final int ratio = 5; //The smaller, the more shading will occur

  private void setupDimensions() {
    centerX = getWidth() / 2;
    centerY = getHeight() / 2;
    baseRadius = Math.min(getWidth(), getHeight()) / 3;
    hatRadius = Math.min(getWidth(), getHeight()) / 5;
  }

  public JoystickView(Context context) {
    super(context);
    getHolder().addCallback(this);
    setOnTouchListener(this);
    if(context instanceof JoystickListener)
      joystickCallback = (JoystickListener) context;
  }

  public JoystickView(Context context, AttributeSet attributes, int style) {
    super(context, attributes, style);
    getHolder().addCallback(this);
    setOnTouchListener(this);
    if(context instanceof JoystickListener)
      joystickCallback = (JoystickListener) context;
  }

  public JoystickView(Context context, AttributeSet attributes) {
    super(context, attributes);
    getHolder().addCallback(this);
    setOnTouchListener(this);
    if(context instanceof JoystickListener)
      joystickCallback = (JoystickListener) context;
  }

  private void drawJoystick(float newX, float newY){

    if(getHolder().getSurface().isValid()){
      Canvas myCanvas = this.getHolder().lockCanvas(); //Stuff to draw
      Paint colors = new Paint();
//      setLayerType(LAYER_TYPE_SOFTWARE, null); this clears the entire image....
//      myCanvas.drawARGB(0,0,0,0); // Clear the BG
      myCanvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);



      //First determine the sin and cos of the angle that the touched point is at relative to the center of the joystick
      float hypotenuse = (float) Math.sqrt(Math.pow(newX - centerX, 2) + Math.pow(newY - centerY, 2));
      float sin = (newY - centerY) / hypotenuse; //sin = o/h
      float cos = (newX - centerX) / hypotenuse; //cos = a/h

      //Draw the base first before shading
      colors.setARGB(255, 100, 100, 100);
      myCanvas.drawCircle(centerX, centerY, baseRadius, colors);
      for(int i = 1; i <= (int) (baseRadius / ratio); i++)
      {
        colors.setARGB(150/i, 255, 0, 0); //Gradually decrease the shade of black drawn to create a nice shading effect
        myCanvas.drawCircle(newX - cos * hypotenuse * (ratio/baseRadius) * i,
            newY - sin * hypotenuse * (ratio/baseRadius) * i, i * (hatRadius * ratio / baseRadius), colors); //Gradually increase the size of the shading effect
      }

      //Drawing the joystick hat
      for(int i = 0; i <= (int) (hatRadius / ratio); i++)
      {
        colors.setARGB(255, (int) (i * (255 * ratio / hatRadius)), (int) (i * (255 * ratio / hatRadius)), 255); //Change the joystick color for shading purposes
        myCanvas.drawCircle(newX, newY, hatRadius - (float) i * (ratio) / 2 , colors); //Draw the shading for the hat
      }

      getHolder().unlockCanvasAndPost(myCanvas); //Write the new drawing to the SurfaceView
    }
  }

  @Override
  public void surfaceCreated(SurfaceHolder surfaceHolder) {
    setupDimensions();
    drawJoystick(centerX, centerY);
  }

  @Override
  public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

  }

  @Override
  public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

  }

  public boolean onTouch(View v, MotionEvent e)
  {
    if(v.equals(this))
    {
      if(e.getAction() != e.ACTION_UP)
      {
        float displacement = (float) Math.sqrt((Math.pow(e.getX() - centerX, 2)) + Math.pow(e.getY() - centerY, 2));
        if(displacement < baseRadius)
        {
          drawJoystick(e.getX(), e.getY());
          joystickCallback.onJoystickMoved((e.getX() - centerX)/baseRadius, (e.getY() - centerY)/baseRadius, getId());
        }
        else
        {
          float ratio = baseRadius / displacement;
          float constrainedX = centerX + (e.getX() - centerX) * ratio;
          float constrainedY = centerY + (e.getY() - centerY) * ratio;
          drawJoystick(constrainedX, constrainedY);
          joystickCallback.onJoystickMoved((constrainedX-centerX)/baseRadius, (constrainedY-centerY)/baseRadius, getId());
        }
      }
      else
        drawJoystick(centerX, centerY);
      joystickCallback.onJoystickMoved(0,0,getId());
    }
    return true;
  }

  public interface JoystickListener
  {
    void onJoystickMoved(float xPercent, float yPercent, int id);
  }
}
