package com.example.adminpucitapp.faculty;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.adminpucitapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;

public class UpdateTeacher extends AppCompatActivity {

    private ImageView updateTeacherImg;
    private EditText updateTeacherName, updateTeacherEmail, updateTeacherPost;
    private Button updateTeacherBtn, deleteTeacherBtn;
    private final int REQ=1;
    private Bitmap bitmap=null;
    private ProgressDialog pd;

    private StorageReference storageReference;
    private DatabaseReference databaseReference;

    private String name,email,image,post, downloadUrl, uniqueKey, category;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_teacher);

        name=getIntent().getStringExtra("name");
        email=getIntent().getStringExtra("email");
        post=getIntent().getStringExtra("post");
        image=getIntent().getStringExtra("image");

        pd= new ProgressDialog(this);

        uniqueKey =getIntent().getStringExtra("key");
        category = getIntent().getStringExtra("category");

        updateTeacherImg=findViewById(R.id.updateTeacherImg);
        updateTeacherName=findViewById(R.id.updateTeacherName);
        updateTeacherEmail=findViewById(R.id.updateTeacherEmail);
        updateTeacherPost=findViewById(R.id.updateTeacherPost);
        updateTeacherBtn=findViewById(R.id.updateTeacherBtn);
        deleteTeacherBtn=findViewById(R.id.deleteTeacherBtn);

        databaseReference = FirebaseDatabase.getInstance().getReference().child("teacher");
        storageReference = FirebaseStorage.getInstance().getReference();


        try {
            Picasso.get().load(image).into(updateTeacherImg);
        } catch (Exception e) {
            e.printStackTrace();
        }

        updateTeacherEmail.setText(email);
        updateTeacherName.setText(name);
        updateTeacherPost.setText(post);

        updateTeacherImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        updateTeacherBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                name=updateTeacherName.getText().toString();
                email=updateTeacherEmail.getText().toString();
                post=updateTeacherPost.getText().toString();

                checkValidation();
            }
        });

        deleteTeacherBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteData();
            }
        });

    }

    private void deleteData() {
        databaseReference.child(category).child(uniqueKey).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(UpdateTeacher.this, "Teacher deleted successfully", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(UpdateTeacher.this,UpdateFaculty.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(UpdateTeacher.this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkValidation() {
        if(name.isEmpty()){
            updateTeacherName.setError("Empty");
            updateTeacherName.requestFocus();
        }
        if(email.isEmpty()){
            updateTeacherEmail.setError("Empty");
            updateTeacherEmail.requestFocus();
        }
        if(post.isEmpty()){
            updateTeacherPost.setError("Empty");
            updateTeacherPost.requestFocus();
        }else if(bitmap==null){
            pd.setMessage("Uploading...");
            pd.show();
            updateData(image);
        }
        else{
            pd.setMessage("Uploading...");
            pd.show();
            uploadImage();
        }

    }

    private void uploadImage() {
        //compressing image before upload
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50,baos);
        byte[] finalimg = baos.toByteArray();
        final StorageReference filePath;
        filePath = storageReference.child(finalimg+"jpg");
        final UploadTask uploadTask= filePath.putBytes(finalimg);
        uploadTask.addOnCompleteListener(UpdateTeacher.this, new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if(task.isSuccessful()){
                    uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    downloadUrl = String.valueOf(uri);
                                    updateData(downloadUrl);
                                }
                            });
                        }
                    });
                } else{
                    pd.dismiss();
                    Toast.makeText(UpdateTeacher.this,"Something went wrong",Toast.LENGTH_SHORT);
                }
            }
        });

    }


    private void updateData(String s) {

        HashMap hm=new HashMap();
        hm.put("name",name);
        hm.put("post",post);
        hm.put("email",email);
        hm.put("image",s);

        databaseReference.child(category).child(uniqueKey).updateChildren(hm).addOnSuccessListener(new OnSuccessListener() {
            @Override
            public void onSuccess(Object o) {
                pd.dismiss();

                Toast.makeText(UpdateTeacher.this, "Teacher updated successfully", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(UpdateTeacher.this,UpdateFaculty.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(UpdateTeacher.this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openGallery() {
        Intent pickImage = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickImage,REQ);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Uri uri= data.getData();
        try {
            bitmap=MediaStore.Images.Media.getBitmap(getContentResolver(),uri);
        } catch (IOException e) {
            e.printStackTrace();
        }
        updateTeacherImg.setImageBitmap(bitmap);
    }
}