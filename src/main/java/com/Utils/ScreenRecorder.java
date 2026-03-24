package com.Utils;

import org.jcodec.api.awt.AWTSequenceEncoder;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class ScreenRecorder {

    private static final S3Client s3Client = S3Client.create();
    private static final String BUCKET = System.getenv("RECORDINGS_BUCKET");

    private final String puzzleType;
    private final Path outputFile;
    private final WebDriver driver;
    private final List<byte[]> frames = new ArrayList<>();

    private ScreenRecorder(String puzzleType, WebDriver driver) throws IOException {
        this.puzzleType = puzzleType;
        this.driver = driver;
        this.outputFile = Files.createTempFile("recording-" + puzzleType + "-", ".mp4");
    }

    public static ScreenRecorder start(String puzzleType, WebDriver driver) throws IOException {
        ScreenRecorder recorder = new ScreenRecorder(puzzleType, driver);
        recorder.captureFramesFor(5000);
        return recorder;
    }

    public void captureFrame() {
        frames.add(((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES));
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

    public void stopAndUpload() {
        captureFramesFor(5000);
        try {
            AWTSequenceEncoder encoder = AWTSequenceEncoder.createSequenceEncoder(outputFile.toFile(), 20);
            for (byte[] pngBytes : frames) {
                BufferedImage img = ImageIO.read(new ByteArrayInputStream(pngBytes));
                if (img != null) {
                    int w = img.getWidth() & ~1;
                    int h = img.getHeight() & ~1;
                    encoder.encodeImage(img.getSubimage(0, 0, w, h));
                }
            }
            encoder.finish();

            String key = "recordings/" + puzzleType.toLowerCase() + "/" + LocalDate.now(ZoneId.of("America/Los_Angeles")) + ".mp4";
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(BUCKET)
                            .key(key)
                            .build(),
                    outputFile
            );
            System.out.println("Recording uploaded to s3://" + BUCKET + "/" + key);
        } catch (Exception e) {
            System.err.println("Failed to stop/upload recording for " + puzzleType + ": " + e.getMessage());
        } finally {
            try {
                Files.deleteIfExists(outputFile);
            } catch (IOException ignored) {}
        }
    }
}
