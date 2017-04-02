package es.hol.audiolibros;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;

public class SplashScreenActivity extends Activity {

   // Establecer la duración de la presentación.
   private static final long SPLASH_SCREEN_DELAY = 3000;

   private static final int TIME = 4 * 1000;// 4 segundos.

   @Override
   protected void onCreate( Bundle savedInstanceState ) {
      super.onCreate( savedInstanceState );

      // En primer lugar configuramos la pantalla en vertical y a pantalla completa ocultando
      // la barra de título o la action bar dependiendo  de la versión de Android que estemos
      // usando utilizando los métodos setRequestedOrientation(int)/requestWindowFeature(int).

      // Fijar orientación vertical.
      setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_PORTRAIT );

      // Ocultar barra de título.
      requestWindowFeature( Window.FEATURE_NO_TITLE );

      setContentView( R.layout.splash_screen );

      new Handler( ).postDelayed( new Runnable( ) {
         @Override
         public void run( ) {
            Intent intent = new Intent( SplashScreenActivity.this, Principal.class );
            startActivity( intent );
            SplashScreenActivity.this.finish( );

            // Manejar transicions animadas entre actividades.
            overridePendingTransition( R.anim.fade_in, R.anim.fade_out );
         }
      }, TIME );

      new Handler( ).postDelayed( new Runnable( ) {
         @Override
         public void run( ) {
         }
      }, TIME );
   }

   @Override
   public void onBackPressed( ) {
      this.finish( );
      super.onBackPressed( );
   }
}