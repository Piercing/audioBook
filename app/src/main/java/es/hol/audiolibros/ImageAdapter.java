package es.hol.audiolibros;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Un BaseAdapter puede usarse para un Adapter en un listview o gridview
 * hay que implementar algunos métodos heredados de la clase 'Adapter',
 * porque 'BaseAdapter' es una subclase de => 'Adapter' estos métodos
 * en este ejemplo son: getCount(), getItem(), getItemId(), getView().
 */
class ImageAdapter extends BaseAdapter {

   public static final String KEY_ROWID = "_id";
   public static final String KEY_TITULO = "titulo";
   public static final String KEY_AUTOR = "autor";
   public static final String KEY_IMAGEN = "imagen";

   // Definir constantes.
   private static final String TABLE_IMG = "libros";

   // Definir variables.
   private Context context;
   private Activity activity;

   // Asignar objetos.
   private DataBase db; // Clase SQLiteOpenHelper.
   private SQLiteDatabase bsSql; // Clase para manipular los datos.
   private Cursor cur; // Interface para manipular las filas devueltas por una consulta.

   // El constructor necesita el contexto de la actividad donde se utiliza el adapter.
   ImageAdapter( Context context ) {
      super( );
      this.context = context;
      cur = obtieneCursor( );
   }

   // Devuelve el número de elementos que se introducen en el adapter.
   public int getCount( ) {
      return cur.getCount( );
   }

   // Este método, debería devolver el objeto que está  en esa posición del
   // adapter. No es necesario en este caso más que devolver un objeto null.
   public Object getItem( int position ) {
      return null;
   }

   // Este método debería devolver el 'id' de fila del item que esta en esa
   // posición del adapter. No es necesario en este caso más que devolver 0.
   public long getItemId( int position ) {
      return 0;
   }

   // Obtener cantidad de libros.
   public int getLibrosCount( ) {

      String countQuery = "SELECT * FROM " + TABLE_IMG;
      bsSql = db.getReadableDatabase( );
      Cursor cursor = bsSql.rawQuery( countQuery, null );
      cursor.close( );

      // Return count.
      return cursor.getCount( );
   }

   // Contenedor de los objetos view del item.
   private static class ViewHolder {
      ImageView gv_image; // imagen
      TextView gv_text; // título
      TextView gv_id; // id del registro (oculto)
   }

   // Crear un nuevo ImageView para cada item referenciado por el Adapter.
   // Este método crea una nueva 'View' para cada elemento que añadimos al
   // ImageAdapter. Se le pasa el View en el que se ha pulsado, converview;
   // si convertview es null, se instancia y configura un ImageView con las
   // propiedades deseadas para la presentación de la imagen; si converview
   // no es null, el 'ImageView' local es inicializado con este objeto View.
   public View getView( int position, View convertView, ViewGroup parent ) {

      ViewHolder view;
      // LayoutInflater inflator = activity.getLayoutInflater();
      LayoutInflater inflator = ( LayoutInflater ) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );

      if ( convertView == null ) { // Si no se recicla, inicializar algunos atributos.

         view = new ViewHolder( );
         convertView = inflator.inflate( R.layout.row_grid, null );

         view.gv_text = ( TextView ) convertView.findViewById( R.id.gv_text );
         view.gv_image = ( ImageView ) convertView.findViewById( R.id.gv_image );
         view.gv_id = ( TextView ) convertView.findViewById( R.id.gv_id );

         convertView.setTag( view );

      } else {
         view = ( ViewHolder ) convertView.getTag( );
      }

      // La imagen de este 'imageView' será el elemento de la
      // posición 'position' del cursor cur, declarado abajo.
      cur.moveToPosition( position );
      byte[] bb = cur.getBlob( 2 );

      // Asignar la imagen convertida de byte a bitmap.
      // imageView.setImageBitmap(BitmapFactory.decodeByteArray(bb, 0, bb.length));

      view.gv_image.setImageBitmap( BitmapFactory.decodeByteArray( bb, 0, bb.length ) );
      //gv_text.setText(cur.getString(1));
      view.gv_text.setText( cur.getString( 1 ) );
      view.gv_id.setText( cur.getString( 0 ) );

      //return imageView;
      return convertView;
   }

   @Override
   public void notifyDataSetChanged( ) {

      // Recargamos el cursor.
      cur = obtieneCursor( );
      super.notifyDataSetChanged( );
   }

   /**
    * Obtener un cursor con todos los favoritos.
    *
    * @return
    */
   private Cursor obtieneCursor( ) {

      db = new DataBase( context );
      // Abre una conexion a la BD para lectura.
      bsSql = db.getReadableDatabase( );

      // Realizar consulta y obtener cursor.
      String countQuery = "SELECT _id,titulo,imagen FROM " + TABLE_IMG + " ORDER BY titulo ASC";
      cur = bsSql.rawQuery( countQuery, null );
      cur.moveToFirst( );
      // cur.close();

      db.close( );

      return cur;
   }
}