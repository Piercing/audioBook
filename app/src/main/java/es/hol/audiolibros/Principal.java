package es.hol.audiolibros;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class Principal extends Activity {

  ImageView image, image1;
  ImageButton imgbtn1;
  ImageButton imgbtn2;
  ImageButton imgbtn3;
  ImageButton imgbtn4;
  Bitmap bitmap;

  private static final String NOM_BDD = "dbfavoritos";
  private DataBase maBaseSQLite;
  private SQLiteDatabase db;


  private SharedPreferences prefs;
  private String server = "", url = "";
  private boolean catalogo = false;
  private int tamano = 0;
  private boolean registrado = false;
  private boolean actualiza = false;


  @Override
  public void onCreate( Bundle savedInstanceState ) {
    super.onCreate( savedInstanceState );

    // fijar de manera permanente la orientación en modo vertical
    setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_PORTRAIT );

    setContentView( R.layout.principal );


    //Obtiene el objeto de preferencias de la aplicacion
    //SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("settings", 0);
    prefs = PreferenceManager.getDefaultSharedPreferences( Principal.this );

    // Obtiene valores almacenados en las preferencias.
    // El segundo parametro indica el valor a devolver si no lo encuentra, en este caso, falso.
    registrado = prefs.getBoolean( "REGISTRO", false );
    catalogo = prefs.getBoolean( "CATALOGO", false );
    tamano = prefs.getInt( "TAMANO", 0 );
    server = prefs.getString( "server", "www.audiobooks.hol.es" );


    Log.i( "Principal", " espace: " + Utils.getInternalAvailableSpace( ) );

    // Crear estructura de directorios
    ContextWrapper context = this;
    File myDir = context.getFilesDir( );

    String carpeta = "Images_Audio";
    File Carpeta = new File( myDir, carpeta );
    Carpeta.mkdirs( );

    carpeta = "Cat_Audio";
    Carpeta = new File( myDir, carpeta );
    Carpeta.mkdirs( );

    // variables de uso general
    String mensa = "";
    boolean errores = false;

    // comprobar registro
    if ( !registrado ) {
      //Codigo que queramos que se ejecute mientras no hay datos de registro

      // crear mensaje y actualizar error
      mensa = "\nNo existen datos de registro.";
      errores = true;

    }

    // comprobar conexion
    if ( !Utils.verificaConexion( this ) ) {

      // crear mensaje y actualizar error
      mensa = mensa + "\nNo hay conexión.";
      errores = true;
    }


    // mostrar aviso por errores
    if ( errores ) {

      mensa = mensa + "\n\nAlgunas opciones no serán operativas.";
      // Mostrar dialogo
      Utils.showAlertDialog( Principal.this, "AVISO", mensa, false );

    }

    // Referenciar objetos
    imgbtn1 = ( ImageButton ) findViewById( R.id.imgbtn1 );
    imgbtn2 = ( ImageButton ) findViewById( R.id.imgbtn2 );
    imgbtn3 = ( ImageButton ) findViewById( R.id.imgbtn3 );
    imgbtn4 = ( ImageButton ) findViewById( R.id.imgbtn4 );


  } // onCreate


 /* Métodos que controlan la pulsación de los botones
 * favoritos()
 * catalogo() incluye actualizar() y cargar()
 * registro()
 * config()
 */

