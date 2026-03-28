package com.Utils;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.ZoneId;

public class ScreenRecorder {

    private static final S3Client s3Client = S3Client.create();
    private static final String BUCKET = System.getenv("RECORDINGS_BUCKET");

    private final String puzzleType;
    private final Path outputFile;
    private final WebDriver driver;
    private final Process ffmpeg;
    private final OutputStream ffmpegIn;

    private ScreenRecorder(String puzzleType, WebDriver driver) throws IOException {
        this.puzzleType = puzzleType;
        this.driver = driver;
        this.outputFile = Files.createTempFile("recording-" + puzzleType + "-", ".mp4");

        this.ffmpeg = new ProcessBuilder(
                "ffmpeg", "-y",
                "-f", "image2pipe", "-vcodec", "png", "-r", "20",
                "-i", "pipe:0",
                "-vcodec", "libx264", "-pix_fmt", "yuv420p",
                "-preset", "ultrafast",
                outputFile.toString()
        ).redirectError(ProcessBuilder.Redirect.DISCARD)
         .redirectOutput(ProcessBuilder.Redirect.DISCARD)
         .start();

        this.ffmpegIn = ffmpeg.getOutputStream();
    }

    public static ScreenRecorder start(String puzzleType, WebDriver driver) throws IOException {
        ScreenRecorder recorder = new ScreenRecorder(puzzleType, driver);
        recorder.captureFramesFor(5000);
        return recorder;
    }

    public void captureFrame() {
        try {
            byte[] png = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            ffmpegIn.write(png);
            ffmpegIn.flush();
        } catch (WebDriverException e) {
            // Session may have expired; skip this frame
        } catch (IOException e) {
            // ffmpeg pipe closed; skip
        }
    }

    public void captureFramesFor(int millis) {
        long end = System.currentTimeMillis() + millis;
        while (System.currentTimeMillis() < end) {
            captureFrame();
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public void finalizeCapture() {
        try {
            captureFramesFor(5000);
        } catch (Exception e) {
            System.err.println("Stopped capturing early for " + puzzleType + ": " + e.getMessage());
        }
    }

    public void stopAndUpload() {
        try {
            ffmpegIn.close();
            ffmpeg.waitFor();
        } catch (Exception e) {
            System.err.println("Failed to finalize ffmpeg for " + puzzleType + ": " + e.getMessage());
        }
        try {
            String key = "recordings/" + puzzleType.toLowerCase() + "/" + LocalDate.now(ZoneId.of("America/Los_Angeles")) + ".mp4";
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(BUCKET)
                            .key(key)
                            .contentType("video/mp4")
                            .build(),
                    outputFile
            );
            System.out.println("Recording uploaded to s3://" + BUCKET + "/" + key);
        } catch (Exception e) {
            System.err.println("Failed to upload recording for " + puzzleType + ": " + e.getMessage());
        } finally {
            try {
                Files.deleteIfExists(outputFile);
            } catch (IOException ignored) {}
        }
    }
}
