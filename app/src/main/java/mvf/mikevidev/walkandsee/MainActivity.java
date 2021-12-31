package mvf.mikevidev.walkandsee;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import mvf.mikevidev.walkandsee.UtilitiesWalkAndSee.Utilities;
import mvf.mikevidev.walkandsee.viewmodels.SearchPlacesActivity;

public class MainActivity extends AppCompatActivity {

    public FirebaseAuth firebaseAuth;
    public static String strUserEmail;
    public static String strUserPass;

    public void doLogin(View view)
    {
        EditText user = findViewById(R.id.etUsernameEmail);
        EditText pass = findViewById(R.id.etPassword);
        final String strUser = user.getText().toString();
        final String strPass = pass.getText().toString();

        if(Utilities.isBlank(strUser) || Utilities.isBlank(strPass))
        {
            Utilities.toastMessage("User and password are required",getApplicationContext());
        }
        else
        {
            firebaseAuth = FirebaseAuth.getInstance();
            if(firebaseAuth.getCurrentUser() == null)
            {
                firebaseAuth.createUserWithEmailAndPassword(strUser, strPass)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d("TAG", "createUserWithEmail:success");
                                FirebaseDatabase.getInstance().getReference().child("users").child(task.getResult().getUser().getUid())
                                                                    .child("email").setValue(strUser);
                                FirebaseDatabase.getInstance().getReference().child("users").child(task.getResult().getUser().getUid())
                                                                    .child("password").setValue(strPass);
                                strUserEmail = strUser;
                                strUserPass = strPass;
                                goToStartMenu();
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w("TAG", "createUserWithEmail:failure", task.getException());
                                Utilities.toastMessage("Connection failed",getApplicationContext());
                            }

                            // ...
                        }
                    });
            }
            else
            {
                firebaseAuth.signInWithEmailAndPassword(strUser, strPass)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Log.d("TAG", "signInWithEmail:success");
                                    FirebaseUser user = firebaseAuth.getCurrentUser();
                                    Log.e("TAG_SIGNIN", "User logged: " + user.getDisplayName());
                                    strUserEmail = strUser;
                                    strUserPass = strPass;
                                    goToStartMenu();
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.w("TAG", "signInWithEmail:failure", task.getException());
                                    Utilities.toastMessage("The user or password is not valid",getApplicationContext());
                                    // ...
                                }

                                // ...
                            }
                        });
            }

        }

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Only for test
        EditText user = findViewById(R.id.etUsernameEmail);
        EditText pass = findViewById(R.id.etPassword);
        user.setText("test@test.es");
        pass.setText("123456");
        doLogin(null);
    }

    public void goToStartMenu()
    {
        Intent intent = new Intent(getApplicationContext(), SearchPlacesActivity.class);
        startActivity(intent);
    }
}