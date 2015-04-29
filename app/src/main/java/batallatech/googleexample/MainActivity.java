package batallatech.googleexample;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.PlusShare;
import com.google.android.gms.plus.model.people.Person;

import org.w3c.dom.Text;


public class MainActivity extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {

    /* Request code used to invoke sign in user interactions. */
    private static final int RC_SIGN_IN = 0;

    /* Client used to interact with Google APIs. */
    private GoogleApiClient mGoogleApiClient;

    /* A flag indicating that a PendingIntent is in progress and prevents
     * us from starting further intents.
     */
    private boolean mIntentInProgress;

    SignInButton signInButton;
    Button signOutButton;
    TextView name;
    private boolean mSignInClicked;
    Button shareBut;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API)
                .addScope(Plus.SCOPE_PLUS_LOGIN)
                .build();

        initialize();



    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
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

    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    protected void onStop() {
        super.onStop();

        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        mSignInClicked = false;
        Toast.makeText(this, "User is connected!", Toast.LENGTH_LONG).show();
        signInButton.setVisibility(View.INVISIBLE);
        signOutButton.setVisibility(View.VISIBLE);

        if (Plus.PeopleApi.getCurrentPerson(mGoogleApiClient) != null) {
            Person currentPerson = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
            String personName = currentPerson.getDisplayName();
            //String personPhoto = currentPerson.getImage();
            String personGooglePlusProfile = currentPerson.getUrl();
            name.setVisibility(View.VISIBLE);
            name.setText(personName);
        }


    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {

//        if (!mIntentInProgress && result.hasResolution()) {
//            try {
//                mIntentInProgress = true;
//                startIntentSenderForResult(result.getResolution().getIntentSender(),
//                        RC_SIGN_IN, null, 0, 0, 0);
//            } catch (IntentSender.SendIntentException e) {
//                // The intent was canceled before it was sent.  Return to the default
//                // state and attempt to connect to get an updated ConnectionResult.
//                mIntentInProgress = false;
//                mGoogleApiClient.connect();
//            }
//        }
        if (!mIntentInProgress) {
            if (mSignInClicked && result.hasResolution()) {
                // The user has already clicked 'sign-in' so we attempt to resolve all
                // errors until the user is signed in, or they cancel.
                try {
                    result.startResolutionForResult(this, RC_SIGN_IN);
                    mIntentInProgress = true;

                } catch (IntentSender.SendIntentException e) {
                    // The intent was canceled before it was sent.  Return to the default
                    // state and attempt to connect to get an updated ConnectionResult.
                    mIntentInProgress = false;
                    mGoogleApiClient.connect();
                }
            }
        }
    }


    protected void onActivityResult(int requestCode, int responseCode, Intent intent) {
        if (requestCode == RC_SIGN_IN) {
            if (responseCode != RESULT_OK) {
                mSignInClicked = false;
            }

            mIntentInProgress = false;

            if (!mGoogleApiClient.isConnected()) {
                mGoogleApiClient.reconnect();
            }
        }
    }

    private void initialize(){

        signInButton = (SignInButton) findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener(this);

        signOutButton = (Button) findViewById(R.id.sign_out);
        signOutButton.setOnClickListener(this);
        name = (TextView) findViewById(R.id.name);
        name.setVisibility(View.INVISIBLE);
        shareBut = (Button) findViewById(R.id.share_button);

        shareBut.setOnClickListener(this);

    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.sign_in_button:
                if (!mGoogleApiClient.isConnecting()) {
                    mSignInClicked = true;
                    mGoogleApiClient.connect();
                    shareBut.setVisibility(View.VISIBLE);
                }
                break;

            case R.id.sign_out:
                    if (mGoogleApiClient.isConnected()) {
                        Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
                        mGoogleApiClient.disconnect();
                        mGoogleApiClient.connect();
                        signInButton.setVisibility(View.VISIBLE);
                        signOutButton.setVisibility(View.INVISIBLE);
                        name.setVisibility(View.INVISIBLE);
                        shareBut.setVisibility(View.INVISIBLE);
                    }

                break;

            case R.id.share_button:
                Intent shareIntent = new PlusShare.Builder(getApplicationContext())
                        .setType("text/plain")
                        .setText("Posting from an app!!")
                        .setContentUrl(Uri.parse("https://developers.google.com/+/"))
                        .getIntent();

                startActivityForResult(shareIntent, 0);
                break;
        }
    }
}