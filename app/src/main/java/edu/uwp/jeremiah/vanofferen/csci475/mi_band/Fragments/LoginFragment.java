package edu.uwp.jeremiah.vanofferen.csci475.mi_band.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import edu.uwp.jeremiah.vanofferen.csci475.mi_band.Interfaces.FragmentButtonClickedListener;
import edu.uwp.jeremiah.vanofferen.csci475.mi_band.Models.Leader;
import edu.uwp.jeremiah.vanofferen.csci475.mi_band.R;

public class LoginFragment extends Fragment {

    private FragmentButtonClickedListener mListener;

    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.login, container, false);
        final EditText username = (EditText) view.findViewById(R.id.username_login);
        final EditText password = (EditText) view.findViewById(R.id.pass_login);

        AppCompatButton login = (AppCompatButton) view.findViewById(R.id.loginBtn);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onLoginLeader(new Leader(username.getText().toString(),password.getText().toString()));
            }
        });

        TextView createAccount = (TextView) view.findViewById(R.id.signupClick);
        createAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onButtonPressed(v.getId());
            }
        });

        TextView forgotPass = (TextView) view.findViewById(R.id.forgotPass);
        forgotPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onButtonPressed(v.getId());
            }
        });

        return view;
    }

    public void onButtonPressed(int id) {
        if (mListener != null) {
            mListener.onButtonClickedFragmentInteraction(id);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof FragmentButtonClickedListener) {
            mListener = (FragmentButtonClickedListener) context;
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
}
