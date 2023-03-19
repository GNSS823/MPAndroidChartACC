package com.example.mpandroidchartacc;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.res.Configuration;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;

import com.example.mpandroidchartacc.databinding.ActivityMainBinding;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private  static final String TAG = "MainActivity";
    private static final int MAX_ENTRIES = 10;

    private ActivityMainBinding binding;

    private SensorManager sensorManager;

    private Sensor accelerometer;

    private LineChart lineChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        lineChart = binding.lineChart;

        // Get the sensor service
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        // Create a data set for the chart
        List<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(new LineDataSet(null, "AccelX"));
        dataSets.add(new LineDataSet(null, "AccelY"));
        dataSets.add(new LineDataSet(null, "AccelZ"));

        // Set the chart's data
        LineData lineData = new LineData(dataSets);
        lineChart.setData(lineData);

        // Customize the chart's appearance
        float margin = Utils.convertDpToPixel(6f);  // 6dp
        lineChart.getDescription().setEnabled(false);
        lineChart.setTouchEnabled(false);
        lineChart.setDragEnabled(false);
        lineChart.setScaleEnabled(false);
        lineChart.setDrawGridBackground(false);
        lineChart.setPinchZoom(false);

        // Set XAxis properties
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);
        xAxis.setDrawLabels(false);

        // Set left YAxis properties
        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setDrawAxisLine(false);
        leftAxis.setTextSize(16f);
        leftAxis.setXOffset(margin);
        switch (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) {
            case Configuration.UI_MODE_NIGHT_YES:
                leftAxis.setTextColor(Color.WHITE);
                break;
            case Configuration.UI_MODE_NIGHT_NO:
                leftAxis.setTextColor(Color.BLACK);
                break;
        }

        // Set right YAxis properties
        YAxis rightAxis = lineChart.getAxisRight();
        rightAxis.setDrawGridLines(false);
        rightAxis.setDrawAxisLine(false);

        // Set the chart's legend
        Legend legend = lineChart.getLegend();
        legend.setTextSize(16f);
        legend.setForm(Legend.LegendForm.LINE);
        legend.setFormSize(16f);
        legend.setFormToTextSpace(8f);
        legend.setXEntrySpace(16f);
        legend.setWordWrapEnabled(true);
        switch (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) {
            case Configuration.UI_MODE_NIGHT_YES:
                legend.setTextColor(Color.WHITE);
                break;
            case Configuration.UI_MODE_NIGHT_NO:
                legend.setTextColor(Color.BLACK);
                break;
        }

        // Set data set properties
        List<Integer> colors = new ArrayList<>();
        colors.add(ContextCompat.getColor(this, R.color.red_200));
        colors.add(ContextCompat.getColor(this, R.color.green_200));
        colors.add(ContextCompat.getColor(this, R.color.blue_200));
        for (ILineDataSet dataSet : dataSets) {
            LineDataSet lineDataSet = (LineDataSet) dataSet;
            lineDataSet.setDrawCircles(true);
            lineDataSet.setDrawValues(false);
            lineDataSet.setLineWidth(5);
            lineDataSet.setCircleRadius(8);
            lineDataSet.setCircleHoleRadius(5);
            lineDataSet.setColor(colors.get(dataSets.indexOf(dataSet)));
            lineDataSet.setCircleColor(colors.get(dataSets.indexOf(dataSet)));
            lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        }

        // Notify the chart that the data has changed
        lineChart.notifyDataSetChanged();
        lineChart.invalidate();
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float accelX = event.values[0];
            float accelY = event.values[1];
            float accelZ = event.values[2];

            // Get the chart's data
            LineData lineData = lineChart.getData();
            List<ILineDataSet> dataSet = lineData.getDataSets();

            // Remove the oldest data from the chart
            for (ILineDataSet set : dataSet) {
                if (set.getEntryCount() > MAX_ENTRIES) {
                    for (int i = 0; i < set.getEntryCount(); i++) {
                        Entry entry = set.getEntryForIndex(i);
                        entry.setX(entry.getX() - 1);
                    }
                    set.removeFirst();
                }
            }

            // Add the new data to the chart
            lineData.addEntry(new Entry(dataSet.get(0).getEntryCount(), accelX), 0);
            lineData.addEntry(new Entry(dataSet.get(1).getEntryCount(), accelY), 1);
            lineData.addEntry(new Entry(dataSet.get(2).getEntryCount(), accelZ), 2);

            // Set the chart's visible range
            lineChart.setVisibleXRange(0, MAX_ENTRIES-1);

            // Notify the chart that the data has changed
            lineData.notifyDataChanged();
            lineChart.notifyDataSetChanged();
            lineChart.invalidate();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}