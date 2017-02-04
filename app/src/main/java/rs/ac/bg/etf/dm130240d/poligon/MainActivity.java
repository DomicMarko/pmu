package rs.ac.bg.etf.dm130240d.poligon;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import rs.ac.bg.etf.dm130240d.poligon.crtanje_poligona.DrawPoligonActivity;
import rs.ac.bg.etf.dm130240d.poligon.game.GameActivity;
import rs.ac.bg.etf.dm130240d.poligon.interfaces.ViewInterface;
import rs.ac.bg.etf.dm130240d.poligon.settings.SettingsActivity;
import rs.ac.bg.etf.dm130240d.poligon.statistics.ListStatisticsActivity;

public class MainActivity extends AppCompatActivity implements ViewInterface {

    private static final String KEY_CONTROLLER = "rs.ac.bg.etf.dm130240d.poligon.MainController";

    MainController controller;
    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            controller = new MainController(this);
        } else {
            controller = (MainController) savedInstanceState.getSerializable(KEY_CONTROLLER);
            controller.setDisplayView(this);
            controller.findFiles();
        }

        listView = (ListView) findViewById(R.id.poligons_list);
        registerForContextMenu(listView);

        if(controller.getNames() != null){
            ArrayAdapter adapter = new ArrayAdapter<String>(this,
                    R.layout.poligon_listview, controller.getNames());
            listView.setAdapter(adapter);
        }

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getApplicationContext(), GameActivity.class);
                intent.putExtra("MAP_PLAN", controller.getFilePaths()[i]);
                intent.putExtra("MAP_NAME", controller.getNames()[i]);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.updateView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.poligon_menu, menu);
        return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.select_level_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.delete_poligon:
                boolean isDeleted = controller.deletePoligon(info.position);
                if(isDeleted) Toast.makeText(this, "Obrisan poligon " + info.position + ".", Toast.LENGTH_SHORT).show();
                else Toast.makeText(this, "Došlo je do greške pri brisanju poligona.", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable(KEY_CONTROLLER, controller);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.option_1:
                Intent intentDraw = new Intent(this,DrawPoligonActivity.class);
                startActivity(intentDraw);
                return true;
            case R.id.option_2:
                Intent intentStatistics = new Intent(this,ListStatisticsActivity.class);
                startActivity(intentStatistics);
                return true;
            case R.id.option_3:
                Intent intentSettings = new Intent(this,SettingsActivity.class);
                startActivity(intentSettings);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void updateView() {

        controller.findFiles();
        if(controller.getNames() != null) {
            ArrayAdapter adapter = new ArrayAdapter<String>(this,
                    R.layout.poligon_listview, controller.getNames());
            listView.setAdapter(adapter);
        }

    }
}
