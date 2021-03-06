package com.aleds90.android.pokemonhelper.view;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.aleds90.android.pokemonhelper.R;
import com.aleds90.android.pokemonhelper.model.Gym;
import com.aleds90.android.pokemonhelper.model.GymDAO;
import com.aleds90.android.pokemonhelper.model.Pokemon;
import com.aleds90.android.pokemonhelper.model.PokemonDAO;

import java.util.ArrayList;

import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;

public class MainActivity extends AppCompatActivity implements LocationListener {

    protected LocationManager locationManager;

    final Context context = this;
    private ImageButton btn_AddGym;
    private ListView pokemons;
    private static final int PERMISSION_REQUEST_CODE = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);


        PokemonDAO pokemonDAO = new PokemonDAO(getApplicationContext());
        ArrayList<Pokemon> pokemonArrayList = pokemonDAO.getPokemons();
        pokemons = (ListView) findViewById(R.id.pokemons);
        pokemons.setAdapter(new PokemonAdapter(pokemonArrayList, getApplicationContext(), MainActivity.this));

        btn_AddGym = (ImageButton) findViewById(R.id.btn_AddGym);
        btn_AddGym.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dialog = new Dialog(context);
                dialog.setContentView(R.layout.dialog_insert_gym);
                dialog.setTitle("INSERISCI NUOVO");
                dialog.setCanceledOnTouchOutside(false);
                final AutoCompleteTextView et_Pokemon = (AutoCompleteTextView) dialog.findViewById(R.id.et_Pokemon);
                String[] pokemonList = getResources().getStringArray(R.array.pokemons);
                ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.select_dialog_item, pokemonList);
                et_Pokemon.setThreshold(1);
                et_Pokemon.setAdapter(adapter);
                et_Pokemon.setTextColor(Color.BLACK);

                final EditText et_CP = (EditText) dialog.findViewById(R.id.et_CP);
                final EditText et_Level = (EditText) dialog.findViewById(R.id.et_Level);
                //final EditText et_Address = (EditText) dialog.findViewById(R.id.et_Address);
                final EditText et_Notes = (EditText) dialog.findViewById(R.id.et_Notes);

                Button btn_OK = (Button) dialog.findViewById(R.id.btn_OK);
                Button btn_Cancell = (Button) dialog.findViewById(R.id.btn_Cancell);

                btn_OK.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (checkPermission()){
                            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                // TODO: Consider calling
                                //    ActivityCompat#requestPermissions
                                // here to request the missing permissions, and then overriding
                                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                //                                          int[] grantResults)
                                // to handle the case where the user grants the permission. See the documentation
                                // for ActivityCompat#requestPermissions for more details.
                                return;
                            }
                            ;
                            PokemonDAO pokemonDAO = new PokemonDAO(getApplicationContext());
                            GymDAO gymDAO = new GymDAO(getApplicationContext());

                            Gym gym1 = new Gym();
                            gym1.setAddress("");
                            gym1.setNotes(et_Notes.getText().toString());
                            gym1.setLongitude(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLongitude());
                            gym1.setLatitude(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLatitude());
                            if (et_Level.getText().toString().equals(null) || et_Level.getText().toString().equals("")) {
                                gym1.setLevel(0);}
                            else {gym1.setLevel((Integer.valueOf(et_Level.getText().toString())));}
                            gym1.setId((int) gymDAO.save(gym1));
                            gymDAO.save(gym1);

                            Pokemon pokemon = new Pokemon();
                            pokemon.setName(et_Pokemon.getText().toString());
                            if (et_CP.getText().toString().equals(null) || et_CP.getText().toString().equals("")) {
                                pokemon.setCp(0);}
                            else {pokemon.setCp((Integer.valueOf(et_CP.getText().toString())));}
                            pokemon.setGym(gym1);
                            pokemonDAO.save(pokemon);

                            pokemons.setAdapter(new PokemonAdapter(pokemonDAO.getPokemons(), getApplicationContext(), MainActivity.this));
                            dialog.dismiss();


                    }else {requestPermission();}
                    }
                });

                btn_Cancell.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
        });



        ShowcaseConfig config = new ShowcaseConfig();
        config.setDelay(500); // half second between each showcase view

        MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(this, "1");

        sequence.setConfig(config);

        sequence.addSequenceItem(btn_AddGym,
                "Clicca per aggiungere un pokemon! Una volta aggiunto alla lista clicca sulla foto del pokemon e controlla la sua posizione!", "FINE");



        sequence.start();

    }


    @Override
    public void onLocationChanged(Location location) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
    private boolean checkPermission(){
        int result = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION);
        if (result == PackageManager.PERMISSION_GRANTED){

            return true;

        } else {

            return false;

        }
    }

    private void requestPermission(){

        if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_FINE_LOCATION)){

            Toast.makeText(context,"GPS permission allows us to access location data. Please allow in App Settings for additional functionality.",Toast.LENGTH_LONG).show();

        } else {

            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},PERMISSION_REQUEST_CODE);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    System.out.println("Permission Granted, Now you can access location data.");

                } else {

                    System.out.println("Permission Denied, You cannot access location data.");

                }
                break;
        }
    }
}
