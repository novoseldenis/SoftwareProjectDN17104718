package com.novoseltech.handymano.views.professional.project;

/**
 @author Denis Novosel
 @student_id 17104718
 @email x17104718@student.ncirl.ie
 @github https://github.com/adminnovoseltech/SoftwareProjectDN17104718
 @class ViewProjectActivity.java
 **/

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.novoseltech.handymano.R;
import com.novoseltech.handymano.adapter.SliderAdapter;
import com.novoseltech.handymano.model.SliderItem;
import com.novoseltech.handymano.views.professional.HomeActivityProfessional;
import com.smarteist.autoimageslider.IndicatorView.animation.type.IndicatorAnimationType;
import com.smarteist.autoimageslider.IndicatorView.draw.controller.DrawController;
import com.smarteist.autoimageslider.SliderAnimations;
import com.smarteist.autoimageslider.SliderView;

public class ViewProjectActivity extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener {

    //Layout components
    private ConstraintLayout cl_projectView;
    private TextView tv_projectTitle;
    private TextView tv_projectDescription;
    private ScrollView svProject;
    private CardView cv_carousel_project;
    private ImageView iv_projectMore;
    private SliderView sliderView;

    //Firebase components
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore fStore = FirebaseFirestore.getInstance();
    private FirebaseUser user = mAuth.getCurrentUser();

    //Objects
    private static final String TAG = ViewProjectActivity.class.getSimpleName();
    private static final int REQUEST_STORAGE_PERMISSION_CODE = 1000;
    private String PROJECT_ID;
    private String projectCreationDate = "";
    private long imageCount = 0;
    private SliderAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_professional_project);

        PROJECT_ID = getIntent().getStringExtra("PROJECT_ID");
        cl_projectView = findViewById(R.id.cl_ProjectViewProfessional);

        sliderView = findViewById(R.id.imageSliderProject);
        adapter = new SliderAdapter(getApplicationContext());
        sliderView.setSliderAdapter(adapter);

        iv_projectMore = findViewById(R.id.iv_proProjectMore);

        //Layout objects
        tv_projectTitle = findViewById(R.id.tv_projectTitle);
        tv_projectDescription = findViewById(R.id.tv_projectDescription);
        tv_projectTitle.setText(PROJECT_ID);

        svProject = findViewById(R.id.svProject);
        cv_carousel_project = findViewById(R.id.cv_carousel_project);

        DocumentReference documentReference = fStore.collection("user")
                .document(user.getUid())
                .collection("projects")
                .document(PROJECT_ID);

        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot documentSnapshot = task.getResult();
                    tv_projectDescription.setText(documentSnapshot.getString("description"));
                    projectCreationDate = documentSnapshot.getString("creation_date");
                    imageCount = documentSnapshot.getLong("imageCount");

                }
            }
        });

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                addImagesToSlider(sliderView);

            }
        }, 300);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                sliderView.setIndicatorAnimation(IndicatorAnimationType.WORM); //set indicator animation by using IndicatorAnimationType. :WORM or THIN_WORM or COLOR or DROP or FILL or NONE or SCALE or SCALE_DOWN or SLIDE and SWAP!!
                sliderView.setSliderTransformAnimation(SliderAnimations.SIMPLETRANSFORMATION);
                sliderView.setAutoCycleDirection(SliderView.AUTO_CYCLE_DIRECTION_BACK_AND_FORTH);
                sliderView.setIndicatorSelectedColor(Color.WHITE);
                sliderView.setIndicatorUnselectedColor(Color.GRAY);
                sliderView.setScrollTimeInSec(4); //set scroll delay in seconds :
                sliderView.startAutoCycle();

                sliderView.setOnIndicatorClickListener(new DrawController.ClickListener() {
                    @Override
                    public void onIndicatorClicked(int position) {
                        Log.i("GGG", "onIndicatorClicked: " + sliderView.getCurrentPagePosition());
                    }
                });
            }
        }, 600);
    }

    public void addImagesToSlider(View view){
        StorageReference storageReference = FirebaseStorage.getInstance().getReference()
                .child("images")
                .child(user.getUid())
                .child("projects")
                .child(PROJECT_ID);

        for(int l = 0; l < imageCount; l++){
            SliderItem sliderItem = new SliderItem();
            StorageReference sr = null;
            sr = storageReference.child(projectCreationDate + "_image_" + l + ".jpeg");
            sr.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if(task.isSuccessful()){
                        Log.d(TAG, task.getResult().toString());
                        sliderItem.setImageUrl(task.getResult().toString());
                        adapter.addItem(sliderItem);
                    }else{
                        Log.e("Error loading images", task.getException().getLocalizedMessage());
                    }
                }
            });

        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.option_edit:

                if(getApplicationContext().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || getApplicationContext().
                        checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_STORAGE_PERMISSION_CODE);
                }else{
                    Intent intent = new Intent(ViewProjectActivity.this, EditProject.class);
                    intent.putExtra("PROJECT_ID", PROJECT_ID);
                    startActivity(intent);
                }



                return true;
            case R.id.option_delete:
                AlertDialog.Builder deleteJobDialog = new AlertDialog.Builder(ViewProjectActivity.this);
                deleteJobDialog.setTitle("Delete project")
                        .setMessage("You are about to delete the project. Continue?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                StorageReference storageReference = FirebaseStorage.getInstance().getReference()
                                        .child("images")
                                        .child(user.getUid())
                                        .child("projects")
                                        .child(PROJECT_ID);

                                for (int l = 0; l < imageCount; l++) {
                                    StorageReference sr = null;
                                    sr = storageReference.child(projectCreationDate + "_image_" + l + ".jpeg");
                                    int j = l;
                                    sr.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Log.d(TAG, "Deleted image " + (j + 1));
                                            } else {
                                                Log.e(TAG, task.getException().getLocalizedMessage());
                                            }
                                        }
                                    });

                                }

                                fStore.collection("user")
                                        .document(user.getUid())
                                        .collection("projects")
                                        .document(PROJECT_ID)
                                        .delete();

                                Intent intent = new Intent(ViewProjectActivity.this, ProjectsActivity.class);
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();

                return true;
            default:
                return false;
        }
    }

    public void ClickMenuProject(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.setOnMenuItemClickListener(this);
        MenuInflater inflater = popupMenu.getMenuInflater();
        inflater.inflate(R.menu.actions, popupMenu.getMenu());
        popupMenu.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(ViewProjectActivity.this, EditProject.class);
            intent.putExtra("PROJECT_ID", PROJECT_ID);
            startActivity(intent);
        } else {
            //Show error message that prevents that informs them about permission
            Toast.makeText(getApplicationContext(), "Storage permission is denied. Please allow it in the Settings to enable this functionality.", Toast.LENGTH_SHORT).show();

        }
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }
}