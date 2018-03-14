package com.example.liam.flashbackplayer;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.Task;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.people.v1.PeopleService;
import com.google.api.services.people.v1.model.EmailAddress;
import com.google.api.services.people.v1.model.ListConnectionsResponse;
import com.google.api.services.people.v1.model.Name;
import com.google.api.services.people.v1.model.Person;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class GoogleLoginActivity extends AppCompatActivity {
    final String clientId = "518276155353-d2p680lu89o5854sm92tism5km5j6s3a.apps.googleusercontent.com";
    final String clientSecret = "Scw4PXx68Ds4AkdmgiYBiUNy";
    public static final String EXTRA_MYEMAIL = "com.example.liam.flashbackplayer.MYEMAIL";
    public static final String EXTRA_EMAILLIST = "com.example.liam.flashbackplayer.EMAILLIST";
    private static final int RC_SIGN_IN = 9001;
    GoogleSignInClient mGoogleSignInClient;

    private String myEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_login);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestServerAuthCode(clientId)
                .requestEmail()
                .requestScopes(new Scope("https://www.googleapis.com/auth/plus.login"),
                        new Scope("https://www.googleapis.com/auth/contacts"))
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        findViewById(R.id.sign_in_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            // Signed in successfully, show authenticated UI.
            updateUI(account);
            myEmail = account.getEmail();
            new PeoplesAsync().execute(account.getServerAuthCode());
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w("SignInFailed", "signInResult:failed code=" + e.getStatusCode());
            updateUI(null);
        }
    }

    void updateUI(GoogleSignInAccount account) {
        if (account == null) {
            Toast.makeText(getApplicationContext(), "Sign in failed",
                    Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getApplicationContext(), "Hello " + account.getDisplayName(),
                    Toast.LENGTH_LONG).show();
        }
    }

    class PeoplesAsync extends AsyncTask<String, Void, HashMap<String, String>> {
        private PeopleService peopleService;

        @Override
        protected HashMap<String, String> doInBackground(String... params) {
            HashMap<String, String> emailList = new HashMap<>();
            try {
                HttpTransport httpTransport = new NetHttpTransport();
                JacksonFactory jsonFactory = new JacksonFactory();

                GoogleTokenResponse tokenResponse =
                        new GoogleAuthorizationCodeTokenRequest(
                                httpTransport,
                                jsonFactory,
                                clientId,
                                clientSecret,
                                params[0],
                                "").execute();

                GoogleCredential credential = new GoogleCredential.Builder()
                        .setTransport(httpTransport)
                        .setJsonFactory(jsonFactory)
                        .setClientSecrets(clientId, clientSecret)
                        .build()
                        .setFromTokenResponse(tokenResponse);

                peopleService =
                        new PeopleService.Builder(httpTransport, jsonFactory, credential).build();

                ListConnectionsResponse response = peopleService.people().connections().list("people/me")
                        .setPersonFields("names,emailAddresses")
                        .execute();
                List<Person> connections = response.getConnections();

                for (Person person : connections) {
                    if (!person.isEmpty()) {
                        List<EmailAddress> emailAddresses = person.getEmailAddresses();
                        List<Name> names = person.getNames();

                        if (emailAddresses != null)
                            for (EmailAddress emailAddress : emailAddresses)
                                Log.d("", "email: " + emailAddress.getValue());
                        Log.d("", "names: " + names.get(0).getDisplayName());

                        emailList.put(emailAddresses.get(0).getValue(), names.get(0).getDisplayName());
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return emailList;
        }

        @Override
        protected void onPostExecute(HashMap<String, String> emailList) {
            super.onPostExecute(emailList);
            Intent output = new Intent();
            output.putExtra(EXTRA_MYEMAIL, myEmail);
            output.putExtra(EXTRA_EMAILLIST, emailList);
            setResult(RESULT_OK, output);
            finish();
        }

        void getMyInfo() {
            try {
                Person profile = peopleService.people().get("people/me")
                        .setPersonFields("names,emailAddresses")
                        .execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
//cs110team4@gmail.com ___ cse110team4