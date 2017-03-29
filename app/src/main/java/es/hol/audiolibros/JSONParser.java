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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;

public class JSONParser {
  static InputStream is = null;
  static JSONArray jarray = null;
  static String json = "";

  // constructor
  public JSONParser( ) {
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

    // try parse the string to a JSON object
    try {
      jarray = new JSONArray( builder.toString( ) );
    } catch ( JSONException e ) {
      Log.e( "JSON Parser", "Error parsing data " + e.toString( ) );

    }

    // return JSON String
    return jarray;
  }


  public JSONArray getJSONFromFile( String ruta ) {
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

    // try parse the string to a JSON object
    try {
      jarray = new JSONArray( builder.toString( ) );
    } catch ( JSONException e ) {
      Log.e( "JSON Parser", "Error parsing data " + e.toString( ) );

    }

    // return JSON String
    return jarray;
  }
}