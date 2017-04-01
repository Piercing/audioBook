package es.hol.audiolibros;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

public class AcercaDe extends Activity {
  /**
   * Called when the activity is first created.
   */
  @Override
  public void onCreate( Bundle savedInstanceState ) {
    super.onCreate( savedInstanceState );

    // Fijar orientacion vertical.
    setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_PORTRAIT );

    // Ocultar barra de título.
    requestWindowFeature( Window.FEATURE_NO_TITLE );
    setContentView( R.layout.acerca_de );

    WindowManager.LayoutParams params = getWindow( ).getAttributes( );
    //params.x = -5;
    params.y = 100;
    params.height = 800;
    params.width = 500;
    params.y = -10;

    this.getWindow( ).setAttributes( params );
  }
}