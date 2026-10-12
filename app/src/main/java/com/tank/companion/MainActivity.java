package com.tank.companion;

import android.app.Activity;
import android.os.Bundle;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.InputType;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity {
    private EditText wordle, date, entry;
    private TextView output, stepLabel, status;
    private LinearLayout calculatorPanel, lockPanel, ownerPanel;
    private TankCore.Result challenge;
    private String[] expected;
    private int index = 0;
    private String lastPassword = "";
    private static final String[] LAYER_NAMES = {
        "Layer 1 - Wordle", "Layer 2 - Daily coordinate", "Layer 3 - Kalebration 3301",
        "Layer 4 - Cicada transform", "Layer 5 - Date confirmation (new checkpoint)",
        "Layer 6 - Chain checksum (new checkpoint)", "Layer 7 - Distorted coordinate",
        "Layer 8 - Waypoint token", "Layer 9 - Final T.A.N.K. password"
    };

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_main);
        wordle = findViewById(R.id.wordleInput);
        date = findViewById(R.id.dateInput);
        entry = findViewById(R.id.layerInput);
        output = findViewById(R.id.outputText);
        stepLabel = findViewById(R.id.layerLabel);
        status = findViewById(R.id.statusText);
        calculatorPanel = findViewById(R.id.calculatorPanel);
        lockPanel = findViewById(R.id.lockPanel);
        ownerPanel = findViewById(R.id.ownerPanel);
        output.setMovementMethod(new ScrollingMovementMethod());
        date.setText(today());
        findViewById(R.id.calculateButton).setOnClickListener(v -> calculate());
        findViewById(R.id.copyButton).setOnClickListener(v -> copyPassword());
        findViewById(R.id.userModeButton).setOnClickListener(v -> showUser());
        findViewById(R.id.ownerModeButton).setOnClickListener(v -> showLock());
        findViewById(R.id.beginChallengeButton).setOnClickListener(v -> beginChallenge());
        findViewById(R.id.nextLayerButton).setOnClickListener(v -> verifyLayer());
        findViewById(R.id.backButton).setOnClickListener(v -> showUser());
        findViewById(R.id.lockAgainButton).setOnClickListener(v -> showUser());
        showUser();
    }

    private String today() {return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());}
    private void toast(String msg) {Toast.makeText(this,msg,Toast.LENGTH_LONG).show();}
    private void showUser() {
        challenge=null; expected=null; index=0;
        calculatorPanel.setVisibility(View.VISIBLE);
        lockPanel.setVisibility(View.GONE);
        ownerPanel.setVisibility(View.GONE);
        status.setText("User Mode — original T.A.N.K. calculator");
    }
    private void showLock() {
        challenge=null; expected=null; index=0;
        calculatorPanel.setVisibility(View.VISIBLE);
        lockPanel.setVisibility(View.VISIBLE);
        ownerPanel.setVisibility(View.GONE);
        stepLabel.setText("Nine-layer daily entry. Layers 5–6 are added checkpoints, not recovered original layers.");
        status.setText("Owner verification demo — local only");
    }
    private void calculate() {
        try {
            TankCore.Result r=TankCore.calculate(wordle.getText().toString(),date.getText().toString());
            lastPassword=r.password;
            output.setText("Layer 1 - Wordle\n"+r.wordle+"\n\n"+
                "Layer 2 - Daily coordinate\n"+r.coordinate+"\n\n"+
                "Layer 3 - Kalebration 3301\n"+r.kalebration+"\n\n"+
                "Layer 4 - Cicada transform\n"+r.cicada+"\n\n"+
                "Layer 7 - Distorted coordinate\n"+r.distorted+"\n\n"+
                "Layer 8 - Waypoint token\n"+r.waypoint+"\n\n"+
                "Layer 9 - TODAY'S T.A.N.K. PASSWORD\n"+r.password);
        } catch(Exception ex){lastPassword="";output.setText("ERROR\n"+ex.getMessage());}
    }
    private void copyPassword() {
        if(lastPassword.length()==0){toast("Calculate a password first.");return;}
        ClipboardManager cm=(ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
        cm.setPrimaryClip(ClipData.newPlainText("T.A.N.K. password", lastPassword));
        toast("Password copied.");
    }
    private static String sha256(String input) throws Exception {
        byte[] hash=MessageDigest.getInstance("SHA-256").digest(input.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb=new StringBuilder();
        for(byte x:hash)sb.append(String.format(Locale.US,"%02X",x&255));
        return sb.toString();
    }
    private void beginChallenge() {
        try {
            if(!today().equals(date.getText().toString().trim())) {toast("For owner mode, select today's date.");return;}
            challenge=TankCore.calculate(wordle.getText().toString(),today());
            // Layers 5 and 6 did not exist as outputs in the supplied original TankCore.
            // These are explicitly NEW checkpoints; the seven original outputs stay unchanged.
            expected=new String[]{challenge.wordle, challenge.coordinate, challenge.kalebration,
               challenge.cicada, challenge.date,
               sha256("TANK-CHECKPOINT6|"+challenge.date+"|"+challenge.kalebration+"|"+challenge.cicada),
               challenge.distorted,challenge.waypoint,challenge.password};
            index=0;
            renderStep();
        }catch(Exception ex){toast("Cannot start: "+ex.getMessage());}
    }
    private void renderStep() {
        lockPanel.setVisibility(View.VISIBLE);
        stepLabel.setText((index+1)+" / 9 — "+LAYER_NAMES[index]);
        entry.setText("");
        entry.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD|
            InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        entry.requestFocus();
        status.setText("Today's challenge: "+challenge.date);
    }
    private void verifyLayer() {
        if(expected==null){toast("Start the challenge first.");return;}
        if(!today().equals(challenge.date)){toast("Date changed. Restart the challenge.");showLock();return;}
        String received=entry.getText().toString().trim();
        if(!expected[index].equalsIgnoreCase(received)) {
            toast("Incorrect layer. Try again.");entry.setText("");return;
        }
        index++;
        if(index==9) {
            expected=null;challenge=null;
            lockPanel.setVisibility(View.GONE);
            calculatorPanel.setVisibility(View.GONE);
            ownerPanel.setVisibility(View.VISIBLE);
            status.setText("Owner demonstration unlocked — not a secure remote authorization");
        }else renderStep();
    }
    @Override protected void onStop(){super.onStop();showUser();}
}
