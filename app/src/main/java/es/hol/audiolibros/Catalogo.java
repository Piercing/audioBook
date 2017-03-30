package es.hol.audiolibros;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;

public class Catalogo extends ListActivity {

  // Definir constantes
  private static final String TAG_ID = "id";
  private static final String TAG_AUTOR = "autor";
  private static final String TAG_TITULO = "titulo";
  private static final String TAG_TEMA = "tema";
  private static final String TAG_URL = "mp3";

  public static final String KEY_ROWID = "_id";
  public static final String KEY_TITULO = "titulo";
  public static final String KEY_AUTOR = "autor";
  public static final String KEY_TEMA = "tema";
  public static final String KEY_DESCRIP = "descrip";
  public static final String KEY_MP3URL = "mp3url";
  public static final String KEY_POSICION = "posicion";
  public static final String KEY_IMAGEN = "imagen";

// Variables de uso general

  // Un mapa es una estructura que nos permite almacenar pares clave/valor.
  // De tal manera que para una clave solamente tenemos un valor.
  ArrayList<HashMap<String, String>> jsonlist = new ArrayList<HashMap<String, String>>( );
  ArrayList<HashMap<String, String>> array_sort = new ArrayList<HashMap<String, String>>( );

  ListAdapter adapter;
  ListView lv;

  // Objetos para busqueda
  EditText inputSearch;
  ImageButton buscar;

  // Nos indica si se ha utilizado algun filtro
  boolean filtro = false;

  private String server, usuario, clave;
  private String url, titulo, mp3url;

  private int _ID = 0;
  private boolean catalogo = false;
  private SharedPreferences prefs;
  private int result[];

  boolean registrado = false;
  boolean favoritos = false;
  boolean reproducir = false;

  // contiene datos de registro
  ArrayList<NameValuePair> datos;


  @Override
  protected void onCreate( Bundle savedInstanceState ) {
    super.onCreate( savedInstanceState );

    // Fijar orientacion vertical
    setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_PORTRAIT );

    setContentView( R.layout.catalogo );

    // Obtenemos las referencias a los controles
    inputSearch = ( EditText ) findViewById( R.id.inputSearch );
    lv = ( ListView ) findViewById( android.R.id.list );
    buscar = ( ImageButton ) findViewById( R.id.ibtn_buscar );

    // Ocultamos el teclado virtual
    // Aparece al tener el foco un EditText
    getWindow( ).setSoftInputMode( WindowManager.LayoutParams.
        SOFT_INPUT_STATE_ALWAYS_HIDDEN );

    // Asociamos el menú contextual al control para el listview y el boton de busqueda
    registerForContextMenu( this.getListView( ) );
    registerForContextMenu( buscar );

    // Obtenemos la lista de preferencias mediante el método getDefaultSharedPreferences() y
    // posteriormente utilizamos los distintos métodos get()
    // para recuperar el valor de cada opción dependiendo de su tipo.
    prefs = PreferenceManager.getDefaultSharedPreferences( Catalogo.this );

    server = prefs.getString( "server", "www.audiobooks.hol.es" );
    usuario = prefs.getString( "user", "" );
    clave = prefs.getString( "pass", "" );
    registrado = prefs.getBoolean( "REGISTRO", false );
    catalogo = prefs.getBoolean( "CATALOGO", false );
    boolean autologin = prefs.getBoolean( "autologin", true );

    // Construir url
    url = "http://" + server + "/WebService/catalogo.json";

    // Lanzar tarea asincrona de carga del catalogo
    new CargaCatalogo( Catalogo.this ).execute( );

