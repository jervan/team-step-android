package edu.uwp.jeremiah.vanofferen.csci475.mi_band.Activities;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ContextThemeWrapper;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import edu.uwp.jeremiah.vanofferen.csci475.mi_band.Fragments.LoginFragment;
import edu.uwp.jeremiah.vanofferen.csci475.mi_band.Fragments.SignupFragment;
import edu.uwp.jeremiah.vanofferen.csci475.mi_band.Interfaces.FragmentButtonClickedListener;
import edu.uwp.jeremiah.vanofferen.csci475.mi_band.Connections.MiBandManager;
import edu.uwp.jeremiah.vanofferen.csci475.mi_band.Models .Group;
import edu.uwp.jeremiah.vanofferen.csci475.mi_band.Models.Leader;
import edu.uwp.jeremiah.vanofferen.csci475.mi_band.Models.Member;
import edu.uwp.jeremiah.vanofferen.csci475.mi_band.R;
import edu.uwp.jeremiah.vanofferen.csci475.mi_band.Connections.NetworkConnectionManager;

import static edu.uwp.jeremiah.vanofferen.csci475.mi_band.Connections.NetworkConnectionManager.*;

/**
 * class to display login and sign up views.
 */

public class LoginActivity extends AppCompatActivity implements FragmentButtonClickedListener {

