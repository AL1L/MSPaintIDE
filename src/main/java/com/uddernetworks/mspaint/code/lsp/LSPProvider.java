package com.uddernetworks.mspaint.code.lsp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public class LSPProvider {

    private static Logger LOGGER = LoggerFactory.getLogger(LSPProvider.class);

    private Supplier<RequestManager> requestManagerSupplier;
    private List<String> commands;
    private String workingDir;
    private Process process;

    public LSPProvider(Supplier<RequestManager> requestManagerSupplier, List<String> commands, String workingDir) {
        this.requestManagerSupplier = requestManagerSupplier;
        this.commands = commands;
        this.workingDir = workingDir;
    }

    public void start() throws IOException {
        if (commands == null || commands.isEmpty() || commands.contains(null)) {
            throw new IOException("Unable to start language server: " + this.toString());
        }
        ProcessBuilder builder = createProcessBuilder();
        LOGGER.info("Starting server process with commands " + commands);
        process = builder.start();
        if (!process.isAlive()) {
            throw new IOException("Unable to start language server: " + this.toString());
        } else {
            LOGGER.info("Server process started " + process);
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            var requestManager = this.requestManagerSupplier.get();
            if (requestManager != null) requestManager.shutdown().thenRun(() -> this.process.destroyForcibly());
        }));
    }

    private ProcessBuilder createProcessBuilder() {
        commands.forEach(c -> c = c.replace("\'", ""));
        ProcessBuilder builder = new ProcessBuilder(commands);
        if (this.workingDir != null) builder.directory(new File(workingDir));
        builder.redirectError(ProcessBuilder.Redirect.INHERIT);
        return builder;
    }

    public InputStream getInputStream() {
        return process != null ? process.getInputStream() : null;
    }

    public OutputStream getOutputStream() {
        return process != null ? process.getOutputStream() : null;
    }

    public void stop() {
        if (process != null) {
            process.destroy();
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof LSPProvider) {
            LSPProvider other = (LSPProvider) obj;
            return commands.size() == other.commands.size() && new HashSet<>(commands) == new HashSet<>(other.commands)
                    && workingDir.equals(other.workingDir);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(commands) ^ Objects.hashCode(workingDir);
    }
}