// ***************************** F A V O R I T O S *****************************************

  public void favoritos( View v ) {
    // obtener si hay favoritos en la BD
    DataBase db = new DataBase( this );
    int datos = db.getLibrosCount( );
    db.close( );
    // Si hay los mostramos en una nueva activity o mostrar mensaje
    if ( datos > 0 ) {
      Intent intent = new Intent( Principal.this, Favoritos.class );
      startActivity( intent );
    } else {
      Utils.mensaje( getApplicationContext( ), "\nNo hay favoritos.\n" );
    }

  }

  // ****************************** C A T A L O G O *******************************************

  public void catalogo( View v ) {

    boolean errores = false;

    // recargar preferencias y obtener tamaño de catalogo desde preferencias
    prefs = PreferenceManager.getDefaultSharedPreferences( Principal.this );
    registrado = prefs.getBoolean( "REGISTRO", false );
    catalogo = prefs.getBoolean( "CATALOGO", false );
    tamano = prefs.getInt( "TAMANO", 0 );

    Log.i( "CATALOGO", catalogo + " tam: " + tamano );

    // Si existe un catalogo descargado verificar actualizaciones
    if ( catalogo == true ) {

      // Si hay conexion a Internet
      if ( Utils.verificaConexion( this ) ) {

        // Construir url
        url = "http://" + server + "/WebService/catalogo.json";

        // comprobar si el tamaño es diferente, indicar que existe una actualizacion
        int result[] = Utils.CompruebaUrl( getBaseContext( ), url );

        if ( result[ 0 ] == 200 ) {


          if ( result[ 1 ] != tamano ) {

            Log.i( "CATALOGO", "code: " + result[ 0 ] + " tam: " + result[ 1 ] );

            AlertDialog.Builder ad = new AlertDialog.Builder( this );
            ad.setTitle( "CATALOGO\nActualización disponible." );
            // asignar icono
            boolean status = true;
            ad.setIcon( ( status ) ? R.drawable.success : R.drawable.fail );
            ad.setMessage( "¿Descargar Actualización?" );
            ad.setCancelable( false );
            ad.setPositiveButton( "Confirmar", new
                DialogInterface.OnClickListener( ) {
                  public void onClick( DialogInterface dialog, int arg1 ) {

                    actualizar( );
                  }
                } );

            ad.setNegativeButton( "Cancelar", new
                DialogInterface.OnClickListener( ) {
                  public void onClick( DialogInterface dialogo1, int id ) {
                    cargar( );
                  }
                } );

            ad.show( );


          } else {
            //errores = true;
            Utils.mensaje( getApplicationContext( ), "No hay actualizaciones." );
            cargar( );
          }

        } else {
          errores = true;
        }

      } else {
        errores = true;
      }

    } else {
      // Si no existe, descargar catalogo
      cargar( );
    }

    // Si hubo errores al comprobar actualizaciones
    // cargar catalogo existente
    if ( errores ) {

      Utils.mensaje( getApplicationContext( ), "Imposible comprobar actualizaciones." );
      cargar( );
    }

  }

  public void actualizar( ) {

    // forzar descargar catalogo
    catalogo = false;

    actualiza = true;

 /*
 // activar editor de preferencias
C.F.G.S. DAM I.E.S. “Al-Ándalus”
Página 148 de 249
 SharedPreferences.Editor prefEditor = prefs.edit();

 // Actualizar valor
 prefEditor.putBoolean("CATALOGO", false);
 prefEditor.commit();
 */

    cargar( );

  }

  public void cargar( ) {

    // variables de uso general
    String mensa = "";
    boolean errores = false;


    // Si catalogo = true es que existe un catalogo descargado en el movil
    // por lo que podemos lanzar la actividad
    if ( catalogo ) {

      // Ejecutar catalogo
      Intent intent = new Intent( Principal.this, Catalogo.class );
      startActivity( intent );

    }
    // Si no hay catalogo verificar conexión y credenciales
    // para obtenerlo y lanzar actividad
    else {

      // Si hay datos de registro verificar con el WebService
      if ( registrado ) {

        // contiene datos de registro
        ArrayList<NameValuePair> datos;

        // Obtener datos desde preferencias
        String usuario = prefs.getString( "user", "" );
        String clave = prefs.getString( "pass", "" );


        // ruta de la función del Web Service
        String url = "http://" + server + "/WebService/valida_User.php";

        //Rellenar datos de registro
        //datos = new String[] {usuario.trim(), cpass};
        datos = new ArrayList<NameValuePair>( );
        datos.add( new BasicNameValuePair( "usuario", usuario ) );
        datos.add( new BasicNameValuePair( "password", clave ) );
        datos.add( new BasicNameValuePair( "url", url ) );


        // Si hay conexion a Internet
        if ( Utils.verificaConexion( this ) ) {

          // ejecutar tarea asincrona de validar usuario
          verificaUsuario validar = new verificaUsuario( this, datos );
          validar.execute( );

          // Si no hay conexion mostrar aviso
        } else {

          // crear mensaje y actualizar errores
          mensa = mensa + "\nComprueba tu conexión.";
          errores = true;
        }

        // Si no hay registro mostrar aviso
      } else {

        // crear mensaje y actualizar errores
        mensa = mensa + "\nNo hay datos de registro.\nDebe registrarse.";
        errores = true;
      }

    }

    // mostrar aviso por errores
    if ( errores ) {

      mensa = mensa + "\n";
      // Mostrar dialogo
      Utils.showAlertDialog( Principal.this, "AVISO", mensa, false );

    } else {


    }
  }



 /*
 * Autentifica al usuario mediante funcion WebService
 *
 * Parámetro: context, contexto de la aplicación
 * Parámetro: datos, ArrayList con los datos
 * Retorno: resp, string con el valor devuelto por el WebService
 */

  // En nuestra tarea no necesitamos entrada,
  // no usaremos información de progreso y devolveremos un String.
  class verificaUsuario extends AsyncTask<Void, Void, String> {

    ProgressDialog Dialog;
    String cpass;
    Context context;
    ArrayList<NameValuePair> datos;

    public verificaUsuario( Context context, ArrayList<NameValuePair> datos ) {
      this.context = context;
      this.datos = datos;
    }

    protected void onPreExecute( ) {

      //para el progress dialog
      Log.i( "VerificaUsuario", "onPreExecute " + context );
      Log.i( "VerificaUsuario", "onPre clave: " + datos.get( 1 ).getValue( ).trim( ) );

      //Dialog = ProgressDialog.show(context,"VALIDACION", "Comprobando datos deusuario");

      Dialog = new ProgressDialog( Principal.this );
      Dialog.setMessage( "Validando...." );
      Dialog.setIndeterminate( false );
      Dialog.setCancelable( false );
      Dialog.show( );

      // ciframos la contraseña aplicandole una máscara (la misma que aplica elWebService)
      String masc = "|#€7`¬23ads4ook12";
      cpass = masc + Utils.Cifrado.cifrar( datos.get( 1 ).getValue( ).trim( ), "MD5" );
      cpass = Utils.Cifrado.cifrar( cpass, "SHA-1" );
      // datos.remove(1);
      datos.set( 1, new BasicNameValuePair( "password", cpass ) );

      Log.i( "VerificaUsuario", "onPre cclave: " + datos.get( 1 ).getValue( ).trim( ) );

      // Codificar en base64
      byte[] byteArray;
      try {
        byteArray = datos.get( 0 ).getValue( ).trim( ).getBytes( "UTF-8" );
        datos.set( 0, new BasicNameValuePair( "usuario",
            Base64.encodeToString( byteArray, Base64.DEFAULT ) ) );
        byteArray = datos.get( 1 ).getValue( ).trim( ).getBytes( "UTF-8" );
        datos.set( 1, new BasicNameValuePair( "password",
            Base64.encodeToString( byteArray, Base64.DEFAULT ) ) );
      } catch ( UnsupportedEncodingException e ) {
        // TODO Auto-generated catch block
        Log.i( "VerificaUsuario", "Error: " + e );
        //e.printStackTrace();
      }


      Log.i( "VerificaUsuario", "onPre b64clave: " + datos.get( 1 ).getValue( ).trim( ) );


    }


    protected String doInBackground( Void... params ) {

      String url = datos.get( 2 ).getValue( ).trim( );

      // elimimar url de datos
      datos.set( 2, new BasicNameValuePair( "url", "" ) );

      // usamos una clase httpHandler para enviar una petición al servidor web.
      Utils.httpHandler handler = new Utils.httpHandler( );
      String resp = handler.post( url, datos );

      Log.i( "VericaUsuario", "resp: " + resp + "url: " + url );

      // valor devuelto para onPostExecute
      return resp;

    }

    /*Una vez terminado doInBackground comprobar lo que halla ocurrido
    * recibe resp
    */
    protected void onPostExecute( String resp ) {

      Log.i( "onPostExecute=", "resp: " + resp );

      //validamos el resultado obtenido
      if ( resp.trim( ).contains( "0" ) ) {

        // Mostrar mensaje
        Log.i( "verifica0 ", "invalido" );
        Utils.mensaje( context, " Usuario o contaseña incorrectos " );

      } else if ( resp.trim( ).contains( "1" ) ) {

        // Mostrar mensaje
        Log.i( "verifica1 ", "valido" );
        //Utils.mensaje(context, " Usuario validado ");

        if ( actualiza ) {
          // Cambiar preferencia para forzar descargar catalogo
// en la actividad catalogo

          // activar editor de preferencias
          SharedPreferences.Editor prefEditor = prefs.edit( );

          // Actualizar valor
          prefEditor.putBoolean( "CATALOGO", false );
          prefEditor.commit( );
        }

        // Ejecutar catalogo
        Intent intent = new Intent( Principal.this, Catalogo.class );
        startActivity( intent );

      } else {

        // Mostrar mensaje
        Log.e( "registerstatus ", "Error de sistema" );
        Utils.mensaje( context, " Error de sistema. \n Vuelva a intentarlo. " );
      }

      Dialog.dismiss( );//ocultamos progess dialog.

    } // Onpostexecute


  } // verificaUsuario


  // **************************** R E G I S T R O ******************************************

  public void registro( View v ) {
    if ( Utils.verificaConexion( this ) ) {

      Intent intent = new Intent( Principal.this, Registro.class );
      startActivity( intent );

    } else {
      // No hay conexion a Internet
      // Mostrar dialogo
      Utils.showAlertDialog( Principal.this, "AVISO",
          "Comprueba tu conexión.", false );
    }

  }

  // *************************** C O N F I G ***********************************************

  public void config( View v ) {
    Intent intent = new Intent( Principal.this, Configuracion.class );
    startActivity( intent );
  }


  // ******************************* M E N U ***********************************************


  // MENU
  // Tendremos que implementar el evento onCreateOptionsMenu() de la actividad que queremos que lo muestre
  // En este evento deberemos “inflar” el menú. Primero obtendremos una referencia al inflater
  // mediante el método getMenuInflater() y posteriormente generaremos la estructura del menú llamando a su
  // método infate() pasándole como parámetro el ID del menu definido en XML, que mi nuestro caso seráR.menu.activity_main.
  // Por último devolveremos el valor true para confirmar que debe mostrarse el menú.

  @Override
  public boolean onCreateOptionsMenu( Menu menu ) {
    super.onCreateOptionsMenu( menu );
    MenuInflater inflater = getMenuInflater( );
    inflater.inflate( R.menu.menu, menu );
    return true; /** true -> el menú ya está visible */
  }

  // Construido el menú, la implementación de cada una de las opciones se incluirá en el evento onOptionsItemSelected()
// de la actividad que mostrará el menú. Este evento recibe como parámetro el item de menú que ha sido pulsado por elusuario,
// cuyo ID podemos recuperar con el método getItemId(). Según este ID podremos saber qué opción ha sido pulsada yejecutar
// unas acciones u otras.
  @Override
  public boolean onOptionsItemSelected( MenuItem item ) {
    Intent intent;

    switch ( item.getItemId( ) ) {

      case R.id.favoritos:

        favoritos( imgbtn1 );

        break;

      case R.id.config:
        intent = new Intent( Principal.this, Configuracion.class );
        startActivity( intent );
        break;

      case R.id.registro:
        intent = new Intent( Principal.this, Registro.class );
        startActivity( intent );
        break;

      case R.id.catalogo:

        catalogo( imgbtn2 );
        break;
      case R.id.acercaDe:
        // lanzar AcercaDe
        intent = new Intent( Principal.this, AcercaDe.class );
        startActivity( intent );

        break;

      case R.id.salir:
        finish( );

        break;
    }
    return true; /** true -> consumimos el item, no se propaga*/
  }


}