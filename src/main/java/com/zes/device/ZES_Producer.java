package com.zes.device;

import com.zes.device.models.ZES_TypeMongoDB;
import com.zes.device.models.ZES_VibrationRaw;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.time.Instant;
import java.util.concurrent.BlockingQueue;

import static com.zes.device.ZES_DeviceApplication.*;

public class ZES_Producer implements Runnable
{
    private static final int ZES_gv_BUFFER_SIZE = 230;
    private static final int ZES_gv_CHECKSUM_OFFSET = 228;
    private static final int ZES_gv_CHECKSUM_SIZE = 2;
    private static final int ZES_gv_ICT_NUMBER_OFFSET = 10;
    private static final int ZES_gv_ICT_NUMBER_SIZE = 10;
    private static final int ZES_gv_READ_TIMEOUT_MS = 300; // 주기 관리: read가 안될 때만 timeout 처리

    private final BlockingQueue<ZES_TypeMongoDB> queue;
    private final int threadNo;
    private final ServerSocket serverSocket;

    public ZES_Producer(BlockingQueue<ZES_TypeMongoDB> queue, int threadNo, ServerSocket serverSocket)
    {
        this.queue = queue;
        this.threadNo = threadNo;
        this.serverSocket = serverSocket;
    }

    @Override
    public void run()
    {
        while (!Thread.currentThread().isInterrupted())
        {
            try
            {
                Socket ZES_lv_socket = serverSocket.accept();
                ZES_lv_socket.setSoTimeout(ZES_gv_READ_TIMEOUT_MS); // 주기 관리
                ZES_readBytesAndEnqueue(ZES_lv_socket);
            }
            catch (IOException e)
            {
                if (!Thread.currentThread().isInterrupted())
                {
                    ZES_gv_logger.severe("IOException in Producer thread " + threadNo + ": " + e.getMessage());
                }
            }
            catch (InterruptedException e)
            {
                ZES_gv_logger.info("Producer thread " + threadNo + " interrupted, shutting down");
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void ZES_readBytesAndEnqueue(Socket socket) throws IOException, InterruptedException
    {
        try
        {
            InputStream ZES_lv_inputStream = socket.getInputStream();
            byte[] ZES_lv_buffer = new byte[ZES_gv_BUFFER_SIZE];
            int ZES_lv_totalBytesRead = 0;
            try
            {
                while (ZES_lv_totalBytesRead < ZES_gv_BUFFER_SIZE)
                {
                    int ZES_lv_bytesRead = ZES_lv_inputStream.read(
                            ZES_lv_buffer,
                            ZES_lv_totalBytesRead,
                            ZES_gv_BUFFER_SIZE - ZES_lv_totalBytesRead
                    );

                    if (ZES_lv_bytesRead == -1)
                    {
                        return; // 주기 관리: 연결 종료 시 즉시 종료 후 close
                    }
                    ZES_lv_totalBytesRead += ZES_lv_bytesRead;
                }
            }
            catch (SocketTimeoutException e)
            {
                ZES_gv_logger.warning("Read timeout in Producer thread " + threadNo + " (주기 관리)");
                return; // 주기 관리: read 안될 때 timeout으로 종료 후 close
            }

            long ZES_lv_timestamp = Instant.now().toEpochMilli();
            if (ZES_validateCheckSum(ZES_lv_buffer))
            {
                String ZES_lv_ictNumber = ZES_convertByteArrayToString(ZES_lv_buffer, ZES_gv_ICT_NUMBER_OFFSET, ZES_gv_ICT_NUMBER_SIZE);
                if (ZES_filterIctNumber(ZES_lv_ictNumber))
                {
                    queue.put(new ZES_VibrationRaw(ZES_lv_timestamp, ZES_lv_buffer, ZES_lv_ictNumber));
                }
            }
            else
            {
                ZES_gv_logger.warning("Checksum validation failed for ICT: " +
                        ZES_convertByteArrayToString(ZES_lv_buffer, ZES_gv_ICT_NUMBER_OFFSET, ZES_gv_ICT_NUMBER_SIZE));
            }
        }
        catch (IOException e)
        {
            ZES_gv_logger.severe("IOException in thread " + threadNo + ": " + e.getMessage());
        }
        catch (InterruptedException e)
        {
            ZES_gv_logger.warning("Thread " + threadNo + " interrupted");
            Thread.currentThread().interrupt();
            throw e;
        }
        finally
        {
            try
            {
                socket.close(); // 주기 관리: 연결 종료 시점
            }
            catch (IOException e)
            {
                ZES_gv_logger.warning("Error closing socket: " + e.getMessage());
            }
        }
    }

    private static boolean ZES_validateCheckSum(byte[] dataBuffer)
    {
        long ZES_lv_checkSum = 0;
        for (int i = 0; i < ZES_gv_CHECKSUM_OFFSET; i++)
        {
            ZES_lv_checkSum += dataBuffer[i] & 0xff;
        }
        return ZES_lv_checkSum == ZES_convertByteArrayToLong(dataBuffer, ZES_gv_CHECKSUM_OFFSET, ZES_gv_CHECKSUM_SIZE);
    }

    private boolean ZES_filterIctNumber(String ictNumber)
    {
        return !ictNumber.contains("\u0000");
    }
}
