package com.commercial.askitloud.japhhi;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.model.Marker;

class PopupAdapter implements InfoWindowAdapter {
    private View popup=null;
    private LayoutInflater inflater=null;

    PopupAdapter(LayoutInflater inflater) {
        this.inflater=inflater;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return(null);
    }

    @SuppressLint("InflateParams")
    @Override
    public View getInfoContents(Marker marker) {
        if (popup == null) {
            popup=inflater.inflate(R.layout.popup, null);
        }
        if (!marker.getTitle().equals("Current Position")) {

            TextView tv = (TextView) popup.findViewById(R.id.title);
            Button btn = (Button) popup.findViewById(R.id.btn);
            tv.setText(marker.getTitle());
            tv = (TextView) popup.findViewById(R.id.snippet);
            tv.setText(marker.getSnippet());
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent phoneIntent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", MapsActivity.contactNumber, null));
                    new Activity().startActivity(phoneIntent);
                }
            });
        }
        else
        {
            popup = null;
        }
        return(popup);
    }
}