package trainedge.demotraining.activity;

import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import trainedge.demotraining.R;
import trainedge.demotraining.adapter.UserAdapter;
import trainedge.demotraining.model.User;

public class AddContactsActivity extends BasicActivity {

    private RecyclerView rvUser;
    private EditText etSearchTerm;
    private String searchTerm;
    private FirebaseUser currentUser;
    private boolean isLoaded = false;
    private ImageView ivSearch;
    private TextView tv_name;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contacts);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final List<User> data = new ArrayList<>();
        rvUser = (RecyclerView) findViewById(R.id.rvUser);
        tv_name = (TextView) findViewById(R.id.tv_name);
        etSearchTerm = (EditText) findViewById(R.id.etSearchTerm);
        ivSearch = (ImageView) findViewById(R.id.ivSearch);
        rvUser.setLayoutManager(new LinearLayoutManager(this));
        RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();
        itemAnimator.setAddDuration(1000);
        itemAnimator.setRemoveDuration(1000);
        rvUser.setItemAnimator(itemAnimator);
        final List<String> loadMyContacts = new ArrayList<>();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        final DatabaseReference usersDb = FirebaseDatabase.getInstance().getReference("Users");
        showProgressDialog("loading...");
        DatabaseReference myContactsDb = FirebaseDatabase.getInstance().getReference(currentUser.getUid()).child("Contacts");
        myContactsDb.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                loadMyContacts.clear();
                if (dataSnapshot.getChildrenCount() >0) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        loadMyContacts.add(snapshot.getKey());

                    }
                    hideProgressDialog();
                    isLoaded = true;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                if (databaseError != null) {
                    Toast.makeText(context, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        ivSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchTerm = etSearchTerm.getText().toString().trim();

                if (isLoaded) {
                    showProgressDialog("Finding...");
                    usersDb.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            int pos = 0;
                            data.clear();
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                boolean isPresent = false;
                                String email = snapshot.child("email").getValue(String.class);
                                String name = snapshot.child("name").getValue(String.class);
                                String photo = snapshot.child("photo").getValue(String.class);
                                String id = snapshot.getKey();
                                for (int i = 0; i < loadMyContacts.size(); i++) {
                                    if (loadMyContacts.get(i).equals(id)) {
                                        isPresent = true;
                                    }
                                }
                                if (email != null && email.contains(searchTerm) && !email.equals(currentUser.getEmail()) && !isPresent) {
                                    //add to arraylist
                                    //uAdapter.insert(pos, new User(name, email, id, photo));

                                }
                                pos++;
                            }
                            if (data.size() == 0) {
                                data.clear();
                               // uAdapter.notifyDataSetChanged();
                            }
                            hideProgressDialog();
                            //pass this to adapter
                            //pass adapter to recyclerview
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            hideProgressDialog();

                        }
                    });

                } else {
                    Toast.makeText(context, "data is still loading", Toast.LENGTH_SHORT).show();
                }

            }
        });


    }


}
