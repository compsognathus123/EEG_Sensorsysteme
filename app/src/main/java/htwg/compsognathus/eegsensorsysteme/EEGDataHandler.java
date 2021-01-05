package htwg.compsognathus.eegsensorsysteme;

import android.os.Handler;
import android.util.Log;

import java.util.concurrent.BlockingQueue;

public class EEGDataHandler extends Thread {

    public static final int SAMPLE = 10;
    public static final int PSD = 11;
    public static final int NUM_UPDATE = 12;

    private Handler mHandler;

    BlockingQueue<EEGSample> eeg_samples;

    private boolean handle;

    FFT fft;

    float av_alpha;

    int sequence_length;
    float overlapping;
    int new_samples;

    double[][] seq, seq_old, psd;
    double[] dummy;

    int num_runs;
    int num_samples;

    /*
            TODO:   Check if algorithmus can follow sample rate
                    Remove imaginary part from FFT (y)
                    Faster transfer to MainActivity

     */

    public EEGDataHandler(BlockingQueue<EEGSample> eeg_samples, Handler mHandler)
    {
        this.eeg_samples = eeg_samples;
        this.mHandler = mHandler;

        av_alpha = 0.3f; // Einfluss des neuen Werts

        sequence_length = 64;
        overlapping = 0.2f;
        new_samples = sequence_length - (int)(sequence_length * overlapping);
        Log.d("MODEBUG", "sequence length: " + sequence_length);
        Log.d("MODEBUG", "new_samples: " + new_samples);
        Log.d("MODEBUG", "sequence_length * overlapping: " + sequence_length * overlapping);

        seq = new double[8][sequence_length];
        seq_old = new double[8][sequence_length];
        psd = new double[8][sequence_length];
        dummy = new double[sequence_length];

        fft = new FFT(sequence_length);

        String window_string = "Window: ";
        for(int i = 0; i < fft.getWindow().length; i++)
        {
            window_string += fft.getWindow()[i] + " ";
        }
        Log.d("MODEBUG", window_string);
    }


    @Override
    public void run()
    {
        EEGSample sample = null;

        while(true)
        {
            try
            {
                sample = eeg_samples.take();

                //If sequence was processed and therefore the power spectral density updated, send psd via handler to MainActivity
                if(doWelchMethod(sample))
                {
                    Log.d("MODEBUG",num_runs + ". PSD calculated." );
                    //mHandler.obtainMessage(PSD, psd).sendToTarget();
                    mHandler.obtainMessage(NUM_UPDATE, num_runs).sendToTarget();

                    num_runs++;
                }
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }

            /*String voltageString = "";
            String valueString = "";
            for(int i = 0; i < 8; i++)
            {
                voltageString += sample.getEEGVoltage(i) + "\t";
                valueString += sample.getEEGValues()[i] + "\t";
            }
            Log.d("MODEBUG", voltageString);
            Log.d("MODEBUG", valueString);*/

            //mHandler.obtainMessage(SAMPLE, sample).sendToTarget();
        }
    }


    private boolean doWelchMethod(EEGSample sample)
    {
        //Collect new samples till new_samples is reached, then process them via Welch
        if(num_samples == new_samples)
        {
            //Copy overlapping samples from previous sequence
            for(int i = 0; i < sequence_length * overlapping; i++)
            {
                for(int j = 0; j < 8; j++)
                {
                    seq[j][i] = seq_old[j][i + new_samples-1];
                }
            }

            //Copy new sequence for next time
            seq_old = seq.clone();

            //Apply window
            for(int i = 0; i < sequence_length; i++)
            {
                double w = fft.getWindow()[i];
                for(int j = 0; j < 8; j++)
                {
                    seq[j][i] *= w;
                }
            }

            //Perform FFT
            for(int i = 0; i < 8; i++)
            {
                fft.fft(seq[i], dummy);

                //Squaring for power spectral density
                for(int j = 0; j < sequence_length; j++)
                {
                    psd[i][j] = (1 - av_alpha) * psd[i][j] + av_alpha * (seq[i][j] * seq[i][j]);
                }
            }

            num_samples = 0;
            return true;
        }
        else
        {
            for(int j = 0; j < 8; j++)
            {
                seq[j][num_samples + (int) (sequence_length * overlapping) - 1] = sample.getEEGVoltage(j);
            }

            num_samples++;
            return false;
        }
    }

    int interpret24bitAsInt32(byte[] byteArray) {
        int newInt = (
                ((0xFF & byteArray[0]) << 16) |
                        ((0xFF & byteArray[1]) << 8) |
                        (0xFF & byteArray[2])
        );
        if ((newInt & 0x00800000) > 0) {
            newInt |= 0xFF000000;
        } else {
            newInt &= 0x00FFFFFF;
        }
        return newInt;
    }

    int interpret16bitAsInt32(byte[] byteArray)
    {
        int newInt = (
                ((0xFF & byteArray[0]) << 8) |
                        (0xFF & byteArray[1])
        );
        if ((newInt & 0x00008000) > 0)
        {
            newInt |= 0xFFFF0000;
        } else {
            newInt &= 0x0000FFFF;
        }
        return newInt;
    }
}
