package htwg.compsognathus.eegsensorsysteme;

import android.util.Log;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;

public class EEGGraph {


    /*LineChart chart;

    List<Entry> entries_ch1, entries_ch2, entries_ch3, entries_ch4, entries_ch5, entries_ch6, entries_ch7, entries_ch8;
    LineDataSet data_set_ch1, data_set_ch2, data_set_ch3, data_set_ch4, data_set_ch5, data_set_ch6, data_set_ch7, data_set_ch8;
    List<ILineDataSet> data_sets;
    LineData linedata;*/

    GraphView graph_view;

    LineGraphSeries<DataPoint> ch1, ch2, ch3, ch4, ch5, ch6, ch7, ch8;
    ArrayList<LineGraphSeries<DataPoint>> serieses;
    long num_datapoints;

    public EEGGraph(GraphView graph_view)
    {
        this.graph_view = graph_view;

        serieses = new ArrayList<LineGraphSeries<DataPoint>>();

        ch1 = new LineGraphSeries<>();
        ch2 = new LineGraphSeries<>();
        ch3 = new LineGraphSeries<>();
        ch4 = new LineGraphSeries<>();
        ch5 = new LineGraphSeries<>();
        ch6 = new LineGraphSeries<>();
        ch7 = new LineGraphSeries<>();
        ch8 = new LineGraphSeries<>();

        serieses.add(ch1);
        serieses.add(ch2);
        serieses.add(ch3);
        serieses.add(ch4);
        serieses.add(ch5);
        serieses.add(ch6);
        serieses.add(ch7);
        serieses.add(ch8);

        for(LineGraphSeries series:serieses)
        {
            graph_view.addSeries(series);
        }

        //graph_view.getViewport().setXAxisBoundsManual(true);
        //graph_view.getViewport().setMinX(0);
        //graph_view.getViewport().setMaxX(40);

    }

    public void updatePSD(double[][] psd)
    {
        for(int i = 0; i < 1; i++)
        {
            LineGraphSeries<DataPoint> series = (LineGraphSeries<DataPoint>)serieses.get(i);
            DataPoint[] data = new DataPoint[psd[0].length];

            for(int j = 0; j < psd[0].length; j++)
            {
                data[j] = new DataPoint(j, psd[i][j]);
            }

            series.resetData(data);
        }
        //graph_view.computeScroll();
        graph_view.clearSecondScale();
        graph_view.invalidate();
    }

    public void addSample(EEGSample sample)
    {
        Log.d("MODEBUG", "Adding Sample: " + num_datapoints);
        for(int i = 0; i < 8; i++)
        {
            LineGraphSeries series = serieses.get(i);
            series.appendData(new DataPoint(num_datapoints, (float) sample.getEEGVoltage(i)),true,1000,false);

        }
        num_datapoints++;
        graph_view.computeScroll();
    }
}

/*  OLD GRAPH via MPAndroidChart
Constructor
        entries_ch1 = new ArrayList<Entry>();
        entries_ch2 = new ArrayList<Entry>();
        entries_ch3 = new ArrayList<Entry>();
        entries_ch4 = new ArrayList<Entry>();
        entries_ch5 = new ArrayList<Entry>();
        entries_ch6 = new ArrayList<Entry>();
        entries_ch7 = new ArrayList<Entry>();
        entries_ch8 = new ArrayList<Entry>();

        data_set_ch1 = new LineDataSet(entries_ch1, "Ch 1");
        data_set_ch1.setColor(Color.BLUE);
        data_set_ch2 = new LineDataSet(entries_ch2, "Ch 2");
        data_set_ch2.setColor(Color.GREEN);
        data_set_ch3 = new LineDataSet(entries_ch3, "Ch 3");
        data_set_ch3.setColor(Color.GRAY);
        data_set_ch4 = new LineDataSet(entries_ch4, "Ch 4");
        data_set_ch4.setColor(Color.YELLOW);
        data_set_ch5 = new LineDataSet(entries_ch5, "Ch 5");
        data_set_ch5.setColor(Color.CYAN);
        data_set_ch6 = new LineDataSet(entries_ch6, "Ch 6");
        data_set_ch6.setColor(Color.DKGRAY);
        data_set_ch7 = new LineDataSet(entries_ch7, "Ch 7");
        data_set_ch7.setColor(Color.MAGENTA);
        data_set_ch8 = new LineDataSet(entries_ch8, "Ch 8");
        data_set_ch8.setColor(Color.RED);

        data_sets = new ArrayList<ILineDataSet>();
        data_sets.add(data_set_ch1);
        data_sets.add(data_set_ch2);
        data_sets.add(data_set_ch3);
        data_sets.add(data_set_ch4);
        data_sets.add(data_set_ch5);
        data_sets.add(data_set_ch6);
        data_sets.add(data_set_ch7);
        data_sets.add(data_set_ch8);

        for(ILineDataSet set:data_sets)
        {
            set.setAxisDependency(YAxis.AxisDependency.LEFT);
            ((LineDataSet)set).setDrawCircles(false);
        }

        linedata = new LineData(data_sets);
        chart.setData(linedata);
        chart.invalidate();


    public void addSample(EEGSample sample)
    {
        for(int i = 0; i < 8; i++)
        {
            LineDataSet set = (LineDataSet) data_sets.get(i);
            set.addEntry(new Entry(set.getEntryCount(), (float) sample.getEEGVoltage(i)));
            set.notifyDataSetChanged();
        }

        linedata.notifyDataChanged();
        chart.notifyDataSetChanged();
        chart.invalidate();
    }
 */