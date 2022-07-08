package com.acme.onlineshop.service;

import com.acme.onlineshop.ApplicationConfiguration;
import com.acme.onlineshop.Main;
import com.acme.onlineshop.controller.errors.ErrorResponseCodes;
import com.acme.onlineshop.dto.SystemInfoDTO;
import com.acme.onlineshop.exception.RESTException;
import com.acme.onlineshop.utils.Profile;
import com.sun.management.OperatingSystemMXBean;
import io.jsonwebtoken.security.WeakKeyException;
import org.springframework.stereotype.Service;

import java.io.File;
import java.lang.management.*;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class SystemService {

    // private final static Runtime RUNTIME = Runtime.getRuntime();
    private final static MemoryMXBean MEMORY_MX_BEAN = ManagementFactory.getMemoryMXBean();
    private final static ThreadMXBean THREAD_MX_BEAN = ManagementFactory.getThreadMXBean();
    private final static OperatingSystemMXBean OPERATING_SYSTEM_MX_BEAN = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
    private final static RuntimeMXBean RUNTIME_MX_BEAN = ManagementFactory.getRuntimeMXBean();
    // private final static OperatingSystem OPERATING_SYSTEM = OperatingSystem.getCurrentSystem();
    private final static int CONVERSION_FACTOR = 1_073_741_824;
    private final static int BIT_LENGTH = 512;

    public Profile getCurrentProfile() {
        return Main.getCurrentProfile();
    }

    public String getTimeZone() {
        return ApplicationConfiguration.getTimeZone().getId();
    }

    public List<String> getAllTimeZonesWithOffset() {
        LocalDateTime time = LocalDateTime.now();
        List<Integer> offsetValues = ZoneId.getAvailableZoneIds().stream().map(id -> ZoneId.of(id).getRules().getOffset(time).getTotalSeconds()).distinct().sorted().toList();
        Map<Integer, List<String>> sorted = new HashMap<>();
        offsetValues.parallelStream().forEach(off -> sorted.put(off, ZoneId.getAvailableZoneIds().stream().filter(zone -> ZoneId.of(zone).getRules().getOffset(time).getTotalSeconds() == off).map(zone -> zone + " ( " + ZoneId.of(zone).getRules().getOffset(time) + " )").sorted().collect(Collectors.toList())));
        List<String> result = new ArrayList<>(ZoneId.getAvailableZoneIds().size());
        offsetValues.forEach(off -> result.addAll(sorted.get(off)));
        return result;
    }

    public void setTimeZone(ZoneId timeZone) {
        ApplicationConfiguration.setTimeZone(timeZone);
    }

    public void updateTokenKey(String newKey) {
        try {
            JwtTokenService.testNewKey(newKey);
        } catch (WeakKeyException exc) {
            RESTException newExc = new RESTException(exc);
            newExc.setErrorCode(ErrorResponseCodes.JWT_ERROR);
            throw newExc;
        }
        setNewTokenKey(newKey, true);
    }

    public void setNewTokenKey(String newKey, boolean saveOnDatabase) {
        JwtTokenService service = Main.getBean(JwtTokenService.class);
        service.updateKey(newKey);
        if (saveOnDatabase) {
            ApplicationConfiguration.setJwtKey(newKey);
        }
    }

    public void generateNewTokenKey() {
        String newKey = "";
        try {
            newKey = generateKey();
        } catch (NoSuchAlgorithmException exc) {
            RESTException newExc = new RESTException(exc);
            newExc.setErrorCode(ErrorResponseCodes.JWT_ERROR);
            throw newExc;
        }
        updateTokenKey(newKey);
    }

    /**
     * After a delaying time, shuts this application down.
     */
    public void shutDown() {
        new Thread(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                Main.exit();
            }
        }).start();
    }

    public static List<SystemInfoDTO> getCurrentSystemInfo() {
        List<SystemInfoDTO> result = new ArrayList<>();
        result.add(new SystemInfoDTO("hours_online", getHoursOnline()));
        result.add(new SystemInfoDTO("cpu_usage", getCPUUsage()));
        float[] memory = getMemoryUsage();
        result.add(new SystemInfoDTO("initial_memory", memory[0]));
        result.add(new SystemInfoDTO("used_heap_memory", memory[1]));
        result.add(new SystemInfoDTO("max_heap_memory", memory[2]));
        result.add(new SystemInfoDTO("committed_memory", memory[3]));
        float[] disk = getDiskSpace();
        result.add(new SystemInfoDTO("total_space", disk[0]));
        result.add(new SystemInfoDTO("free_space", disk[1]));
        result.add(new SystemInfoDTO("usable_space", disk[2]));
        return result;
    }

    /**
     * Returns current CPU usage/load of entire environment/operating system as floating point number between 0 &amp; 1
     *
     * @return CPU-load over all processors
     * @see OperatingSystemMXBean#getCpuLoad()
     */
    public static float getCPUUsage() {
        return (float) OPERATING_SYSTEM_MX_BEAN.getCpuLoad();
    }

    /**
     * Generates random byte array with predefined length ;&amp returns converted Base64 string
     *
     * @see Base64.Encoder#encodeToString(byte[])
     * @return {@link String} representation of Base64 key with predefined bit-length
     * @throws NoSuchAlgorithmException If no algorithm is available
     */
    private static String generateKey() throws NoSuchAlgorithmException {
        byte[] bytes = new byte[BIT_LENGTH / 8];
        SecureRandom.getInstanceStrong().nextBytes(bytes);
        byte[] encoded = Base64.getEncoder().encode(bytes);
        return new String(encoded, 0, encoded.length, StandardCharsets.UTF_8);
    }

    /**
     * Calculates &amp; returns hours since this application is running
     *
     * @return Hours since this application is running
     */
    private static float getHoursOnline() {
        return (float) TimeUnit.MILLISECONDS.toHours(RUNTIME_MX_BEAN.getUptime());
    }

    /**
     * <p>Returns array with 4 floats representing current memory loads in GB</p>
     * <ol>
     *     <li>Initial memory</li>
     *     <li>Used heap memory</li>
     *     <li>Max heap memory</li>
     *     <li>Committed memory</li>
     * </ol>
     *
     * @return Array with current memory state in GB
     */
    private static float[] getMemoryUsage() {
        MemoryUsage memoryUsage = MEMORY_MX_BEAN.getHeapMemoryUsage();
        return new float[] {
            (float) memoryUsage.getInit()/ CONVERSION_FACTOR,
            (float) memoryUsage.getUsed()/ CONVERSION_FACTOR,
            (float) memoryUsage.getMax()/ CONVERSION_FACTOR,
            (float) memoryUsage.getCommitted()/ CONVERSION_FACTOR
        };
    }

    /**
     * <p>Returns array with 3 floats representing current disk space in GB</p>
     * <ol>
     *     <li>Total space</li>
     *     <li>Free space</li>
     *     <li>Usable space</li>
     * </ol>
     *
     * @return Array with current disk space in GB
     */
    private static float[] getDiskSpace() {
        File[] roots = File.listRoots();
        if (roots.length == 0) {
            return new float[] {0f, 0f, 0f};
        } else if(roots.length < 2) {
            return new float[] {
                (float) roots[0].getTotalSpace()/ CONVERSION_FACTOR,
                (float) roots[0].getFreeSpace()/ CONVERSION_FACTOR,
                (float) roots[0].getUsableSpace()/ CONVERSION_FACTOR
            };
        } else {
            long totalSpace = 0;
            long totalFreeSpace = 0;
            long totalUsableSpace = 0;
            for(File root: roots) {
                totalSpace += root.getTotalSpace();
                totalFreeSpace += root.getFreeSpace();
                totalUsableSpace += root.getUsableSpace();
            }
            return new float[]{
                    (float) totalSpace/ CONVERSION_FACTOR,
                    (float) totalFreeSpace/ CONVERSION_FACTOR,
                    (float) totalUsableSpace/ CONVERSION_FACTOR
            };
        }
    }
}
