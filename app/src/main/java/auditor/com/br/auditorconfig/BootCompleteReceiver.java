package auditor.com.br.auditorconfig;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.Settings;

/**
 * Created by Edi on 15/04/2015.
 */
public class BootCompleteReceiver extends BroadcastReceiver {

    private String serviceURL;
    private String imgPATH;
    private String androidID;
    public static final String PREFS_NAME = "AUDITOR_CONFIG";

    @Override
    public void onReceive(Context context, Intent intent) {

        Intent service = new Intent(context, TestService.class);

        //Restaura as preferencias gravadas
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        this.serviceURL= settings.getString("serviceURL", "");
        this.imgPATH = settings.getString("imgPATH", "");

        // Mostra a id do dispositivo
        this.androidID = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);

        service.putExtra("serviceURL", this.serviceURL);
        service.putExtra("imgPATH", this.imgPATH);
        service.putExtra("androidID", this.androidID);

        context.startService(service);

    }

}