package com.example.passtask;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private boolean isWorkoutPhase = true; // or false, depending on your initial state


    private EditText etWorkoutDuration, etRestDuration;
    private Button btnStart, btnStop;
    private TextView tvTimeRemaining;
    private ProgressBar pbProgress;
    private CountDownTimer timer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        createNotificationChannel();
        etWorkoutDuration = findViewById(R.id.et_workout_duration);
        etRestDuration = findViewById(R.id.et_rest_duration);
        btnStart = findViewById(R.id.btn_start);
        btnStop = findViewById(R.id.btn_stop);
        tvTimeRemaining = findViewById(R.id.tv_time_remaining);
        pbProgress = findViewById(R.id.pb_progress);
        boolean isWorkoutPhase = false;
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startTimer();
            }
        });

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopTimer();
            }
        });
    }

    public void startTimer() {
        long workoutDuration = Long.parseLong(etWorkoutDuration.getText().toString()) * 1000;
        long restDuration = Long.parseLong(etRestDuration.getText().toString()) * 1000;

        timer = new CountDownTimer(workoutDuration + restDuration, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long minutes = (millisUntilFinished / 1000) / 60;
                long seconds = (millisUntilFinished / 1000) % 60;

                tvTimeRemaining.setText(String.format("Time Remaining: %02d:%02d", minutes, seconds));

                int progress = (int) (100 - (millisUntilFinished * 100) / (workoutDuration + restDuration));
                pbProgress.setProgress(progress);
            }

            @Override
            public void onFinish() {
                tvTimeRemaining.setText("Time Remaining: 00:00");
                pbProgress.setProgress(100);
                String CHANNEL_ID = "my_channel_id";
                NotificationCompat.Builder builder = new NotificationCompat.Builder(MainActivity.this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle("Timer finished")
                        .setContentText("Your workout has finished.")
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);

                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(MainActivity.this);
                if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                int notificationId=0;
                notificationManager.notify(notificationId, builder.build());

                // Add vibration
                Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                if (vibrator != null && vibrator.hasVibrator()) {
                    long[] pattern = {0, 1000, 1000}; // vibration pattern: off, on for 1 second, off for 1 second
                    vibrator.vibrate(VibrationEffect.createWaveform(pattern, 0));
                }

                // Add sound
                MediaPlayer mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.woosh); // replace sound_file with your sound file name
                mediaPlayer.start();
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        mediaPlayer.release();
                    }
                });

                // Create and show a notification
                builder = new NotificationCompat.Builder(MainActivity.this, "default")
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle("Timer Finished")
                        .setContentText("Your workout/rest time is over")
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);

                notificationManager = NotificationManagerCompat.from(MainActivity.this);
                if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                notificationManager.notify(1, builder.build());
            }


        };

        timer.start();
    }

    private void stopTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopTimer();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "My Channel Name";
            String description = "My Channel Description";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("my_channel_id", name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }


    private void showNotification(String message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "timer_channel")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Timer App")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        // Add actions to the notification
        Intent stopIntent = new Intent(this, MainActivity.class);
        stopIntent.putExtra("action", "stop");
        PendingIntent stopPendingIntent = PendingIntent.getActivity(this, 0, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.addAction(R.drawable.ic_stop, "Stop Timer", stopPendingIntent);

        // Show the notification
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        notificationManager.notify(1, builder.build());
    }
    NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
            .setSmallIcon(R.drawable.ic_stop)
            .setContentTitle("Timer")
            .setContentText("The timer has finished!");



}