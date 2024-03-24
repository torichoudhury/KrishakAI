package com.example.krishakai;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AddInventory extends AppCompatActivity {

    private static final String TAG = "AddInventory";

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
}