package rs.ac.bg.etf.dm130240d.poligon.statistics;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import rs.ac.bg.etf.dm130240d.poligon.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ListStatisticsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ListStatisticsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ListStatisticsFragment extends Fragment {


    private static final String LIST_CONTROLLER = "package rs.ac.bg.etf.dm130240d.poligon.statistics.ListController";

    private ListController listController;

    private ListView listView;

    private OnFragmentInteractionListener mListener;

    public ListStatisticsFragment() {
        // Required empty public constructor
    }


    // TODO: Rename and change types and number of parameters
    public static ListStatisticsFragment newInstance(ListController controller) {
        ListStatisticsFragment fragment = new ListStatisticsFragment();
        Bundle args = new Bundle();
        args.putSerializable(LIST_CONTROLLER, controller);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

            listController = (ListController) getArguments().getSerializable(LIST_CONTROLLER);
            if(listController == null) listController = new ListController(getContext());
        } else{
            listController = new ListController(getContext());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_list_statistics, container, false);
        listView = (ListView) view.findViewById(R.id.poligons_statistics_list);
        registerForContextMenu(listView);

        if(listController.getNames() != null){
            ArrayAdapter adapter = new ArrayAdapter<String>(getActivity(),
                    R.layout.poligon_listview, listController.getNames());
            listView.setAdapter(adapter);
        }

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                mListener.onFragmentInteraction(listController.getNames()[i]);
            }
        });

        ((AppCompatActivity) getActivity()).getSupportActionBar().show();



        // Inflate the layout for this fragment
        return view;
    }

    public ListController getListController() {
        return listController;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.select_score_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.delete_scores_for_poligon:
                String deleteMessage = listController.clearPoligonStatistics(info.position);
                Toast.makeText(getActivity(), deleteMessage, Toast.LENGTH_SHORT).show();
                ((ListStatisticsActivity)getActivity()).updateView();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
    //        mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        ((AppCompatActivity) getActivity()).getSupportActionBar().show();
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(String poligonName);
    }
}
