package com.example.krishakai;

import android.content.Intent;
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

public class SellInventory extends AppCompatActivity {

    private static final String TAG = "SellInventory";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sell_inventory);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        AutoCompleteTextView item = findViewById(R.id.itemsInp);
        initializeAutoCompleteTextView(item, R.array.crop_array);
        EditText quantity = findViewById(R.id.qtyInp);
        EditText sell = findViewById(R.id.sellInp);
        Button sellbtn = findViewById(R.id.sellBtn);
        TextView chkinv = findViewById(R.id.chkinv2);
        TextView qtychk = findViewById(R.id.itemsAlert);

        chkinv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Navigate to SignupActivity when TextView is clicked
                Intent intent = new Intent(SellInventory.this, InventoryActivity.class);
                startActivity(intent);
            }
        });

        sellbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userItem = item.getText().toString().trim();
                String userQuantity = quantity.getText().toString().trim();
                String userSell = sell.getText().toString().trim();

                if (userItem.isEmpty() || userQuantity.isEmpty() || userSell.isEmpty()) {
                    Toast.makeText(SellInventory.this, "Please enter all fields", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(SellInventory.this, "Processing", Toast.LENGTH_SHORT).show();
                    sellInv(userItem,userQuantity,userSell);
                    // Clear input fields
                    item.setText("");
                    quantity.setText("");
                    sell.setText("");
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

    private void sellInv(String userItem, String userQuantity, String userSell) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference costRef = database.getReference("TotalCost");
        DatabaseReference tinvRef = database.getReference("TotalInventoryItems");
        DatabaseReference tinvPriceRef = database.getReference("TotalInventoryPrice");
        DatabaseReference tProfitRef = database.getReference("TotalProfit");
        DatabaseReference tLossRef = database.getReference("TotalLoss");
        DatabaseReference itemRef = database.getReference(userItem + "/items");
        DatabaseReference itempriceRef = database.getReference(userItem + "/price");
        DatabaseReference itemcostRef = database.getReference(userItem + "/cost");
        Double userQty = Double.parseDouble(userQuantity);
        Double userS = Double.parseDouble(userSell);
        Double avgSell = userS/userQty;

        itemRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Double value = dataSnapshot.getValue(Double.class);
                Log.d(TAG, userItem + " inventory: " + value);
                itemcostRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Double cost = dataSnapshot.getValue(Double.class);
                        Double avgcost = cost/value;
                        if (userQty>value) {
                            Toast.makeText(SellInventory.this, "Operation Unsuccessful! Quantity exceeds items in inventory", Toast.LENGTH_SHORT).show();
                        } else {
                            Double tvalue = value-userQty;
                            Double tavgcost = avgcost*userQty;
                            itemRef.setValue(tvalue);
                            itemcostRef.setValue(cost-tavgcost);
                            tinvRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    Double value = dataSnapshot.getValue(Double.class);
                                    Double tvalue = value-userQty;
                                    tinvRef.setValue(tvalue);
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Log.w(TAG, "Failed to read value for " + userItem, error.toException());
                                }
                            });
                            costRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    Double tcost = dataSnapshot.getValue(Double.class);
                                    costRef.setValue(tcost-tavgcost);
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
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
                                            tinvPriceRef.setValue(tprice-totalprice);
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            Log.w(TAG, "Failed to read value for " + userItem, error.toException());
                                        }
                                    });

                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Log.w(TAG, "Failed to read value for " + userItem, error.toException());
                                }
                            });

                            if(avgSell>avgcost){
                                tProfitRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        Double tprof = dataSnapshot.getValue(Double.class);
                                        tProfitRef.setValue(tprof+userS-tavgcost);
                                    }
                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        Log.w(TAG, "Failed to read value for " + userItem, error.toException());
                                    }
                                });
                            } else {
                                tLossRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        Double tloss = dataSnapshot.getValue(Double.class);
                                        tLossRef.setValue(tloss-userS+tavgcost);
                                    }
                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        Log.w(TAG, "Failed to read value for " + userItem, error.toException());
                                    }
                                });
                            }
                            Toast.makeText(SellInventory.this, "Operation Successfull!!", Toast.LENGTH_SHORT).show();
                        }
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
                Log.w(TAG, "Failed to read value for " + userItem, error.toException());
            }
        });
    }
}