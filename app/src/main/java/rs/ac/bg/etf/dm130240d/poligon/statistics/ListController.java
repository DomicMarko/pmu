package rs.ac.bg.etf.dm130240d.poligon.statistics;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.Serializable;

import rs.ac.bg.etf.dm130240d.poligon.db.DbModel;

/**
 * Created by Marko on 2/3/2017.
 */

public class ListController implements Serializable{

    private String[] names;
    private String[] filePaths;
    private Context currentContext;

    public ListController(Context currentContext) {
        this.currentContext = currentContext;
        findFiles();
    }

    public String[] getNames() {
        return names;
    }

    public String[] getFilePaths() {
        return filePaths;
    }

    public void findFiles(){
        File root = Environment.getExternalStorageDirectory();
        File dir1 = new File(root.getAbsolutePath() + "/Poligoni");
        if(dir1.exists()){
            names = new String[dir1.listFiles().length];
            filePaths = new String[dir1.listFiles().length];

            for(int i = 0; i < dir1.listFiles().length; i++){
                names[i] = dir1.listFiles()[i].getName();
                filePaths[i] = dir1.listFiles()[i].getAbsolutePath();
            }
        }
    }

    public String clearPoligonStatistics(long id){
        String result = "Uspešno ste obrisali sve statistike za odabrani poligon.";
        String poligonName = names[(int)id];
        try{

            DbModel dbModel = new DbModel(currentContext);
            int numOfAffectedRows = dbModel.deletePoligonRows(poligonName);

        } catch (Exception ex){
            result = "Došlo je do greške pri brisanju svih statistika: " + ex.toString();
        }

        return result;
    }

    public String clearAllStatistics(){
        String result = "Uspešno ste obrisali sve statistike za sve poligone.";

        try{

            DbModel dbModel = new DbModel(currentContext);
            int numOfAffectedRows = dbModel.deleteAllRows();

        } catch (Exception ex){
            result = "Došlo je do greške pri brisanju svih statistika: " + ex.toString();
        }

        return result;
    }
}
