package edu.uwp.jeremiah.vanofferen.csci475.mi_band.Activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import edu.uwp.jeremiah.vanofferen.csci475.mi_band.Connections.NetworkConnectionManager;
import edu.uwp.jeremiah.vanofferen.csci475.mi_band.Fragments.AddMemberFragment;
import edu.uwp.jeremiah.vanofferen.csci475.mi_band.Fragments.AvailableListFragment;
import edu.uwp.jeremiah.vanofferen.csci475.mi_band.Fragments.EditMemberFragment;
import edu.uwp.jeremiah.vanofferen.csci475.mi_band.Interfaces.FragmentButtonClickedListener;
import edu.uwp.jeremiah.vanofferen.csci475.mi_band.Interfaces.FragmentItemSelectedListener;
import edu.uwp.jeremiah.vanofferen.csci475.mi_band.Connections.MiBandManager;
import edu.uwp.jeremiah.vanofferen.csci475.mi_band.Models.Group;
import edu.uwp.jeremiah.vanofferen.csci475.mi_band.Models.Leader;
import edu.uwp.jeremiah.vanofferen.csci475.mi_band.Models.Member;
import edu.uwp.jeremiah.vanofferen.csci475.mi_band.R;
import edu.uwp.jeremiah.vanofferen.csci475.mi_band.Fragments.MemberListFragment;
import edu.uwp.jeremiah.vanofferen.csci475.mi_band.Service.BackgroundService;
import edu.uwp.jeremiah.vanofferen.csci475.mi_band.Service.QueryPreferences;

import static edu.uwp.jeremiah.vanofferen.csci475.mi_band.Connections.NetworkConnectionManager.*;

