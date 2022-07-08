package com.acme.build;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LiquibaseUtil {

    private final static String LABEL_PREFIX = "version-";
    private final static String FILE_SUFFIX = "-changelog.json";
    private final static String DEFAULT_CHANGELOG = "0001"+ FILE_SUFFIX;
    private final static Pattern FILE_REGEX = Pattern.compile("\\d+"+ FILE_SUFFIX);
    private final static Pattern DIGIT_REGEX = Pattern.compile("(\\d+).*");

    private int changeLogID = -1;

    public String getLabel() {
        return LABEL_PREFIX+changeLogID;
    }

    public String getLastChangelogFile(File changeLogRoot) {
        if(Objects.requireNonNull(changeLogRoot, "Root dir of liquibase change logs mustn't be null").isDirectory()) {
            List<String> allChangeLogs = getAllChangeLogs(changeLogRoot);
            if(allChangeLogs.isEmpty()) {
                return DEFAULT_CHANGELOG;
            } else {
                allChangeLogs.sort(Comparator.naturalOrder());
                String last = allChangeLogs.get(allChangeLogs.size() - 1);
                changeLogID = getID(last);
                return last;
            }
        } else {
            throw new IllegalArgumentException("Given path (\"%s\") is NOT a directory".formatted(changeLogRoot));
        }
    }

    public String getNextChangelogFile(File changeLogRoot) {
        String result = getLastChangelogFile(changeLogRoot);
        if(changeLogID == 1) {
            if(result.equals(DEFAULT_CHANGELOG)) {
                return getNextID() + FILE_SUFFIX;
            } else {
                return result;
            }
        } else {
            return getNextID() + FILE_SUFFIX;
        }
    }

    public void updateMainChangeLog(File mainChangeLogPath, String newChangeLogPath) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();

        // Entire file changelog as JSON
        JsonNode changeLog = mapper.readTree(mainChangeLogPath);
        // "databaseChangeLog" as JSON array
        ArrayNode databaseChangeLog = (ArrayNode) changeLog.get("databaseChangeLog");
        // Create new "file" node with new changelog file
        ObjectNode newPathNode = mapper.createObjectNode();
        newPathNode.put("file", newChangeLogPath);
        newPathNode.put("relativeToChangelogFile", "true");
        // Create new "insert" node with "file" node
        ObjectNode newIncludeNode = mapper.createObjectNode();
        newIncludeNode.set("include", newPathNode);
        // Add "insert" node to "databaseChangeLog" array
        databaseChangeLog.add(newIncludeNode);
        // Write back to file
        writer.writeValue(mainChangeLogPath, changeLog);
    }

    public void updateAuthor(File rootFile, String newChangeLog, String authorName, boolean updateIncrement) throws IOException {
        File changeLogFile = new File(rootFile, newChangeLog);
        ObjectMapper mapper = new ObjectMapper();
        ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();

        // Entire file changelog as JSON
        JsonNode changeLog = mapper.readTree(changeLogFile);
        // "databaseChangeLog" as JSON array
        ArrayNode databaseChangeLog = (ArrayNode) changeLog.get("databaseChangeLog");
        databaseChangeLog.findValues("changeSet").forEach( changeSet -> {
            ((ObjectNode)changeSet).put("author", authorName);
            if(updateIncrement) {
                updateIncrementBy(changeSet);
            }
        });
        // Write back to file
        writer.writeValue(changeLogFile, changeLog);
    }

    /**
     * THIS IS PROBABLY JUST TEMPORARY, SINCE IT'S A WORKAROUND FOR A BUG IN LIQUIBASE & HSQLDB
     *
     * @see <a href="https://github.com/liquibase/liquibase/issues/2826">GitHub: Liquibase Issiue #2826</a>
     */
    private void updateIncrementBy(JsonNode changeSet) {
        changeSet.findValues("changes").forEach(changes -> {
            JsonNode sequence = changes.findValue("alterSequence");
            if(sequence != null){
                ObjectNode alterSequence = ((ObjectNode) sequence);
                JsonNode value = alterSequence.remove("incrementBy");
                if(value == null) {
                    throw new IllegalStateException("JSON changelog has no 'incrementBy' node!!!");
                } else {
                    alterSequence.set("increment by", value);
                }
            } else {
                sequence = changes.findValue("createSequence");
                if(sequence != null) {
                    ObjectNode alterSequence = ((ObjectNode) sequence);
                    JsonNode value = alterSequence.remove("incrementBy");
                    if(value == null) {
                        throw new IllegalStateException("JSON changelog has no 'incrementBy' node!!!");
                    } else {
                        alterSequence.set("increment by", value);
                    }
                }
            }
        });
    }

    private int getID(String fileName) {
        Matcher matcher = DIGIT_REGEX.matcher(fileName);
        if(matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        } else {
            throw new IllegalStateException("Couldn't extract ID from file name: "+fileName);
        }
    }

    private String getNextID() {
        changeLogID += 1;
        return StringUtils.leftPad(Integer.toString(changeLogID), 4, "0");
    }

    private static List<String> getAllChangeLogs(File rootDir) {
        String[] result = rootDir.list((File dir, String name) -> FILE_REGEX.matcher(name).matches());
        if(result == null) {
            throw new IllegalStateException("Couldn't find any changelogs!!!");
        } else {
            return Arrays.asList(result);
        }
    }
}
