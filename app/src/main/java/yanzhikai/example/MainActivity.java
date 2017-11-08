package yanzhikai.example;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import yanzhikai.bar.StateBar;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private StateBar mStateBar;
    private Button btn_listening,btn_active,btn_thinking,btn_speaking,btn_mic_off,btn_sys_error;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mStateBar = (StateBar) findViewById(R.id.sb);
        btn_listening = (Button) findViewById(R.id.btn_listening);
        btn_listening.setOnClickListener(this);
        btn_active = (Button) findViewById(R.id.btn_active);
        btn_active.setOnClickListener(this);
        btn_thinking = (Button) findViewById(R.id.btn_thinking);
        btn_thinking.setOnClickListener(this);
        btn_speaking = (Button) findViewById(R.id.btn_speaking);
        btn_speaking.setOnClickListener(this);
        btn_mic_off = (Button) findViewById(R.id.btn_mic_off);
        btn_mic_off.setOnClickListener(this);
        btn_sys_error = (Button) findViewById(R.id.btn_sys_error);
        btn_sys_error.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_listening:
                mStateBar.startListen();
                break;
            case R.id.btn_active:
                mStateBar.startActive();
                break;
            case R.id.btn_thinking:
                mStateBar.startThink();
                break;
            case R.id.btn_speaking:
                mStateBar.startSpeaking();
                break;
            case R.id.btn_mic_off:

                break;
            case R.id.btn_sys_error:

                break;
        }
    }
}
