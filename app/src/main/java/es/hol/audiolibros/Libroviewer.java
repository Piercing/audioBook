package es.hol.audiolibros;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class Libroviewer extends Activity {

   /*  Constantes globales. */
   public static final int KEY_ROWID = 0;
   public static final int KEY_TITULO = 1;
   public static final int KEY_AUTOR = 2;
   public static final int KEY_TEMA = 3;
   public static final int KEY_DESCRIP = 4;
   public static final int KEY_MP3URL = 5;
   public static final int KEY_POSICION = 6;
   public static final int KEY_IMAGEN = 7;

   private Cursor cur;
   private byte[] bb;

   private String titulo, mp3url;
   private int pos, id;

   public void onCreate( Bundle savedInstanceState ) {
      super.onCreate( savedInstanceState );

      // Fijar de manera permanente la orientación en modo vertical.
      setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_PORTRAIT );

      // Ocultar barra de título.
      requestWindowFeature( Window.FEATURE_NO_TITLE );

      setContentView( R.layout.libroviewer );

      // Obtener los valores pasados en el Intent.
      Bundle extras = getIntent( ).getExtras( );
      int idpos = extras.getInt( "ID" );

      // Refenciar objetos View.
      ImageView image = ( ImageView ) findViewById( R.id.imageView1 );
      TextView tit = ( TextView ) findViewById( R.id.txt_tit );
      TextView aut = ( TextView ) findViewById( R.id.txt_aut );
      TextView desc = ( TextView ) findViewById( R.id.txt_des );

      // Obtener cursor con el registro especificado en pos.
      DataBase db = new DataBase( this );
      cur = db.getLibro( idpos );
      db.close( );

      // Asignar datos del cursor a objetos View.
      tit.setText( cur.getString( KEY_TITULO ) );
      tit.setSelected( true );
      aut.setText( cur.getString( KEY_AUTOR ) );
      desc.setText( cur.getString( KEY_DESCRIP ) );

      tit.setSelected( true );

      bb = cur.getBlob( KEY_IMAGEN );
      image.setImageBitmap( BitmapFactory.decodeByteArray( bb, 0, bb.length ) );

      mp3url = cur.getString( KEY_MP3URL );
      pos = cur.getInt( KEY_POSICION );
      titulo = cur.getString( KEY_TITULO ) + " - " + cur.getString( KEY_AUTOR );
      id = cur.getInt( KEY_ROWID );

      cur.close( );

      WindowManager.LayoutParams params = getWindow( ).getAttributes( );
      //params.x = -5;
      params.y = 100;
      params.height = 800;
      params.width = 500;
      params.y = -10;

      this.getWindow( ).setAttributes( params );

   } // onCreate

   /**
    * Cierra la actividad.
    *
    * @param v
    */
   public void Volver( View v ) {
      finish( );
   }

   // Al pulsar boton play.
   public void play( View boton ) {

      //Utils.mensaje(getApplicationContext(),"Reproducir");
      Log.d( "LibroV", mp3url + "-" + titulo );

      // Obtener cursor con el registro especificado
      // en ==>  'pos'. Puede que éste haya cambiado.
      DataBase db = new DataBase( this );
      cur = db.getLibro( id );
      db.close( );

      pos = cur.getInt( KEY_POSICION );
      cur.close( );

      // Obtiene el objeto de preferencias de la aplicación.
      SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences( Libroviewer.this );

      // Obtener datos desde preferencias.
      String usuario = prefs.getString( "user", "" );
      String clave = prefs.getString( "pass", "" );
      String server = prefs.getString( "server", "www.audiobooks.hol.es" );
      boolean registrado = prefs.getBoolean( "REGISTRO", false );

      // Comprobar conexión.
      if ( !Utils.verificaConexion( this ) ) {

         // No hay conexión a Internet. Mostrar diálogo.
         Utils.showAlertDialog( Libroviewer.this, "Internet", "Comprueba tu conexión", false );

         return;
      }

      // Algunas url contienen espacios y darían error.
      mp3url = mp3url.replaceAll( "\\s", "%20" );

      Log.d( "LibroV", mp3url );

      // Comprobar que existe la url.
      int[] result = Utils.CompruebaUrl( getBaseContext( ), mp3url );

      if ( result[ 0 ] == 200 || result[ 0 ] == 0 ) {
         //toast = Toast.makeText(getApplicationContext(),
         // "Existe: "+mp3url, Toast.LENGTH_LONG);
         // toast.setGravity(Gravity.TOP, 25, 300);
         // toast.show();

         // Crear un Intent, pasar parámetros y lanzar una nueva Activity con el reproductor.
         Intent i = new Intent( getApplicationContext( ), MusicPlayerActivity.class );

         // Llamamos al método 'putExtra' de la clase 'Intent'. Tiene dos parámetros de tipo 'String'
         // clave-valor, en el primero indicamos el nombre del dato y en el segundo el valor del dato:
         i.putExtra( "titulo_autor", titulo );
         i.putExtra( "url", mp3url );
         i.putExtra( "pos", String.valueOf( pos ) );
         i.putExtra( "id", String.valueOf( id ) );
         i.putExtra( "img", Base64.encodeToString( bb, Base64.DEFAULT ) );
         i.putExtra( "desde", "favoritos" );

         startActivity( i );

      } else {
         Utils.mensaje( getApplicationContext( ), "Code: " + result[ 0 ] + "\nNo se encontró el audiolibro o\nno hay conexión." );
      }
   }

   // En nuestra tarea no necesitamos entrada, no usaremos
   // información de progreso  y devolveremos un  'String'.
   private class verificaUsuario extends AsyncTask<Void, Void, String> {

      ProgressDialog Dialog;
      String cpass;

      Context context;
      ArrayList<NameValuePair> datos;

      // Una vez terminado doInBackground, comprobar lo que halla ocurrido. Recibe resp.
      verificaUsuario( Context context, ArrayList<NameValuePair> datos ) {
         this.context = context;
         this.datos = datos;
      }

      protected void onPreExecute( ) {

         // Para el progress dialog.
         Log.i( "VerificaUsuario", "onPreExecute " + context );
         Log.i( "VerificaUsuario", "onPre clave: " + datos.get( 1 ).getValue( ).trim( ) );

         //Dialog = ProgressDialog.show(context,"VALIDACION", "Comprobando datos de usuario");

         Dialog = new ProgressDialog( Libroviewer.this );
         Dialog.setMessage( "Validando...." );
         Dialog.setIndeterminate( false );
         Dialog.setCancelable( false );
         Dialog.show( );

         // Ciframos la contraseña aplicándole una máscara (la misma que aplica el WebService).
         String masc = "|#€7`¬23ads4ook12";
         cpass = masc + Utils.Cifrado.cifrar( datos.get( 1 ).getValue( ).trim( ), "MD5" );
         cpass = datos.get( 1 ).getValue( ).trim( );
         cpass = Utils.Cifrado.cifrar( cpass, "SHA-1" );
         datos.set( 1, new BasicNameValuePair( "password", cpass ) );

         Log.i( "VerificaUsuario", "onPre cclave: " + datos.get( 1 ).getValue( ).trim( ) );

         // Codificar en base64.
         byte[] byteArray;

         try {

            byteArray = datos.get( 0 ).getValue( ).trim( ).getBytes( "UTF-8" );
            datos.set( 0, new BasicNameValuePair( "usuario", Base64.encodeToString( byteArray, Base64.DEFAULT ) ) );

            byteArray = datos.get( 1 ).getValue( ).trim( ).getBytes( "UTF-8" );
            datos.set( 1, new BasicNameValuePair( "password", Base64.encodeToString( byteArray, Base64.DEFAULT ) ) );

         } catch ( UnsupportedEncodingException e ) {

            Log.i( "VerificaUsuario", "Error: " + e );
            //e.printStackTrace();
         }

         Log.i( "VerificaUsuario", "onPre b64clave: " + datos.get( 1 ).getValue( ).trim( ) );
      }

      protected String doInBackground( Void... params ) {

         String url = datos.get( 2 ).getValue( ).trim( );

         // Elimimar url de datos.
         datos.set( 2, new BasicNameValuePair( "url", "" ) );

         // Usamos una clase httpHandler para enviar una petición al servidor web.
         Utils.httpHandler handler = new Utils.httpHandler( );
         String resp = handler.post( url, datos );

         Log.i( "VericaUsuario", "resp: " + resp + "url: " + url );

         // Valor devuelto para onPostExecute.
         return resp;
      }

      protected void onPostExecute( String resp ) {

         Log.i( "onPostExecute=", "resp: " + resp );

         // Validamos el resultado obtenido.
         if ( resp.trim( ).contains( "0" ) ) {

            // Mostrar mensaje.
            Log.i( "verifica0 ", "invalido" );
            Utils.mensaje( context, " Usuario o contaseña incorrectos " );

         } else if ( resp.trim( ).contains( "1" ) ) {

            // Mostrar mensaje.
            Log.i( "verifica1 ", "valido" );
            //Utils.mensaje(context, " Usuario validado ");

            // Crear un Intent, pasar parámetros y lanzar una nueva Activity con el reproductor.
            Intent i = new Intent( getApplicationContext( ), MusicPlayerActivity.class );

            // Llamamos al método 'putExtra' de la clase 'Intent'. Tiene dos parámetros de tipo 'String'
            // clave-valor, en el primero indicamos el nombre del dato y en el segundo el valor del dato:
            i.putExtra( "titulo_autor", titulo );
            i.putExtra( "url", mp3url );
            i.putExtra( "pos", String.valueOf( pos ) );
            i.putExtra( "id", String.valueOf( id ) );
            i.putExtra( "img", Base64.encodeToString( bb, Base64.DEFAULT ) );
            i.putExtra( "desde", "favoritos" );

            startActivity( i );

         } else {

            // Mostrar mensaje.
            Log.e( "registerstatus ", "Error de sistema" );
            Utils.mensaje( context, " Error de sistema. \n Vuelva a intentarlo. " );
         }

         Dialog.dismiss(/* ocultamos progess dialog */ );

      } // Onpostexecute

   } // verificaUsuario

}