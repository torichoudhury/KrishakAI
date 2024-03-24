package com.example.krishakai;

import static com.example.krishakai.BuildConfig.GEMINI_KEY;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.ai.client.generativeai.type.Content;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AddInventory extends AppCompatActivity {

    private static final String TAG = "AddInventory";
    private static final int REQUEST_IMAGE_CAPTURE = 101;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_inventory);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        AutoCompleteTextView item = findViewById(R.id.itemsInput);
        initializeAutoCompleteTextView(item, R.array.crop_array);
        EditText quantity = findViewById(R.id.quantityInput);
        EditText cost = findViewById(R.id.costInput);
        Button submitbtn = findViewById(R.id.submitbtn);
        TextView chkinv = findViewById(R.id.chkinv);
        imageView = findViewById(R.id.fruitcap);


        chkinv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Navigate to SignupActivity when TextView is clicked
                Intent intent = new Intent(AddInventory.this, InventoryActivity.class);
                startActivity(intent);
            }
        });

        submitbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userItem = item.getText().toString().trim();
                String userQuantity = quantity.getText().toString().trim();
                String userCost = cost.getText().toString().trim();

                if (userItem.isEmpty() || userQuantity.isEmpty() || userCost.isEmpty()) {
                    Toast.makeText(AddInventory.this, "Please enter all fields", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(AddInventory.this, "Operation Successful!", Toast.LENGTH_SHORT).show();
                    addInv(userItem,userQuantity,userCost);
                    // Clear input fields
                    item.setText("");
                    quantity.setText("");
                    cost.setText("");
                    imageView.setImageBitmap(null);
                    imageView.setBackgroundResource(R.drawable.camera);
                    // Close the keyboard
                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        });
    }

    private void initializeAutoCompleteTextView(AutoCompleteTextView autoCompleteTextView, int arrayResource) {
        // Create an ArrayAdapter for the AutoCompleteTextView
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                arrayResource,
                android.R.layout.simple_dropdown_item_1line // Use a dropdown layout for suggestions
        );
        autoCompleteTextView.setAdapter(adapter);
    }

    private void addInv(String userItem, String userQuantity, String userCost) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference costRef = database.getReference("TotalCost");
        DatabaseReference tinvRef = database.getReference("TotalInventoryItems");
        DatabaseReference tinvPriceRef = database.getReference("TotalInventoryPrice");
        DatabaseReference itemRef = database.getReference(userItem+"/items");
        DatabaseReference itempriceRef = database.getReference(userItem+"/price");
        DatabaseReference itemcostRef = database.getReference(userItem+"/cost");
        Double userQty = Double.parseDouble(userQuantity);
        Double userC = Double.parseDouble(userCost);

        itemRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Double value = dataSnapshot.getValue(Double.class);
                Log.d(TAG, userItem + " inventory: " + value);
                Double tvalue = value+userQty;
                itemRef.setValue(tvalue);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value for " + userItem, error.toException());
            }
        });
        itempriceRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Double price = dataSnapshot.getValue(Double.class);
                Double totalprice = price*userQty;
                tinvPriceRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Double tprice = dataSnapshot.getValue(Double.class);
                        tinvPriceRef.setValue(tprice+totalprice);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Failed to read value
                        Log.w(TAG, "Failed to read value for " + userItem, error.toException());
                    }
                });

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value for " + userItem, error.toException());
            }
        });
        tinvRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Double value = dataSnapshot.getValue(Double.class);
                Double tvalue = value+userQty;
                tinvRef.setValue(tvalue);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value for " + userItem, error.toException());
            }
        });
        costRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Double value = dataSnapshot.getValue(Double.class);
                Double tvalue = value+userC;
                costRef.setValue(tvalue);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value for " + userItem, error.toException());
            }
        });
        itemcostRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Double value = dataSnapshot.getValue(Double.class);
                Double tvalue = value+userC;
                itemcostRef.setValue(tvalue);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value for " + userItem, error.toException());
            }
        });
    }

    public void onImageViewClicked(View view) {
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_IMAGE_CAPTURE);
        } else {
            dispatchTakePictureIntent();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent();
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            GenerativeModel gm = new GenerativeModel("gemini-pro-vision",GEMINI_KEY);
            GenerativeModelFutures model = GenerativeModelFutures.from(gm);

            Bundle extras = data.getExtras();
            assert extras != null;
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            imageView.setBackground(null);
            Bitmap roundedBitmap = getRoundedCornerBitmap(imageBitmap, 30); // Adjust the corner radius as needed
            imageView.setImageBitmap(roundedBitmap);

            Content content = new Content.Builder()
                    .addText("you will be given a fruit or vegetable image and identify it which fruits or vegetables. one word only and if there is no fruit or vegetable in the picture then just say 'No fruits or vegetable found' ")
                    .addImage(imageBitmap)
                    .build();

            Executor executor = Executors.newSingleThreadExecutor();
            ListenableFuture<GenerateContentResponse> response = model.generateContent(content);
            Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
                String resultText;
                @Override
                public void onSuccess(GenerateContentResponse result) {
                    resultText = result.getText();
                    assert resultText != null;
                    Log.d("YourTag", resultText);
                    AutoCompleteTextView item = findViewById(R.id.itemsInput);

                    if(resultText.toLowerCase().trim().equals("banana") || resultText.toLowerCase().trim().equals("bananas")) {
                        item.setText("Bananas");
                    } else if(resultText.toLowerCase().trim().equals("grape") || resultText.toLowerCase().trim().equals("grapes")){
                        item.setText("Grapes");
                    } else if(resultText.toLowerCase().trim().equals("tomato") || resultText.toLowerCase().trim().equals("tomatoes")){
                        item.setText("Tomatoes");
                    } else if(resultText.toLowerCase().trim().equals("apple") || resultText.toLowerCase().trim().equals("apples")){
                        item.setText("Apples");
                    } else {
                        // Handle unmatched cases (e.g., Log or display a message)
                        Log.d("YourTag", "Unmatched fruit: " + resultText);
                    }
                }


                @Override
                public void onFailure(Throwable t) {
                    t.printStackTrace();
                }
            }, executor);
        }
    }

    private Bitmap getRoundedCornerBitmap(Bitmap bitmap, int cornerRadius) {
        Bitmap roundedBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(roundedBitmap);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, paint);

        paint.setXfermode(new android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return roundedBitmap;
    }
}