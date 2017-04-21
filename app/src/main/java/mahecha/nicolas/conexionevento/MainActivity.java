package mahecha.nicolas.conexionevento;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.net.sip.SipSession;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;


public class MainActivity extends AppCompatActivity {
    private MiServiceIBinder mServiceIBinder;

    TextView texto,texto2;
    private MiTareaAsincrona tarea1;
    int j=0;
    HashMap<String, String> queryValues;
    String resultado;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        texto = (TextView) findViewById(R.id.tx_ser);
        texto2 =(TextView)findViewById(R.id.saltos);



        //texto.setText("cambio");

        Intent intent = new Intent(MainActivity.this, MiServiceIBinder.class);
        bindService(intent, sConnectionIB, Context.BIND_AUTO_CREATE);

        tarea1 = new MiTareaAsincrona();
        tarea1.execute();


//            String resultado = String.valueOf(mServiceIBinder.getResultado());
//            texto.setText("Su resuldato es: " + resultado);


    }


    // CONFIGURACION INTERFACE SERVICECONNECTION IBINDER
    private ServiceConnection sConnectionIB = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MiServiceIBinder.MiBinderIBinder binder = (MiServiceIBinder.MiBinderIBinder) service;
            mServiceIBinder = binder.getService();
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {}
    };


    private void tareaLarga()
    {
        try {
            Thread.sleep(10000);
        } catch(InterruptedException e) {}
    }


    private class MiTareaAsincrona extends AsyncTask<Void, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {


            while(true) {
                tareaLarga();

                publishProgress();

                if(isCancelled())
                    break;
            }


            return true;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
           // int progreso = values[0].intValue();
            //texto.setText(""+progreso);
            if (mServiceIBinder != null) {
                resultado = String.valueOf(mServiceIBinder.getResultado());
                if(resultado != null) {
                    texto.setText("Su resuldato es: " + resultado);



                    if(!resultado.contentEquals(""))
                    {
                        saltos();
                        enviar();
                    }

                    mServiceIBinder.cleanr();




                }
            }
        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected void onPostExecute(Boolean result) {
            if(result)
                Toast.makeText(MainActivity.this, "Tarea finalizada!", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onCancelled() {
            Toast.makeText(MainActivity.this, "Tarea cancelada!", Toast.LENGTH_SHORT).show();
        }
    }

    public void enviar()
    {

        queryValues = new HashMap<String, String>();
        queryValues.put("fkidusuario", "3");
        queryValues.put("reporte", String.valueOf(resultado));
        queryValues.put("tiempo", tiempo());
        enviarepo();
    }

    ////////////////////***************OBTIENE TIEMPO**************///////////////////
    public String tiempo()
    {
        Date date = new Date();
        CharSequence s  = DateFormat.format("yyyy/M/d H:m", date.getTime());
        String time = s.toString();
        return time ;
    }




    private void enviarepo() {

        Gson gson = new GsonBuilder().create();

        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        ArrayList<HashMap<String, String>> wordList;
        wordList = new ArrayList<HashMap<String, String>>();
        wordList.add(queryValues);



        if(wordList.size()!=0)
        {
            final String jrep = gson.toJson(wordList);
            params.put("jrep", jrep);
            client.post("http://elca.sytes.net:5537/testELCA_APP/detalles_pedidov7/reportmant.php", params, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(String response) {
                    Toast.makeText(getApplicationContext(), "Enviado!!!",
                            Toast.LENGTH_LONG).show();
                    //System.out.println(jrep);
                    //System.out.println(response);

                }
                @Override
                public void onFailure(int statusCode, Throwable error,
                                      String content) {
                    if (statusCode == 404) {
                        Toast.makeText(getApplicationContext(), "Requested resource not found", Toast.LENGTH_LONG).show();
                    } else if (statusCode == 500) {
                        Toast.makeText(getApplicationContext(), "Something went wrong at server end", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Dispositivo Sin Conexión a Internet",
                                Toast.LENGTH_LONG).show();
                    }

                }
            });


        }else{Toast.makeText(getApplicationContext(), "Nada de nada",
                Toast.LENGTH_LONG).show();}

    }


    public void saltos() {
        String[] split = resultado.split("\n");
        StringBuilder sb = new StringBuilder();
//        for (int i = 0; i < split.length; i++) {
//            sb.append(split[i]);
//            if (i != split.length - 1) {
//                sb.append(" ");
//            }
//        }
//        String joined = sb.toString();

        //texto2.setText(""+split.length);

        for (int i = 0; i < split.length; i++) {
            sb.append(split[i]);
            if (i != split.length - 1) {
                sb.append(" ");
            }

            String joined = sb.toString();
            if(joined.contains("AVERIA")&& joined.contains("1D002"))
            {
                fragmentos();
            }



            String[] split2 = joined.split(" ");
            StringBuilder sb2 = new StringBuilder();
                for (int j = 0; j < split2.length; j++) {
                sb2.append(split2[j]);
                    if (j != split2.length - 1) {
                        sb2.append(" ");
                    }
                }
            String joined2 = sb2.toString();
            texto2.setText(joined2);

        }

    }

    public  void fragmentos()
    {


            ///////////////*************FRAGMENTOS***************////////////////
//            BlankFragment mapa1 = new BlankFragment();
//            getSupportFragmentManager().beginTransaction().add(R.id.contenedor, mapa1);

            BlankFragment map = new BlankFragment();
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.contenedor,map, "tag");
            ft.addToBackStack("tag");
            ft.commit();


    }
}