public class HomeActivity extends AppCompatActivity
        implements FragmentButtonClickedListener, FragmentItemSelectedListener {

    private static final String TAG = "HomeActivity";
    private static final String HOME_FRAG = "home";
    private static final String ADD_MEMBER_FRAG = "add_member";
    private static final String EDIT_MEMBER_FRAG = "edit_member";
    private static final String DEVICE_LIST_FRAG = "device_list";

    private FragmentManager fragmentManager;
    private Member newMember;
    private MiBandManager miBandManager;
    private ProgressBar progwheel;
    private AppCompatActivity mActivity;
    private FloatingActionButton fab;
    private AppCompatButton groupBut;
    private TextView groupTitle;

    public static Intent newIntent(Context packageContext) {
        Intent intent = new Intent(packageContext, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        if (!BackgroundService.isServiceOn(this)) {
            BackgroundService.setService(this);
        }

        mActivity = this;

        progwheel = (ProgressBar) (findViewById(R.id.progress_wheel));
        miBandManager = MiBandManager.getInstance(this);

        fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentById(R.id.fragment_container);

        if (fragment == null) {
            fragment = MemberListFragment.newInstance();
            fragmentManager.beginTransaction()
                    .add(R.id.fragment_container, fragment, HOME_FRAG)
                    .commit();
        }
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        groupBut = (AppCompatButton) findViewById(R.id.groupButton);
        groupTitle = (TextView) findViewById(R.id.groupTitle);
        String name = miBandManager.getGroup().getName().substring(0,1).toUpperCase() + miBandManager.getGroup().getName().substring(1);

        if (miBandManager.getGroup().getLeader().isAdmin()) {
            groupTitle.setVisibility(View.GONE);
            groupBut.setVisibility(View.VISIBLE);
            groupBut.setText(name);
            groupBut.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    groupSelectionDialog();
                }
            });
        } else {
            groupTitle.setVisibility(View.VISIBLE);
            groupBut.setVisibility(View.GONE);
            groupTitle.setText(name);
        }

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fab.hide();
                groupBut.setVisibility(View.GONE);
                Fragment fragment = new AddMemberFragment();
                fragmentManager.beginTransaction()
                        .addToBackStack(null)
                        .replace(R.id.fragment_container, fragment, ADD_MEMBER_FRAG)
                        .commit();
            }
        });
    }

    private void groupSelectionDialog() {

        final MemberListFragment memberListFragment = (MemberListFragment) fragmentManager.findFragmentByTag(HOME_FRAG);
        final Leader leader = miBandManager.getGroup().getLeader();

        NetworkConnectionManager<Void, Void, ArrayList<Group>> groupsGetConnection = new NetworkConnectionManager<Void, Void, ArrayList<Group>>(RequestType.GET_GROUPS) {

            @Override
            protected void onPostExecute(final ArrayList<Group> groups) {
                progwheel.setVisibility(View.GONE);
                String[] groupNames = new String[groups.size()];
                for (int i = 0; i < groupNames.length; i++){
                    groupNames[i] = groups.get(i).getName();
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(HomeActivity.this, R.style.DialogCustom));
                builder.setTitle("Groups");
                builder.setPositiveButton("Create Group", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // second dialog if create group is clicked
                        AlertDialog.Builder builder2 = new AlertDialog.Builder(new ContextThemeWrapper(HomeActivity.this, R.style.DialogCustom));
                        View view = getLayoutInflater().inflate(R.layout.dialog_group_input, null);
                        builder2.setView(view);
                        final EditText groupNameEditText = (EditText) view.findViewById(R.id.groupName);
                        groupNameEditText.requestFocus();
                        builder2
                                .setCancelable(false)
                                .setPositiveButton("Create", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(final DialogInterface dialog, int which) {
                                        if(groupNameEditText!=null) {

                                            Group group = new Group(groupNameEditText.getText().toString());
                                            NetworkConnectionManager<Group, Void, Group> createGroup = new NetworkConnectionManager<Group, Void, Group>(RequestType.POST_GROUP){
                                                @Override
                                                protected void onPreExecute(){
                                                    progwheel.setVisibility(View.VISIBLE);
                                                }

                                                @Override
                                                protected void onPostExecute(Group group) {
                                                    // creates a new group
                                                    progwheel.setVisibility(View.GONE);
                                                    if (group != null) {
                                                        leader.setDefaultGroup(group.getId());
                                                        Leader[] leaders = new Leader[] {leader};
                                                        NetworkConnectionManager<Leader,Void,Void> updateLeader = new NetworkConnectionManager<>(RequestType.PUT_MEMBER);
                                                        updateLeader.execute(leaders);
                                                        group.setLeader(leader);
                                                        miBandManager.setGroup(group);
                                                        memberListFragment.updateGroup(group);
                                                        groupBut.setText(group.getName());
                                                        dialog.dismiss();
                                                    } else {
                                                        Toast.makeText(HomeActivity.this, "Group name already taken", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            };
                                            Group[] groups = new Group[] {group};
                                            createGroup.execute(groups);
                                        } else {

                                        }

                                    }
                                })
                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                })
                                .show();
                        dialog.dismiss();
                    }
                });


                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                builder.setItems(groupNames, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, int which) {
                        //retrieve group members from server
                        final NetworkConnectionManager<Leader, Void, Group> GroupNetworkConnection = new NetworkConnectionManager<Leader, Void, Group>(RequestType.GET_GROUP) {

                            @Override
                            protected void onPreExecute(){
                                progwheel.setVisibility(View.VISIBLE);
                            }

                            @Override
                            protected void onPostExecute(Group group) {
                                progwheel.setVisibility(View.GONE);
                                if (group != null) {
                                    group.setLeader(leader);
                                    miBandManager.setGroup(group);
                                    memberListFragment.updateGroup(group);
                                    groupBut.setText(group.getName());
                                    dialog.dismiss();
                                } else {
                                    Toast.makeText(mActivity, "Network Error!!!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        };

                        leader.setDefaultGroup(groups.get(which).getId());
                        Leader[] leaders = new Leader[] {leader};
                        NetworkConnectionManager<Leader,Void,Void> updateLeader = new NetworkConnectionManager<>(RequestType.PUT_MEMBER);
                        updateLeader.execute(leaders);
                        GroupNetworkConnection.execute(leaders);

                    }
                }).show();
            }
        };
        groupsGetConnection.execute();
    }

    @Override
    protected void onDestroy () {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {

        Fragment editFrag = fragmentManager.findFragmentByTag(EDIT_MEMBER_FRAG);
        Fragment addFrag = fragmentManager.findFragmentByTag(ADD_MEMBER_FRAG);
        if ((editFrag != null && editFrag.isVisible()) || (addFrag != null && addFrag.isVisible())) {
            fab.show();
            if (miBandManager.getGroup().getLeader().isAdmin())
                groupBut.setVisibility(View.VISIBLE);
        }
        if (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStack();
        } else {
            AlertDialog exitWarning = new AlertDialog.Builder(this)
                    .setMessage("Logging Out of " + getString(R.string.app_name))
                    .setPositiveButton("Logout", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            QueryPreferences.setStoredGroup(HomeActivity.this, null);
                            QueryPreferences.setFirstRun(HomeActivity.this, true);
                            if (miBandManager != null) {
                                miBandManager.close();
                            }
                            if (BackgroundService.isServiceOn(HomeActivity.this)) {
                                BackgroundService.cancelService(HomeActivity.this);
                            }
                            HomeActivity.super.onBackPressed();
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create();
            exitWarning.show();

        }
    }

    @Override
    public void onButtonClickedFragmentInteraction(int buttonID) {

        Fragment availableList = new AvailableListFragment();

        switch (buttonID){

            case R.id.addDeviceBtn:
                hideKeyboard(this);
                fragmentManager.beginTransaction()
                        .addToBackStack("backstack")
                        .replace(R.id.fragment_container, availableList, DEVICE_LIST_FRAG)
                        .commit();
                break;

            case R.id.chooseDevice:
                hideKeyboard(this);
                fragmentManager.beginTransaction()
                        .addToBackStack("backstack")
                        .replace(R.id.fragment_container, availableList, DEVICE_LIST_FRAG)
                        .commit();
                break;

            case R.id.updateBtn:
                hideKeyboard(this);
                NetworkConnectionManager<Member, Void, Member> updateMemberNetworkConnection =
                        new NetworkConnectionManager<Member, Void, Member>(RequestType.PUT_MEMBER) {
                            @Override
                            protected void onPreExecute(){
                                progwheel.setVisibility(View.VISIBLE);
                            }

                            @Override
                            protected void onPostExecute(Member member) {

                                fragmentManager.popBackStack();
                                if (member != null) {
                                    progwheel.setVisibility(View.GONE);
                                    fab.show();
                                    if (miBandManager.getGroup().getLeader().isAdmin())
                                        groupBut.setVisibility(View.VISIBLE);
                                    clearFragBackStack();
                                    Fragment home = new MemberListFragment();
                                    fragmentManager.beginTransaction()
                                            .replace(R.id.fragment_container, home, HOME_FRAG)
                                            .commit();
                                } else {
                                    progwheel.setVisibility(View.GONE);
                                    Toast.makeText(mActivity, "Network Error: Member Info Not Saved", Toast.LENGTH_SHORT).show();
                                }
                            }

                        };

                Member[] members = new Member[] {newMember};
                updateMemberNetworkConnection.execute(members);

                break;
            default:
        }
    }

    @Override
    public void onAddMemberInfo(Member member) {
        newMember = member;
    }

    @Override
    public void onMemberSelectedFragmentInteraction(Member member) {
        Log.d(TAG, "Member Clicked");
            miBandManager.selectMemberDevice(member, false);
    }

    @Override
    public void onMemberDoubleClickedFragmentInteraction(Member member) {
        miBandManager.selectMemberDevice(member, false);
        Fragment editMember = EditMemberFragment.newInstance(member);
        fab.hide();
        groupBut.setVisibility(View.GONE);
        fragmentManager.beginTransaction()
                .addToBackStack("backstack")
                .replace(R.id.fragment_container, editMember, EDIT_MEMBER_FRAG)
                .commit();
    }

    @Override
    public void onMemberSwipedFragmentInteraction(final Member member) {
        Log.d("MEMBERdelete", member.toString());
        NetworkConnectionManager<Member, Void, Boolean> deleteMemberConnection =
               new NetworkConnectionManager<Member, Void, Boolean>(RequestType.DELETE_MEMBER) {
                   @Override
                   protected void onPreExecute(){
                       progwheel.setVisibility(View.VISIBLE);
                   }

                   @Override
                   protected void onPostExecute(Boolean isDeleted) {
                       progwheel.setVisibility(View.GONE);
                       Log.d(TAG, "isDeleted: " + isDeleted);
                       if (isDeleted) {
                           miBandManager.removeMember(member);
                       } else {
                           MemberListFragment homeFrag = (MemberListFragment) fragmentManager.findFragmentByTag(HOME_FRAG);
                           if (homeFrag != null) {
                               homeFrag.updateGroup(miBandManager.getGroup());
                               Toast.makeText(HomeActivity.this, "Network Error unable to delete member", Toast.LENGTH_SHORT).show();
                           }
                       }
                   }
        };
        Member[] members = new Member[] {member};
        deleteMemberConnection.execute(members);
    }

    @Override
    public void onDeviceSelectedFragmentInteraction(String deviceID) {
        newMember.setDeviceAddress(deviceID);
        newMember.setGroupNumber(miBandManager.getGroup().getId());

        NetworkConnectionManager<Member, Void, Member> addMemberNetworkConnection =
                new NetworkConnectionManager<Member, Void, Member>(RequestType.POST_MEMBER) {
                    @Override
                    protected void onPreExecute(){
                        progwheel.setVisibility(View.VISIBLE);
                    }

                    @Override
                    protected void onPostExecute(Member member) {
                        fragmentManager.popBackStack();
                        if (member != null) {
                            miBandManager.selectMemberDevice(member, true);
                            progwheel.setVisibility(View.GONE);
                            miBandManager.getGroup().addMember(member);

                            Fragment editMember = EditMemberFragment.newInstance(member, "Add Member");
                            fragmentManager.beginTransaction()
                                    .replace(R.id.fragment_container, editMember, EDIT_MEMBER_FRAG)
                                    .commit();
                        } else {
                            progwheel.setVisibility(View.GONE);
                            Toast.makeText(mActivity, "Network Error: Member Info Not Saved", Toast.LENGTH_SHORT).show();
                        }
                    }

                };

        NetworkConnectionManager<Member, Void, Member> updateMemberNetworkConnection =
                new NetworkConnectionManager<Member, Void, Member>(RequestType.PUT_MEMBER) {
                    @Override
                    protected void onPreExecute(){
                        progwheel.setVisibility(View.VISIBLE);
                    }

                    @Override
                    protected void onPostExecute(Member member) {

                        fragmentManager.popBackStack();
                        if (member != null) {
                            miBandManager.disconnectSelectedMemberDevice();
                            miBandManager.selectMemberDevice(member, true);
                            progwheel.setVisibility(View.GONE);
                        } else {
                            progwheel.setVisibility(View.GONE);
                            Toast.makeText(mActivity, "Network Error: Member Info Not Saved", Toast.LENGTH_SHORT).show();
                        }
                    }

                };

        Member[] members = new Member[] {newMember};
        if (newMember.getId() == 0) {
            addMemberNetworkConnection.execute(members);
        } else {
            updateMemberNetworkConnection.execute(members);
        }
        newMember = null;
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

    private void clearFragBackStack() {
        while (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStackImmediate();
        }
    }


    // not needed for this activity
    @Override
    public void onLoginLeader(Leader leader) {

    }

    @Override
    public void onCreateLeader(Leader leader) {

    }

}
