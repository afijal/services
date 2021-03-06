/*
 * Copyright (C) 2016 University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.opendatakit.services.resolve.conflict;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import org.opendatakit.consts.IntentConsts;
import org.opendatakit.activities.IAppAwareActivity;
import org.opendatakit.services.database.AndroidConnectFactory;
import org.opendatakit.fragment.AboutMenuFragment;
import org.opendatakit.services.R;

import java.util.ArrayList;

/**
 * Activity that scans all tables and resolves conflicts on any that have conflicts.
 * This is useful for standalone use of the ODK Services APK. And is especially useful
 * when debugging the Sync functionality, as it avoids the need to build and deploy a
 * debug version of Tables or Survey so that you can have them launch the conflict resolution
 * activity. With this activity, you can just select "Resolve Conflicts" from the ODK Services
 * menu.
 *
 * This does not, however, resolve any outstanding checkpoints, which can also cause Sync
 * to terminate with a checkpoints-or-conflicts exit code.
 *
 * @author mitchellsundt@gmail.com
 */
public class AllConflictsResolutionActivity extends Activity implements IAppAwareActivity,
        LoaderManager.LoaderCallbacks<ArrayList<String>> {

    private static final String TAG = AllConflictsResolutionActivity.class.getSimpleName();

    private static final int RESOLVE_CONFLICT_ACTIVITY_RESULT_CODE = 30;

    private static final int FETCH_IN_CONFLICT_TABLE_IDS_LOADER = 0x02;

    private static final String TABLE_ID_LIST = "tableIdList";

    private String mAppName;
    private ArrayList<String> mTableIdList = null;

    private Handler handler = new Handler();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_conflicts_resolver);

        if ( savedInstanceState != null ) {
            if ( savedInstanceState.containsKey(TABLE_ID_LIST) ) {
                mTableIdList = savedInstanceState.getStringArrayList(TABLE_ID_LIST);
            }
        }
        // IMPORTANT NOTE: the Application object is not yet created!

        // Used to ensure that the singleton has been initialized properly
        AndroidConnectFactory.configure();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if ( mTableIdList != null ) {
            outState.putStringArrayList(TABLE_ID_LIST, mTableIdList);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Do this in on resume so that if we resolve a row it will be refreshed
        // when we come back.
        mAppName = getIntent().getStringExtra(IntentConsts.INTENT_KEY_APP_NAME);
        if (mAppName == null) {
            Log.e(TAG, IntentConsts.INTENT_KEY_APP_NAME + " not supplied on intent");
            setResult(Activity.RESULT_CANCELED);
            finish();
            return;
        }
        if ( mTableIdList == null ) {
            // TODO: do database call to get this
            getLoaderManager().initLoader(FETCH_IN_CONFLICT_TABLE_IDS_LOADER, null, this);
        }

        launchResolveConflictsOnFirstTable();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.action_verify_server_settings).setVisible(false);
        menu.findItem(R.id.action_resolve_conflict).setVisible(false);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_verify_server_settings) {
            return true;
        }
        if (id == R.id.action_resolve_conflict) {
            return true;
        }
        if (id == R.id.action_about) {

            FragmentManager mgr = getFragmentManager();
            Fragment newFragment = mgr.findFragmentByTag(AboutMenuFragment.NAME);
            if ( newFragment == null ) {
                newFragment = new AboutMenuFragment();
            }
            FragmentTransaction trans = mgr.beginTransaction();
            trans.replace(R.id.all_conflicts_activity_view, newFragment, AboutMenuFragment.NAME);
            trans.addToBackStack(AboutMenuFragment.NAME);
            trans.commit();

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override public String getAppName() {
        return mAppName;
    }

    private void launchResolveConflictsOnFirstTable() {
        if ( mTableIdList != null && mTableIdList.isEmpty() ) {
            Toast.makeText(this, R.string.all_tables_scanned_for_conflicts, Toast.LENGTH_LONG).show();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    setResult(Activity.RESULT_OK);
                    finish();
                }
            }, 1000L);
            return;
        }

        if ( mTableIdList != null ) {
            String nextTableId = mTableIdList.get(mTableIdList.size()-1);
            mTableIdList.remove(mTableIdList.size()-1);
            Intent i = new Intent(this, ConflictResolutionActivity.class);
            i.putExtra(IntentConsts.INTENT_KEY_APP_NAME, getAppName());
            i.putExtra(IntentConsts.INTENT_KEY_TABLE_ID, nextTableId);
            startActivityForResult(i, RESOLVE_CONFLICT_ACTIVITY_RESULT_CODE);
            return;
        }
    }

    @Override
    public Loader<ArrayList<String>> onCreateLoader(int id, Bundle args) {
        // Now create and return a OdkResolveCheckpointRowLoader that will take care of
        // creating an ArrayList<ResolveRowEntry> for the data being displayed.
        return new FetchInConflictTableIdsLoader(this, mAppName);
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<String>> loader,
                               ArrayList<String> resolveRowEntryArrayList) {

        // Swap the new list in.
        mTableIdList = resolveRowEntryArrayList;
        launchResolveConflictsOnFirstTable();
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<String>> loader) {
        // This is called when the last ArrayList<ResolveRowEntry> provided to onLoadFinished()
        // above is about to be released. We need to make sure we are no
        // longer using it.
        mTableIdList = null;
    }

}