    private static final String TAG = "LoginActivity";
    private FragmentManager fragmentManager;
    private MiBandManager miBandManager;
    private Leader newLeader;
    private AppCompatActivity mActivity;
    private ProgressBar progwheel;
    boolean homeActivityStarted = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_container);

        progwheel = (ProgressBar) findViewById(R.id.progress_wheel);
        mActivity = this;

        // Check for BLE support
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "BLE NOT SUPPORTED!!!", Toast.LENGTH_LONG).show();
            finish();
        }

        permissionCheck();

        fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentById(R.id.fragment_container);

        if (fragment == null) {
            fragment = new LoginFragment();
            fragmentManager.beginTransaction()
                    .add(R.id.fragment_container, fragment)
                    .commit();
        }
    }

    @Override
    protected void onDestroy () {
        super.onDestroy();
        if (miBandManager != null && !homeActivityStarted) {
            miBandManager.close();
        }
    }

    @Override
    public void onBackPressed () {

        Log.d("backstack count", "" + fragmentManager.getBackStackEntryCount());
        if (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    // checks for fine location permission
    private void permissionCheck () {
        // check for fine location runtime permission

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // request permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 0);

        } else {

            miBandManager = MiBandManager.getInstance(this);
        }
    }

    // this method handles permissions response
    @Override
    public void onRequestPermissionsResult ( int requestCode, @NonNull String[] permissions,
                                             @NonNull int[] grantResults){
        switch (requestCode) {
            case 0: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    //permission granted start scanning
                    miBandManager = MiBandManager.getInstance(this);


                } else {
                    // GPS permission denied prompt user to enable
                    notifyBluetooth();
                }
            }
        }
    }

    // this method shows dialog notifying user app will not work without bluetooth
    private void notifyBluetooth () {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(this.getString(R.string.app_name) + " requires access to your devices bluetooth to work!!!")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        permissionCheck();

                    }
                });
        builder.setNegativeButton("Exit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
            }
        });
        builder.setCancelable(false);

        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onButtonClickedFragmentInteraction(int buttonID) {

        switch (buttonID) {
            case R.id.createGroup:
                hideKeyboard(this);
                NetworkConnectionManager<Leader, Void, Leader> createLeader = new NetworkConnectionManager<Leader, Void, Leader>(RequestType.POST_MEMBER) {
                    @Override
                    protected void onPreExecute(){
                        progwheel.setVisibility(View.VISIBLE);
                    }

                    @Override
                    protected void onPostExecute(Leader leader) {
                        progwheel.setVisibility(View.GONE);
                        if (leader != null) {
                            newLeader = leader;
                            groupCreationDialog();
                        } else {
                            progwheel.setVisibility(View.GONE);
                            Toast.makeText(mActivity, "Username or Email already taken", Toast.LENGTH_SHORT).show();
                        }
                    }
                };
                Leader[] leaders = new Leader[] {newLeader};
                createLeader.execute(leaders);
                break;
            case R.id.loginClick:
                hideKeyboard(this);
                fragmentManager.popBackStack();
                break;
            case R.id.signupClick:
                hideKeyboard(this);
                Fragment fragment = new SignupFragment();
                fragmentManager.beginTransaction()
                        .addToBackStack("Backstack")
                        .replace(R.id.fragment_container, fragment)
                        .commit();
                break;
            default:
            }
        }

    @Override
    public void onLoginLeader(Leader leader) {

        final NetworkConnectionManager<Leader, Void, Group> GroupNetworkConnection = new NetworkConnectionManager<Leader, Void, Group>(RequestType.GET_GROUP) {

            @Override
            protected void onPostExecute(Group group) {
                if (group != null) {
                    miBandManager.setGroup(group);
                    miBandManager.disconnectSelectedMemberDevice();
                    startActivity(HomeActivity.newIntent(LoginActivity.this));
                    homeActivityStarted = true;
                    progwheel.setVisibility(View.GONE);
                    finish();
                } else {
                    progwheel.setVisibility(View.GONE);
                    if (newLeader.getDefaultGroup() == 1 && newLeader.isAdmin()) {
                        groupCreationDialog();
                    } else {
                        Toast.makeText(mActivity, "Group Not Found", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        };

        NetworkConnectionManager<Leader, Void, Leader> LeaderNetworkConnection = new NetworkConnectionManager<Leader, Void, Leader>(RequestType.GET_LOGIN) {

            @Override
            protected void onPreExecute(){
                progwheel.setVisibility(View.VISIBLE);
            }

            @Override
            protected void onPostExecute(Leader leader) {
                if (leader != null) {
                    newLeader = leader;
                    if (leader.getDefaultGroup() == 0) {
                        progwheel.setVisibility(View.GONE);
                        groupCreationDialog();
                    } else {
                        Leader[] leaders = new Leader[]{leader};
                        GroupNetworkConnection.execute(leaders);
                    }
                } else {
                    progwheel.setVisibility(View.GONE);
                    Toast.makeText(mActivity, "Username Password Combination Invalid", Toast.LENGTH_SHORT).show();
                }
            }
        };

        Leader[] leaders = new Leader[]{leader};
        LeaderNetworkConnection.execute(leaders);

    }

    @Override
    public void onCreateLeader(Leader leader) {
        if (newLeader == null) {
            this.newLeader = leader;
        } else {
            this.newLeader.setUsername(leader.getUsername());
            this.newLeader.setEmail(leader.getEmail());
            this.newLeader.setPassword(leader.getPassword());
        }
    }

    @Override
    public void onAddMemberInfo(Member member) {
        this.newLeader.setDeviceAddress(member.getDeviceAddress());
        this.newLeader.setFirstName(member.getFirstName());
        this.newLeader.setLastName(member.getLastName());
        this.newLeader.setGender(member.getGender());
        this.newLeader.setAge(member.getAge());
        this.newLeader.setHeightInInches(member.getHeightInInches());
        this.newLeader.setWeightInPounds(member.getWeightInPounds());
    }

    public static void hideKeyboard(Activity activity) {
        try
        {
            InputMethodManager inputManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            View currentFocus = activity.getCurrentFocus();
            if (currentFocus !=null)
                inputManager.hideSoftInputFromWindow(currentFocus.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
        catch (Exception e)
        {
            // Ignore exceptions if any
            Log.e("KeyBoardUtil", e.toString(), e);
        }
    }

    private void groupCreationDialog() {

        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(new ContextThemeWrapper(LoginActivity.this, R.style.DialogCustom));
        View view = getLayoutInflater().inflate(R.layout.dialog_group_input, null);
        dialogBuilder.setView(view);
        final EditText groupNameEditText = (EditText) view.findViewById(R.id.groupName);
        groupNameEditText.requestFocus();
        final AlertDialog groupCreatorDialog = dialogBuilder
                .setCancelable(false)
                .setPositiveButton("Create", null)
                .create();
        // Using onShowListener as a work around to keep dialog from getting dismissed if no group name is entered
        // http://stackoverflow.com/questions/2620444/how-to-prevent-a-dialog-from-closing-when-a-button-is-clicked
        groupCreatorDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button createButton = ((AlertDialog) dialogInterface).getButton(AlertDialog.BUTTON_POSITIVE);
                createButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(!groupNameEditText.getText().toString().equals("")) {

                            Group group = new Group(groupNameEditText.getText().toString());
                            Group[] groups = new Group[] {group};

                            NetworkConnectionManager<Group, Void, Group> createGroup = new NetworkConnectionManager<Group, Void, Group>(RequestType.POST_GROUP){
                                @Override
                                protected void onPreExecute(){
                                    progwheel.setVisibility(View.VISIBLE);
                                }

                                @Override
                                protected void onPostExecute(Group group) {
                                    // create a new group
                                    progwheel.setVisibility(View.GONE);
                                    if (group != null) {
                                        newLeader.setDefaultGroup(group.getId());
                                        Leader[] leaders = new Leader[] {newLeader};
                                        NetworkConnectionManager<Leader,Void,Void> updateLeader = new NetworkConnectionManager<>(RequestType.PUT_MEMBER);
                                        updateLeader.execute(leaders);
                                        group.setLeader(newLeader);
                                        miBandManager.setGroup(group);
                                        groupCreatorDialog.dismiss();
                                        startActivity(HomeActivity.newIntent(LoginActivity.this));
                                        homeActivityStarted = true;
                                        finish();
                                    } else {
                                        Toast.makeText(LoginActivity.this, "Group Name Already Taken", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            };
                            createGroup.execute(groups);
                        } else {
                            Toast.makeText(LoginActivity.this, "Please Enter a Group Name", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
        groupCreatorDialog.show();
    }
}
