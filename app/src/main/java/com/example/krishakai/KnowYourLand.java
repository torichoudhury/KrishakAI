package com.example.krishakai;

import static com.example.krishakai.BuildConfig.GEMINI_KEY;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class KnowYourLand extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_know_your_land);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        AutoCompleteTextView timeInput = findViewById(R.id.timeInput);
        AutoCompleteTextView soilInput = findViewById(R.id.soilInput);
        AutoCompleteTextView countryInput = findViewById(R.id.countryInput);
        AutoCompleteTextView seasonInput = findViewById(R.id.seasonInput);
        AutoCompleteTextView marketInput = findViewById(R.id.marketInput);
        AutoCompleteTextView waterInput = findViewById(R.id.waterInput);
        AutoCompleteTextView pestInput = findViewById(R.id.pestInput);

        initializeAutoCompleteTextView(soilInput, R.array.soil_array);
        initializeAutoCompleteTextView(timeInput, R.array.time_array);
        initializeAutoCompleteTextView(countryInput, R.array.country_array);
        initializeAutoCompleteTextView(seasonInput, R.array.season_array);
        initializeAutoCompleteTextView(marketInput, R.array.general_array);
        initializeAutoCompleteTextView(waterInput, R.array.general_array);
        initializeAutoCompleteTextView(pestInput, R.array.general_array);

        Button resbutton = findViewById(R.id.resbutton);
        EditText landarea = findViewById(R.id.landInput);

        resbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userLandArea = landarea.getText().toString().trim();
                String userSoilType = soilInput.getText().toString().trim();
                String userTimePeriod = timeInput.getText().toString().trim();
                String userCountry = countryInput.getText().toString().trim();
                String userSeasonType = seasonInput.getText().toString().trim();
                String userMarketDemand = marketInput.getText().toString().trim();
                String userWaterRequirements = waterInput.getText().toString().trim();
                String userPestResistance = pestInput.getText().toString().trim();

                // Perform basic validation (you should add more thorough validation)
                if (userLandArea.isEmpty() || userSoilType.isEmpty() || userTimePeriod.isEmpty() || userSeasonType.isEmpty() || userCountry.isEmpty() || userMarketDemand.isEmpty() || userWaterRequirements.isEmpty() || userPestResistance.isEmpty()) {
                    Toast.makeText(KnowYourLand.this, "Please enter all fields", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(KnowYourLand.this, "Operation Successful!", Toast.LENGTH_SHORT).show();
                    displayRes(userLandArea,userSoilType,userTimePeriod,userCountry,userSeasonType,userMarketDemand,userWaterRequirements,userPestResistance);
                    landarea.setText("");
                    soilInput.setText("");
                    timeInput.setText("");
                    countryInput.setText("");
                    seasonInput.setText("");
                    marketInput.setText("");
                    waterInput.setText("");
                    pestInput.setText("");
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

    private void displayRes(String userLandArea, String userSoilType, String userTimePeriod, String userCountry, String userSeasonType, String userMarketDemand, String userWaterRequirements, String userPestResistance) {
        GenerativeModel gm = new GenerativeModel("gemini-pro", GEMINI_KEY);
        GenerativeModelFutures model = GenerativeModelFutures.from(gm);

        Content content = new Content.Builder()
                .addText("Given a land area of " + userLandArea + " acres in" + userCountry + ", soil type " + userSoilType + ", time period " + userTimePeriod + ", season type " + userSeasonType + ", market demand " + userMarketDemand + ", water requirements " + userWaterRequirements + ", and pest resistance " + userPestResistance + ", recommend the most profitable crops for cultivation. Give at least 5 crops, only the names of crops required and the total price of cultivating the crops in indian Rupees.")
                .build();

        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);

        Executor executor = Executors.newSingleThreadExecutor();
        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                TextView restext = findViewById(R.id.restext);
                String resultText = result.getText();
                Log.d("YourTag", resultText);
                restext.setText(resultText);
            }

            @Override
            public void onFailure(Throwable t) {
                t.printStackTrace();
            }
        }, executor);
    }
}