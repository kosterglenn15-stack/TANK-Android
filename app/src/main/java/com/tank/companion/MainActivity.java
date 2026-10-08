package com.tank.companion;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity {
    private static final String RELEASES_API =
            "https://api.github.com/repos/kosterglenn15-stack/TANK-Android/releases?per_page=20";

    private EditText wordleInput;
    private EditText dateInput;
    private TextView outputText;
    private TextView updateStatus;
    private Button updateButton;

    private String updateUrl = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        wordleInput = findViewById(R.id.wordleInput);
        dateInput = findViewById(R.id.dateInput);
        outputText = findViewById(R.id.outputText);
        updateStatus = findViewById(R.id.updateStatus);
        updateButton = findViewById(R.id.updateButton);

        dateInput.setText(new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date()));

        Button calculateButton = findViewById(R.id.calculateButton);
        calculateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                runTankSequence();
            }
        });

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (updateUrl != null) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(updateUrl));
                    startActivity(intent);
                }
            }
        });

        checkForUpdates();
    }

    private void runTankSequence() {
        String wordle = wordleInput.getText().toString().trim();
        String date = dateInput.getText().toString().trim();

        try {
            TankCore.calculate(wordle, date);

            outputText.setText(
                    "T.A.N.K. sequence completed.\n\n" +
                    "Verification complete."
            );
        } catch (Exception ex) {
            String message = ex.getMessage();
            if (message == null || message.trim().isEmpty()) {
                message = "Invalid input.";
            }
            outputText.setText("INPUT ERROR\n" + message);
        }
    }

    private void checkForUpdates() {
        updateStatus.setText("Checking for updates...");
        updateButton.setVisibility(View.GONE);

        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;

                try {
                    URL url = new URL(RELEASES_API);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setConnectTimeout(8000);
                    connection.setReadTimeout(8000);
                    connection.setRequestProperty("Accept", "application/vnd.github+json");
                    connection.setRequestProperty("User-Agent", "TANK-Companion");

                    int status = connection.getResponseCode();

                    if (status != 200) {
                        setUpdateStatus("Update check unavailable.", null);
                        return;
                    }

                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(connection.getInputStream())
                    );

                    StringBuilder body = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        body.append(line);
                    }

                    reader.close();

                    JSONArray releases = new JSONArray(body.toString());

                    int currentVersion = parseVersion(BuildConfig.VERSION_NAME);
                    int bestVersion = currentVersion;
                    String bestUrl = null;
                    String bestName = null;

                    for (int i = 0; i < releases.length(); i++) {
                        JSONObject release = releases.getJSONObject(i);

                        if (release.optBoolean("draft", false) ||
                                release.optBoolean("prerelease", false)) {
                            continue;
                        }

                        String tag = release.optString("tag_name", "");
                        int candidateVersion = parseVersion(tag);

                        if (candidateVersion <= bestVersion) {
                            continue;
                        }

                        String candidateUrl = release.optString("html_url", "");

                        JSONArray assets = release.optJSONArray("assets");

                        if (assets != null) {
                            for (int a = 0; a < assets.length(); a++) {
                                JSONObject asset = assets.getJSONObject(a);
                                String assetName = asset.optString("name", "");

                                if ("app-release.apk".equalsIgnoreCase(assetName) ||
                                        assetName.toLowerCase(Locale.US).endsWith(".apk")) {

                                    String direct = asset.optString("browser_download_url", "");

                                    if (!direct.isEmpty()) {
                                        candidateUrl = direct;
                                        break;
                                    }
                                }
                            }
                        }

                        bestVersion = candidateVersion;
                        bestUrl = candidateUrl;
                        bestName = tag;
                    }

                    if (bestUrl != null) {
                        setUpdateStatus("Update available: " + bestName, bestUrl);
                    } else {
                        setUpdateStatus("T.A.N.K. Companion is up to date.", null);
                    }

                } catch (Exception ex) {
                    setUpdateStatus("Update check unavailable.", null);
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
            }
        }).start();
    }

    private void setUpdateStatus(final String message, final String url) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                updateStatus.setText(message);
                updateUrl = url;

                if (url != null && !url.isEmpty()) {
                    updateButton.setVisibility(View.VISIBLE);
                } else {
                    updateButton.setVisibility(View.GONE);
                }
            }
        });
    }

    private int parseVersion(String value) {
        if (value == null) return -1;

        String cleaned = value.trim().toLowerCase(Locale.US);

        if (cleaned.startsWith("v")) {
            cleaned = cleaned.substring(1);
        }

        String[] parts = cleaned.split("\\.");

        if (parts.length < 2 || parts.length > 3) {
            return -1;
        }

        try {
            int major = Integer.parseInt(parts[0]);
            int minor = Integer.parseInt(parts[1]);
            int patch = parts.length == 3 ? Integer.parseInt(parts[2]) : 0;

            return major * 1000000 + minor * 1000 + patch;

        } catch (NumberFormatException ex) {
            return -1;
        }
    }
}
