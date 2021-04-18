package com.novoseltech.handymano.views.message;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.SetOptions;
import com.novoseltech.handymano.R;
import com.novoseltech.handymano.adapter.ChatAdapter;
import com.novoseltech.handymano.model.ChatModel;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    RecyclerView rv_chatContent;
    LinearLayoutManager linearLayoutManager;

    EditText et_chatMessage;
    ChatAdapter chatAdapter;

    String SENDER_NAME = "";
    String RECIPIENT_ID = "";
    String TRADE_NAME = "";
    String MODE = "";

    List<String> messageRecipientsSender = new ArrayList<>();
    List<String> messageRecipientsReceiver = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        MODE = getIntent().getStringExtra("MODE");

        FirebaseFirestore fStore = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String UID = user.getUid();

        et_chatMessage = findViewById(R.id.et_chatMessage);

        if(MODE.equals("PROFILE_VISIT")){
            RECIPIENT_ID = getIntent().getStringExtra("TRADE_ID");
            TRADE_NAME = getIntent().getStringExtra("TRADE_NAME");

        }else{
            RECIPIENT_ID = getIntent().getStringExtra("USER_ID");
        }


        CollectionReference chatReference = fStore.collection("chat").document(UID).collection(RECIPIENT_ID);
        CollectionReference receiverReference = fStore.collection("chat").document(RECIPIENT_ID).collection(UID);



        //CHECK IF RECIPIENTS CHAT DOCUMENT CONTAINS LIST OF CHATS
        fStore.collection("chat").document(RECIPIENT_ID)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){

                    if(task.getResult().contains("recipients")){
                        DocumentSnapshot documentSnapshot = task.getResult();
                        messageRecipientsReceiver = (List<String>) documentSnapshot.get("recipients");
                    }else{
                        messageRecipientsReceiver.add("");
                    }

                }
            }
        });

        //CHECK IF MY CHAT DOCUMENT CONTAINS LIST OF CHATS
        fStore.collection("chat").document(UID)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){

                    if(task.getResult().contains("recipients")){
                        DocumentSnapshot documentSnapshot = task.getResult();
                        messageRecipientsSender = (List<String>) documentSnapshot.get("recipients");
                    }else{
                        messageRecipientsSender.add("");
                    }

                }
            }
        });


        //GET MY USERNAME
        fStore.collection("user").document(UID)
        .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot documentSnapshot = task.getResult();
                    SENDER_NAME = documentSnapshot.getString("username");
                }
            }
        });



        findViewById(R.id.ic_sendMessage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChatModel chat = new ChatModel(user.getUid(),SENDER_NAME, et_chatMessage.getText().toString(), new Date());

                receiverReference.add(chat);
                chatReference.add(chat);
                et_chatMessage.setText("");

                Log.d("LOG TESTING: ", RECIPIENT_ID+","+TRADE_NAME);


                if(!messageRecipientsReceiver.contains(UID + "," + SENDER_NAME)){



                    if(messageRecipientsReceiver.get(0).equals("")){
                        messageRecipientsReceiver.clear();
                        messageRecipientsReceiver.add(UID + "," + SENDER_NAME);
                    }else{
                        messageRecipientsReceiver.add(UID + "," + SENDER_NAME);
                    }
                    fStore.collection("chat").document(RECIPIENT_ID)
                            .update("recipients", messageRecipientsReceiver)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Log.d("LOG: ", "Updated recipients");
                                }
                            });



                }

                if(!messageRecipientsSender.contains(RECIPIENT_ID + "," + TRADE_NAME)){
                    if(messageRecipientsSender.get(0).equals("")){
                        messageRecipientsSender.add(RECIPIENT_ID+','+TRADE_NAME);
                        messageRecipientsSender.clear();
                    }else{
                        messageRecipientsSender.add(RECIPIENT_ID+','+TRADE_NAME);
                    }

                    fStore.collection("chat").document(UID)
                            .update("recipients", messageRecipientsSender)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Log.d("LOG: ", "Updated recipients");
                                }
                            });
                }


            }
        });

        rv_chatContent = findViewById(R.id.rv_chatContent);
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        rv_chatContent.setLayoutManager(linearLayoutManager);

        Query query = FirebaseFirestore.getInstance()
                .collection("chat").document(UID).collection(RECIPIENT_ID).orderBy("timestamp", Query.Direction.ASCENDING);

        FirestoreRecyclerOptions<ChatModel> options = new FirestoreRecyclerOptions.Builder<ChatModel>().setQuery(query, ChatModel.class).build();
        chatAdapter = new ChatAdapter(options, SENDER_NAME);
        chatAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                rv_chatContent.scrollToPosition(chatAdapter.getItemCount() - 1);
            }
        });
        rv_chatContent.setAdapter(chatAdapter);



    }

    @Override
    protected void onStart() {
        super.onStart();
        chatAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        chatAdapter.stopListening();
    }
}