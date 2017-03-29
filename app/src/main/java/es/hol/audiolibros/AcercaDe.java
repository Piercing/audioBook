package es.hol.audiolibros;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Window;

public class AcercaDe extends Activity {
    /** Called when the activity is first created. */
    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Fijar orientacion vertical.
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Ocultar barra de título.
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.acerca_de);
    }
}