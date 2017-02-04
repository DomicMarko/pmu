package rs.ac.bg.etf.dm130240d.poligon.statistics;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import rs.ac.bg.etf.dm130240d.poligon.R;
import rs.ac.bg.etf.dm130240d.poligon.db.DbContract;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PoligonStatisticFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link PoligonStatisticFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PoligonStatisticFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String POLIGON_NAME = "package rs.ac.bg.etf.dm130240d.poligon.statistics.POLIGON_NAME";

    // TODO: Rename and change types of parameters
    private String poligonName;
    private TextView header;
    private ListView poligonScores;
    private PoligonController poligonController;
    private OnFragmentInteractionListener mListener;

    public PoligonStatisticFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @return A new instance of fragment PoligonStatisticFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PoligonStatisticFragment newInstance(String param1) {
        PoligonStatisticFragment fragment = new PoligonStatisticFragment();
        Bundle args = new Bundle();
        args.putString(POLIGON_NAME, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)  poligonName = getArguments().getString(POLIGON_NAME);
        else poligonName = null;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_poligon_statistic, container, false);
        boolean twoFragments = getResources().getBoolean(R.bool.dual_pane);
        if(!twoFragments){
            ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
        }

        header = (TextView) view.findViewById(R.id.poligonStatisticHeaderTextView);
        poligonScores = (ListView) view.findViewById(R.id.poligonStatisticListView);
        if(poligonName == null || poligonName.equals("")) header.setText("Odaberite poligon");
        else{
            header.setText(poligonName);
            poligonController = new PoligonController(poligonName, getActivity());
            Cursor resultC = poligonController.getResultCursor();

            String[] columns = new String[] {DbContract.TableStatistics.KEY_USER_NAME, DbContract.TableStatistics.KEY_TIME};
            int[] to = new int[] {R.id.usernameTextView, R.id.timeTextView };

            SimpleCursorAdapter adapter = new SimpleCursorAdapter(getActivity(), R.layout.poligon_scores, poligonController.getResultCursor(), columns, to, 0);

            poligonScores.setAdapter(adapter);
        }

        return view;
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
        void onFragmentInteraction(Uri uri);
    }
}
