package com.example.test.ui.home;

import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.example.test.MultipartRequest;
import com.example.test.MyApplication;
import com.example.test.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class HomeFragment extends Fragment {
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        final EditText usernameEditText = getActivity().findViewById(R.id.username);
        final EditText passwordEditText = getActivity().findViewById(R.id.password);
        final Button loginButton = getActivity().findViewById(R.id.login);
        final ProgressBar loadingProgressBar = getActivity().findViewById(R.id.loading);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view) {
                loginUpload(usernameEditText.getText(),passwordEditText.getText());
            }
        });
    }

    private void loginUpload(Editable id, Editable ps) {
        Map m = new HashMap<String, String>();
        m.put("id", id.toString());
        m.put("pass", ps.toString());

        MultipartRequest mr = new MultipartRequest(getString(R.string.LoginUrl), new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_LONG).show();
            }
        },
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("Response", response);
                        try {
                            JSONObject jObj = new JSONObject(response);
                            String message = jObj.getString("message");

                            Toast.makeText(getActivity(), message + "!", Toast.LENGTH_LONG).show();

                        } catch (JSONException e) {
                            // JSON error
                            e.printStackTrace();
                            Toast.makeText(getActivity(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                },
                null, 0, m,
                null,
                "file", null);

        MyApplication.getInstance().addToRequestQueue(mr);
    }
}
