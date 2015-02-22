package com.example.omgups;
//фрагмент "штопбыл". Потом заменится на кучу других
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
 
public class SecondFragment extends Fragment {
 
    public SecondFragment(){}
 
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	setRetainInstance(true);
        View rootView = inflater.inflate(R.layout.fragment_first, container, false);
 
        return rootView;
    }
}