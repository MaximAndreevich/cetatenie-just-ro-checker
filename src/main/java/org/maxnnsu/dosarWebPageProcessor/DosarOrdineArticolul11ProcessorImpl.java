package org.maxnnsu.dosarWebPageProcessor;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.List;
import java.util.stream.Collectors;

public class DosarOrdineArticolul11ProcessorImpl implements DosarWebPageProcessorService {
    @Override
    public List<String> getPdfUrlsToProcess(Document webPage) {
        List<Element> tableLines = webPage.getElementsByClass("penci-entry-content").get(0).getElementsByAttribute("href");
        return tableLines.stream()
                .filter(line -> StringUtils.containsIgnoreCase(line.attr("href"), ".pdf"))
                .map(line -> line.attr("href"))
                .collect(Collectors.toList());
    }
}
