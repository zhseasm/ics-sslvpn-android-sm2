/*
 * Copyright (c) 2012-2016 Arne Schwabe
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.zd.vpn.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.zd.vpn.VpnProfile;
import com.zd.vpn.core.ProfileManager;


public class ShowConfigFragment extends Fragment {
	private String configtext;
    private TextView mConfigView;
	private ImageButton mfabButton;

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View v=inflater.inflate(com.zd.vpn.R.layout.viewconfig, container,false);
		mConfigView = (TextView) v.findViewById(com.zd.vpn.R.id.configview);
		

		mfabButton = (ImageButton) v.findViewById(com.zd.vpn.R.id.share_config);
        if (mfabButton!=null) {
			mfabButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					shareConfig();
				}
			});
			mfabButton.setVisibility(View.INVISIBLE);
		}
		return v;
	}

	private void startGenConfig(final VpnProfile vp, final TextView cv) {
		
		new Thread() {
			public void run() {
				/* Add a few newlines to make the textview scrollable past the FAB */
				configtext = vp.getConfigFile(getActivity(),false) + "\n\n\n";
				getActivity().runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						cv.setText(configtext);
                        if (mfabButton!=null)
						    mfabButton.setVisibility(View.VISIBLE);
					}
				});
				
				
			}
		}.start();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
		    inflater.inflate(com.zd.vpn.R.menu.configmenu, menu);
	}
	
	private void shareConfig() {
		Intent shareIntent = new Intent(Intent.ACTION_SEND);
		shareIntent.putExtra(Intent.EXTRA_TEXT, configtext);
		shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(com.zd.vpn.R.string.export_config_title));
		shareIntent.setType("text/plain");
		startActivity(Intent.createChooser(shareIntent, getString(com.zd.vpn.R.string.export_config_chooser_title)));
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		final int itemId = item.getItemId();
		if (itemId == com.zd.vpn.R.id.sendConfig) {
			shareConfig();
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}

    @Override
    public void onResume() {
        super.onResume();

		populateConfigText();
    }

	private void populateConfigText() {
		String profileUUID = getArguments().getString(getActivity().getPackageName() + ".profileUUID");
		final VpnProfile vp = ProfileManager.get(getActivity(),profileUUID);
		int check=vp.checkProfile(getActivity());

		if(check!= com.zd.vpn.R.string.no_error_found) {
            mConfigView.setText(check);
            configtext = getString(check);
        }
        else {
            // Run in own Thread since Keystore does not like to be queried from the main thread

            mConfigView.setText("Generating config...");
            startGenConfig(vp, mConfigView);
        }
	}

	@Override
	public void setUserVisibleHint(boolean visible)
	{
		super.setUserVisibleHint(visible);
		if (visible && isResumed())
			populateConfigText();
	}
}