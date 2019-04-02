package de.blankedv.lanbahnpanel.view

/*  (c) 2011, Michael Blank
 *
 *  This file is part of LANBAHN Panel Client.

    LANBAHN Panel is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    LANBAHN Panel distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Foobar.  If not, see <http://www.gnu.org/licenses/>.

*/

import android.app.Activity
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.PackageManager.NameNotFoundException
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.TextView
import de.blankedv.lanbahnpanel.R
import de.blankedv.lanbahnpanel.model.connString
import org.jetbrains.anko.find


class HelpActivity : Activity() {

    private var cancel: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.help)
        val webv = findViewById<View>(R.id.webView1) as WebView
        val webSetting = webv.getSettings()
        webSetting.setBuiltInZoomControls(true)
        webSetting.setJavaScriptEnabled(true)

        webv.setWebViewClient(WebViewClient())
        webv.loadUrl("file:///android_asset/docs/index.html")

        cancel = findViewById<View>(R.id.cancel) as Button
        cancel!!.setOnClickListener { finish() }

    }

}