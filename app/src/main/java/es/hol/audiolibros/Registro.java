package es.hol.audiolibros;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class Registro extends Activity {
  // Variables objetos View
  EditText user;
  EditText pass;
  EditText mail;
  Button btn_regis;

  // Variables de uso general
  String IP_Server = ""; // Ip o dominio del server
  String URL_connect = ""; // ruta del WebService
  String mensa = null; // mensajes para toast
  Context context; // contexto
  ArrayList<NameValuePair> datos; // contiene datos de registro

  boolean result_back;
  private ProgressDialog pDialog;

  @Override
  public void onCreate( Bundle savedInstanceState ) {
    super.onCreate( savedInstanceState );

    // fijar de manera permanente la orientación en modo vertical
    setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_PORTRAIT );

    // Ocultar barra de titulo
    requestWindowFeature( Window.FEATURE_NO_TITLE );

    setContentView( R.layout.registro );

    // Obtener el contexto de la aplicacion
    context = getApplicationContext( );

    // Referenciar objetos View
    user = ( EditText ) findViewById( R.id.edusuario );
    pass = ( EditText ) findViewById( R.id.edpassword );
    mail = ( EditText ) findViewById( R.id.edcorreo );

    btn_regis = ( Button ) findViewById( R.id.btn_regis );


    // Obtenemos la lista de preferencias mediante el método getDefaultSharedPreferences() y posteriormente
    // utilizamos los distintos métodos get() para recuperar el valor de cada opción dependiendo de su tipo.
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences( Registro.this );

    IP_Server = prefs.getString( "server", "www.audiobooks.hol.es" );
    URL_connect = "http://" + IP_Server + "/WebService/registro.php";//ruta en donde estan nuestros archivos

    // Asignamos los valores de prefs a los objetos
    user.setText( prefs.getString( "user", "" ) );
    pass.setText( prefs.getString( "pass", "" ) );
    mail.setText( prefs.getString( "mail", "" ) );

    // Ocultamos el teclado virtual
    // Aparece al tener el foco un EditText
    getWindow( ).setSoftInputMode( WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN );

    // Escuchador del boton registrar
    btn_regis.setOnClickListener( new View.OnClickListener( ) {
      public void onClick( View view ) {

        // Verificar conexion
        if ( !Utils.verificaConexion( getApplicationContext( ) ) ) {
          // No hay conexion a Internet
          // Mostrar dialogo
          Utils.showAlertDialog( Registro.this, "Internet", "Comprueba tu conexión", false );

          return;
        }

        //Extraemos datos de los EditText
        String usuario = user.getText( ).toString( ).trim( );
        String passw = pass.getText( ).toString( ).trim( );
        String correo = mail.getText( ).toString( ).trim( );

        //verificamos si no estan en blanco
        if ( checkregdata( usuario, passw, correo ) == true ) {

          // ciframos la contraseña aplicandole una máscara (la misma que aplica el WebService)
          String masc = "|#€7`¬23ads4ook12";
          String cpass = masc + Utils.Cifrado.cifrar( passw, "MD5" );
          cpass = Utils.Cifrado.cifrar( cpass, "SHA-1" );

          //Rellenar datos de registro
          datos = new ArrayList<NameValuePair>( );
          datos.add( new BasicNameValuePair( "usuario", usuario ) );
          datos.add( new BasicNameValuePair( "password", cpass ) );
          datos.add( new BasicNameValuePair( "correo", correo ) );
          datos.add( new BasicNameValuePair( "clave", passw ) );

          // Codificar en base64
          byte[] byteArray;
          try {
            byteArray = datos.get( 0 ).getValue( ).trim( ).getBytes( "UTF-8" );
            datos.set( 0, new BasicNameValuePair( "usuario",

                Base64.encodeToString( byteArray, Base64.DEFAULT ) ) );
            byteArray = datos.get( 1 ).getValue( ).trim( ).getBytes( "UTF-8" );
            datos.set( 1, new BasicNameValuePair( "password",

                Base64.encodeToString( byteArray, Base64.DEFAULT ) ) );
            byteArray = datos.get( 2 ).getValue( ).trim( ).getBytes( "UTF-8" );
            datos.set( 2, new BasicNameValuePair( "correo",

                Base64.encodeToString( byteArray, Base64.DEFAULT ) ) );
            byteArray = datos.get( 3 ).getValue( ).trim( ).getBytes( "UTF-8" );
            datos.set( 3, new BasicNameValuePair( "clave",

                Base64.encodeToString( byteArray, Base64.DEFAULT ) ) );
          } catch ( UnsupportedEncodingException e ) {
            // TODO Auto-generated catch block
            Log.i( "Registro", "Error: " + e );
            //e.printStackTrace();
          }

          // ejecutar tarea asincrona de registro
          new asyncreg( ).execute( );

        } else {
          Log.e( "registerstatus ", "Validar campos" );

        }// Check

      } // onclick

    } ); // listener


  } // onCreate

  // Cierra la actividad
  public void Volver( View v ) {
    finish( );
  }


  //vibra y muestra un Toast
  public void err_reg( Context context, String mensa, boolean vip ) {

    if ( vip ) {
      // Activar vibracion
      Vibrator vibrator = ( Vibrator ) getSystemService( Context.VIBRATOR_SERVICE );
      vibrator.vibrate( 200 );
    }

    // Mostrar mensaje
    Utils.mensaje( context, mensa );

  }


  //validamos los campos
  public boolean checkregdata( String username, String password, String mail ) {

    String mensa = "";
    boolean result = true;

    if ( username.equals( "" ) ) {
      Log.e( "Register ui", "registerdata user" );
      mensa = " Usuario vacio \n";
      result = false;
    }

    if ( password.equals( "" ) || password.length( ) < 4 ) {
      Log.e( "Register ui", "registerdata pass" );
      mensa = mensa + " Contraseña vacia o longitud incorrecta \n";
      result = false;
    }

    if ( mail.equals( "" ) || !mail.contains( "@" ) ) {
      Log.e( "Register ui", "registerdata mail error" );
      mensa = mensa + " Correo vacio o incorrecto \n";
      result = false;
    }

    if ( !result ) {
      // Mostrar mensaje
      err_reg( context, mensa, true );

      return false;
    } else {

      return true;
    }

  }

