package rs.ac.bg.etf.dm130240d.poligon.statistics;

import android.content.Context;
import android.database.Cursor;
import android.widget.Toast;

import java.io.Serializable;

import rs.ac.bg.etf.dm130240d.poligon.db.DbModel;

/**
 * Created by Marko on 2/3/2017.
 */

public class PoligonController implements Serializable {

    private Context context;
    private String poligonName, username;
    private int time;
    private Cursor resultCursor;

    public PoligonController(String poligonName, Context context) {
        this.poligonName = poligonName;
        this.context = context;
        findDetails();
    }

    public void findDetails(){
        try {

            DbModel dbModel = new DbModel(context);
            resultCursor = dbModel.getUsersForPoligon(poligonName);


        } catch (Exception ex){
            Toast.makeText(context, "Došlo je do greške pri komunikaciji sa bazom. Molimo Vas, pokušajte ponovo.", Toast.LENGTH_SHORT).show();

        }

    }

    public Cursor getResultCursor() {
        return resultCursor;
    }
}
