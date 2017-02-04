package rs.ac.bg.etf.dm130240d.poligon.game;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import rs.ac.bg.etf.dm130240d.poligon.R;

/**
 * Created by Marko on 2/3/2017.
 */

public class SaveResultFragment extends DialogFragment {

    private static final String KEY_GAME_CONTROLLER = "rs.ac.bg.etf.dm130240d.poligon.game.GameController";
    private TextView userTextView;
    private EditText usernameEditText;

    public static SaveResultFragment newInstance(String time) {
        SaveResultFragment f = new SaveResultFragment();

        // Supply num input as an argument.
        Bundle dialogBundle = new Bundle();
        dialogBundle.putString(KEY_GAME_CONTROLLER, time);
        f.setArguments(dialogBundle);

        return f;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        String time = (String) this.getArguments().getString(KEY_GAME_CONTROLLER);

        View view = inflater.inflate(R.layout.save_result_dialog, null);
        userTextView = (TextView)view.findViewById(R.id.timeOfPlayer);
        userTextView.setText("Ostvareno vreme: " + time);

        usernameEditText = (EditText)view.findViewById(R.id.usernameInput);

        builder.setIcon(R.drawable.ic_launcher)
                .setTitle(R.string.congrats_message)
                .setPositiveButton(R.string.save_result_btn,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                ((GameActivity)getActivity()).doPositiveClick(usernameEditText.getText().toString());
                            }
                        }
                )
                .setNegativeButton(R.string.cancel_btn,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                ((GameActivity)getActivity()).doNegativeClick();
                            }
                        }
                );

        builder.setView(view);

        return builder.create();

    }
/*
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.save_result_dialog, container, false);
        View tv = v.findViewById(R.id.timeOfPlayer);
        ((TextView)tv).setText("Ostvareno vreme: " + gameController.getEndTime());

        return v;

    }
    */
}
