package com.example.krishakai;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

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

public class InventoryActivity extends AppCompatActivity {
    private static final String TAG = "InventoryActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_inventory_activity);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button additemsbutton = findViewById(R.id.additemsbutton);
        Button sellitemsbutton = findViewById(R.id.sellitemsbutton);

        additemsbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(InventoryActivity.this, AddInventory.class);
                startActivity(intent);
            }
        });
        sellitemsbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(InventoryActivity.this, SellInventory.class);
                startActivity(intent);
            }
        });

        displayInventory("Apples", R.id.appleitems, R.id.appleearn, R.id.applespl);
        displayInventory("Bananas", R.id.bananaitems,  R.id.bananaearn, R.id.bananaspl);
        displayInventory("Grapes", R.id.grapesitems,  R.id.grapesearn, R.id.grapespl);
        displayInventory("Tomatoes", R.id.tomatoitems,  R.id.tomatoearn, R.id.tomatoespl);
    }

    private void displayInventory(String itemName, int textViewId, int priceViewId, int costViewId) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference itemRef = database.getReference(itemName+"/items");
        DatabaseReference priceRef = database.getReference(itemName+"/price");
        DatabaseReference costRef = database.getReference(itemName+"/cost");
        TextView itemTextView = findViewById(textViewId);
        TextView priceTextView = findViewById(priceViewId);
        TextView costTextView = findViewById(costViewId);

        itemRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Double value = dataSnapshot.getValue(Double.class);
                Log.d(TAG, itemName + " inventory: " + value);
                itemTextView.setText("Items remaining: " + value + " kg");
                priceRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Double price = dataSnapshot.getValue(Double.class);
                        Double totalprice = price*value;
                        priceTextView.setText("Estimated earnings: " + totalprice + " Rs");
                        costRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                Double cost = dataSnapshot.getValue(Double.class);
                                Double rem = totalprice - cost;
                                if(rem<0){
                                    rem = cost - totalprice;
                                    costTextView.setText("Estimated Loss: " + rem + " Rs");
                                    costTextView.setTextColor(Color.RED);
                                } else {
                                    costTextView.setText("Estimated Profit: " + rem + " Rs");
                                    costTextView.setTextColor(Color.parseColor("#006400"));
                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                // Failed to read value
                                Log.w(TAG, "Failed to read value for " + itemName, error.toException());
                            }
                        });

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Failed to read value
                        Log.w(TAG, "Failed to read value for " + itemName, error.toException());
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value for " + itemName, error.toException());
            }
        });
    }
}