/* CLASE ASYNCTASK
 *
 * usaremos esta para poder mostrar el dialogo de progreso mientras enviamos y obtenemos los datos
 * podria hacerse lo mismo sin usar esto pero si el tiempo de respuesta es demasiado lo que podria ocurrir
 * si la conexion es lenta o el servidor tarda en responder la aplicacion sera inestable.
 * ademas observariamos el mensaje de que la app no responde.(ANR)
 */

  // las peticiones http deben ejecutarse de una manera asíncrona, el motivo es porque a partir de la
  // versión 4 de android dará un error si estas peticiones se ejecutan en el hilo principal,
  // y esto tiene sentido, pues este tipo de sentencias pueden ralentizar la aplicación, aún así,
  // en las versiones 2.x de android funcionará si lo ejecutamos en el hilo principal.
  // Para evitar errores usaremos un proceso Asynctask para comunicarnos con el servidor.

  class asyncreg extends AsyncTask<String, String, String> {

    //String user, pass, mail;

    protected void onPreExecute( ) {
      //para el progress dialog
      pDialog = new ProgressDialog( Registro.this );
      pDialog.setMessage( "Registrando...." );
      pDialog.setIndeterminate( false );
      pDialog.setCancelable( false );
      pDialog.show( );
    }

    protected String doInBackground( String... params ) {

      //String usuario = params[0];
      //String passw = params[1];
      //String correo = params[2];

      // usamos una clase httpHandler para enviar una petición al servidor web.
      Utils.httpHandler handler = new Utils.httpHandler( );
      String resp = handler.post( URL_connect, datos );

      Log.e( "resp: ", resp + " url: " + URL_connect );

      // valor devuelto para onPostExecute
      return resp;

    }

    /*Una vez terminado doInBackground comprobar lo que halla ocurrido
    * recibe resp
    */
    protected void onPostExecute( String resp ) {
      pDialog.dismiss( );//ocultamos progess dialog.
      Log.e( "onPostExecute=", "" + resp );

      //validamos el resultado obtenido
      if ( resp.trim( ).contains( "0" ) ) {

        // Mostrar mensaje
        Log.e( "registerstatus0 ", "invalido" );
        err_reg( getBaseContext( ), " Usuario ya existe ", true );
      } else if ( resp.trim( ).contains( "1" ) ) {

        // Mostrar mensaje
        Log.e( "registerstatus1 ", "invalido" );
        err_reg( getBaseContext( ), " Correo no válido ", true );
      } else if ( resp.trim( ).contains( "2" ) ) {

        // Mostrar mensaje
        Log.e( "registerstatus2 ", "invalido" );
        err_reg( getBaseContext( ), " Error en BD ", true );
      } else if ( resp.trim( ).contains( "3" ) ) {

        // Mostrar mensaje
        Utils.mensaje( getBaseContext( ),
            " REGISTRO EFECTUADO \n \n" +
                "Se ha enviado un mensaje a su dirección \n " +
                "de correo con los datos de registro. \n \n" +
                "Ya puede disfrutar de audiolibros \n \n" );
        Log.e( "registerstatus3 ", "valido" );

        // Actualizar preferencias
        SharedPreferences prefs =
            PreferenceManager.getDefaultSharedPreferences( context );

        SharedPreferences.Editor editor = prefs.edit( );
        editor.putString( "user", user.getText( ).toString( ).trim( ) );
        editor.putString( "pass", pass.getText( ).toString( ).trim( ) ); // contraseña sin cifrar
        editor.putString( "mail", mail.getText( ).toString( ).trim( ) );
        editor.putBoolean( "REGISTRO", true );
        editor.putBoolean( "reg_man", false );
        editor.commit( );

        // Cerrar actividad
        finish( );

      } else {

        // Mostrar mensaje
        err_reg( getBaseContext( ), " Error de sistema. \n Vuelva a intentarlo. ",
            true );
        Log.e( "registerstatus ", "Error de sistema" );

      }

    } // Onpostexecute

  } // asyncreg

} //activity