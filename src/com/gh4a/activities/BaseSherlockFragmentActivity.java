/*
 * Copyright 2011 Azwan Adli Abdullah
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.gh4a.activities;

import java.util.Calendar;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.gh4a.Constants;
import com.gh4a.Gh4Application;
import com.gh4a.R;
import com.gh4a.db.BookmarksProvider;
import com.gh4a.utils.ToastUtils;

/**
 * The Base activity.
 */
public class BaseSherlockFragmentActivity extends SherlockFragmentActivity {
    /**
     * Common function when device search button pressed, then open
     * SearchActivity.
     *
     * @return true, if successful
     */
    @Override
    public boolean onSearchRequested() {
        Intent intent = new Intent().setClass(getApplication(), SearchActivity.class);
        startActivity(intent);
        return true;
    }

    /* (non-Javadoc)
     * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!Gh4Application.get(this).isAuthorized()) {
            MenuInflater inflater = getSupportMenuInflater();
            inflater.inflate(R.menu.anon_menu, menu);
        }
        return true;        
    }
    
    /* (non-Javadoc)
     * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
        case android.R.id.home:
            navigateUp();
            return true;
        case R.id.logout:
            Gh4Application.get(this).logout();
            return true;
        case R.id.login:
            Intent intent = new Intent().setClass(this, Github4AndroidActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP
                    |Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            return true;
        case R.id.about:
            openAboutDialog();
            return true;
        case R.id.explore:
            intent = new Intent(this, ExploreActivity.class);
            startActivity(intent);
            return true;
        case R.id.search:
            intent = new Intent(this, SearchActivity.class);
            startActivity(intent);
            return true;
        case R.id.bookmarks:
            intent = new Intent(this, BookmarkListActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void navigateUp() {
    }

    private void openAboutDialog() {
        Dialog dialog = new Dialog(this);

        dialog.setContentView(R.layout.about_dialog);
        
        TextView tvCopyright = (TextView) dialog.findViewById(R.id.copyright);
        tvCopyright.setText("Copyright " + Calendar.getInstance().get(Calendar.YEAR) + " Azwan Adli");
        
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String versionName = packageInfo.versionName;
            dialog.setTitle(getResources().getString(R.string.app_name) + " v" + versionName);
        }  catch (PackageManager.NameNotFoundException e) {
            dialog.setTitle(getResources().getString(R.string.app_name));
        }
        
        dialog.findViewById(R.id.btn_by_email).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent sendIntent = new Intent(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_EMAIL, new String[] { getString(R.string.my_email) });
                sendIntent.setType("message/rfc822");
                startActivity(Intent.createChooser(sendIntent, getString(R.string.send_email_title)));
            }
        });
        
        dialog.findViewById(R.id.btn_by_gh4a).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Context context = BaseSherlockFragmentActivity.this;
                if (Gh4Application.get(context).isAuthorized()) {
                    Intent intent = new Intent(context, IssueCreateActivity.class);
                    intent.putExtra(Constants.Repository.REPO_OWNER, getString(R.string.my_username));
                    intent.putExtra(Constants.Repository.REPO_NAME, getString(R.string.my_repo));
                    startActivity(intent);
                } else {
                    ToastUtils.showMessage(context, R.string.login_prompt);
                }
            }
        });
        
        dialog.show();
    }

    public void showLoading() {
        if (findViewById(R.id.pager) != null) {
            findViewById(R.id.pager).setVisibility(View.INVISIBLE);
        } else if (findViewById(R.id.web_view) != null) {
            findViewById(R.id.web_view).setVisibility(View.INVISIBLE);
        } else if (findViewById(R.id.list_view) != null) {
            findViewById(R.id.list_view).setVisibility(View.INVISIBLE);
        } else if (findViewById(R.id.main_content) != null) {
            findViewById(R.id.main_content).setVisibility(View.INVISIBLE);
        }
        if (findViewById(R.id.pb) != null) {
            findViewById(R.id.pb).setVisibility(View.VISIBLE);
        }
    }
    
    public void hideLoading() {
        if (findViewById(R.id.pager) != null) {
            findViewById(R.id.pager).setVisibility(View.VISIBLE);
        } else if (findViewById(R.id.list_view) != null) {
            findViewById(R.id.list_view).setVisibility(View.VISIBLE);
        } else if (findViewById(R.id.main_content) != null) {
            findViewById(R.id.main_content).setVisibility(View.VISIBLE);
        } else if (findViewById(R.id.web_view) != null) {
            findViewById(R.id.web_view).setVisibility(View.VISIBLE);
        }
        if (findViewById(R.id.pb) != null) {
            findViewById(R.id.pb).setVisibility(View.GONE);
        }
    }
    
    public ProgressDialog showProgressDialog(String message, boolean cancelable) {
        return ProgressDialog.show(this, "", message, cancelable);
    }
    
    public void stopProgressDialog(ProgressDialog progressDialog) {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
    
    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        return info != null && info.isAvailable() && info.isConnected();
    }
    
    public void setErrorView() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.error);
        
        findViewById(R.id.btn_home).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = BaseSherlockFragmentActivity.this;
                Gh4Application app = Gh4Application.get(context);
                if (app.isAuthorized()) {
                    app.openUserInfoActivity(context, app.getAuthLogin(), null,
                            Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                } else {
                    Intent intent = new Intent(context, Github4AndroidActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            }
        });
    }

    protected void saveBookmark(String name, int type, Intent intent, String extraData) {
        ContentValues cv = new ContentValues();
        cv.put(BookmarksProvider.Columns.NAME, name);
        cv.put(BookmarksProvider.Columns.TYPE, type);
        cv.put(BookmarksProvider.Columns.URI, intent.toUri(0));
        cv.put(BookmarksProvider.Columns.EXTRA, extraData);
        if (getContentResolver().insert(BookmarksProvider.Columns.CONTENT_URI, cv) != null) {
            ToastUtils.showMessage(this, R.string.bookmark_saved);
        }
    }

    public ViewPager setupPager(PagerAdapter adapter, int[] titleResIds) {
        final ActionBar actionBar = getSupportActionBar();
        ViewPager pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(adapter);

        pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrollStateChanged(int state) {}

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageSelected(int position) {
                actionBar.getTabAt(position).select();
            }
        });

        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        for (int i = 0; i < titleResIds.length; i++) {
            actionBar.addTab(actionBar.newTab()
                .setText(titleResIds[i])
                .setTabListener(new TabListener(i, pager)));
        }

        return pager;
    }

    private static class TabListener implements ActionBar.TabListener {
        private final int mTag;
        private ViewPager mPager;

        public TabListener(int tag, ViewPager pager) {
            mTag = tag;
            mPager = pager;
        }

        public void onTabSelected(Tab tab, FragmentTransaction ft) {
            mPager.setCurrentItem(mTag);
        }

        public void onTabUnselected(Tab tab, FragmentTransaction ft) {
        }

        public void onTabReselected(Tab tab, FragmentTransaction ft) {
        }
    }
}