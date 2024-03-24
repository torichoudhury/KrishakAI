package com.example.krishakai;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
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

public class DashboardActivity extends AppCompatActivity {
    private static final String TAG = "InventoryActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        displayDashboard();
    }

    private void displayDashboard(){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference costRef = database.getReference("TotalCost");
        DatabaseReference tinvRef = database.getReference("TotalInventoryItems");
        DatabaseReference tinvPriceRef = database.getReference("TotalInventoryPrice");
        DatabaseReference tPofitRef = database.getReference("TotalProfit");
        DatabaseReference tLossRef = database.getReference("TotalLoss");
        TextView netplText = findViewById(R.id.netplText);
        TextView netPLText = findViewById(R.id.netPLText);
        TextView netinvText = findViewById(R.id.netinvText);
        TextView inventoryText = findViewById(R.id.inventoryText);
        TextView costText = findViewById(R.id.costText);
        TextView profittext = findViewById(R.id.profittext);
        TextView losstext = findViewById(R.id.losstext);
        TextView netText = findViewById(R.id.netText);
        costRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Double value = dataSnapshot.getValue(Double.class);
                Log.d(TAG, "Cost: " + value);
                costText.setText("Total Cost: " + value + " Rs");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value for cost", error.toException());
            }
        });
        tinvRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Double value = dataSnapshot.getValue(Double.class);
                Log.d(TAG, "Inventory: " + value);
                inventoryText.setText("Total Inventory: " + value);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value for inventory", error.toException());
            }
        });
        tinvPriceRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Double value = dataSnapshot.getValue(Double.class);
                Log.d(TAG, "Assets: " + value);
                netinvText.setText(""+ value);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value for assets", error.toException());
            }
        });
        tPofitRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Double profit = dataSnapshot.getValue(Double.class);
                Log.d(TAG, "Profit: " + profit);
                profittext.setText("Profit: " + profit);
                tLossRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Double loss = dataSnapshot.getValue(Double.class);
                        Log.d(TAG, "Loss: " + loss);
                        losstext.setText("Loss:"+ loss);

                        if(profit-loss<0){
                            Double val = loss-profit;
                            netText.setText("Net Loss: " + val);
                            netplText.setText("" + val);
                            netPLText.setText("Net Loss");
                            netText.setTextColor(Color.RED);
                            netplText.setTextColor(Color.RED);
                            netPLText.setTextColor(Color.RED);
                        } else {
                            Double val = profit-loss;
                            netText.setText("Net Profit: " + val);
                            netplText.setText("" + val);
                            netPLText.setText("Net Profit");
                            netText.setTextColor(Color.parseColor("#1C9101"));
                            netplText.setTextColor(Color.parseColor("#1C9101"));
                            netPLText.setTextColor(Color.parseColor("#1C9101"));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Failed to read value
                        Log.w(TAG, "Failed to read value for Loss", error.toException());
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value for profit", error.toException());
            }
        });
    }

    public void showPopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.getMenuInflater().inflate(R.menu.user_popup_menu, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.menu_option1) {
                    startActivity(new Intent(DashboardActivity.this, AddInventory.class));
                    return true;
                } else if (item.getItemId() == R.id.menu_option2) {
                    startActivity(new Intent(DashboardActivity.this, InventoryActivity.class));
                    return true;
                } else if (item.getItemId() == R.id.menu_option3) {
                    startActivity(new Intent(DashboardActivity.this, SellInventory.class));
                    return true;
                } else if (item.getItemId() == R.id.menu_option4) {
                    startActivity(new Intent(DashboardActivity.this, KnowYourLand.class));
                    return true;
                } else if (item.getItemId() == R.id.menu_option5) {
                    startActivity(new Intent(DashboardActivity.this, WeatherActivity.class));
                    return true;
                }
                // Add more if-else statements for additional menu options
                return false;
            }
        });
        popupMenu.show();
    }
}