/*
 *  Copyright  2015 Google Inc. All Rights Reserved.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.google.blockly;

import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.blockly.model.Workspace;

import java.io.IOException;


public class BlocklyActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    public static final String WORKSPACE_FOLDER_PREFIX = "sample_";
    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    private ToolboxFragment mToolboxFragment;
    private TrashFragment mOscar;

    private WorkspaceFragment mWorkspaceFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        position++; // indexing
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        mWorkspaceFragment = WorkspaceFragment.newInstance(position, this);
        fragmentManager.beginTransaction()
                .replace(R.id.container, mWorkspaceFragment)
                .commit();

        onSectionAttached(position);    // Because indexing.

        mWorkspaceFragment.setTrashClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getSupportFragmentManager();
                if (mOscar != null) {
                    fragmentManager.beginTransaction()
                            .show(mOscar)
                            .commit();
                }
            }
        });

        // Load workspaces and toolboxes.
        Workspace workspace = mWorkspaceFragment.getWorkspace();
        AssetManager assetManager = getAssets();
        try {
            workspace.loadBlockFactory(assetManager.open(
                    WORKSPACE_FOLDER_PREFIX + position + "/block_definitions.json"));
            workspace.loadToolboxContents(assetManager.open(
                    WORKSPACE_FOLDER_PREFIX + position + "/toolbox.xml"));
            // TODO (fenichel): Load workspace contents from XML or leave empty.
            workspace.setToolboxFragment(mToolboxFragment);
        } catch (IOException e) {
            e.printStackTrace();
        }

        workspace.setTrashFragment(mOscar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_section1);
                break;
            case 2:
                mTitle = getString(R.string.title_section2);
                break;
            case 3:
                mTitle = getString(R.string.title_section3);
                break;
            default:
                break;
        }
    }

    private void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blockly);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        final DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mToolboxFragment =
                (ToolboxFragment) getSupportFragmentManager().findFragmentById(R.id.toolbox);
        if (mToolboxFragment != null) {
            mToolboxFragment.setDrawerLayout(drawerLayout);
            // HACK because of lifecycle problems.
            mWorkspaceFragment.getWorkspace().setToolboxFragment(mToolboxFragment);
        }

        // Set up the toolbox that lives inside the trash can.
        mOscar = (TrashFragment) getSupportFragmentManager().findFragmentById(R.id.trash);
        if (mOscar != null) {
            // Start hidden.
            getSupportFragmentManager().beginTransaction()
                    .hide(mOscar)
                    .commit();

            mOscar.setDrawerLayout(drawerLayout);
            // HACK because of lifecycle problems.
            mWorkspaceFragment.getWorkspace().setTrashFragment(mOscar);
        }
    }
}