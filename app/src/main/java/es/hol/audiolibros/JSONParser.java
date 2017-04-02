package es.hol.audiolibros;

import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

class JSONParser {

   static InputStream is = null;
   static String json = "";
   private static JSONArray jarray = null;

   /**
    * Contructor
    */
   JSONParser( ) {
   }

   /**
    * Analiza un documento JSON para devolver la colección de datos.
    */
   static class JSONToStringCollection {

      JSONObject object;

      /**
       * Comstructor.
       *
       * @param object
       */
      JSONToStringCollection( JSONObject object ) {
         this.object = object;
      }

      /**
       * Analiza el objeto JSON y extrae la colección de datos.
       *
       * @return ArrayList
       * @throws JSONException
       */
      ArrayList<String> getArrayList( ) throws JSONException {

         ArrayList<String> data = new ArrayList<>( );

         // Si no es igual a un nuevo objeto JSON, extraigo los datos de éste.
         if ( !object.equals( new JSONObject( ) ) ) {

            // Del documento JSON extraemos el valor de 'error'.
            JSONObject jsonObject = object.getJSONObject( "error" );

            // Añadio el error al arrayList.
            data.add( jsonObject.getJSONObject( "error" ).toString( ) );

            // Del documento JSON extraemos el array 'data',
            // que contiene una colección de 'usuarios'.
            JSONArray array = object.getJSONArray( "data" );

            // Recorremos el array para analizar todos los valores que contiene.
            for ( int i = 0; i < array.length( ); i++ ) {
               JSONObject obj = array.getJSONObject( i );

               // Añadimos al array los datos extraidos
               data.add( obj.getJSONObject( "idUsuario" ).toString( ) );
               data.add( obj.getJSONObject( "login" ).toString( ) );
               data.add( obj.getJSONObject( "email" ).toString( ) );
            }
         }
         return data;
      }
   }


   public JSONArray getJSONFromUrl( String url ) {
      StringBuilder builder = new StringBuilder( );
      HttpClient client = new DefaultHttpClient( );

      try {
         HttpGet httpGet = new HttpGet( url );
         HttpResponse response = client.execute( httpGet );
         StatusLine statusLine = response.getStatusLine( );
         int statusCode = statusLine.getStatusCode( );

         if ( statusCode == 200 ) {
            HttpEntity entity = response.getEntity( );
            InputStream content = entity.getContent( );

            BufferedReader reader = new BufferedReader( new InputStreamReader( content ) );

            String line;
            while ( ( line = reader.readLine( ) ) != null ) {
               builder.append( line );
               //builder.append(line+"\n");
            }
         } else {
            Log.e( "==>", "Falló la descarga del archivo" );

         }

      } catch ( ClientProtocolException e ) {
         e.printStackTrace( );
      } catch ( SocketTimeoutException e ) {
         Log.e( "==>", "Error when calling postData", e );

      } catch ( IOException e ) {
         e.printStackTrace( );
      }


      try {/* Parsear objeto String a objeto JSON. */
         jarray = new JSONArray( builder.toString( ) );
      } catch ( JSONException e ) {
         Log.e( "JSON Parser", "Error parsing data " + e.toString( ) );

      }
      // Devolver JSON String.
      return jarray;
   }

   /**
    * Obtener JSON desde archivo y parsearlo a objeto String.
    *
    * @param ruta
    * @return
    */
   JSONArray getJSONFromFile( String ruta ) {
      StringBuilder builder = new StringBuilder( );

      try {
         FileInputStream stream = new FileInputStream( ruta );
         BufferedReader reader = new BufferedReader( new InputStreamReader( stream ), 1024 * 5 );

         String line;
         while ( ( line = reader.readLine( ) ) != null ) {
            builder.append( line );
            //builder.append(line+"\n");
         }

      } catch ( OutOfMemoryError e ) {
         Log.e( "JSON Parser", "Error parsing data " + e.toString( ) );
      } catch ( Exception e ) {
         e.printStackTrace( );
      }

      try {/* Parsear objeto String a objeto JSON. */
         jarray = new JSONArray( builder.toString( ) );
      } catch ( JSONException e ) {
         Log.e( "JSON Parser", "Error parsing data " + e.toString( ) );

      }
      // Devolver JSON String.
      return jarray;
   }
}