package com.example.test.app;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.zip.Inflater;

/**
 * Created by GGBomb2 on 2015/11/7.
 */
public class MapFragment extends Fragment {

    View view=null;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        view= inflater.inflate(R.layout.map_fragment,container,false);
        return view;
    }
}
