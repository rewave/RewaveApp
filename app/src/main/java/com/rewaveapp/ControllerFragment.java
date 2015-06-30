package com.rewaveapp;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;


public class ControllerFragment extends Fragment {

    private OnFragmentInteractionListener listener;
    private Menu menu;

    public static ControllerFragment newInstance() {
        return new ControllerFragment();
    }

    public ControllerFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_controller, container, false);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            listener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onDetach() {
        listener = null;
        super.onDetach();
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        inflater.inflate(R.menu.menu_controller, menu);
        this.menu = menu;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case (R.id.ping):
                listener.sendCommand("ping");
                break;

            case (R.id.left):
                listener.sendCommand("left");
                break;

            case (R.id.right):
                listener.sendCommand("right");
                break;


            case (R.id.exit):
                getActivity().onBackPressed();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onConnectionSuccess() {
        setHasOptionsMenu(true);
        getActivity().findViewById(R.id.connected_message).setVisibility(View.VISIBLE);
    }

    public interface OnFragmentInteractionListener {
        void sendCommand(String command);
    }
}
