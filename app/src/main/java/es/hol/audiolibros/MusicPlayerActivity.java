package es.hol.audiolibros;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.IOException;

// La aplicación extiende la clase Activity.
// Además implementamos varios interfaces que corresponden a los escuchadores de eventos.
// OnCompletionListener será invocado cuando la reproducción llegue al final.
// OnSeekBarChangeListener será invocado cuando la barra de progreso cambie.
// OnPreparedListener será invocado cuando el audio esté preparado.
// OnErrorListener será invocado cuando hay un error.
//public class AndroidBuildingMusicPlayerActivity extends Activity implements OnCompletionListener,SeekBar.OnSeekBarChangeListener {
public class MusicPlayerActivity extends Activity implements OnPreparedListener, OnErrorListener,
    OnCompletionListener, SeekBar.OnSeekBarChangeListener {


  // declaración de variables.
  private ImageButton btnVolbajar;
  private ImageButton btnBackward;
  private ImageButton btnPlay;
  private ImageButton btnForward;
  private ImageButton btnVolsubir;
  private ImageButton btnSalir;
  private SeekBar songProgressBar;
  private TextView songTitleLabel;
  private TextView songCurrentDurationLabel;
  private TextView songTotalDurationLabel;
  private ImageView img_player;
  private ProgressDialog pDialog;
  private AudioManager audioManager = null;

  // Media Player
  private MediaPlayer mp;

  // Handler para actualizar timer, progress bar etc,.
  private Handler mHandler;// = new Handler();

  private Utils utils;
  private int seekForwardTime = 5000; // 5000 milliseconds
  private int seekBackwardTime = 5000; // 5000 milliseconds

  // definimos una variable de tipo Bundle para los parametros pasados por Intent
// contiene desde, id, titulo_autor, url, pos
  private Bundle bundle;

  // almacena la posicion de reproduccion del audiolibro
  private int pos_actual = 0;

  @Override
  public void onCreate( Bundle savedInstanceState ) {
    super.onCreate( savedInstanceState );

    // Fijar orientacion vertical
    setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_PORTRAIT );

    setContentView( R.layout.player );

    setVolumeControlStream( AudioManager.STREAM_MUSIC );

    // Referenciar objetos View
    // componenetes del reproductor
    btnPlay = ( ImageButton ) findViewById( R.id.btnPlay );
    btnForward = ( ImageButton ) findViewById( R.id.btnForward );
    btnBackward = ( ImageButton ) findViewById( R.id.btnBackward );
    btnVolsubir = ( ImageButton ) findViewById( R.id.btnVolsubir );
    btnVolbajar = ( ImageButton ) findViewById( R.id.btnVolbajar );
    btnSalir = ( ImageButton ) findViewById( R.id.btnSalir );
    songProgressBar = ( SeekBar ) findViewById( R.id.songProgressBar );
    songTitleLabel = ( TextView ) findViewById( R.id.songTitle );
    songCurrentDurationLabel = ( TextView ) findViewById( R.id.songCurrentDurationLabel );
    songTotalDurationLabel = ( TextView ) findViewById( R.id.songTotalDurationLabel );
    img_player = ( ImageView ) findViewById( R.id.player_img );

    //Obtener el manejador del servicio de audio.
    audioManager = ( AudioManager ) getSystemService( Context.AUDIO_SERVICE );

    // Mediaplayer
    mp = new MediaPlayer( );

    //mp = new MediaPlayer();
    mHandler = new Handler( );

// songManager = new SongsManager();
    utils = new Utils( );


    // Escuchadores / Listeners
    // Indicamos mediante el método setOnSeekBarChangeListener la referencia de la clase
    // que será informada cuando cambie la barra de progreso.
    songProgressBar.setOnSeekBarChangeListener( this ); // Important

    // Indicamos mediante el método setOnCompletionListener la referencia de la clase
    // que será informada cuando el audio finalice.
    mp.setOnCompletionListener( this );

    // Indicamos mediante el método setOnPreparedListener la referencia de la clase
    // que será informada cuando el audio esté preparado.
    mp.setOnPreparedListener( this );

    // Getting all songs list
    //songsList = songManager.getPlayList();

    // By default play first song
    //playSong(0);

    // inicializamos llamando al método getExtras() de la clase Intent (para recuperar el o los parámetros
    // que envió la otra actividad
    bundle = getIntent( ).getExtras( );

    if ( bundle.getString( "desde" ).contains( "favoritos" ) ) {
      // asignar punto de reproduccion
      pos_actual = Integer.parseInt( bundle.getString( "pos" ) );

      // Obtener y convertir imagen de base64 a bitmap
      byte[] bytarray = Base64.decode( bundle.getString( "img" ), Base64.DEFAULT );
      Bitmap bmimage = BitmapFactory.decodeByteArray( bytarray, 0, bytarray.length );

      img_player.setImageBitmap( bmimage );

    }

    // asignar url
    String url = bundle.getString( "url" );

    // Pasar a reproducir el audio-stream
    reproducir( url );


    /**
     * Evento de pulsacion del boton Play
     * reproduce el audio y cambia el boton a pause
     * pausa el audio y cambia el boton a play
     */
    btnPlay.setOnClickListener( new View.OnClickListener( ) {

      @Override
      public void onClick( View arg0 ) {
        // comprobar si está reproduciendo
        if ( mp.isPlaying( ) ) {
          if ( mp != null ) {
            mp.pause( );
            // cambiar botón a play
            btnPlay.setImageResource( R.drawable.btn_play );
          }
        } else {
          // Reanudar audio
          if ( mp != null ) {
            mp.start( );
            // Cambiar boton a pause
            btnPlay.setImageResource( R.drawable.btn_pause );
          }

        }
      }
    } );

    /**
     * Boton avanzar
     * Avanza los segundo especificados
     * */
    btnForward.setOnClickListener( new View.OnClickListener( ) {

      @Override
      public void onClick( View arg0 ) {
        // obtener la posicion actual
        int currentPosition = mp.getCurrentPosition( );
        // comprobar que el tiempo a avanzar es menor que la duracion del audio
        if ( currentPosition + seekForwardTime <= mp.getDuration( ) ) {
          // avanzar audio
          mp.seekTo( currentPosition + seekForwardTime );
        } else {
          // avanzar hasta el final
          mp.seekTo( mp.getDuration( ) );
        }
      }
    } );

    /**
     * Boton retroceder
     * retrocede los segundos especificados
     * */
    btnBackward.setOnClickListener( new View.OnClickListener( ) {

      @Override
      public void onClick( View arg0 ) {
        // obtener la posicion actual
        int currentPosition = mp.getCurrentPosition( );
        // comprobar que el tiempo a retroceder es mayor que 0 sec
        if ( currentPosition - seekBackwardTime >= 0 ) {
          // retroceder audio
          mp.seekTo( currentPosition - seekBackwardTime );
        } else {
          // retroceder a posicion inicial
          mp.seekTo( 0 );
        }

      }
    } );

    /**
     * Boton subir volumen
     *
     * */
    btnVolsubir.setOnClickListener( new View.OnClickListener( ) {

      @Override
      public void onClick( View arg0 ) {

        audioManager.adjustStreamVolume( AudioManager.STREAM_MUSIC,
            AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI );

      }
    } );

    /**
     * Boton bajar volumen
     *
     * */
    btnVolbajar.setOnClickListener( new View.OnClickListener( ) {

      @Override
      public void onClick( View arg0 ) {

        audioManager.adjustStreamVolume( AudioManager.STREAM_MUSIC,
            AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI );

      }
    } );


    /**
     * Boton salir
     *
     * */
    btnSalir.setOnClickListener( new View.OnClickListener( ) {

      @Override
      public void onClick( View arg0 ) {

        mp.pause( );

        // Recuperamos el valor "desde" pasado por en el intent
        String desde = bundle.getString( "desde" );

        if ( desde.equals( "catalogo" ) ) {

          // Cerrar reproductor
          finish( );

        } else { // es un favorito

          // Creamos un objeto de la clase AlertDialog a través de la clase Builder
// Configuramos el mensaje y título del diálogo
// Evitamos que el diálogo sea saltado por cualquier medio distinto a presionar // alguno de los dos botones
          // Llamamos al método setPositiveButton y setNegativeButton indicando el texto a // mostrar en el botón y la clase
          // anónima que capturará el evento clic del botón

          AlertDialog.Builder dialogo = new
              AlertDialog.Builder( MusicPlayerActivity.this );
          dialogo.setMessage( "¿Desea guardar estado de reproducción?" )
              .setTitle( " Salir " )
              .setCancelable( false )
              .setNegativeButton( "Salir",
                  new DialogInterface.OnClickListener( ) {
                    public void onClick( DialogInterface dialog, int id ) {
                      finish( );
                      //dialog.cancel();
                    }
                  } )
              .setPositiveButton( "Guardar",
                  new DialogInterface.OnClickListener( ) {
                    public void onClick( DialogInterface dialog, int id ) {


                      pos_actual = mp.getCurrentPosition( );
                      int _id = Integer.parseInt( bundle.getString( "id" ) );

                      guardar_estado( pos_actual, _id );

                      finish( );
                    }
                  } );

          // Creamos el dialogo definido y lo mostramos
          AlertDialog alert = dialogo.create( );
          alert.show( );

        } // if
      }
    } );

  } //onCreate


  /**
   * reproducir
   * Param: url
   */
  public void reproducir( String url ) {

    //String url ="http://audiolibros.hol.es/libros/gatonegro-audiolibros-ivoox1666676.mp3";
    //mp.release();
    //mp = new MediaPlayer();
    //mHandler = new Handler();
    //mp.setOnPreparedListener(this);
    mp.setAudioStreamType( AudioManager.STREAM_MUSIC );

 /*
 Toast toast = Toast.makeText(getApplicationContext(),
 "url: "+url, Toast.LENGTH_LONG);
 toast.setGravity(Gravity.TOP, 25, 300);
 toast.show();
 */

    try {
      mp.reset( );
      mp.setOnErrorListener( this );
      mp.setOnPreparedListener( this );
      mp.setDataSource( url );
      //Log.d("URL2:",url);
      mp.prepareAsync( );

    } catch ( IllegalArgumentException e ) {
      e.printStackTrace( );
      Log.d( "MP", "Error1" );
    } catch ( SecurityException e ) {
      e.printStackTrace( );
      Log.d( "MP", "Error2" );
    } catch ( IllegalStateException e ) {
      e.printStackTrace( );
      Log.d( "MP", "Error3" );
    } catch ( IOException e ) {
      e.printStackTrace( );
      Log.d( "MP", "Error4" );
    }


    pDialog = new ProgressDialog( this ); //AndroidBuildingMusicPlayerActivity

    //pDialog = ProgressDialog.show(this, "", "Cargando...", true);
    pDialog.setMessage( "Conectando..." );
    pDialog.setIndeterminate( false );
    pDialog.setCancelable( true );
    pDialog.setProgressStyle( ProgressDialog.STYLE_SPINNER );

    pDialog.show( );

  }


  public void onPrepared( MediaPlayer mp ) {

    // eliminar diálogo de progreso
    pDialog.dismiss( );


    // Obtener el titulo + autor pasado por el Intent
    // Alargamos la cadena para poder hacer scroll
    String cancion = bundle.getString( "titulo_autor" );
    int lcancion = cancion.length( );
    String relleno = " ";

    Log.d( "Cancion", cancion + "/" );

    if ( lcancion < 42 ) {
      for ( int i = 0; i < 60 - lcancion; i++ ) {
        cancion = cancion + relleno;
      }

      // añadimos un punto al final de la cadena para que los espacios añadidos tengan efecto
      cancion = cancion + "·";
    }

    Log.d( "Cancion", cancion + "/" );

    // Asignamos el título y forzamos la seleccion del textview para activar scroll horizontal
    // El TextView empezara a ser scrollable cuando tenga el foco y/o haya sido seteada como selected

    songTitleLabel.setText( cancion );
    songTitleLabel.setSelected( true );

    // comenzar reproducción
    mp.start( );
    mp.seekTo( pos_actual ); // si es favorito

    // Cambiar la imagen del boton de play a pause
    btnPlay.setImageResource( R.drawable.btn_pause );

    // valores de la barra de progreso
    songProgressBar.setProgress( 0 );
    songProgressBar.setMax( 100 );

    // actualizar barra de progreso
    updateProgressBar( );
  }

  public boolean onError( MediaPlayer arg0, int arg1, int arg2 ) {


    // eliminar diálogo de progreso
    if ( pDialog.isShowing( ) )
      pDialog.dismiss( );

    // detener tarea en segundo plano
    mHandler.removeCallbacks( mUpdateTimeTask );

    // detener, obtener posicion y liberar reproductor
    mp.stop( );
    pos_actual = mp.getCurrentPosition( );
    mp.reset( );

    // Recuperamos el valor "desde" pasado por en el intent
    String desde = bundle.getString( "desde" );

    if ( desde.equals( "catalogo" ) ) {
      // Creamos un objeto de la clase AlertDialog a través de la clase Builder
      // Configuramos el mensaje y título del diálogo
      // Evitamos que el diálogo sea saltado por cualquier medio distinto a presionar alguno de los dos
      // botones
      // Llamamos al método setPositiveButton y setNegativeButton indicando el texto a mostrar en el botón // y la clase
      // anónima que capturará el evento clic del botón

      AlertDialog.Builder dialogo = new AlertDialog.Builder( MusicPlayerActivity.this );
      dialogo.setMessage( "Se ha producido un error o no hay conexión." )
          .setTitle( " Salir " )
          .setCancelable( false )
          .setNegativeButton( "Salir",
              new DialogInterface.OnClickListener( ) {
                public void onClick( DialogInterface dialog, int id ) {
                  finish( );
                  //dialog.cancel();
                }
              } );


      // Creamos el dialogo definido y lo mostramos
      AlertDialog alert = dialogo.create( );
      alert.show( );

    } else { // es un favorito

      // Creamos un objeto de la clase AlertDialog a través de la clase Builder
      // Configuramos el mensaje y título del diálogo
      // Evitamos que el diálogo sea saltado por cualquier medio distinto a presionar alguno de los dos
      // botones
      // Llamamos al método setPositiveButton y setNegativeButton indicando el texto a mostrar en el botón // y la clase
      // anónima que capturará el evento clic del botón

      AlertDialog.Builder dialogo = new AlertDialog.Builder( MusicPlayerActivity.this );
      dialogo.setMessage( "¿Desea guardar estado de reproducción?" )
          .setTitle( " Salir " )
          .setCancelable( false )
          .setNegativeButton( "Salir",
              new DialogInterface.OnClickListener( ) {
                public void onClick( DialogInterface dialog, int id ) {
                  finish( );
                  //dialog.cancel();
                }
              } )
          .setPositiveButton( "Guardar",
              new DialogInterface.OnClickListener( ) {
                public void onClick( DialogInterface dialog, int id ) {

                  pos_actual = mp.getCurrentPosition( );

                  int _id = Integer.parseInt( bundle.getString( "id" ) );

                  guardar_estado( pos_actual, _id );

                  finish( );

                }
              } );

      // Creamos el dialogo definido y lo mostramos
      AlertDialog alert = dialogo.create( );
      alert.show( );

    }

    return false;

  }


  /**
   * Actualizar barra de progreso
   */
  public void updateProgressBar( ) {
    mHandler.postDelayed( mUpdateTimeTask, 100 );
  }

  /**
   * Hilo secundario
   */
  private Runnable mUpdateTimeTask = new Runnable( ) {
    public void run( ) {
      long totalDuration = mp.getDuration( );
      long currentDuration = mp.getCurrentPosition( );

      // Muestra la duracion total
      songTotalDurationLabel.setText( "" + utils.milliSecondsToTimer( totalDuration ) );
      // Muestra la posicion actual
      songCurrentDurationLabel.setText( "" + utils.milliSecondsToTimer( currentDuration ) );

      // Actualiza barra de progreso
      int progress = ( int ) ( utils.getProgressPercentage( currentDuration, totalDuration ) );
      //Log.d("Progress", ""+progress);
      songProgressBar.setProgress( progress );

      // Ejecutar el proceso cada 100 milliseconds
      mHandler.postDelayed( this, 100 );
    }
  };

  /**
   *
   * */
  @Override
  public void onProgressChanged( SeekBar seekBar, int progress, boolean fromTouch ) {

  }

  /**
   * Cuando el usuario mueve la barra de progreso
   */
  @Override
  public void onStartTrackingTouch( SeekBar seekBar ) {

    // detenemos el proceso en segundo plano
    mHandler.removeCallbacks( mUpdateTimeTask );
  }

  /**
   * Cuando el usuario para el movimiento de la barra de progreso
   */
  @Override
  public void onStopTrackingTouch( SeekBar seekBar ) {

    // detenemos el proceso en segundo plano
    mHandler.removeCallbacks( mUpdateTimeTask );

    int totalDuration = mp.getDuration( );
    int currentPosition = utils.progressToTimer( seekBar.getProgress( ), totalDuration );

    // avanzar o retroceder
    mp.seekTo( currentPosition );

    // actualizar barra de progreso
    updateProgressBar( );
  }

  /**
   * Cuando el audio se completa
   */
  @Override
  public void onCompletion( MediaPlayer arg0 ) {

    // detenemos el proceso en segundo plano runnable
    //mHandler.removeCallbacks(mUpdateTimeTask);

    // cambiar botón a play
    btnPlay.setImageResource( R.drawable.btn_play );
    mp.pause( );
    mp.seekTo( 0 );

    // asignar valores a barra de progreso
    songProgressBar.setProgress( 0 );


    // Actualizar barra de progreso
    updateProgressBar( );

  }

  public void guardar_estado( int pos, int id ) {

    // Definir constantes
    final String KEY_ROWID = "_id";
    final String KEY_POSICION = "posicion";
    final String DATABASE_TABLE = "libros";

    // Asignar objetos
    DataBase db; // Clase SQLiteOpenHelper
    SQLiteDatabase bsSql; // Clase para manipular los datos

    db = new DataBase( getApplicationContext( ) );
    // Abre una conexion a la BD para lectura
    bsSql = db.getWritableDatabase( );

    ContentValues args = new ContentValues( );
    args.put( KEY_POSICION, pos );

    //manda una sentencia UPDATE a la BD para modificar el contacto identifiado por id
    bsSql.update( DATABASE_TABLE, args, KEY_ROWID + "=" + id, null );

    bsSql.close( );
    db.close( );

  }

  @Override
  public void onDestroy( ) {
    super.onDestroy( );

    if ( bundle.getString( "desde" ).contains( "favoritos" ) ) {

      // btnSalir.performClick();

    }

    // detenemos el proceso en segundo plano runnable
    mHandler.removeCallbacks( mUpdateTimeTask );

    if ( mp != null ) {
      if ( mp.isPlaying( ) ) {
        mp.stop( );
        pos_actual = mp.getCurrentPosition( );
      }
      // liberar reproductor
      mp.reset( );
      mp.release( );
    }


  }

}