package org.glassfish.modernized.admingui;

import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.glassfish.modernized.admingui.utilis.LoggerRow;

@Named
@ViewScoped
public class LoggerSettingsView implements Serializable {

    // --------- Onglet General
    private boolean writeToSystemLog = true;
    private boolean logToFile = true;
    private boolean rotateAtMidnight = true;
    private boolean multilineMode = false;

    private String consoleFormat = "ULF";
    private String fileFormat = "ODL";

    private List<String> consoleFormats;
    private List<String> fileFormats;

    private List<String> formatExcludeFields = new ArrayList<>(Arrays.asList("tid","timeMillis","levelValue"));

    private int fileRotationLimit = 100;       // MB
    private int fileRotationTime = 0;          // minutes
    private int flushFrequency = 1;
    private int maxHistoryFiles = 0;

    private String logFile = "${com.sun.aas.instanceRoot}/logs/server.log";
    private String logHandler = "org.glassfish.main.jul.handler.SimpleLogHandler";

    // --------- Onglet Log Levels
    private List<LoggerRow> loggers;
    private List<LoggerRow> selected = new ArrayList<>();
    private String bulkLevel = "INFO";
    private List<String> levels;

    @PostConstruct
    public void init() {
        consoleFormats = Arrays.asList("ULF", "ODL");
        fileFormats = Arrays.asList("ODL", "ULF");
        levels = Arrays.asList("OFF","SEVERE","WARNING","INFO","CONFIG","FINE","FINER","FINEST","ALL");

        // Démo : quelques loggers typiques GlassFish / Jakarta
        loggers = new ArrayList<>();
        loggers.add(new LoggerRow("com.sun.enterprise.container.common", "INFO"));
        loggers.add(new LoggerRow("org.glassfish.grizzly.http2", "INFO"));
        loggers.add(new LoggerRow("jakarta.enterprise.system", "INFO"));
        loggers.add(new LoggerRow("org.apache.jasper", "INFO"));
        loggers.add(new LoggerRow("org.glassfish.main", "INFO"));
        loggers.add(new LoggerRow("jakarta.enterprise.web", "INFO"));
        loggers.add(new LoggerRow("org.jvnet.hk2.osgiadapter", "WARNING"));
    }

    // --------- Actions
    public void loadDefaults() {
        writeToSystemLog = true;
        logToFile = true;
        rotateAtMidnight = true;
        multilineMode = false;
        consoleFormat = "ULF";
        fileFormat = "ODL";
        formatExcludeFields = new ArrayList<>(Arrays.asList("tid","timeMillis","levelValue"));
        fileRotationLimit = 100;
        fileRotationTime = 0;
        flushFrequency = 1;
        maxHistoryFiles = 0;
        logFile = "${com.sun.aas.instanceRoot}/logs/server.log";
        logHandler = "org.glassfish.main.jul.handler.SimpleLogHandler";
        // Ici, tu pourrais recharger depuis une source réelle
    }

    public void save() {
        // TODO: Persister dans ton service (asadmin, JMX, rest, base de données…)
        // Pour la démo, rien à faire.
    }

    public void addLogger() {
        // Ajoute une ligne vide à compléter
        loggers.add(new LoggerRow("new.logger.name", "INFO"));
    }

    public void deleteSelected() {
        if (selected != null && !selected.isEmpty()) {
            loggers.removeAll(selected);
            selected.clear();
        }
    }

    public void applyBulkLevel() {
        if (selected != null) {
            for (LoggerRow r : selected) {
                r.setLevel(bulkLevel);
            }
        }
    }

    // --------- Getters/Setters (générés)
    public boolean isWriteToSystemLog() { return writeToSystemLog; }
    public void setWriteToSystemLog(boolean v) { this.writeToSystemLog = v; }

    public boolean isLogToFile() { return logToFile; }
    public void setLogToFile(boolean v) { this.logToFile = v; }

    public boolean isRotateAtMidnight() { return rotateAtMidnight; }
    public void setRotateAtMidnight(boolean v) { this.rotateAtMidnight = v; }

    public boolean isMultilineMode() { return multilineMode; }
    public void setMultilineMode(boolean v) { this.multilineMode = v; }

    public String getConsoleFormat() { return consoleFormat; }
    public void setConsoleFormat(String consoleFormat) { this.consoleFormat = consoleFormat; }

    public String getFileFormat() { return fileFormat; }
    public void setFileFormat(String fileFormat) { this.fileFormat = fileFormat; }

    public List<String> getConsoleFormats() { return consoleFormats; }
    public List<String> getFileFormats() { return fileFormats; }

    public List<String> getFormatExcludeFields() { return formatExcludeFields; }
    public void setFormatExcludeFields(List<String> formatExcludeFields) { this.formatExcludeFields = formatExcludeFields; }

    public int getFileRotationLimit() { return fileRotationLimit; }
    public void setFileRotationLimit(int fileRotationLimit) { this.fileRotationLimit = fileRotationLimit; }

    public int getFileRotationTime() { return fileRotationTime; }
    public void setFileRotationTime(int fileRotationTime) { this.fileRotationTime = fileRotationTime; }

    public int getFlushFrequency() { return flushFrequency; }
    public void setFlushFrequency(int flushFrequency) { this.flushFrequency = flushFrequency; }

    public int getMaxHistoryFiles() { return maxHistoryFiles; }
    public void setMaxHistoryFiles(int maxHistoryFiles) { this.maxHistoryFiles = maxHistoryFiles; }

    public String getLogFile() { return logFile; }
    public void setLogFile(String logFile) { this.logFile = logFile; }

    public String getLogHandler() { return logHandler; }
    public void setLogHandler(String logHandler) { this.logHandler = logHandler; }

    public List<LoggerRow> getLoggers() { return loggers; }
    public void setLoggers(List<LoggerRow> loggers) { this.loggers = loggers; }

    public List<LoggerRow> getSelected() { return selected; }
    public void setSelected(List<LoggerRow> selected) { this.selected = selected; }

    public String getBulkLevel() { return bulkLevel; }
    public void setBulkLevel(String bulkLevel) { this.bulkLevel = bulkLevel; }

    public List<String> getLevels() { return levels; }
}
