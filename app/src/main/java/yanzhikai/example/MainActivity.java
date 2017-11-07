package yanzhikai.example;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import yanzhikai.bar.StateBar;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private StateBar mStateBar;
    private Button btn_listen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mStateBar = (StateBar) findViewById(R.id.sb);
        btn_listen = (Button) findViewById(R.id.btn_listen);
        btn_listen.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_listen:
//                mStateBar.startListen();
                mStateBar.startActive();
                break;
        }
    }
}
