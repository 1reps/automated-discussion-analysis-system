package com.adas.infrastructure.media;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * ffprobe를 사용하여 오디오 길이(ms)를 추출하는 인프라 서비스.
 */
@Service
@RequiredArgsConstructor
@EnableConfigurationProperties(MediaProperties.class)
public class MediaProbeService {

    private static final Logger log = LoggerFactory.getLogger(MediaProbeService.class);

    private final MediaProperties props;

    /**
     * 실패 시 null을 반환하는 안전한 길이 추출.
     */
    public Long safeProbeDurationMs(MultipartFile file) {
        try {
            return probeDurationMs(file);
        } catch (Exception e) {
            log.warn("ffprobe duration failed: {}", e.getMessage());
            return null;
        }
    }

    /**
     * ffprobe를 실행하여 duration(초)을 얻고 ms로 변환한다.
     */
    public Long probeDurationMs(MultipartFile file) throws IOException, InterruptedException {
        if (file == null || file.isEmpty()) {
            return null;
        }

        File temp = File.createTempFile("upload-", ".bin");
        try (FileOutputStream fos = new FileOutputStream(temp)) {
            fos.write(file.getBytes());
        }

        List<String> cmd = new ArrayList<>();
        cmd.add(props.getFfprobePath());
        cmd.add("-v");
        cmd.add("error");
        cmd.add("-show_entries");
        cmd.add("format=duration");
        cmd.add("-of");
        cmd.add("default=noprint_wrappers=1:nokey=1");
        cmd.add(temp.getAbsolutePath());

        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectErrorStream(true);
        Process proc = pb.start();

        // 비동기 읽기 + 타임아웃 처리
        ExecutorService ex = Executors.newSingleThreadExecutor();
        Future<String> out = ex.submit(() -> {
            try (BufferedReader br = new BufferedReader(
                new InputStreamReader(proc.getInputStream(), StandardCharsets.UTF_8))) {
                return br.readLine();
            }
        });

        boolean finished = proc.waitFor(props.getProbeTimeoutMs(), TimeUnit.MILLISECONDS);
        String line = null;
        try {
            line = out.get(Math.max(1, (int) (props.getProbeTimeoutMs() / 1000)), TimeUnit.SECONDS);
        } catch (Exception ignore) {
        }
        ex.shutdownNow();

        if (!finished) {
            proc.destroyForcibly();
            throw new InterruptedException("ffprobe timeout");
        }

        if (line == null || line.isBlank()) {
            return null;
        }
        try {
            double seconds = Double.parseDouble(line.trim());
            long ms = (long) Math.round(seconds * 1000.0);
            return ms >= 0 ? ms : null;
        } catch (NumberFormatException nfe) {
            log.debug("ffprobe parse failed: {}", line);
            return null;
        } finally {
            if (!temp.delete()) {
                log.debug("temp file not deleted: {}", temp.getAbsolutePath());
            }
        }
    }
}
