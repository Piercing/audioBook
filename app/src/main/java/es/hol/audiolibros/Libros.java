package es.hol.audiolibros;

public class Libros {

    private int Id;
    private String Titulo;
    private String Autor;
    private String Tema;
    private String Descrip;
    private String Mp3url;
    private int Posicion;
    private byte[] Imagen;

    Libros( int _Id, String _Titulo, String _Autor, String _Tema, String _Descrip, String _Mp3url, int _Posicion, byte[] _Imagen ) {

        Id = _Id;
        Titulo = _Titulo;
        Autor = _Autor;
        Tema = _Tema;
        Descrip = _Descrip;
        Imagen = _Imagen;
        Mp3url = _Mp3url;
        Posicion = _Posicion;
    }

    Libros( String _Titulo, String _Autor, String _Tema, String _Descrip, String _Mp3url, int _Posicion, byte[] _Imagen ) {

        Titulo = _Titulo;
        Autor = _Autor;
        Tema = _Tema;
        Descrip = _Descrip;
        Mp3url = _Mp3url;
        Posicion = _Posicion;
        Imagen = _Imagen;
    }

    Libros() {

        Titulo = null;
        Autor = null;
        Tema = null;
        Descrip = null;
        Mp3url = null;
        Posicion = 0;
        Imagen = null;
    }

    public int getId() {
        return Id;
    }

    public void setId( int _Id ) {
        Id = _Id;
    }

    public String getTitulo() {
        return Titulo;
    }

    public void setTitulo( String _Titulo ) {
        Titulo = _Titulo;
    }

    public String getAutor() {
        return Autor;
    }

    public void setAutor( String _Autor ) {
        Autor = _Autor;
    }

    public String getTema() {
        return Tema;
    }

    public void setTema( String _Tema ) {
        Tema = _Tema;
    }

    public String getDescrip() {
        return Descrip;
    }

    public void setDescrip( String _Descrip ) {
        Descrip = _Descrip;
    }

    public String getMp3url() {
        return Mp3url;
    }

    public void setMp3url( String _Mp3url ) {
        Mp3url = _Mp3url;
    }

    public int getPosicion() {
        return Posicion;
    }

    public void setPosicion( int _Posicion ) {
        Posicion = _Posicion;
    }

    public byte[] getImagen() {
        return Imagen;
    }

    public void setImagen( byte[] _Imagen ) {
        Imagen = _Imagen;
    }

}