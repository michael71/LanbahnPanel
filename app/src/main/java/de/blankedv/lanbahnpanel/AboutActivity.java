package de.blankedv.lanbahnpanel;

/*  (c) 2011, Michael Blank
 * 
 *  This file is part of SRCP Client.

    SRCP Client is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    SRCP Client is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Foobar.  If not, see <http://www.gnu.org/licenses/>.

*/

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import static de.blankedv.lanbahnpanel.LanbahnPanelApplication.*;

public class AboutActivity extends Activity {

	private Button cancel;
	private String vinfo="";
	private TextView versTv,connTo;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.about);
		versTv = (TextView)findViewById(R.id.version);
		connTo = (TextView)findViewById(R.id.connected_to);
		
		int version = -1;
		String vName="";

		PackageInfo pInfo;
		try {
			pInfo = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_META_DATA);
			version = pInfo.versionCode;
			vName =pInfo.versionName;
			vinfo = "Version: "+vName + "  (" + version + ")";

		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		 
		versTv.setText(vinfo.toString());
		
		if (connString.length() >0) { 
			connTo.setText("connected turnout: "+connString);
		}
		else
		{
			connTo.setText("currently not connected turnout any SXnet server");
		}

		cancel = (Button)findViewById(R.id.cancel);

		cancel.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {  
				finish();
			}
		});
		
//		upload = (Button)findViewById(R.id.upload);
		
//		if ((DEBUG) && (debugFileEnabled) ){
//			upload.setVisibility(View.VISIBLE);
//
//			upload.setOnClickListener(new View.OnClickListener() {
//				public void onClick(View v) {  
//					app.uploadDebugFile();
//				}
//
//			});
//		}
//		else  {
//			// don't show upload button when there is no debug log file.
//			upload.setVisibility(View.GONE);
//		}
	}

}