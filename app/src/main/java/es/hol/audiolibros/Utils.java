package es.hol.audiolibros;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.StatFs;
import android.os.StrictMode;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.ByteArrayBuffer;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

class Utils {


   /**
    * Convierte el tiempo en milisegundos
    * al formato de tiempo
    * HH:MM:SS
    */
   String milliSecondsToTimer( long milliseconds ) {
      String finalTimerString = "";
      String secondsString = "";

      // Convertir la duración total.
      int hours = ( int ) ( milliseconds / ( 1000 * 60 * 60 ) );
      int minutes = ( int ) ( milliseconds % ( 1000 * 60 * 60 ) ) / ( 1000 * 60 );
      int seconds = ( int ) ( ( milliseconds % ( 1000 * 60 * 60 ) ) % ( 1000 * 60 ) / 1000 );

      // Añadir horas.
      if ( hours > 0 ) {
         finalTimerString = hours + ":";
      }

      // Colocar 0 a los segundos si son de un dígito.
      if ( seconds < 10 ) {
         secondsString = "0" + seconds;
      } else {
         secondsString = "" + seconds;
      }

      finalTimerString = finalTimerString + minutes + ":" + secondsString;

      // devolver la cadena de tiempo.
      return finalTimerString;
   }

   /**
    * Funcion para obtener el porcentaje de progreso
    *
    * @param currentDuration
    * @param totalDuration
    */
   int getProgressPercentage( long currentDuration, long totalDuration ) {
      Double percentage = ( double ) 0;

      long currentSeconds = ( int ) ( currentDuration / 1000 );
      long totalSeconds = ( int ) ( totalDuration / 1000 );

      // Calculando porcentaje.
      percentage = ( ( ( double ) currentSeconds ) / totalSeconds ) * 100;

      // Devolviendo porcentaje.
      return percentage.intValue( );
   }

   /**
    * Función para cambiar el progreso a escala de tiempo.
    *
    * @param progress      -
    * @param totalDuration returns current duration in milliseconds
    */
   int progressToTimer( int progress, int totalDuration ) {
      int currentDuration = 0;
      totalDuration = totalDuration / 1000;
      currentDuration = ( int ) ( ( ( ( double ) progress ) / 100 ) * totalDuration );

      // Devolver la duracion actual en milisegundos.
      return currentDuration * 1000;
   }

   /**
    * Función para convertir bitmap en array de byte.
    *
    * @param image returns array de byte []
    */