    // Escuchador para pulsacion de un elemento de la lista
    lv.setOnItemClickListener( new OnItemClickListener( ) {
      public void onItemClick( AdapterView<?> parent, View view, int position, long id ) {

        // forzamos mostrar el menu contextual
        openContextMenu( view );

      }
    } );


  } // onCreate


  // MENU CONTEXTUAL

  @Override
  public void onCreateContextMenu( ContextMenu menu, View v, ContextMenuInfo menuInfo ) {
    super.onCreateContextMenu( menu, v, menuInfo );

    // obtendremos una referencia al inflater mediante el método getMenuInflater()
    MenuInflater inflater = getMenuInflater( );


    // para que se construya un menú distinto dependiendo del control asociado.
    // Esto lo haremos obteniendo el ID del control al que se va a asociar el menú contextual,
    // que se recibe en forma de parámetro (View v) en el evento onCreateContextMenu().
    // Utilizaremos para ello una llamada al método getId() de dicho parámetro

    if ( v.getId( ) == android.R.id.list ) // Para el listview
    {
      // convertimos el parámetro menuInfo a un objeto de tipo AdapterContextMenuInfo
      // menuInfo. Este parámetro contiene información adicional del control que se ha pulsado
      // para mostrar el menú contextual, y en el caso particular del control ListView contiene
      // la posición del elemento concreto de la lista que se ha pulsado.
      AdapterView.AdapterContextMenuInfo info = ( AdapterView.AdapterContextMenuInfo ) menuInfo;

      // personalizar el título del menú contextual mediante setHeaderTitle()
      // para que muestre el texto del elemento seleccionado en la lista
      // Obtener el id del item pulsado para usarlo posteriormente

      if ( filtro ) {
        titulo = array_sort.get( info.position ).get( "titulo" ) + " - " + array_sort.get( info.position ).get( "autor" );
        mp3url = array_sort.get( info.position ).get( "mp3" );
        _ID = Integer.parseInt( array_sort.get( info.position ).get( "id" ).trim( ) );

      } else {
        titulo = jsonlist.get( info.position ).get( "titulo" ) + " - " + jsonlist.get( info.position ).get( "autor" );
        mp3url = jsonlist.get( info.position ).get( "mp3" );
        _ID = Integer.parseInt( jsonlist.get( info.position ).get( "id" ).trim( ) );

      }

      Log.i( "Catalogo", "id: " + String.valueOf( _ID ) );

      menu.setHeaderTitle( "OPCIONES\n" + titulo );

      // generamos la estructura del menú llamando a su método infate() pasándole como parámetro
      // el ID del menu definido en XML
      inflater.inflate( R.menu.menu_ctx, menu );
    }

    if ( v.getId( ) == R.id.ibtn_buscar ) // para el imagebutton
    {

      // Si existen datos se activa el menu contextual para busquedas
      int tam = jsonlist.size( );
      if ( tam > 0 ) {
        menu.setHeaderTitle( "Filtrar por: " );
        inflater.inflate( R.menu.filtrar_ctx, menu );
      } else {

        Utils.mensaje( getApplicationContext( ), " No hay datos " );

        finish( );
      }

    }

  }


  @Override
  public boolean onContextItemSelected( MenuItem item ) {

    // utilizamos la información del objeto AdapterContextMenuInfo para saber qué elemento
    // de la lista se ha pulsado, lo obtenemos mediante una llamada al método getMenuInfo()
    // de la opción de menú (MenuItem) recibida como parámetro.
    AdapterContextMenuInfo info = ( AdapterContextMenuInfo ) item.getMenuInfo( );

    // obtener datos para las opciones agregar a favoritos o reproducir.
    if ( ( item.getItemId( ) == R.id.CtxLstOpc1 ) || ( item.getItemId( ) == R.id.CtxLstOpc2 ) ) {
      // Si hay datos de registro verificar con el WebService
      if ( registrado ) {

        // ruta de la función del Web Service
        String url = "http://" + server + "/WebService/valida_User.php";

        //Rellenar datos de registro
        datos = new ArrayList<>( );
        datos.add( new BasicNameValuePair( "usuario", usuario ) );
        datos.add( new BasicNameValuePair( "password", clave ) );
        datos.add( new BasicNameValuePair( "url", url ) );
        datos.add( new BasicNameValuePair( "pos", String.valueOf( info.position ) ) );

        // Si no hay registro mostrar aviso
      } else {

        // Mostrar dialogo
        Utils.showAlertDialog( Catalogo.this, "AVISO",
            "\nNo hay datos de registro.\nDebe registrarse.", false );
      }
    }

    switch ( item.getItemId( ) ) {

      case R.id.CtxLstOpc1: // Agregar a favoritos

        // Utils.mensaje(getApplicationContext(),"Pendiente de codificar.\n\nHas pulsado "+
        // lv.getAdapter().getItem(info.position));

        favoritos = true;
        reproducir = false;

        // ejecutar tarea asincrona de validar usuario
        verificaUsuario validar = new verificaUsuario( this, datos );
        validar.execute( );

        //agregar_favorito(_ID, info.position);

        return true;

      case R.id.CtxLstOpc2: // Reproducir

        // Comprobar conexion
        if ( !Utils.verificaConexion( getApplicationContext( ) ) ) {
          // No hay conexion a Internet
          // Mostrar dialogo
          Utils.showAlertDialog( Catalogo.this, "Internet",
              "Comprueba tu conexión", false );

          return true;
        }

        // algunas url contienen espacios y darían error
        mp3url = mp3url.replaceAll( "\\s", "%20" );

        // comprobar url
        result = Utils.CompruebaUrl( getApplicationContext( ), mp3url );

        if ( result[ 0 ] == 200 || result[0] != 200 ) {

          reproducir = true;
          favoritos = false;

          // ejecutar tarea asincrona de validar usuario
          verificaUsuario validar2 = new verificaUsuario( this, datos );
          validar2.execute( );

        } else {
          Utils.mensaje( getApplicationContext( ), "Code: " + result[ 0 ] +
              "\nNo se encontró el audiolibro o\nno hay conexión." );
        }

        return true;

      case R.id.CtxLstOpc3: // Salir

        return true;

      case R.id.Ctx_autor:

        // Obtener los datos filtrados por autor
        filtrado( "autor" );

        return true;

      case R.id.Ctx_titulo:

        // Obtener los datos filtrados por titulo
        filtrado( "titulo" );

        return true;

      case R.id.Ctx_tema:

        // Obtener los datos filtrados por tema
        filtrado( "tema" );

        return true;

      case R.id.Ctx_qfiltro:

        // Mostrar listview
        ListAdapter adapter = new SimpleAdapter( this, jsonlist, R.layout.list_item,
            new String[]{ TAG_AUTOR, TAG_TITULO, TAG_TEMA, TAG_URL },
            new int[]{ R.id.autor, R.id.titulo, R.id.tema, R.id.url } );
        setListAdapter( adapter );
        lv = getListView( );

        filtro = false;

        inputSearch.setText( "" );

        return true;

      case R.id.Ctx_salir:

        inputSearch.setText( "" );

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
  private class verificaUsuario extends AsyncTask<Void, Void, String> {

    ProgressDialog Dialog;
    String cpass;

    Context context;
    ArrayList<NameValuePair> datos;

    verificaUsuario( Context context, ArrayList<NameValuePair> datos ) {
      this.context = context;
      this.datos = datos;
    }

    protected void onPreExecute( ) {

      //para el progress dialog
      Log.i( "VerificaUsuario", "onPreExecute " + context );
      Log.i( "VerificaUsuario", "onPre clave: " + datos.get( 1 ).getValue( ).trim( ) );

      Dialog = ProgressDialog.show( context, "", "Validando..." );

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

        if ( favoritos ) {
          int pos = Integer.parseInt( datos.get( 3 ).getValue( ).trim( ) );
          agregar_favorito( _ID, pos );
        } else if ( reproducir ) {
          // Crear un Intent, pasar parametros y lanzar una nueva Activity con el reproductor
          Intent i = new Intent( getApplicationContext( ), MusicPlayerActivity.class );

          // llamamos al método putExtra de la clase Intent. Tiene dos parámetros
          // de tipo String, clave-valor en el primero indicamos el nombre del dato
          // y en el segundo el valor del dato:
          i.putExtra( "titulo_autor", titulo );
          i.putExtra( "url", mp3url );
          i.putExtra( "desde", "catalogo" );

          startActivity( i );
        }

      } else {
        // Mostrar mensaje
        Log.e( "registerstatus ", "Error de sistema" );
        Utils.mensaje( context, " Error de sistema. \n Vuelva a intentarlo. " );
      }

      Dialog.dismiss( );//ocultamos progess dialog.

    } // Onpostexecute


  } // verificaUsuario


  // Añadir a la BD de favoritos los datos del libro seleccionado del catalogo
  public void agregar_favorito( int id, int pos ) {

    // 1º Descargar imagen segun id y guardarla
    // 2º Comprimir imagen para la BD
    // 3º Asignar resto de datos

    byte[] imageInByte = null;
    FileOutputStream fos;
    // Comprobar que existe el recurso
    String url = "http://" + server.trim( ) + "/libros/fotolibro/" + String.valueOf( id ).trim( ) + "-gra.jpg";
    result = Utils.CompruebaUrl( getBaseContext( ), url );

    Log.i( "Catalogo", "url: " + url );
    Log.i( "Catalogo", "code: " + result[ 0 ] );

    if ( result[ 0 ] != 0 ) {
      // No existe imagen, descargar generica
      url = "http://" + server.trim( ) + "/libros/fotolibro/" + "0-gra.jpg";

      // forzar result
      result[ 0 ] = 0;

      Log.i( "Catalogo1", "url: " + url );
      Log.i( "Catalogo1", "code: " + result[ 0 ] ); //String.valueOf(result[0]));
    }

    // Si es correcto
    if ( result[ 0 ] == 0 ) {

      String Titulo, Autor, Tema, Descrip, Mp3url;
      Bitmap Imagen = Utils.DownloadImage.downloadImage( url );

      // Extraer el nombre de la imagen
      File file = new File( url.trim( ) );
      String name = file.getName( );

      // Definimos una array de Bytes
      ByteArrayOutputStream bytes = new ByteArrayOutputStream( );

      // Elegimos el formato de compresion y calidad
      Imagen.compress( Bitmap.CompressFormat.JPEG, 100, bytes );

      // Crear drectorio si no existe
      File storagePath = new File( Environment.getExternalStorageDirectory( ), "Images_Audio" );
      storagePath.mkdirs( );

      // Crear el fichero en SD card
      file = new File( Environment.getExternalStorageDirectory( ) + "/Images_Audio/" + File.separator + name );

      try {
        file.createNewFile( );
      } catch ( IOException e ) {
        e.printStackTrace( );
      }

      // Crear un nuevo FileOutputStream y escribir los bytes en un fichero

      try {
        fos = new FileOutputStream( file );
        fos.write( bytes.toByteArray( ) );
        fos.close( );
        //Toast.makeText(this, "Imagen guardada", Toast.LENGTH_SHORT).show();
      } catch ( IOException e ) {
        e.printStackTrace( );
      }

      if ( Imagen != null ) {
        // convert bitmap to byte and compres
        ByteArrayOutputStream stream = new ByteArrayOutputStream( );
        Imagen.compress( Bitmap.CompressFormat.JPEG, 50, stream );
        imageInByte = stream.toByteArray( );
      }

      // Obtener descripcion de la BD del servidor

      // ruta de la función del Web Service
      url = "http://" + server + "/WebService/db_descrip.php";

      //Rellenar datos paara consulta
      datos.clear( );
      //datos = new ArrayList<NameValuePair>();
      datos.add( new BasicNameValuePair( "_id", String.valueOf( id ) ) );

      // usamos una clase httpHandler para enviar una petición al servidor web.
      Utils.httpHandler handler = new Utils.httpHandler( );
      String resp = handler.post( url, datos );

      Log.i( "Favorito", "resp: " + resp + "url: " + url );

      Descrip = resp.trim( );

      if ( filtro ) {
        Titulo = array_sort.get( pos ).get( "titulo" );
        Autor = array_sort.get( pos ).get( "autor" );
        Tema = array_sort.get( pos ).get( "tema" );
        //Descrip = array_sort.get(pos).get("descripcion");
        Mp3url = array_sort.get( pos ).get( "mp3" );

      } else {
        Titulo = jsonlist.get( pos ).get( "titulo" );
        Autor = jsonlist.get( pos ).get( "autor" );
        Tema = jsonlist.get( pos ).get( "tema" );
        //Descrip = jsonlist.get(pos).get("descripcion");
        Mp3url = jsonlist.get( pos ).get( "mp3" );
      }

      if ( Tema == null ) {
        Tema = "No definido";
      }

      if ( Descrip == null ) {
        Descrip = "Sin descripción";
      }

      ContentValues Values = new ContentValues( );

      Values.put( KEY_TITULO, Titulo );
      Values.put( KEY_AUTOR, Autor );
      Values.put( KEY_TEMA, Tema );
      Values.put( KEY_DESCRIP, Descrip );
      Values.put( KEY_MP3URL, Mp3url );
      Values.put( KEY_POSICION, 0 );
      Values.put( KEY_IMAGEN, imageInByte );

      if ( Imagen != null ) {

        // Agregar el elemento seleccionado a la BD
        DataBase db = new DataBase( this );
        boolean result = db.insertaLibro( Values );
        db.close( );

        if ( !result ) {
          Utils.mensaje( getApplicationContext( ), "\nError: No se pudo insertar a favoritos." );

        } else {
          Utils.mensaje( getApplicationContext( ), "\nLibro añadido a favoritos.\n" );
        }

      } else {
        Utils.mensaje( getApplicationContext( ), "\nError: No se pudo insertar a favoritos." );
      }

    } else {

      Utils.mensaje( getApplicationContext( ), "Code: " + result[ 0 ] +
          "\nNo se pudo insertar o\nno hay conexión." );
    }
  }

  // Método evento Onclick botones para filtro listview
  public void filtrar( View v ) {

    // filtro = true;

    switch ( v.getId( ) ) {

      case R.id.ibtn_buscar:

        // forzamos mostrar el menu contextual al pulsar el boton
        openContextMenu( v );
        //openOptionsMenu();

        break;
    } // switch

  }


  public void filtrado( String valor ) {
    // Obtener los datos filtrados por valor

    int textlength = 0;

    textlength = inputSearch.getText( ).length( );
    array_sort.clear( );
    for ( int i = 0; i < jsonlist.size( ); i++ ) {
      if ( textlength <= jsonlist.get( i ).get( valor ).length( ) ) {
        // sin distingir may/min
        if ( jsonlist.get( i ).get( valor ).toLowerCase( ).contains(
            inputSearch.getText( ).toString( ).toLowerCase( ).trim( ) ) ) {
          array_sort.add( jsonlist.get( i ) );
        }
      }
    }

    filtro = true;

    // Mostrar nuevo listview con los datos filtrados
    ListAdapter adapter = new SimpleAdapter( this, array_sort, R.layout.list_item,
        new String[]{ TAG_AUTOR, TAG_TITULO, TAG_TEMA,
            TAG_URL },
        new int[]{ R.id.autor, R.id.titulo, R.id.tema, R.id.url } );
    setListAdapter( adapter );

    lv = getListView( );

  }

  // Clase asíncrona para cargar el catalogo en el listview
  private class CargaCatalogo extends AsyncTask<String, Void, Boolean> {

    private ProgressDialog dialog;
    private ListActivity activity;
    private Context context;

    public CargaCatalogo( ListActivity activity ) {
      this.activity = activity;
      context = activity;
      dialog = new ProgressDialog( context );
    }

    protected void onPreExecute( ) {

      // Asignar titulo y mostrar progressdialog
      // dependiendo si existe o hay que bajarlo
      if ( !catalogo ) {
        this.dialog.setTitle( "Conexión con \nwww.audiobooks.hol.es" );
        this.dialog.setMessage( "Obteniendo Datos\nConviritiendo Datos\nPuede durar..." );
        this.dialog.show( );
      } else {
        //this.dialog.setTitle("CATALOGO");
        //this.dialog.setMessage("Conviritiendo Datos\nPuede durar...");
        //this.dialog.show();

        dialog = ProgressDialog.show( Catalogo.this, "CATALOGO", "Conviritiendo Datos\nPuede durar..." );
      }
    }

    protected Boolean doInBackground( final String... args ) {

      // Si catalogo = true es que tenemos descargado el fichero en el movil
      // si false tenemos que descargarlo
      if ( !catalogo ) {

        result = Utils.CompruebaUrl( getBaseContext( ), url );

        // Si es correcto
        if ( result[ 0 ] == 200 ) {

          // descargamos catalogo en la SD
          Utils.descarga_file( getBaseContext( ), url );

          // Como existe en la SD cambiar la url
          //url = Environment.getExternalStorageDirectory()+"/Cat_Audio/catalogo.json";
          // Como existe en la memoria cambiar la url
          url = context.getFilesDir( ) + "/Cat_Audio/catalogo.json";
          Log.d( "URL", url );

          // Verificar que el catalogo existe
          File file = new File( url );

          if ( file.exists( ) ) {
            Log.d( "File", "Existe" );
            // Actualizar preferencias
            //Obtenemos el editor de las preferencias.
            SharedPreferences.Editor editor = prefs.edit( );

            // almacenar un booleano CATALOGO con valor true.
            // y un int con el tamaño del catalogo
            editor.putBoolean( "CATALOGO", true );
            editor.putInt( "TAMANO", result[ 1 ] );

            // Tras haber indicado los cambios a realizar (en nuestro caso dos),
            // le indicamos al editor que los almacene en las preferencias.
            editor.apply( );
          }
        }

      } else {

        // Como existe en la SD cambiar la url
        //url = Environment.getExternalStorageDirectory()+"/Cat_Audio/catalogo.json";
        //url = Environment.getDataDirectory()+"/Cat_Audio/catalogo.json";
        url = context.getFilesDir( ) + "/Cat_Audio/catalogo.json";
        Log.d( "URL", url );

      }

      // Obtener los datos del archivo json y crear la lista
      JSONParser jParser = new JSONParser( );
      // Obtener el archivo .json y asignarlo al objeto array
      //JSONArray json = jParser.getJSONFromUrl(url);

      // crear el json desde el archivo de la SD
      JSONArray json = jParser.getJSONFromFile( url );

      if ( json == null ) {

        return null;
      }

      for ( int i = 0; i < json.length( ); i++ ) {
        try {
          JSONObject c = json.getJSONObject( i );
          String vid = c.getString( TAG_ID );
          String vautor = c.getString( TAG_AUTOR );
          String vtitulo = c.getString( TAG_TITULO );
          String vtema = c.getString( TAG_TEMA );
          String vurl = c.getString( TAG_URL );
          HashMap<String, String> map = new HashMap<String, String>( );
          // añadimos cada nodo al HashMap clave => valor
          map.put( TAG_ID, vid );
          map.put( TAG_AUTOR, vautor );
          map.put( TAG_TITULO, vtitulo );
          map.put( TAG_TEMA, vtema );
          map.put( TAG_URL, vurl );

          // añadimos el nodo creado al array jsonlist
          jsonlist.add( map );

        } catch ( JSONException e ) {
          // TODO Auto-generated catch block
          e.printStackTrace( );
        }
      }
      return null;
    }

    @Override
    protected void onPostExecute( final Boolean success ) {
      if ( dialog.isShowing( ) ) {
        dialog.dismiss( );
      }

      // Si no existen datos mostrar mensaje y salir
      int tam = jsonlist.size( );
      if ( tam == 0 ) {
        // No hay conexion a Internet
        // Mostrar dialogo
        //Utilities.showAlertDialog(Catalogo.this, "Internet","No existe Catálogo o no hay conexión", false);

        Utils.mensaje( getApplicationContext( ), " No existe Catálogo o no hay conexión " );

        try {
          Thread.sleep( 2000 );
        } catch ( InterruptedException ex ) {
          Thread.currentThread( ).interrupt( );
        }

        finish( );
      }

      // Mostrar listview
      ListAdapter adapter = new SimpleAdapter( context, jsonlist, R.layout.list_item,
          new String[]{ TAG_AUTOR, TAG_TITULO, TAG_TEMA, TAG_URL },
          new int[]{ R.id.autor, R.id.titulo, R.id.tema, R.id.url } );
      setListAdapter( adapter );
      lv = getListView( );

    }


  }

  @Override
  public void onDestroy( ) {
    super.onDestroy( );

  }

}