package es.hol.audiolibros;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

// Esta clase se encarga de abrir la base de datos si existe o de crearla si ésta no existiera.
// Incluso de actualizar la versión si decidimos crear una nueva estructura de la base de datos.
// Para crear la base de datos crearemosuna subclase de SQLiteOpenHelper y sobreescribiremos el
// método onCreate(). Opcionalmente también se puede implemenar el método onUpgrade() y onOpen().
// El método 'onCreate()' se utiliza para crear la BD la primera vez. En el proceso de creación
// usaremos sentencias SQL como 'CREATE TABLE'.
// El método onUpgrade() se utiliza para cambiar de versión, y lo normal es realizar un proceso
// de migración de datos, antes de hacer un DROP TABLE.
class DataBase extends SQLiteOpenHelper {

   // Definir constantes
   private static final String KEY_ROWID = "_id";
   private static final String KEY_TITULO = "titulo";
   private static final String KEY_AUTOR = "autor";
   private static final String KEY_TEMA = "tema";
   private static final String KEY_DESCRIP = "descrip";
   private static final String KEY_MP3URL = "mp3url";
   private static final String KEY_POSICION = "posicion";
   private static final String KEY_IMAGEN = "imagen";

   private static final String TAG = "AdaptadorBD";

   private static final String DATABASE_NAME = "dbfavoritos";
   private static final String DATABASE_TABLE = "libros";
   private static final int DATABASE_VERSION = 1;

   private static final String CREATE_BDD =
       "create table " + DATABASE_TABLE +
           "(" + KEY_ROWID + " integer primary key autoincrement, "
           + KEY_TITULO + " text not null, "
           + KEY_AUTOR + " text not null, "
           + KEY_TEMA + " text not null, "
           + KEY_DESCRIP + " text not null, "
           + KEY_MP3URL + " text not null, "
           + KEY_POSICION + " integer, "
           + KEY_IMAGEN + " BLOB);";

   // Array de strings para su uso en los diferentes métodos.
   private String[] todasColumnas = new String[]{ KEY_ROWID, KEY_TITULO, KEY_AUTOR,
       KEY_TEMA, KEY_DESCRIP, KEY_MP3URL, KEY_POSICION, KEY_IMAGEN };

   // Definir objeto.
   private SQLiteDatabase bsSql;

   /**
    * Constructor
    *
    * @param context
    */
   DataBase( Context context ) {
      super( context, DATABASE_NAME, null, DATABASE_VERSION );
   }

   @Override
   public void onCreate( SQLiteDatabase db ) {

      try { /* Crear tabla a partir de los datos de la variable CREATE_BDD. */

         // Ejecuta la sentencia SQL de creación de la BD.
         db.execSQL( CREATE_BDD );
      } catch ( SQLException e ) {
         e.printStackTrace( );
      }
   }

   @Override
   public void onUpgrade( SQLiteDatabase db, int oldVersion, int newVersion ) {
      Log.w( TAG, "Actualizando base de datos de la versión " + oldVersion
          + " a " + newVersion + ", borraremos todos los datos" );

      // Elimina tabla de la BD.
      db.execSQL( "DROP TABLE IF EXISTS " + DATABASE_TABLE );

      // Crea la nueva BD.
      onCreate( db );
   }

   /**
    * METODOS PARA GESTIONAR LA BASE DE DATOS
    */

    /* INSERTAR: inserta una fila en la BD a partir de los datos de un libro, mediante el método insert(). */
   void insertarLibro( String Titulo, String Autor, String Tema, String Descrip, String Mp3url, int Posicion, byte[]
       Imagen ) {

      SQLiteDatabase db = this.getWritableDatabase( );

      ContentValues Values = new ContentValues( );

      Values.put( KEY_TITULO, Titulo );
      Values.put( KEY_AUTOR, Autor );
      Values.put( KEY_TEMA, Tema );
      Values.put( KEY_DESCRIP, Descrip );
      Values.put( KEY_MP3URL, Mp3url );
      Values.put( KEY_POSICION, Posicion );
      Values.put( KEY_IMAGEN, Imagen );

      // Sentencia INSERT a la BD para insertar una fila con los valores initialValues.
      db.insert( DATABASE_TABLE, null, Values );

      db.close( );

   }

   // Actualiza los datos del  'libro identificado'  por número, con los
   // nuevos valores pasados como parámetros, mediante el método update().
   boolean insertaLibro( ContentValues args ) {

      SQLiteDatabase db = this.getWritableDatabase( );

      // Sentencia INSERT a la BD para insertar una fila con los valores args.
      return db.insert( DATABASE_TABLE, null, args ) > 0;
   }

   /* MODIFICAR:  actualiza los datos del  'libro identificado'  por número,
    con los nuevos valores pasados como parámetros,mediante el método update().*/
   public boolean actualizarLibro( int Id, String Titulo, String Autor,
                                   String Tema, String Mp3url, String Descrip,
                                   int Posicion, byte[] Imagen ) {

      SQLiteDatabase db = this.getWritableDatabase( );

      ContentValues args = new ContentValues( );

      args.put( KEY_TITULO, Titulo );
      args.put( KEY_AUTOR, Autor );
      args.put( KEY_TEMA, Tema );
      args.put( KEY_MP3URL, Mp3url );
      args.put( KEY_DESCRIP, Descrip );
      args.put( KEY_POSICION, Posicion );
      args.put( KEY_IMAGEN, Imagen );

      // Manda una sentencia UPDATE a la BD para modificar el contacto identifiado por id.
      return db.update( DATABASE_TABLE, args, KEY_ROWID + "=" + Id, null ) > 0;
   }

   // Actualiza los datos del libro identificado por numero, con los
   // nuevos valores pasados como parámetros,mediante el método update().
   public boolean actualizaLibro( int Id, ContentValues args ) {

      SQLiteDatabase db = this.getWritableDatabase( );

      // Manda una sentencia UPDATE a la BD para modificar el contacto identifiado por id.
      return db.update( DATABASE_TABLE, args, KEY_ROWID + "=" + Id, null ) > 0;
   }

   /* ELIMINAR: elimina el libro identificado por número, mediante el método delete(). */
   boolean borrarLibro( long numero ) {

      SQLiteDatabase db = this.getWritableDatabase( );

      // Manda una sentencia DELETE a la BD para eliminar la fila identificada por número.
      return db.delete( DATABASE_TABLE, KEY_ROWID + "=" + numero, null ) > 0;
   }

   /* CONSULTAR: consulta a la BD para obtener todos los libros usando el método query().*/
   public Cursor getTodosLibros( ) {

      SQLiteDatabase db = this.getWritableDatabase( );

      return db.query( DATABASE_TABLE, todasColumnas, null, null, null, null, null );
   }

   // Consulta de un libro por 'id' (clave primaria).
   Cursor getLibro( int Id ) throws SQLException {

      SQLiteDatabase db = this.getReadableDatabase( );

      Cursor mCursor = db.query( true, DATABASE_TABLE, todasColumnas,
          KEY_ROWID + "=" + Id, null, null, null, null, null );

      // Si hay datos devueltos, apunta al principio.
      if ( mCursor != null ) mCursor.moveToFirst( );

      return mCursor;
   }

   /**
    * Obtener cantidad de libros.
    *
    * @return
    */
   int getLibrosCount( ) {

      String countQuery = "SELECT * FROM " + DATABASE_TABLE;

      SQLiteDatabase db = this.getReadableDatabase( );

      Cursor cursor = db.rawQuery( countQuery, null );
      int cant = cursor.getCount( );
      cursor.close( );
      // return count
      return cant;
   }
}