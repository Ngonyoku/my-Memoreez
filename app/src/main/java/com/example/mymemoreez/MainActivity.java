package com.example.mymemoreez;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    private static final int IMAGE_CHOOSER = 1;

    private EditText et_Filename;
    private Button btn_Upload;
    private CircleImageView imageView;
    private TextView tv_Show_Uploads;
    private ProgressBar progressBar;

    private Uri imageUri;

    private DatabaseReference mDatabseRef;
    private StorageReference mStorageRef;
    private StorageTask mStorageTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        et_Filename = findViewById(R.id.editText_file_name);
        btn_Upload = findViewById(R.id.button_upload);
        imageView = findViewById(R.id.image_view);
        tv_Show_Uploads = findViewById(R.id.textView_Show_Uploads);
        progressBar = findViewById(R.id.progressBar);
        mDatabseRef = FirebaseDatabase.getInstance().getReference("Uploads");
        mStorageRef = FirebaseStorage.getInstance().getReference("Uploads");

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImage();
            }
        });

        btn_Upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                uploadImageToFirerbase();
            }
        });

        tv_Show_Uploads.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openUploadImagesActivity();
            }
        });
    }

    private void showImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, IMAGE_CHOOSER);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_CHOOSER && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            Picasso.get().load(imageUri).into(imageView);
        }
    }

    private String getFileExtension(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    private void uploadImageToFirerbase() {
        if (imageUri != null) {
            StorageReference fileStorage = mStorageRef.child(System.currentTimeMillis() + "." + getFileExtension(imageUri));
            mStorageTask = fileStorage.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(MainActivity.this, "Upload Was Successful!!", Toast.LENGTH_SHORT).show();

                            Upload upload = new Upload(et_Filename.getText().toString().trim(),
                                    taskSnapshot.getMetadata().getReference().getDownloadUrl().toString());

                            String uploadId = mDatabseRef.push().getKey();
                            mDatabseRef.child(uploadId).setValue(upload);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        } else {
            Toast.makeText(this, "No File Selected", Toast.LENGTH_SHORT).show();
        }
    }

    private void openUploadImagesActivity(){
        Intent intent = new Intent(MainActivity.this,ImagesActivity.class);
        startActivity(intent);
    }
}
