package htwg.compsognathus.eegsensorsysteme;

public class EEGSample {

    public static final double SCALE_FACTOR = 0.02235174446;

    private int eeg_values[];
    private int sample_number;
    //private int timestamp;

    public EEGSample(int sample_number, int eeg_values[])
    {
        this.sample_number = sample_number;
        this.eeg_values = eeg_values;
    }

    public int[] getEEGValues()
    {
        return eeg_values;
    }

    public double getEEGVoltage(int electrode_index)
    {
        return eeg_values[electrode_index] * SCALE_FACTOR;
    }

    public int getSampleNumber()
    {
        return sample_number;
    }

    public static EEGSample create(byte [] serial_data)
    {
        int eeg_values[] = new int[8];

        for(int i = 1; i < 25; i+=3)
        {
            int newInt = (
                    ((0xFF & serial_data[i]) << 16) |
                            ((0xFF & serial_data[i+1]) << 8) |
                            (0xFF & serial_data[i+2])
            );
            if ((newInt & 0x00800000) > 0) {
                newInt |= 0xFF000000;
            } else {
                newInt &= 0x00FFFFFF;
            }
            eeg_values[(i-1)/3] = newInt;
        }

        return new EEGSample(serial_data[0], eeg_values);
    }

    public static EEGSample interpolate(EEGSample s1, EEGSample s2)
    {
        int eeg_values_new[] = new int[8];

        for(int i = 0; i < eeg_values_new.length; i++)
        {
            eeg_values_new[i] = (s1.getEEGValues()[i] + s2.getEEGValues()[i]) / 2;
        }

        return new EEGSample(s1.getSampleNumber()+1, eeg_values_new);
    }

}