   public static byte[] convertBitmapToByteArray( Bitmap image ) {

      if ( image == null ) {
         return null;
      } else {
         byte[] b = null;
         try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream( );
            image.compress( CompressFormat.JPEG, 100, stream );
            b = stream.toByteArray( );
         } catch ( Exception e ) {
            e.printStackTrace( );
         }
         return b;
      }
   }

   /**
    * Autentifica al usuario mediante función WebService usando clase asíncrona.
    * <p>
    * Parámetro: context, contexto de la aplicación
    * Parámetro: datos, ArrayList con los datos
    * Retorno: resp, string con el valor devuelto por el WebService
    */

   static class verificaUsuario extends AsyncTask<Void, Void, String> {

      ProgressDialog Dialog;
      String cpass;

      Context context;
      ArrayList<NameValuePair> datos;

      public verificaUsuario( Context context, ArrayList<NameValuePair> datos ) {
         this.context = context;
         this.datos = datos;
      }

      protected void onPreExecute( ) {

         // Para el progress dialog.
         Log.i( "VerificaUsuario", "onPreExecute " + context );
         Log.i( "VerificaUsuario", "onPre clave: " + datos.get( 1 ).getValue( ).trim( ) );

         Dialog = ProgressDialog.show( context, "VALIDACION", "Comprobando datos de usuario" );

         // Ciframos la contraseña aplicándole una máscara (la misma que aplica el WebService).
         String masc = "|#€7`¬23ads4ook12";
         cpass = masc + Utils.Cifrado.cifrar( datos.get( 1 ).getValue( ).trim( ), "MD5" );
         cpass = Utils.Cifrado.cifrar( cpass, "SHA-1" );
         datos.set( 1, new BasicNameValuePair( "password", cpass ) );

         Log.i( "VerificaUsuario", "onPre cclave: " + datos.get( 1 ).getValue( ).trim( ) );
      }

      protected String doInBackground( Void... params ) {

         String url = datos.get( 2 ).getValue( ).trim( );

         // Usamos una clase httpHandler para enviar una petición al servidor web.
         Utils.httpHandler handler = new Utils.httpHandler( );
         String resp = handler.post( url, datos );

         Log.i( "VericaUsuario", "resp: " + resp + "url: " + url );

         // Valor devuelto para onPostExecute.
         return resp;

      }

      /**
       * Terminado doInBackground comprobar lo que halla ocurrido.
       *
       * @param resp, recibe respuesta.
       */
      protected void onPostExecute( String resp ) {

         Dialog.dismiss( ); /* ocultamos progess dialog. */

         Log.i( "onPostExecute=", "resp: " + resp );

         // Validamos el resultado obtenido.
         if ( resp.trim( ).contains( "0" ) ) {

            // Mostrar mensaje.
            Log.i( "verifica0 ", "invalido" );
            Utils.mensaje( context, " Usuario o contaseña incorrectos " );
            //err_reg(context," Usuario no existe ", true);

         } else if ( resp.trim( ).contains( "1" ) ) {

            // Mostrar mensaje.
            Log.i( "verifica1 ", "válido" );
            Utils.mensaje( context, " Usuario validado " );
            //err_reg(context," Correo no válido ", true);

         } else {

            // Mostrar mensaje.
            //err_reg(context," Error de sistema. \n Vuelva a intentarlo. ", true);
            Log.e( "registerstatus ", "Error de sistema" );
            Utils.mensaje( context, " Error de sistema. \n Vuelva a intentarlo. " );
         }

      } // Onpostexecute

   } // verificaUsuario


   /**
    * Comprueba si hay conexión de datos
    * <p>
    * Parámetro: context, contexto de la aplicación
    * <p>
    * Retorno: bConectado true o false
    */

   static boolean verificaConexion( Context ctx ) {

      boolean bConectado = false;

      ConnectivityManager connec = ( ConnectivityManager ) ctx.getSystemService( Context.CONNECTIVITY_SERVICE );

      // Almacenar las conexiones disponibles, no sólo wifi, también GPRS.
      NetworkInfo[] redes = connec.getAllNetworkInfo( );

      // Iterar para comprobar el estado de las conexiones.
      for ( int i = 0; i < 2; i++ ) {
         // ¿Tenemos conexión? ponemos a true
         if ( redes[ i ].getState( ) == NetworkInfo.State.CONNECTED ) {
            bConectado = true;
         }
      }

      return bConectado;
   }


   /**
    * Funcion que muestra un simple Alert Dialog
    *
    * @param context - application context
    * @param title   - alert dialog title
    * @param message - alert message
    * @param status  - success/failure (used to set icon)
    */
   static void showAlertDialog( Context context, String title, String message, Boolean status ) {

      AlertDialog alertDialog = new AlertDialog.Builder( context ).create( );

      // Setting Dialog Title.
      alertDialog.setTitle( title );

      // Setting Dialog Message.
      alertDialog.setMessage( message );

      alertDialog.setCancelable( false );

      // Setting alert dialog icon.
      alertDialog.setIcon( ( status ) ? R.drawable.success : R.drawable.aviso );

      // Setting OK Button.
      alertDialog.setButton( "OK", new DialogInterface.OnClickListener( ) {
         public void onClick( DialogInterface dialog, int which ) {
         }
      } );

      // Showing Alert Message.
      alertDialog.show( );

   }

   /**
    * Muestra un mensaje centrado en la pantalla
    * <p>
    * Parámetro: context, para poder mostrar mensajes
    * Parámetro: mensa, cadena a mostrar
    * <p>
    * Retorno: No
    */

   static void mensaje( Context context, String mensa ) {

      Toast toast = Toast.makeText( context, mensa, Toast.LENGTH_LONG );
      toast.setGravity( Gravity.BOTTOM, 0, 0 );
      toast.show( );
   }

   /**
    * Comprueba que existe un recurso
    * <p>
    * Parámetro: context para poder mostrar mensajes
    * Parámetro: url dirección del recurso
    * <p>
    * Retorno: int[] que contine el código de respuesta y el tamaño del recurso
    */

   static int[] CompruebaUrl( Context context, String url ) {

      int responseCode = 0;
      int totalSize = 0;

      try {

      /*disableConnectionReuseIfNecessary( );
      System.setProperty( "http.keepAlive", "false" );*/

         // Desactivar redirecciones.
         HttpURLConnection.setFollowRedirects( false );

         // Establecemos la conexión con el destino.
         HttpURLConnection con = ( HttpURLConnection ) new URL( url ).openConnection( );

         // Conectamos utilizando el método GET.
         con.setRequestMethod( "GET" );

         // Obtener código de respuesta.
         responseCode = con.getResponseCode( );

         // Obtener tamaño del recurso.
         totalSize = con.getContentLength( );

         // Desconectar conexión.
         con.disconnect( );

      } catch ( UnknownHostException uhe ) {
         // Handle exceptions as necessary
         //Utils.mensaje( context, "Error: \n" + uhe );
      } catch ( FileNotFoundException fnfe ) {
         // Handle exceptions as necessary
         Utils.mensaje( context, "No Existe: " + url );
      } catch ( Exception e ) {
         // Handle exceptions as necessary
         //Utils.mensaje( context, "Error: \n" + e );
      }

      Log.i( "CompUrl", "Code: " + responseCode + " Size: " + totalSize );

      // Devolver código y tamaño.
      return new int[]{ responseCode, totalSize };

   }

   /**
    * Descargar imagen desde url
    * <p>
    * Parámetro: url dirección del recurso
    * <p>
    * Retorno: Bitmap que contiene la imagen
    */
   static class DownloadImage {

      static Bitmap downloadImage( String URL ) {

         Bitmap bitmap = null;
         InputStream in = null;

         try {
            in = OpenHttpConnection( URL );
            bitmap = BitmapFactory.decodeStream( in );
            in.close( );
         } catch ( IOException e1 ) {
            e1.printStackTrace( );
         }

         return bitmap;

      } // downloadImage
   }

   /**
    * Abrir una conexion HTTP y devolver un objeto InputStream
    *
    * @param urlString
    * @return
    * @throws IOException
    */
   private static InputStream OpenHttpConnection( String urlString ) throws IOException {
      InputStream in = null;
      //int response = -1;

      URL url = new URL( urlString );
      URLConnection conn = url.openConnection( );

      if ( !( conn instanceof HttpURLConnection ) )
         throw new IOException( "Not an HTTP connection" );

      try {

         // Permitir el acceso a la red en el subproceso de la interfaz de usuario.
         StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder( ).permitAll( ).build( );
         StrictMode.setThreadPolicy( policy );

         HttpURLConnection connection = ( HttpURLConnection ) url.openConnection( );
         // HttpURLConnection httpConn = ( HttpURLConnection ) conn;

         connection.setAllowUserInteraction( false );
         connection.setInstanceFollowRedirects( true );
         String request = connection.getRequestMethod( );
         connection.setRequestMethod( request );
         connection.setDoInput( true );
         connection.setDoOutput( false );
         connection.connect( );

         int response = connection.getResponseCode( );
         if ( response == HttpURLConnection.HTTP_OK ) {
            in = connection.getInputStream( );
         }
      } catch ( MalformedURLException e ) {
         Log.d( "Exception: ", "MalformedURLException" );
      } catch ( IOException e ) {
         Log.d( "Exception: ", "IOException" );
      } catch ( Exception ex ) {
         throw new IOException( "Error connecting" );
      }
      return in;
   }

   /**
    * Descargar imagen desde url
    * <p>
    * Parámetro: url dirección del recurso
    * <p>
    * Retorno: Array de byte que contiene la imagen
    */
   static class DownloadImage1 {

      static byte[] getLogoImage( String url ) {

         try {
            URL imageUrl = new URL( url );
            URLConnection ucon = imageUrl.openConnection( );

            InputStream is = ucon.getInputStream( );
            BufferedInputStream bis = new BufferedInputStream( is );

            ByteArrayBuffer baf = new ByteArrayBuffer( 500 );
            int current = 0;
            while ( ( current = bis.read( ) ) != -1 ) {
               baf.append( ( byte ) current );
            }

            return baf.toByteArray( );
         } catch ( Exception e ) {
            Log.d( "ImageManager", "Error: " + e.toString( ) );
         }

         return null;
      }

   }

   /**
    * Descargar imagen desde url usando clase asíncrona
    * <p>
    * Parámetro: url dirección del recurso
    * <p>
    * Retorno: Bitmap que contine la imagen
    */


   static class ImageDownloader3 extends AsyncTask<String, Void, Bitmap> {
      ProgressDialog Dialog;

      Context context;
      String url;

      public ImageDownloader3( Context context, String url ) {
         this.context = context;
         this.url = url;
      }

      @Override
      protected Bitmap doInBackground( String... param ) {
         //return downloadBitmap(param[0]);
         return downloadBitmap( url );
      }

      @Override
      protected void onPreExecute( ) {
         super.onPreExecute( );
         Log.i( "Async-Example", "onPreExecute Called " + context );
         Dialog = ProgressDialog.show( context, "Wait", "Downloading Image" );
      }

      @Override
      protected void onPostExecute( Bitmap result ) {
         Log.i( "Async-Example", "onPostExecute Called" );
         // downloadedImg.setImageBitmap(result);
         Dialog.dismiss( );
         Toast.makeText( context, url, Toast.LENGTH_LONG ).show( );

      }

      /**
       * Descargar bitmap.
       *
       * @param url
       * @return
       */
      private Bitmap downloadBitmap( String url ) {
         // Inicializar el objeto de cliente HTTP predeterminado.
         final DefaultHttpClient client = new DefaultHttpClient( );

         // Formando una solicitud HttpGet.
         final HttpGet getRequest = new HttpGet( url );

         try {
            HttpResponse response = client.execute( getRequest );

            // Comprobar 200 ==> OK.
            final int statusCode = response.getStatusLine( ).getStatusCode( );
            if ( statusCode != HttpStatus.SC_OK ) {
               Log.w( "ImageDownloader", "Error " + statusCode + " while retrieving bitmap from " + url );
               return null;
            }

            final HttpEntity entity = response.getEntity( );
            if ( entity != null ) {
               InputStream inputStream = null;

               try {

                  // Obtener contenido del flujo
                  inputStream = entity.getContent( );

                  // Decodificar los datos del flujo en
                  // imagen Bitmap que android entiende.
                  return BitmapFactory.decodeStream( inputStream );

               } finally {

                  if ( inputStream != null ) {
                     inputStream.close( );
                  }
                  entity.consumeContent( );
               }
            }
         } catch ( Exception e ) {
            getRequest.abort( );
            Log.e( "ImageDownloader", "Something went wrong while retrieving bitmap from " + url + e.toString( ) );
         }
         return null;
      }

   }

   /**
    * Descargar imagen desde url usando clase asíncrona
    * <p>
    * Parámetro: url dirección del recurso
    * <p>
    * Retorno: Bitmap que contine la imagen
    */

   static class DownloadImage0 extends AsyncTask<String, Void, Bitmap> {

      ProgressDialog mProgressDialog;

      Context context;
      String URL;

      public DownloadImage0( Context context, String URL ) {
         this.context = context;
         this.URL = URL;
      }

      @Override
      protected void onPreExecute( ) {
         super.onPreExecute( );
         // Create a progressdialog
         mProgressDialog = new ProgressDialog( context );
         // Set progressdialog title
         mProgressDialog.setTitle( "Download Image Tutorial" );
         // Set progressdialog message
         mProgressDialog.setMessage( "Loading..." );
         mProgressDialog.setIndeterminate( false );
         // Show progressdialog
         mProgressDialog.show( );
      }

      @Override
      protected Bitmap doInBackground( String... param ) {

         String imageURL = URL;
         Bitmap bitmap = null;

         try {

            // Download Image from URL.
            InputStream input = new java.net.URL( imageURL ).openStream( );

            // Decode Bitmap.
            bitmap = BitmapFactory.decodeStream( input );

         } catch ( Exception e ) {
            e.printStackTrace( );
         }

         return bitmap;
      }

      @Override
      protected void onPostExecute( Bitmap result ) {
         // Close progressdialog
         mProgressDialog.dismiss( );
      }
   }

   /**
    * Descargar imagen desde url usando clase asíncrona
    * <p>
    * Parámetro: cadena de typo byte[]
    * <p>
    * Retorno: string cifrado
    */

   static class Cifrado {

      // Algoritmos.
      public static String MD2 = "MD2";
      public static String MD5 = "MD5";
      public static String SHA1 = "SHA-1";
      public static String SHA256 = "SHA-256";
      public static String SHA384 = "SHA-384";
      public static String SHA512 = "SHA-512";

      /***
       * Convierte un array de bytes a String usando valores hexadecimales
       * @param digest arreglo de bytes a convertir
       * @return String creado a partir de <code>digest</code>
       */

      private static String toHexadecimal( byte[] digest ) {
         String hash = "";
         for ( byte aux : digest ) {
            int b = aux & 0xff;
            if ( Integer.toHexString( b ).length( ) == 1 ) hash += "0";
            hash += Integer.toHexString( b );
         }
         return hash;
      }

      /**
       * Encripta un mensaje de texto mediante algoritmo de resumen de mensaje.
       *
       * @param message   texto a encriptar
       * @param algorithm algoritmo de encriptación, puede ser: MD2, MD5, SHA-1, SHA-256, SHA-384, SHA-512
       * @return mensaje encriptado
       */

      static String cifrar( String message, String algorithm ) {
         byte[] digest = null;
         byte[] buffer = message.getBytes( );
         try {
            MessageDigest messageDigest = MessageDigest.getInstance( algorithm );
            messageDigest.reset( );
            messageDigest.update( buffer );
            digest = messageDigest.digest( );
         } catch ( NoSuchAlgorithmException ex ) {
            System.out.println( "Error creando Digest" );
         }
         return toHexadecimal( digest );
      }
   }

   /**
    * CLASE AUXILIAR PARA EL ENVIO DE PETICIONES A NUESTRO SISTEMA Y MANEJO DE RESPUESTA.
    */
   public class Httppostaux {

      InputStream is = null;
      String result = "";

      public JSONArray getserverdata( ArrayList<NameValuePair> parameters, String urlwebserver ) {

         // Conecta via http y envia un post.
         httppostconnect( parameters, urlwebserver );

         if ( is != null ) { /* si obtuvo una respuesta */
            getpostresponse( );
            return getjsonarray( );
         } else {
            return null;
         }
      }

      /**
       * Petición HTTP.
       *
       * @param parametros
       * @param urlwebserver
       */

      private void httppostconnect( ArrayList<NameValuePair> parametros, String urlwebserver ) {

         try {
            HttpClient httpclient = new DefaultHttpClient( );
            HttpPost httppost = new HttpPost( urlwebserver );
            httppost.setEntity( new UrlEncodedFormEntity( parametros ) );

            // Ejecutar petición enviando datos por POST.
            HttpResponse response = httpclient.execute( httppost );
            HttpEntity entity = response.getEntity( );
            is = entity.getContent( );

         } catch ( Exception e ) {
            Log.e( "log_tag", "Error in http connection " + e.toString( ) );
         }
      }

      /**
       * Obtener respuesta del server.
       */
      void getpostresponse( ) {

         // Convierte respuesta a String.
         try {
            BufferedReader reader = new BufferedReader( new InputStreamReader( is, "iso-8859-1" ), 8 );
            StringBuilder sb = new StringBuilder( );
            String line = null;
            while ( ( line = reader.readLine( ) ) != null ) {
               sb.append( line ).append( "\n" );
            }

            is.close( );
            result = sb.toString( );

            Log.e( "getpostresponse", " result= " + sb.toString( ) );

         } catch ( Exception e ) {
            Log.e( "log_tag", "Error converting result " + e.toString( ) );
         }
      }

      JSONArray getjsonarray( ) {
         // Parsear json data.
         try {
            return new JSONArray( result );
         } catch ( JSONException e ) {
            Log.e( "log_tag", "Error parsing data " + e.toString( ) );
            return null;
         }
      }
   }

   /**
    * En esta clase lo que hacemos es implementar la comunicación, aquí está
    * lo que se necesita para enviar la petición a PHP junto con información.
    */
   static class httpHandler {

      String post( String posturl, ArrayList<NameValuePair> params ) {

         InputStream is = null;
         String text = "";

         Log.i( "Handler ", posturl );

         // Creamos el objeto de HttpClient que nos permitirá conectarnos mediante peticiones http.
         HttpClient httpclient = new DefaultHttpClient( );

         // El objeto HttpPost permite que enviemos una petición de tipo POST a una URL especificada.
         HttpPost httppost = new HttpPost( posturl );

         try {

            // Anexamos los parámetros al objeto para que al enviarse
            // al ====> 'servidor' envíen los datos que hemos añadido.
            httppost.setEntity( new UrlEncodedFormEntity( params ) );

            // Finalmente ejecutamos enviando la info al server.
            HttpResponse resp = httpclient.execute( httppost );

            // Obtenemos una respuesta.
            HttpEntity ent = resp.getEntity( );
            is = ent.getContent( );

            //String valor = EntityUtils.toString(resp.getEntity(), HTTP.UTF_8);
            //Log.i("Handler: ", "Valor: " + valor);

            if ( is != null ) {

               try { /* Convierte respuesta a String. */

                  BufferedReader reader = new BufferedReader( new InputStreamReader( is, "UTF-8" ), 8 );
                  StringBuilder sb = new StringBuilder( );

                  String line = null;
                  while ( ( line = reader.readLine( ) ) != null ) {
                     sb.append( line ).append( "\n" );
                  }

                  is.close( );

                  text = sb.toString( );
                  Log.i( "Handler: ", "Result: " + is );
                  Log.i( "Handler: ", "Result= " + sb.toString( ) );
                  Log.i( "Handler: ", "Result: " + text );

               } catch ( Exception e ) {
                  Log.e( "log_tag", "Error converting result " + e.toString( ) );
               }
            }
            //String text = EntityUtils.toString(ent);

         } catch ( Exception e ) {
            Log.e( "Handler ", "Error al enviar al server" );
            e.printStackTrace( );
            return "error";
         }

         return text;
      }
   }

   /**
    * Descargar fichero desde url y guardarlo en el dispositivo
    * <p>
    * Parámetro: ruta dirección del recurso
    * <p>
    * Retorno: nada
    */
   static void descarga_file( Context context, String ruta ) {

      try { /* Indico URL del archivo y el nombre que tendrá el archivo */

         String fileName = "catalogo.json";

         // Crear drectorio si no existe.
         String path = context.getFilesDir( ) + "/Cat_Audio/";
         File storagePath = new File( path ); // + File.separator
         storagePath.mkdirs( );

         Log.i( "Descarga", "Ruta: " + storagePath );

         // Definir ruta como URL.
         URL url = new URL( ruta );
         //File file = new File(storagePath, fileName);

         long startTime = System.currentTimeMillis( );
         Log.d( "Descarga", "descarga comenzada" );
         Log.d( "Descarga", "descarga url:" + url );
         Log.d( "Descarga", "nombre archivo:" + fileName );

         // Abrir una conexion a URL y obtener el tamaño del recurso.
         URLConnection ucon = url.openConnection( );
         int totalSize = ucon.getContentLength( );

         Log.d( "Descarga", "CONEXION CORRECTA tam: " + totalSize );

         // Defino InputStreams para leer desde la URLConnection.
         InputStream is = ucon.getInputStream( );

         // Definir buffer de lectura con tamaño inicial.
         BufferedInputStream bis = new BufferedInputStream( is, 1024 * 5 );

         Log.d( "Descarga", "INPUT STREAM" );

         // File file = new File(context.getDir("Cat_Audio", Context.MODE_PRIVATE) + "/"+ fileName);

         // Creamos un objeto del tipo de fichero
         // donde  descargaremos  nuestro fichero.
         File file = new File( storagePath, fileName );

         // Si existe lo borramos.
         if ( file.exists( ) ) {
            file.delete( );
         }

         // boolean resp = file.createNewFile();
         // Log.i("Descarga", "resp: "+ resp);

         // Crear buffer con tamaño inicial y una variable
         // para ir almacenando el tamaño temporal de éste.
         ByteArrayBuffer baf = new ByteArrayBuffer( 5 * 1024 );
         int current = 0;

         // Leer y añadir bytes para el Buffer hasta que no queden más para leer:(-1).
         while ( ( current = bis.read( ) ) != -1 ) {
            baf.append( ( byte ) current );
         }

         Log.d( "Descarga", "path: " + path + fileName );

         // Utilizaremos un objeto del tipo => 'fileoutputstream'
         // para escribir el archivo que descargamos en el nuevo.
         FileOutputStream fos = new FileOutputStream( file );

         // Guardar el contenido del buffer en disco.
         fos.write( baf.toByteArray( ) );
         fos.flush( );

         // Cerrar y limpiar.
         fos.close( );
         bis.close( );
         baf.clear( );

         Log.d( "Descarga", "descarga lista en " + ( ( System.currentTimeMillis( ) - startTime ) / 1000 ) + " segundos" );

      } catch ( IOException e ) {
         Log.d( "Descarga", "Error: " + e );
      }

   }

   /**
    * Descargar fichero desde url y guardarlo en la SD del dispositivo
    * <p>
    * Parámetro: ruta dirección del recurso
    * <p>
    * <p>
    * Retorno: nada
    */

   public static void descarga_fileSD( String ruta ) {

      try {

         // Indico URL al archivo y el nombre que tendrá el archivo.
         String fileName = "catalogo.json";

         // Crear drectorio si no existe.
         File storagePath = new File( Environment.getExternalStorageDirectory( ), "Cat_Audio" );
         storagePath.mkdirs( );

         // Definir variables.
         URL url = new URL( ruta );
         File file = new File( storagePath, fileName );

         long startTime = System.currentTimeMillis( );
         Log.d( "Descarga", "descarga comenzada" );
         Log.d( "Descarga", "descarga url:" + url );
         Log.d( "Descarga", "nombre archivo:" + fileName );

            /* Abrir una conexion a URL. */
         URLConnection ucon = url.openConnection( );
         int totalSize = ucon.getContentLength( );
         Log.d( "Descarga", "CONEXION CORRECTA tam: " + totalSize );

         // Defino InputStreams y buffer para leer desde la URLConnection.
         InputStream is = ucon.getInputStream( );
         BufferedInputStream bis = new BufferedInputStream( is );
         Log.d( "Descarga", "INPUT STREAM" );

         // Definir buffer con capacidad inicial.
         ByteArrayBuffer baf = new ByteArrayBuffer( 5 * 1024 );
         int current = 0;

         // Leer y añadir bytes para el Buffer hasta que no queden más para leer:(-1).
         while ( ( current = bis.read( ) ) != -1 ) {
            baf.append( ( byte ) current );
         }

         // Guardar datos leídos.
         FileOutputStream fos = new FileOutputStream( file );
         fos.write( baf.toByteArray( ) );
         fos.flush( );
         fos.close( );

         Log.d( "Descarga", "descarga lista en " + ( ( System.currentTimeMillis( ) - startTime ) / 1000 ) + " segundos" );

      } catch ( IOException e ) {
         Log.d( "Descarga", "Error: " + e );
      }

   }
