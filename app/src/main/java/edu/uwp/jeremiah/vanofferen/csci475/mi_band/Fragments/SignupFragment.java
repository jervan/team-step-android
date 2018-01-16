package edu.uwp.jeremiah.vanofferen.csci475.mi_band.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import edu.uwp.jeremiah.vanofferen.csci475.mi_band.Interfaces.FragmentButtonClickedListener;
import edu.uwp.jeremiah.vanofferen.csci475.mi_band.Models.Leader;
import edu.uwp.jeremiah.vanofferen.csci475.mi_band.R;

public class SignupFragment extends Fragment {

    private EditText username, password, confirmPass, email;
    private Button createGroup;
    private TextView login;

    private FragmentButtonClickedListener mListener;

    public SignupFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.signup, container, false);
        username = (EditText) view.findViewById(R.id.name_signup);
        password = (EditText) view.findViewById(R.id.pass_signup);
        confirmPass = (EditText) view.findViewById(R.id.confirm_pass);
        email = (EditText) view.findViewById(R.id.email_signup);
        createGroup = (Button) view.findViewById(R.id.createGroup);
        createGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String user;
                if(username.getText().length() !=0){
                    user = username.getText().toString();
                }
                else{
                    Toast.makeText(getActivity(), "Forgot to set username!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(password.getText().toString().length()<6){
                    Toast.makeText(getActivity(), "Password should be at least 6 characters!", Toast.LENGTH_SHORT).show();
                    return;
                }

                String pass;
                if(checkPass(password.getText().toString(), confirmPass.getText().toString())){
                    pass = password.getText().toString();
                }
                else{
                    Toast.makeText(getActivity(), "Passwords don't match!", Toast.LENGTH_SHORT).show();
                    return;
                }
                String emailA;
                if(email.getText().length() !=0){
                    emailA = email.getText().toString();
                }
                else{
                    Toast.makeText(getActivity(), "Forgot to set email!", Toast.LENGTH_SHORT).show();
                    return;
                }
                    mListener.onCreateLeader(new Leader(user, pass, emailA));
                    onButtonPressed(view.getId());
            }
        });
        login = (TextView) view.findViewById(R.id.loginClick);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                onButtonPressed(view.getId());
            }
        });
        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
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

    //check passwords
    public boolean checkPass(String password,String confirmPassword)
    {
        boolean pstatus = false;
        if (confirmPassword != null && password != null)
        {
            if (password.equals(confirmPassword)) {
                pstatus = true;
            }
        }
        return pstatus;
    }
}
