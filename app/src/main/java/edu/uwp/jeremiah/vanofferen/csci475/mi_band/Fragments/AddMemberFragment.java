package edu.uwp.jeremiah.vanofferen.csci475.mi_band.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import edu.uwp.jeremiah.vanofferen.csci475.mi_band.Interfaces.FragmentButtonClickedListener;
import edu.uwp.jeremiah.vanofferen.csci475.mi_band.Connections.MiBandManager;
import edu.uwp.jeremiah.vanofferen.csci475.mi_band.Models.Group;
import edu.uwp.jeremiah.vanofferen.csci475.mi_band.Models.Member;
import edu.uwp.jeremiah.vanofferen.csci475.mi_band.R;


public class AddMemberFragment extends Fragment  {

        //variables from layout
        private EditText firstName, lastName, hFeet, hInches, weight, age;
        private RadioButton male, female, left, right;
        private AppCompatButton addBtn;
        private CardView deviceInfo;
        private MiBandManager miBandManager;
        private Group group;
        private int gender;

        private FragmentButtonClickedListener mListener;

        public AddMemberFragment() {
            // Required empty public constructor
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            miBandManager = MiBandManager.getInstance(getActivity());
            group = miBandManager.getGroup();

        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_add_member, container, false);
            firstName = (EditText) view.findViewById(R.id.first_name);
            lastName = (EditText) view.findViewById(R.id.last_name);
            hFeet = (EditText) view.findViewById(R.id.student_height_feet);
            hInches = (EditText) view.findViewById(R.id.student_height_inches);
            weight = (EditText) view.findViewById(R.id.weight_add);
            age = (EditText) view.findViewById(R.id.age);

            addBtn = (AppCompatButton) view.findViewById(R.id.addDeviceBtn);
            deviceInfo = (CardView) view.findViewById(R.id.card_view);
            deviceInfo.setVisibility(View.GONE);
            addBtn.setText("Select Device");
            male = (RadioButton) view.findViewById(R.id.male);
            female = (RadioButton) view.findViewById(R.id.female);
            left = (RadioButton) view.findViewById(R.id.left_hand);
            right = (RadioButton) view.findViewById(R.id.right_hand);


            addBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String firstname;
                    if (firstName.getText().length() != 0) {
                        firstname = firstName.getText().toString();
                    } else {
                        Toast.makeText(getActivity(), "Forgot to set first name!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String lastname = "";
                    /* Removed by client request
                    if (lastName.getText().length() != 0) {
                        lastname = lastName.getText().toString();
                    } else {
                        Toast.makeText(getActivity(), "Forgot to set last name!", Toast.LENGTH_SHORT).show();
                        return;
                    }*/

                    double heightFeet;
                    if (hFeet.getText().length() != 0) {
                        heightFeet = Double.parseDouble(hFeet.getText().toString());
                    } else {
                        Toast.makeText(getActivity(), "Forgot to set height (feet)!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    double heightInches;
                    if (hInches.getText().length() != 0) {
                        heightInches = Double.parseDouble(hInches.getText().toString());
                    } else {
                        Toast.makeText(getActivity(), "Forgot to set height (inches)!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    //convert height to centimeters
                    double heightC = (heightFeet * 12 + heightInches * 1) * 2.54;

                    double w;
                    if (weight.getText().length() != 0) {
                        w = Double.parseDouble(weight.getText().toString());
                    } else {
                        Toast.makeText(getActivity(), "Forgot to set height weight!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int years;
                    if (age.getText().length() != 0) {
                        years = Integer.parseInt(age.getText().toString());
                    } else {
                        Toast.makeText(getActivity(), "Forgot to set age field!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (male.isChecked()) {
                        gender = 1;
                    } else {
                        gender = 0;
                    }

                    double fullInches = heightFeet*12+heightInches;
                    mListener.onAddMemberInfo(new Member(" ", firstname, lastname, gender,
                            years, (int) fullInches, (int) w));

                    onButtonPressed(view.getId());
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

