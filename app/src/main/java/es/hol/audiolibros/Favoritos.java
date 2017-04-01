package es.hol.audiolibros;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.TextView;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class Favoritos extends Activity {

  // Definir constantes
  public static final int KEY_ROWID = 0;
  public static final int KEY_TITULO = 1;
  public static final int KEY_AUTOR = 2;
  public static final int KEY_TEMA = 3;
  public static final int KEY_DESCRIP = 4;
  public static final int KEY_MP3URL = 5;
  public static final int KEY_POSICION = 6;
  public static final int KEY_IMAGEN = 7;

  // Definir variables de uso general
  public Cursor cur;

  private int _ID; // id del registro del item pulsado (puede no coincidir)

  GridView gridview;
  ImageAdapter adapter;
  private int result[];
  private String titulo, mp3url;
  private int pos, id;
  byte[] imageInByte = null;

  /**
   * Called when the activity is first created.
   */
  @Override
  public void onCreate( Bundle savedInstanceState ) {
    super.onCreate( savedInstanceState );

    // fijar de manera permanente la orientación en modo vertical
    setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_PORTRAIT );

    // Obtener el action Bar para el botón volver.
    ActionBar actionBar = getActionBar( );
    actionBar.setDisplayHomeAsUpEnabled( true );

    setContentView( R.layout.favoritos );

    // crear el gridview a partir del elemento del xml gridview
    GridView gridview = ( GridView ) findViewById( R.id.gridview );

    //Asociamos el menú contextual al control para el gridview
    this.registerForContextMenu( gridview );

// con setAdapter se llena el gridview con datos. en este caso un nuevo objeto
// de la clase ImageAdapter, que está definida en otro archivo.
    adapter = new ImageAdapter( this );
    gridview.setAdapter( adapter );

    // para que detecte la pulsación se le añade un listener de itemClick
    // que recibe un OniTemClickListener creado con new
    gridview.setOnItemClickListener( new OnItemClickListener( ) {
      // dentro de este listener difinimos la función que se ejecuta al hacer click en un item.
      // el metodo pertenece a AdapterView, es decir, es AdapterView.OnItemClickListener
      // dentro de este, tenemos el método onItemClick, que es el que se invoca al pulsar
      // un item del AdapterView, esa función recibe el objeto padre, que es un adapterview en el
      // que se ha pulsado, una vista, que es el elemento sobre el que se ha pulsado,
      // una posicion, que es la posicion del elemento dentro del adapter,
      // y un id, que es el id de fila del item que se ha pulsado
      public void onItemClick( AdapterView<?> parent, View v, int position, long id ) {

        // Referenciar y obtener el contenido del textview que almacena el id del registro de la BD
        // Es necesario dado que la posicion del item no puede coincider con el id del registro
        _ID = Integer.parseInt( ( ( TextView ) v.findViewById( R.id.gv_id ) ).getText( ).toString( ).trim( ) );

        Log.i( "Favoritos item/id", position + " " + String.valueOf( _ID ) );

        // forzamos mostrar el menu contextual
        openContextMenu( v );

        // mostrar una nueva actividad con los datos ampliados
        //Intent intent=new Intent(Favoritos.this,Libroviewer.class);
        //intent.putExtra("ID", _ID);
        //startActivity(intent);

      }
    } );

  } // Oncreate

  // MENU CONTEXTUAL

  @Override
  public void onCreateContextMenu( ContextMenu menu, View v, ContextMenuInfo menuInfo ) {
    super.onCreateContextMenu( menu, v, menuInfo );

    // obtendremos una referencia al inflater mediante el método getMenuInflater()
    MenuInflater inflater = getMenuInflater( );

    // convertimos el parámetro menuInfo a un objeto de tipo AdapterContextMenuInfo
    // menuInfo. Este parámetro contiene información adicional del control que se ha pulsado
    // para mostrar el menú contextual, y en el caso particular del control ListView contiene
    // la posición del elemento concreto de la lista que se ha pulsado.
    AdapterView.AdapterContextMenuInfo info = ( AdapterView.AdapterContextMenuInfo ) menuInfo;

    // Obtener datos necesarios de la BD
    cur = obtieneCursor( _ID );
    titulo = cur.getString( KEY_TITULO ).trim( ) + " - " + cur.getString( KEY_AUTOR ).trim( );
    cur.close( );

    // personalizar el título del menú contextual mediante setHeaderTitle()
    menu.setHeaderTitle( "OPCIONES\n" + titulo );

    // generamos la estructura del menú llamando a su método infate() pasándole como parámetro
    // el ID del menu definido en XML
    inflater.inflate( R.menu.favoritos_ctx, menu );

    // Obtener id del elemento pulsado
    // Referenciar y obtener el contenido del textview que almacena el id del registro de la BD
    int _ID2 = Integer.parseInt( ( ( TextView ) v.findViewById( R.id.gv_id ) ).getText( ).toString( ).trim( ) );
    Log.d( "Favoritos ctxmenu", String.valueOf( _ID2 ) );

    Log.i( "Favoritos pos", String.valueOf( info.position ) );

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


  @Override
  public boolean onContextItemSelected( MenuItem item ) {

    // utilizamos la información del objeto AdapterContextMenuInfo para saber qué elemento de la lista se ha
    // pulsado, lo obtenemos mediante una llamada al método getMenuInfo() de la opción de menú (MenuItem)
    // recibida como parámetro.
    /// AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();


    switch ( item.getItemId( ) ) {

      case R.id.CtxGvOpc0: // Detalles

        // mostrar una nueva actividad con los datos ampliados
        Intent intent = new Intent( Favoritos.this, Libroviewer.class );
        intent.putExtra( "ID", _ID );
        startActivity( intent );

        return true;

      case R.id.CtxGvOpc1: // Eliminar

        // Eliminar el elemento seleccionado de la BD
        DataBase db = new DataBase( this );
        db.borrarLibro( _ID );
        db.close( );

        // Notificar al adaptador que hay cambios
        adapter.notifyDataSetChanged( );

        // Mensaje
        Utils.mensaje( getApplicationContext( ), "\nLibro eliminado de favoritos.\n" );

        return true;

      case R.id.CtxGvOpc2: // Reproducir

        //Obtiene el objeto de preferencias de la aplicacion
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences( Favoritos.this );
        // Obtener datos desde preferencias
        String usuario = prefs.getString( "user", "" );
        String clave = prefs.getString( "pass", "" );
        String server = prefs.getString( "server", "www.audiobooks.hol.es" );
        boolean registrado = prefs.getBoolean( "REGISTRO", false );

        // Comprobar conexion
        if ( !Utils.verificaConexion( this ) ) {
          // No hay conexion a Internet
          // Mostrar dialogo
          Utils.showAlertDialog( Favoritos.this, "Internet", "Comprueba tu conexión", false );

          return true;
        }

        // Obtener datos necesarios de la BD
        cur = obtieneCursor( _ID );
        mp3url = cur.getString( KEY_MP3URL );
        titulo = cur.getString( KEY_TITULO ).trim( ) + " - " + cur.getString( KEY_AUTOR ).trim( );
        pos = cur.getInt( KEY_POSICION );
        id = cur.getInt( KEY_ROWID );
        imageInByte = cur.getBlob( KEY_IMAGEN );
        cur.close( );

        Log.i( "Favoritos play", mp3url );
        Log.i( "Favoritos play", titulo );

        // algunas url contienen espacios y darían error
        mp3url = mp3url.replaceAll( "\\s", "%20" );

        // Comprobar que existe la url
        result = Utils.CompruebaUrl( getBaseContext( ), mp3url );

        if ( result[ 0 ] == 200 ) {

          // Si hay datos de registro verificar con el WebService
          if ( registrado ) {

            // contiene datos de registro
            ArrayList<NameValuePair> datos;

            // ruta de la función del Web Service
            String url = "http://" + server + "/WebService/valida_User.php";

            //Rellenar datos de registro
            datos = new ArrayList<NameValuePair>( );
            datos.add( new BasicNameValuePair( "usuario", usuario ) );
            datos.add( new BasicNameValuePair( "password", clave ) );
            datos.add( new BasicNameValuePair( "url", url ) );

            // ejecutar tarea asincrona de validar usuario
            verificaUsuario validar = new verificaUsuario( this, datos );
            validar.execute( );


            // Si no hay registro mostrar aviso
          } else {

            // Mostrar dialogo
            Utils.showAlertDialog( Favoritos.this, "AVISO",
                "\nNo hay datos de registro.\nDebe registrarse.", false );
          }

        } else {

          Utils.mensaje( getApplicationContext( ), "Code: " + result[ 0 ] +
              "\nNo se encontró el audiolibro o " + "\nno hay conexión." );

        }

        return true;

      case R.id.CtxGvOpc3: // Salir

        return true;

      default:
        return super.onContextItemSelected( item );
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

      //Dialog = ProgressDialog.show(context,"VALIDACION", "Comprobando datos de usuario");

      Dialog = new ProgressDialog( Favoritos.this );
      Dialog.setMessage( "Validando...." );
      Dialog.setIndeterminate( false );
      Dialog.setCancelable( false );
      Dialog.show( );

      // ciframos la contraseña aplicandole una máscara (la misma que aplica el WebService)
      String masc = "|#€7`¬23ads4ook12";
      cpass = masc + Utils.Cifrado.cifrar( datos.get( 1 ).getValue( ).trim( ), "MD5" );
      cpass = Utils.Cifrado.cifrar( cpass, "SHA-1" );
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

        // Crear un Intent, pasar parametros y lanzar una nueva Activity con el reproductor

        Intent i = new Intent( getApplicationContext( ), MusicPlayerActivity.class );

        // llamamos al método putExtra de la clase Intent. Tiene dos parámetros de tipo String,
        // clave-valor en el primero indicamos el nombre del dato y en el segundo el valor del dato:

        i.putExtra( "titulo_autor", titulo );
        i.putExtra( "url", mp3url );
        i.putExtra( "pos", String.valueOf( pos ) );
        i.putExtra( "id", String.valueOf( id ) );
        i.putExtra( "img", Base64.encodeToString( imageInByte, Base64.DEFAULT ) );
        i.putExtra( "desde", "favoritos" );

        startActivity( i );

      } else {
        // Mostrar mensaje
        Log.e( "registerstatus ", "Error de sistema" );
        Utils.mensaje( context, " Error de sistema. \n Vuelva a intentarlo. " );
      }

      Dialog.dismiss( );//ocultamos progess dialog.

    } // Onpostexecute
  } // verificaUsuario


  /* Obtener un cursor con todos los favoritos
  *
  */
  private Cursor obtieneCursor( int id ) {

    DataBase db = new DataBase( this );

    cur = db.getLibro( id );
    cur.moveToFirst( );

    db.close( );

    return cur;
  }

  @Override
  public void onDestroy( ) {
    super.onDestroy( );

    //cur.close();
    // liberar cursor
  }

}