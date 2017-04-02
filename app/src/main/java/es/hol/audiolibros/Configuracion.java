package es.hol.audiolibros;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.WindowManager;

// Las preferencias no son más que datos que una aplicación debe guardar para personalizar la experiencia del usuario,
// por ejemplo información personal, opciones de presentación, etc.
// Cada preferencia se almacenará en forma de clave-valor, es decir, cada una de ellas estará compuesta
// por un identificador único (p.e. “email”) y un valor asociado a dicho identificador (p.e. “prueba@email.com”).
// Además, los datos se guardan en ficheros XML.
// al extender a PreferenceActivity, se encargará por nosotros de crear la interfaz gráfica de nuestra lista de opciones
// según hemos la definido en el XML y se preocupará por nosotros de mostrar, modificar y guardar las opciones cuando sea
// necesario tras la acción del usuario haciendo uso de la API de preferencias compartidas (Shared Preferences).

public class Configuracion extends PreferenceActivity implements OnSharedPreferenceChangeListener {

   SharedPreferences prefs;
   Context context;

   String servidor, usuario, clave, correo;

   public void onCreate( Bundle savedInstanceState ) {
      super.onCreate( savedInstanceState );

      // Fijar orientacion vertical.
      setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_PORTRAIT );

      // Ocultamos el teclado virtual. Aparece al tener el foco un EditText.
      getWindow( ).setSoftInputMode( WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN );

      // Obtener el action Bar para el botón volver.
      ActionBar actionBar = getActionBar( );
      actionBar.setDisplayHomeAsUpEnabled( true );

      context = this; //getApplicationContext(); getBaseContext();
      prefs = PreferenceManager.getDefaultSharedPreferences( context );

      // Obtener valores para verificar cambios.
      servidor = prefs.getString( "server", "" );
      usuario = prefs.getString( "user", "" );
      clave = prefs.getString( "pass", "" );
      correo = prefs.getString( "mail", "" );

      // Llamada al método ===> 'addPreferencesFromResource()', mediante el que
      // indicaremos el fichero XML en el que definimos la pantalla de opciones.
      addPreferencesFromResource( R.xml.configuracion );
   }

   /**
    * Al pulsar botón volver en la status bar, ir a la actividad principal.
    *
    * @param item
    * @return
    */
   public boolean onOptionsItemSelected( MenuItem item ) {
      Intent myIntent = new Intent( getApplicationContext( ), Principal.class );
      startActivityForResult( myIntent, 0 );
      return true;
   }

   // Método llamado cada vez que un cambio es confirmado.
   // Lo utilizamos para verificar el contenido de los parámetros
   @Override
   public void onSharedPreferenceChanged( SharedPreferences prefs, String key ) {

      // activar editor de preferencias.
      SharedPreferences.Editor prefEditor = prefs.edit( );

      Log.i( "Config", "key: " + key );


      // Validar server.
      if ( key.equals( "server" ) ) {

         String valor = prefs.getString( "server", "" );

         Log.i( "Config", "key if: " + key );

         if ( valor.equals( "" ) ) {

            showErrorDialog( context, "Server vacio.\n\nSe asignara el server por defecto.", false );
            Log.i( "Config", "key vacio: " + key + " " + valor );

            // Asignar valor por defecto.
            prefEditor.putString( "server", "www.audiobooks.hol.es" );
            prefEditor.apply( );
            // reload();

         } else if ( !valor.equals( servidor ) ) {

            showErrorDialog( context, "Server desconocido.\n\nSe asignara el server por defecto.", false );
            Log.i( "Config", "key vacio: " + key + " " + valor );

            // Asignar valor por defecto.
            prefEditor.putString( "server", "www.audiobooks.hol.es" );
            prefEditor.commit( );
            // reload();
         }
      }

      // Validar usuario.
      if ( key.equals( "user" ) ) {

         String valor = prefs.getString( "user", "" );

         Log.i( "Config", "key if: " + key );

         if ( valor.equals( "" ) || !valor.equals( usuario ) ) {

            showErrorDialog( context, "Modificar valores.\n\nSi no coinciden con " +
                "los datos de registro, la aplicación no será operativa.", false );
            Log.i( "Config", "key vacio: " + key + " " + valor );

         }
      }


      // Validar contraseña.
      if ( key.equals( "pass" ) ) {

         String valor = prefs.getString( "pass", "" );

         Log.i( "Config", "key if: " + key );

         if ( valor.equals( "" ) || !valor.equals( clave ) ) {

            showErrorDialog( context, "Modificar valores.\n\nSi no coinciden con " +
                "los datos de registro, la aplicación no será operativa.", false );
            Log.i( "Config", "key vacio: " + key + " " + valor );
         }
      }

      // Validar correo.
      if ( key.equals( "mail" ) ) {

         String valor = prefs.getString( "mail", "" );

         Log.i( "Config", "key if: " + key );

         if ( valor.equals( "" ) || !valor.equals( correo ) ) {

            showErrorDialog( context, "Modificar valores.\n\nSi no coinciden con " +
                "los datos de registro, la aplicación no será operativa.", false );
            Log.i( "Config", "key vacio: " + key + " " + valor );

         }
      }
      // Registro manual.
      if ( key.equals( "reg_man" ) ) {

         boolean valor = prefs.getBoolean( "reg_man", false );

         Log.i( "Config", "key if: " + key );

         if ( valor ) {

            showErrorDialog( context, "Modificar valores.\n\nSi no coinciden con " +
                "los datos de registro, la aplicación no será operativa.", false );

            // Editar valor.
            prefEditor.putBoolean( "REGISTRO", true );
            prefEditor.commit( );

            Log.i( "Config", "key vacio: " + key + " " + valor );

         } else {

            // Editar valor.
            prefEditor.putBoolean( "REGISTRO", false );
            prefEditor.commit( );
         }
      }

      // Reiniciar datos entorno.
      if ( key.equals( "entorno" ) ) {

         boolean valor = prefs.getBoolean( "entorno", false );

         Log.i( "Config", "key if: " + key );

         if ( valor ) {

            showErrorDialog( context, "Reiniciar valores.\n\nSe borrarán los datos de entorno.", false );

            // Editar valor.
            prefEditor.putBoolean( "CATALOGO", false );
            prefEditor.putInt( "TAMANO", 0 );
            prefEditor.commit( );

            Log.i( "Config", "key vacio: " + key + " " + valor );

         } else {

         }
      }
      return;
   }

   private void showErrorDialog( Context context, String errorString, boolean status ) {
      //String okButtonString = context.getString(R.string.ok_name);

      AlertDialog.Builder ad = new AlertDialog.Builder( context );
      ad.setTitle( "AVISO" );

      // Asignar icono
      ad.setIcon( ( status ) ? R.drawable.success : R.drawable.fail );
      ad.setMessage( errorString );
      ad.setPositiveButton( "Ok", new DialogInterface.OnClickListener( ) {
         public void onClick( DialogInterface dialog, int arg1 ) {
            reload( );
         }
      } );

      //ad.create();
      ad.show( );
      return;

   }

   // Relanzar la actividad usando el mismo intent.
   private void reload( ) {
      startActivity( getIntent( ) );
      finish( );
   }

   // Para obtener el método de notificación ===> hay que registrar tu actividad como un
   // escuchador válido. Lo mejor es registrarse en onResume y desregistrarse en onPause:
   @Override
   protected void onResume( ) {
      super.onResume( );
      prefs.registerOnSharedPreferenceChangeListener( this );
   }

   @Override
   protected void onPause( ) {
      super.onPause( );
      prefs.unregisterOnSharedPreferenceChangeListener( this );
   }
}