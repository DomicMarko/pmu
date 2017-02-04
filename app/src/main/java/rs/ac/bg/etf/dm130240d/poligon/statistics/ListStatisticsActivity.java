package rs.ac.bg.etf.dm130240d.poligon.statistics;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.Toast;

import rs.ac.bg.etf.dm130240d.poligon.R;
import rs.ac.bg.etf.dm130240d.poligon.interfaces.ViewInterface;

public class ListStatisticsActivity extends AppCompatActivity implements ListStatisticsFragment.OnFragmentInteractionListener, PoligonStatisticFragment.OnFragmentInteractionListener, ViewInterface {

    private FragmentManager fm;
    private FrameLayout land1, land2, port;
    private ListStatisticsFragment listFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_statistics);

        ActionBar actionBar = this.getSupportActionBar();
        actionBar.setTitle("Statistika");

        fm = getSupportFragmentManager();
        insertFragments();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.poligon_scores_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.clear_all_statistics:
                ListController controller = listFragment.getListController();
                String resultMessage = controller.clearAllStatistics();
                Toast.makeText(this, resultMessage, Toast.LENGTH_SHORT).show();
                this.updateView();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        boolean twoFragments = getResources().getBoolean(R.bool.dual_pane);
        removeFragments(!twoFragments);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onFragmentInteraction(String poligonName) {

        PoligonStatisticFragment newFragment = new PoligonStatisticFragment().newInstance(poligonName);
        FragmentTransaction ft = fm.beginTransaction();
        boolean twoFragments = getResources().getBoolean(R.bool.dual_pane);

        if(twoFragments){

            ft = ft.replace(R.id.statisticFragmentPart2, newFragment);
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            ft.commit();
        } else{

            ft = ft.replace(R.id.singleListStatistic, newFragment);
            ft.addToBackStack("lista");
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            ft.commit();
        }
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void updateView() {
        boolean twoFragments = getResources().getBoolean(R.bool.dual_pane);
        removeFragments(twoFragments);
        insertFragments();
    }

    public void removeFragments(boolean twoFragments){
        FragmentTransaction ft = fm.beginTransaction();
        if(twoFragments){
            land1.removeAllViews();
            land2.removeAllViews();
            Fragment fragment = fm.findFragmentById(R.id.statisticFragmentPart1);
            ft = ft.remove(fragment);
            fragment = fm.findFragmentById(R.id.statisticFragmentPart2);
            ft = ft.remove(fragment);
            ft.commit();
        } else{
            Fragment fragment = fm.findFragmentById(R.id.singleListStatistic);
            ft = ft.remove(fragment);
            ft.commit();
            port.removeAllViews();
        }
    }

    public void insertFragments(){
        listFragment = new ListStatisticsFragment().newInstance(null);

        FragmentTransaction ft = fm.beginTransaction();
        boolean twoFragments = getResources().getBoolean(R.bool.dual_pane);

        if(twoFragments){
            PoligonStatisticFragment newFragment2 = new PoligonStatisticFragment().newInstance(null);
            land1 = (FrameLayout)findViewById(R.id.statisticFragmentPart1);
            land2 = (FrameLayout)findViewById(R.id.statisticFragmentPart2);
            ft = ft.add(R.id.statisticFragmentPart1, listFragment);
            ft = ft.add(R.id.statisticFragmentPart2, newFragment2);
            ft.commit();
            for(int i = 0; i < fm.getBackStackEntryCount(); ++i) {
                fm.popBackStack();
            }
        } else{
            port = (FrameLayout) findViewById(R.id.singleListStatistic);
            ft = ft.replace(R.id.singleListStatistic, listFragment);
            ft.commit();
            for(int i = 0; i < fm.getBackStackEntryCount(); ++i) {
                fm.popBackStack();
            }
        }
    }
}
