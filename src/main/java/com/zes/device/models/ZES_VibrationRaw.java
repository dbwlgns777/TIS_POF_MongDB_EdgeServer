package com.zes.device.models;

import com.zes.device.config.ZES_MongoConfig;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

public class ZES_VibrationRaw extends ZES_TypeMongoDB
{
    private static final int HEADER_START = 0;
    private static final int HEADER_END = 9;
    private static final int ICT_START = 10;
    private static final int ICT_END = 19;
    private static final int SENSOR_START = 26;
    private static final int SENSOR_END = 225;

    public ZES_VibrationRaw(long timestamp, byte[] bytes, String ictNumber)
    {
        super(timestamp, bytes, ictNumber);
    }

    @Override
    public ZES_TypeMongoDB ZES_saveRealTime()
    {
        try
        {
            String header = ZES_extractAscii(HEADER_START, HEADER_END);
            String ictNumber = ZES_extractAscii(ICT_START, ICT_END);
            List<Integer> sensorData = ZES_extractSensorData();

            System.out.println("Header: " + header + ", ICT_NUMBER: " + ictNumber);
            int printCount = Math.min(10, sensorData.size());
            for (int i = 0; i < printCount; i++)
            {
                System.out.println("Sensor_Data[" + (i + 1) + "]: " + sensorData.get(i));
            }

            Document document = new Document();
            document.append("Header", header);
            document.append("ICT_NUMBER", ictNumber);
            document.append("Sensor_Data", sensorData);
            ZES_MongoConfig.ZES_getCollection(ZES_MongoConfig.ZES_gv_vibrationCollection).insertOne(document);
        }
        catch (Exception e)
        {
            ZES_handleException(e);
        }
        finally
        {
            return this;
        }
    }

    private String ZES_extractAscii(int start, int end)
    {
        byte[] value = new byte[end - start + 1];
        System.arraycopy(ZES_gv_bytes, start, value, 0, value.length);
        return new String(value);
    }

    private List<Integer> ZES_extractSensorData()
    {
        List<Integer> sensorData = new ArrayList<>();
        for (int i = SENSOR_START; i <= SENSOR_END; i += 2)
        {
            int high = ZES_gv_bytes[i] & 0xFF;
            int low = ZES_gv_bytes[i + 1] & 0xFF;
            sensorData.add((high << 8) | low);
        }
        return sensorData;
    }
}
