package edu.uwp.jeremiah.vanofferen.csci475.mi_band.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import edu.uwp.jeremiah.vanofferen.csci475.mi_band.Activities.HomeActivity;
import edu.uwp.jeremiah.vanofferen.csci475.mi_band.Activities.LoginActivity;
import edu.uwp.jeremiah.vanofferen.csci475.mi_band.Interfaces.FragmentButtonClickedListener;
import edu.uwp.jeremiah.vanofferen.csci475.mi_band.Connections.MiBandManager;
import edu.uwp.jeremiah.vanofferen.csci475.mi_band.Models.Leader;
import edu.uwp.jeremiah.vanofferen.csci475.mi_band.Models.Member;
import edu.uwp.jeremiah.vanofferen.csci475.mi_band.R;


public class EditMemberFragment extends Fragment {
    //variables from layout
    private EditText firstName,lastName,hFeet,hInches,weight,age;
    private RadioButton leftHand, rightHand, male, female;
    private AppCompatButton updateBtn, chooseDevice;
    private CardView deviceInfo;
    private TextView device, deviceStatus, stepCount, batteryLevel;
    private Member member;
    private static final String ARG_MEMBER = "member";
    private static final String ARG_BUTTON = "button";
    private MiBandManager miBandManager;
    private Handler handler;

    private FragmentButtonClickedListener mListener;

    public EditMemberFragment() {
        // Required empty public constructor
    }

    public static EditMemberFragment newInstance(Member member) {
        EditMemberFragment fragment = new EditMemberFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_MEMBER, member);
        fragment.setArguments(args);
        //set edit texts here?
        return fragment;
    }
    //--------------------------------------------------------------------------------------------------------------------------------------
    public static EditMemberFragment newInstance(Member member, String buttonTitle) {
        EditMemberFragment fragment = new EditMemberFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_MEMBER, member);
        args.putString(ARG_BUTTON, buttonTitle);
        fragment.setArguments(args);
        //set edit texts here?
        return fragment;
    }




    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null){
            member = getArguments().getParcelable(ARG_MEMBER);
        }
        miBandManager = MiBandManager.getInstance(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_member, container, false);
        firstName = (EditText) view.findViewById(R.id.first_name);
        lastName = (EditText) view.findViewById(R.id.last_name);
        hFeet = (EditText) view.findViewById(R.id.student_height_feet);
        hInches = (EditText) view.findViewById(R.id.student_height_inches);
        weight = (EditText) view.findViewById(R.id.weight_add);
        leftHand = (RadioButton) view.findViewById(R.id.left_hand);
        rightHand = (RadioButton) view.findViewById(R.id.right_hand);
        male = (RadioButton) view.findViewById(R.id.male);
        female = (RadioButton) view.findViewById(R.id.female);
        updateBtn = (AppCompatButton) view.findViewById(R.id.updateBtn);
        deviceInfo = (CardView) view.findViewById(R.id.card_view);
        age = (EditText) view.findViewById(R.id.age);
        deviceInfo = (CardView) view.findViewById(R.id.card_view);
        device = (TextView) deviceInfo.findViewById(R.id.device);
        deviceStatus = (TextView) deviceInfo.findViewById(R.id.status);
        stepCount = (TextView) deviceInfo.findViewById(R.id.steps);
        batteryLevel = (TextView) deviceInfo.findViewById(R.id.battery);
        chooseDevice = (AppCompatButton) view.findViewById(R.id.chooseDevice);


        //set edit texts
        firstName.setText(member.getFirstName());
        lastName.setText(member.getLastName());
        double fullHeightInches = member.getHeightInInches();
        int heightFeet = (int) fullHeightInches / 12;
        int heightInches = (int) fullHeightInches % 12;
        hFeet.setText("" + heightFeet);
        hInches.setText(""+ heightInches);
        double weightlbs = member.getWeightInPounds();
        weight.setText("" + (int) weightlbs);
        age.setText("" + member.getAge());
        if (member.getGender() == 1) {
            male.setChecked(true);
        } else {
            female.setChecked(true);
        }



        updateDeviceInfo();

        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 1)
                    updateDeviceInfo();
                super.handleMessage(msg);
            }
        };

        miBandManager.addHandler(handler);



        if(getActivity() instanceof LoginActivity){
            updateBtn.setText("Login");
        }

        if(getActivity() instanceof HomeActivity && !getArguments().containsKey("button")){
            if (member instanceof Leader) {
                updateBtn.setText("Update Leader");
            } else {
                updateBtn.setText("Update Member");
            }
        }

        if(getArguments().containsKey("button")){
            updateBtn.setText(getArguments().get("button").toString());
        }

        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                if (updateMember()) {
                    mListener.onAddMemberInfo(member);
                    onButtonPressed(view.getId());
                }
            }
        });

        chooseDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (updateMember()) {
                    mListener.onAddMemberInfo(member);
                    onButtonPressed(v.getId());
                }
            }
        });

        return view;
    }

    private void updateDeviceInfo() {
        device.setText(member.getDeviceAddress());
        deviceStatus.setText(member.getDeviceStatus());
        String steps = member.getLastKnownStepCount() + "";
        stepCount.setText(steps);
        String battery = member.getBatteryLevel() + "%";
        batteryLevel.setText(battery);
    }

    public boolean updateMember() {

        if (firstName.getText().length() != 0) {
            member.setFirstName(firstName.getText().toString());
        } else {
            Toast.makeText(getActivity(), "Forgot to set first name!", Toast.LENGTH_SHORT).show();
            return false;
        }

        /* Removed by clients request

        if (lastName.getText().length() != 0) {
            member.setLastName(lastName.getText().toString());
        } else {
            Toast.makeText(getActivity(), "Forgot to set last name!", Toast.LENGTH_SHORT).show();
            return false;
        }*/

        double heightFeet;
        if (hFeet.getText().length() != 0) {
            heightFeet = Double.parseDouble(hFeet.getText().toString());
        } else {
            Toast.makeText(getActivity(), "Forgot to set height (feet)!", Toast.LENGTH_SHORT).show();
            return false;
        }

        double heightInches;
        if (hInches.getText().length() != 0) {
            heightInches = Double.parseDouble(hInches.getText().toString());
            double fullInches = heightFeet * 12 + heightInches;
            member.setHeightInInches((int) fullInches);
        } else {
            Toast.makeText(getActivity(), "Forgot to set height (inches)!", Toast.LENGTH_SHORT).show();
            return false;
        }

        double w;
        if (weight.getText().length() != 0) {
            w = Double.parseDouble(weight.getText().toString());
            member.setWeightInPounds((int) w);
        } else {
            Toast.makeText(getActivity(), "Forgot to set height weight!", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (age.getText().length() != 0) {
            member.setAge(Integer.parseInt(age.getText().toString()));
        } else {
            Toast.makeText(getActivity(), "Forgot to set age field!", Toast.LENGTH_SHORT).show();
            return false;
        }

        int gender;
        if (male.isChecked()) {
            gender = 1;
        } else {
            gender = 0;
        }
        member.setGender(gender);

        return true;
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
        miBandManager.removeHandler(handler);
        handler = null;
    }
}