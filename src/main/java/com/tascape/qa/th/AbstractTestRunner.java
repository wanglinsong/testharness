/*
 * Copyright 2015 - 2016 Nebula Bay.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.tascape.qa.th;

import com.tascape.qa.th.db.DbHandler;
import com.tascape.qa.th.db.TestResult;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Appender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author linsong wang
 */
public abstract class AbstractTestRunner {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractTestRunner.class);

    private static final ThreadLocal<TestResult> TEST_CASE_RESULT = new ThreadLocal<>();

    public static void setTestCaseResult(TestResult testCaseResult) {
        TEST_CASE_RESULT.set(testCaseResult);
    }

    public static TestResult getTestCaseResult() {
        return TEST_CASE_RESULT.get();
    }

    protected SystemConfiguration sysConfig = SystemConfiguration.getInstance();

    protected DbHandler db = null;

    protected TestResult tcr = null;

    protected String execId = "";

    public abstract void runTestCase() throws Exception;

    protected void generateHtml(Path logFile) {
        Pattern http = Pattern.compile("((http|https)://\\S+)");

        Path html = logFile.getParent().resolve("log.html");
        LOG.trace("creating file {}", html);
        try (PrintWriter pw = new PrintWriter(html.toFile())) {
            pw.println("<html><body><pre>");
            pw.println("<a href='../'>Suite Log Directory</a><br /><a href='./'>Test Log Directory</a>");
            pw.println();
            List<String> lines = FileUtils.readLines(logFile.toFile(),Charset.defaultCharset());
            List<File> files = new ArrayList<>(Arrays.asList(logFile.getParent().toFile().listFiles()));

            for (String line : lines) {
                String newline = line.replaceAll(">", "&gt;");
                newline = newline.replaceAll("<", "&lt;");
                if (newline.contains(" INFO  ")) {
                    newline = "<b>" + newline + "</b>";
                } else if (newline.contains(" WARN  ")) {
                    newline = "<font color='9F6000'><b>" + newline + "</b></font>";
                } else if (newline.contains(" ERROR ")
                    || newline.contains("Failure in test")
                    || newline.contains("AssertionError")) {
                    newline = "<font color='red'><b>" + newline + "</b></font>";
                } else {
                    Matcher m = http.matcher(line);
                    if (m.find()) {
                        String url = m.group(1);
                        String a = String.format("<a href='%s'>%s</a>", url, url);
                        newline = newline.replace(url, a);
                    }
                }
                pw.println(newline);
                for (File file : files) {
                    String path = file.getAbsolutePath();
                    String name = file.getName();
                    if (newline.contains(path)) {
                        if (name.endsWith(".png")) {
                            pw.printf("<a href=\"%s\" target=\"_blank\"><img src=\"%s\" width=\"360px\"/></a>",
                                name, name);
                        }
                        String a = String.format("<a href=\"%s\" target=\"_blank\">%s</a>", name, name);
                        int len = newline.indexOf("    ");
                        pw.printf((len > 0 ? newline.substring(0, len + 5) : "") + a);
                        pw.println();
                        files.remove(file);
                        break;
                    }
                }
            }

            pw.println("</pre></body></html>");
            logFile.toFile().delete();
        } catch (IOException ex) {
            LOG.warn(ex.getMessage());
        }
    }

    Path addLog4jFileAppender(final Path path) throws IOException {
        org.apache.log4j.Logger rootLogger = org.apache.log4j.Logger.getRootLogger();

        String pattern = "%d{HH:mm:ss.SSS} %-5p %t %C{1}.%M:%L - %m%n";
        final String threadName = Thread.currentThread().getName();

        class ThreadFilter extends Filter {
            @Override
            public int decide(LoggingEvent event) {
                if (event.getThreadName().startsWith(threadName)) {
                    return Filter.ACCEPT;
                }
                return Filter.DENY;
            }
        }

        FileAppender fa = new FileAppender(new PatternLayout(pattern), path.toFile().getAbsolutePath());
        fa.addFilter(new ThreadFilter());
        fa.setThreshold(sysConfig.getTestLogLevel());
        fa.setImmediateFlush(true);
        fa.setAppend(true);
        fa.setName(path.toFile().getAbsolutePath());

        fa.activateOptions();
        rootLogger.addAppender(fa);

        return path;
    }

    void removeLog4jAppender(Path path) {
        if (path == null) {
            LOG.warn("Appender name is null");
            return;
        }
        org.apache.log4j.Logger rootLogger = org.apache.log4j.Logger.getRootLogger();
        Appender appender = rootLogger.getAppender(path.toFile().getAbsolutePath());
        if (appender != null) {
            appender.close();
            rootLogger.removeAppender(appender);
        }
    }
}