/**
 * Funciones para control del espacio libre de memoria
 *
 * Memoria Total, Libre y ocupada.
 */

   /**************************************************************************************
    Returns size in MegaBytes.
    If you need calculate external memory, change this:
    StatFs statFs = new StatFs(Environment.getRootDirectory().getAbsolutePath());
    to this:
    StatFs statFs = new StatFs(Environment.getExternalStorageDirectory().getAbsolutePath());
    **************************************************************************************/
   public static long TotalMemory( ) {
      StatFs statFs = new StatFs( Environment.getRootDirectory( ).getAbsolutePath( ) );
      return ( ( long ) statFs.getBlockCount( ) * ( long ) statFs.getBlockSize( ) ) / 1048576;
   }

   public static long FreeMemory( ) {
      StatFs statFs = new StatFs( Environment.getRootDirectory( ).getAbsolutePath( ) );
      return ( statFs.getAvailableBlocks( ) * ( long ) statFs.getBlockSize( ) ) / 1048576;
   }

   public static long BusyMemory( ) {
      StatFs statFs = new StatFs( Environment.getRootDirectory( ).getAbsolutePath( ) );
      long Total = ( ( long ) statFs.getBlockCount( ) * ( long ) statFs.getBlockSize( ) ) / 1048576;
      long Free = ( statFs.getAvailableBlocks( ) * ( long ) statFs.getBlockSize( ) ) / 1048576;
      long Busy = Total - Free;
      return Busy;
   }

   /**
    * @return Numero de bytes disponibles en almacenamiento interno.
    */
   static long getInternalAvailableSpace( ) {
      long availableSpace = -1L;
      try {
         StatFs stat = new StatFs( Environment.getDataDirectory( )
             .getPath( ) );
         stat.restat( Environment.getDataDirectory( ).getPath( ) );
         availableSpace = ( long ) stat.getAvailableBlocks( ) * ( long ) stat.getBlockSize( );
      } catch ( Exception e ) {
         e.printStackTrace( );
      }
      return availableSpace;
   }

} // Utils