package com.example.locationalert.activity;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.locationalert.App;
import com.example.locationalert.R;
import com.example.locationalert.utils.StringUtils;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class UserActivity extends AppCompatActivity {

    @BindView(R.id.username_edit_text)
    EditText usernameEditText;

    @BindView(R.id.email_edit_text)
    EditText emailEditText;

    @BindView(R.id.password_edit_text)
    EditText passwordEditText;

    @BindView(R.id.login_button)
    Button loginButton;

    @BindView(R.id.register_button)
    Button registerButton;

    @BindView(R.id.google_sign_in_button)
    ViewGroup googleSignInButton;

    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    private GoogleApiClient googleApiClient;

    private static final int GOOGLE_REQUEST_CODE = 5;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        ButterKnife.bind(this);
        setupGoogleClient();

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginUser();
            }
        });
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createUser();
            }
        });
        googleSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signInWithGoogle();
            }
        });

        checkIfUsernameAlreadyExists();
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (googleApiClient != null && !googleApiClient.isConnected() && !googleApiClient.isConnecting()) {
            googleApiClient.connect();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (googleApiClient != null && (googleApiClient.isConnecting() || googleApiClient.isConnecting())) {
            googleApiClient.disconnect();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GOOGLE_REQUEST_CODE && resultCode == RESULT_OK) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);

            if (result != null && result.isSuccess() && result.getSignInAccount() != null) {
                String token = result.getSignInAccount().getIdToken();

                AuthCredential credential = GoogleAuthProvider.getCredential(token, null);
                firebaseAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isComplete() && task.isSuccessful()) {
                            showMap();
                        } else {
                            showGoogleSignInError();
                        }
                    }
                });
            }
        }
    }

    private void showGoogleSignInError() {
        Toast.makeText(this, "Greska u prijavljivanju.", Toast.LENGTH_SHORT).show();
    }

    private void setupGoogleClient() {
        GoogleSignInOptions options = new GoogleSignInOptions.Builder()
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, options)
                .build();
    }

    private void signInWithGoogle() {
        if (googleApiClient != null) {
            startActivityForResult(Auth.GoogleSignInApi.getSignInIntent(googleApiClient), GOOGLE_REQUEST_CODE);
        }
    }

    private void loginUser() {
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        if (!StringUtils.isEmail(email) && !StringUtils.isValidPassword(password)) {
            checkEmail(email);
            checkPassword(password);
        } else {
            firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isComplete() && task.isSuccessful()) {
                        showMap();
                    } else {
                        showUserError();
                    }
                }
            });
        }
    }

    private void showUserError() {
        Toast.makeText(this, "Ne postoji korisnik s tim e-mailom.", Toast.LENGTH_SHORT).show();
    }

    private void createUser() {
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        if (!StringUtils.isEmail(email) && !StringUtils.isValidPassword(password)) {
            checkEmail(email);
            checkPassword(password);
        } else {
            firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isComplete() && task.isSuccessful()) {
                        showMap();
                    } else {
                        showRegisterError();
                    }
                }
            });
        }
    }

    private void showRegisterError() {
        Toast.makeText(this, "Korisnik s tim e-mailom vec postoji.", Toast.LENGTH_SHORT).show();
    }

    private void checkPassword(String password) {
        if (StringUtils.isEmpty(password)) {
            passwordEditText.setError(getString(R.string.password_empty_error));
        } else if (!StringUtils.isValidPassword(password)) {
            passwordEditText.setError(getString(R.string.password_length_error));
        } else {
            passwordEditText.setError(null);
        }
    }

    private void checkEmail(String email) {
        if (StringUtils.isEmpty(email)) {
            emailEditText.setError(getString(R.string.email_empty_error));
        } else if (StringUtils.isEmail(email)) {
            emailEditText.setError(getString(R.string.email_pattern_error));
        } else {
            emailEditText.setError(null);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 50 && grantResults[0] == PERMISSION_GRANTED) {
            showMap();
        }
    }

    private void checkIfUsernameAlreadyExists() {
        if (firebaseAuth.getCurrentUser() != null) {
            showMap();
        }
    }

    private void showMap() {
        String username = usernameEditText.getText().toString();

        if (!StringUtils.isEmpty(username)) {
            SharedPreferences preferences = App.getPreferences();
            preferences.edit().putString(getString(R.string.username_key), username).apply();
        }

        int fineLocation = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        int coarseLocation = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);

        if (fineLocation != PERMISSION_GRANTED && coarseLocation != PERMISSION_GRANTED) {
            String[] permissions = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};

            ActivityCompat.requestPermissions(this, permissions, 50);
        } else {
            startActivity(new Intent(this, MapActivity.class));
        }
    }
}
