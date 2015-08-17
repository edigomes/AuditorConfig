package auditor.com.br.auditorconfig;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Environment;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import java.io.File;

public class AuditorConfig extends ActionBarActivity {

    TextView text;

    private TextView TXT_serviceURL;
    private TextView TXT_imgPATH;
    private TextView TXT_androidID;
    private Intent intent;

    private String androidID;
    String config_string;
    public static final String PREFS_NAME = "AUDITOR_CONFIG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auditor_config);

        this.text = (TextView) findViewById(R.id.test);
        this.TXT_serviceURL = (TextView) findViewById(R.id.serviceURL);
        this.TXT_imgPATH = (TextView) findViewById(R.id.imgPATH);
        this.TXT_androidID = (TextView) findViewById(R.id.androidID);

        //Restaura as preferencias gravadas
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        this.TXT_serviceURL.setText(settings.getString("serviceURL", ""));
        this.TXT_imgPATH.setText(settings.getString("imgPATH", ""));

        // Mostra a id do dispositivo
        this.androidID = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
        this.TXT_androidID.setText(androidID);

        final Intent intent = new Intent(AuditorConfig.this, TestService.class);

        Button test_button = (Button) findViewById(R.id.test_button);
        test_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                intent.putExtra("serviceURL", AuditorConfig.this.TXT_serviceURL.getText().toString());
                intent.putExtra("imgPATH", AuditorConfig.this.TXT_imgPATH.getText().toString());
                intent.putExtra("androidID", AuditorConfig.this.androidID.toString());
                startService(intent);

                alert("O Serviço foi iniciado.");

            }
        });

        Button stop_service = (Button) findViewById(R.id.stopService);
        stop_service.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                stopService(intent);

                alert("O Serviço foi parado.");

            }
        });

        Button saveConfig = (Button) findViewById(R.id.saveConfig);
        saveConfig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
                editor.putString("serviceURL", TXT_serviceURL.getText().toString());
                editor.putString("imgPATH", TXT_imgPATH.getText().toString());
                editor.commit();
                //Log.v("App", "Log: OK");
                alert("Sua configuração foi salva.");
            }
        });

    }

    public void setText(String txt) {
        text.setText(txt);
    }

    public void showNotification(String txt) {

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.abc_switch_thumb_material)
                        .setContentTitle("Auditor Config")
                        .setContentText(txt)
                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));

        // Sets an ID for the notification
        int mNotificationId = 001;
        // Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // Builds the notification and issues it.
        mNotifyMgr.notify(mNotificationId, mBuilder.build());

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_auditor_config, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void alert(String txt) {

        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("AuditorConfig:");
        alertDialog.setMessage(txt);
        alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        alertDialog.show();

    }

}
