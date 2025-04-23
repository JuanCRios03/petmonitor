package co.edu.unipiloto.petmonitor.CasosdeUso;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import co.edu.unipiloto.petmonitor.R;

public class monitoreoEjercicio extends AppCompatActivity {

    private ImageView imageView;
    private TextView tvContador;
    private Handler handler = new Handler();
    private int segundos = 0;
    private boolean contadorActivo = false;

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (contadorActivo) {
                segundos++;
                int minutos = segundos / 60;
                int seg = segundos % 60;
                String tiempo = String.format("%02d:%02d", minutos, seg);
                tvContador.setText(tiempo);
                handler.postDelayed(this, 1000);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_monitoreo_ejercicio);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        imageView = findViewById(R.id.imageView);
        tvContador = findViewById(R.id.tvContador);
        Button btnIniciar = findViewById(R.id.btnIniciar);
        Button btnPausar = findViewById(R.id.btnPausar);
        Button btnFinalizar = findViewById(R.id.btnFinalizar);

        btnIniciar.setOnClickListener(v -> {
            imageView.setVisibility(View.GONE);
            tvContador.setVisibility(View.VISIBLE);
            if (!contadorActivo) {
                contadorActivo = true;
                handler.post(runnable);
            }
        });

        btnPausar.setOnClickListener(v -> {
            contadorActivo = false;
            handler.removeCallbacks(runnable);
        });

        btnFinalizar.setOnClickListener(v -> {
            contadorActivo = false;
            handler.removeCallbacks(runnable);
            segundos = 0;
            tvContador.setText("00:00");
            tvContador.setVisibility(View.GONE);
            imageView.setVisibility(View.VISIBLE);
        });
    }
}
