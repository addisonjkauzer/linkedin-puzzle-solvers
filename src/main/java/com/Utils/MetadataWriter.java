package com.Utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.time.LocalDate;
import java.time.ZoneId;

public class MetadataWriter {

    private static final S3Client s3 = S3Client.create();
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final String BUCKET = System.getenv("RECORDINGS_BUCKET");
    private static final String KEY = "metadata.json";
    private static final int MAX_RETRIES = 5;

    public static void appendEntry(String puzzleType, long solveTimeMs, int guessCount) {
        for (int attempt = 0; attempt < MAX_RETRIES; attempt++) {
            try {
                String currentEtag = null;
                ArrayNode entries;

                try {
                    byte[] body = s3.getObject(
                            GetObjectRequest.builder().bucket(BUCKET).key(KEY).build(),
                            ResponseTransformer.toBytes()
                    ).asByteArray();
                    HeadObjectResponse head = s3.headObject(
                            HeadObjectRequest.builder().bucket(BUCKET).key(KEY).build()
                    );
                    currentEtag = head.eTag();
                    entries = (ArrayNode) mapper.readTree(body);
                } catch (NoSuchKeyException e) {
                    entries = mapper.createArrayNode();
                }

                String date = LocalDate.now(ZoneId.of("America/Los_Angeles")).toString();
                ObjectNode entry = mapper.createObjectNode();
                entry.put("date", date);
                entry.put("puzzleType", puzzleType);
                entry.put("recordingKey", "recordings/" + puzzleType.toLowerCase() + "/" + date + ".mp4");
                entry.put("solveTimeMs", solveTimeMs);
                entry.put("guessCount", guessCount);

                // Replace existing entry for same date+puzzleType, otherwise append
                boolean replaced = false;
                for (int i = 0; i < entries.size(); i++) {
                    JsonNode e = entries.get(i);
                    if (date.equals(e.get("date").asText()) && puzzleType.equals(e.get("puzzleType").asText())) {
                        ((ArrayNode) entries).set(i, entry);
                        replaced = true;
                        break;
                    }
                }
                if (!replaced) entries.add(entry);

                // Retain only the last 365 days of entries per puzzle type
                LocalDate cutoff = LocalDate.now(ZoneId.of("America/Los_Angeles")).minusDays(365);
                ArrayNode trimmed = mapper.createArrayNode();
                entries.forEach(e -> {
                    String entryDate = e.get("date").asText();
                    if (!LocalDate.parse(entryDate).isBefore(cutoff)) {
                        trimmed.add(e);
                    }
                });
                entries = trimmed;

                PutObjectRequest.Builder putReq = PutObjectRequest.builder()
                        .bucket(BUCKET)
                        .key(KEY)
                        .contentType("application/json");
                if (currentEtag != null) {
                    putReq.ifMatch(currentEtag);
                }
                s3.putObject(putReq.build(), RequestBody.fromBytes(mapper.writeValueAsBytes(entries)));
                System.out.println("MetadataWriter: appended entry for " + puzzleType + " on " + date);
                return;

            } catch (S3Exception e) {
                if ("PreconditionFailed".equals(e.awsErrorDetails().errorCode()) && attempt < MAX_RETRIES - 1) {
                    try {
                        Thread.sleep(200L * (1L << attempt));
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                } else {
                    System.err.println("MetadataWriter failed after " + (attempt + 1) + " attempts: " + e.getMessage());
                    return;
                }
            } catch (Exception e) {
                System.err.println("MetadataWriter unexpected error: " + e.getMessage());
                return;
            }
        }
    }
}
