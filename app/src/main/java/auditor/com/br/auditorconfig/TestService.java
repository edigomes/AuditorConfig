package auditor.com.br.auditorconfig;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class TestService extends Service {

    String serviceURL;
    public String imgPATH;
    String androidID;
    boolean running;
    int filesFound = -1;
    public static final String PREFS_NAME = "AUDITOR_CONFIG";
    Worker w;

    public TestService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        super.onStartCommand(intent, flags, startId);

        Log.v("Service", "Processo reiniciado...");

        Intent service = new Intent(this, TestService.class);

        //Restaura as preferencias gravadas
        SharedPreferences settings = this.getSharedPreferences(PREFS_NAME, 0);
        this.serviceURL= settings.getString("serviceURL", "");
        this.imgPATH = settings.getString("imgPATH", "");

        // Mostra a id do dispositivo
        this.androidID = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);

        /*try {
            this.serviceURL = intent.getStringExtra("serviceURL");
            this.imgPATH = intent.getStringExtra("imgPATH");
            this.androidID = intent.getStringExtra("androidID");
        } catch (Exception e) {
            e.printStackTrace();
        }*/

        w = new Worker(this.imgPATH); // config
        w.start();

        // Reinicia com uma cópia da intent
        return START_STICKY;

    }

    @Override
    public void onDestroy() {
        w.running = false;
        super.onDestroy();
    }

    class Worker extends Thread {

        private String imgPATH;
        private FilenameFilter jpgFilter;
        public boolean running = true;

        public Worker(String imgPATH) {
            this.imgPATH = imgPATH;
        }

        public void run(){

            File file = null;

            try {
                file = new File(this.imgPATH);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "MANTEM_TELA_ATIVA");
            wl.acquire();

            while(running) {

                try {

                    Log.v("Files", "imgPath: " + this.imgPATH);
                    File files[] = file.listFiles(getJPGFilter());

                    Log.v("Files", "Arquivos encontrados: " + files.length);

                    Thread.sleep(5000);

                    if (files.length > 0) { //&& files.length == filesFound) {

                        showNotification("Files found: "+files.length);
                        Thread.sleep(1000);

                        for (File f : files) {

                            if (sendFile(f)) {
                                try {
                                    f.delete();
                                    showNotification("File sended: " + f.getName());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                            Log.i("Script", String.valueOf(f.getName()+" -> "+f.lastModified()));
                        }

                        Thread.sleep(5000);

                    } else {
                        Log.v("Files", "Nenhum arquivo foi encontrado");
                    }


                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            wl.release();

        }

        /**
         * Filtro para pegar somente imagens da pasta
         * @return
         */
        private FilenameFilter getJPGFilter() {
            return new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    String lowercaseName = name.toLowerCase();
                    if (lowercaseName.endsWith(".jpg")) {
                        return true;
                    } else {
                        return false;
                    }
                }
            };
        }

    }

    public boolean sendFile(File file) {

        String responseStr = "";
        boolean resp = true;
        ArrayList delete_files = new ArrayList();
        //delete_files.add("as");

        try {

            // Create a new HttpClient and Post Header
            HttpClient httpClient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(this.serviceURL);
            //httppost.addHeader("Content-Type", "application/x-www-form-urlencoded");

            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

            /* Get files */
            //String path = Environment.getExternalStorageDirectory().toString()+"/DCIM/Facebook";

            builder.addPart("uploaded_file0", new FileBody(file));

            SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String dhAtividade = fmt.format(new Date(file.lastModified()));

            // Get from config
            builder.addTextBody("serviceURL", this.serviceURL);
            builder.addTextBody("imgPATH", this.imgPATH);
            builder.addTextBody("androidID", this.androidID);
            builder.addTextBody("dhAtividade", dhAtividade);

            HttpEntity yourEntity = builder.build();
            httppost.setEntity(yourEntity);


            // Execute HTTP Post Request
            try {
                HttpResponse response = httpClient.execute(httppost);
                Log.d("Resp", "Ai: " + response.getStatusLine());
                responseStr = EntityUtils.toString(response.getEntity());
                Log.v("Files", "Log: " + responseStr);
            } catch (ConnectException | UnknownHostException e) {
                Log.v("Files", "Log: Sem resposta do server");
                resp = false;
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

        //showNotification("Response: "+responseStr);
        return resp;

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

}
