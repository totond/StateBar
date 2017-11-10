package yanzhikai.example;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Random;

import yanzhikai.bar.StateBar;
import yanzhikai.bar.StateListener;

import static yanzhikai.bar.StateBar.IDLE;
import static yanzhikai.bar.StateBar.LISTENING;
import static yanzhikai.bar.StateBar.LISTENING_ACTIVE;
import static yanzhikai.bar.StateBar.MIC_OFF;
import static yanzhikai.bar.StateBar.SPEAKING;
import static yanzhikai.bar.StateBar.SYSTEM_ERROR;
import static yanzhikai.bar.StateBar.THINKING;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private StateBar mStateBar;
    private TextView tv;
    private Button btn_idle,btn_next,btn_mic_off,btn_sys_error,btn_papa;
    private Random random = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mStateBar = (StateBar) findViewById(R.id.sb);
//        btn_idle = (Button) findViewById(R.id.btn_idle);
//        btn_idle.setOnClickListener(this);
        btn_next = (Button) findViewById(R.id.btn_next);
        btn_next.setOnClickListener(this);
        btn_mic_off = (Button) findViewById(R.id.btn_mic_off);
        btn_mic_off.setOnClickListener(this);
        btn_sys_error = (Button) findViewById(R.id.btn_sys_error);
        btn_sys_error.setOnClickListener(this);
        btn_papa = (Button) findViewById(R.id.btn_papa);
        btn_papa.setOnClickListener(this);

        tv = (TextView) findViewById(R.id.tv);

        mStateBar.setStateListener(new StateListener() {
            @Override
            public void onStateChanged(@StateBar.State int currentState,@StateBar.State int newState) {
                switch (newState){
                    case IDLE:
                        tv.setText("IDLE");
                        break;
                    case LISTENING:
                        tv.setText("LISTENING");
                        break;
                    case LISTENING_ACTIVE:
                        tv.setText("LISTENING_ACTIVE");
                        break;
                    case THINKING:
                        tv.setText("THINKING");
                        break;
                    case SPEAKING:
                        tv.setText("SPEAKING");
                        break;
                    case MIC_OFF:
                        tv.setText("MIC_OFF");
                        break;
                    case SYSTEM_ERROR:
                        tv.setText("SYSTEM_ERROR");
                        break;
                }
            }
        });
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_next:
                mStateBar.nextState();
                break;
            case R.id.btn_mic_off:
                mStateBar.startMicOff();
                break;
            case R.id.btn_sys_error:
                mStateBar.startSysError();
                break;
            case R.id.btn_papa:
                mStateBar.setIncrease(random.nextInt(100) + 20);
                break;
        }
    }
}
