package org.maxnnsu.dosarWebPageProcessor;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;

import java.util.List;
import java.util.Objects;

public class DosarStadiuProcessorImpl implements DosarWebPageProcessorService {
    @Override
    public List<String> getPdfUrlsToProcess(Document webPage) {
        return Objects.requireNonNull(webPage.getElementById("1576832773102-627a212f-45ce")).getElementsByTag("a").stream()
                .filter(elem -> StringUtils.containsIgnoreCase(elem.attr("href"), "https:"))
                .filter(elem -> StringUtils.containsIgnoreCase(elem.attr("href"), ".pdf"))
                .map(record -> record.attr("href"))
                .toList();
    }
}
