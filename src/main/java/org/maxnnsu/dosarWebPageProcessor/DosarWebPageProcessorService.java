package org.maxnnsu.dosarWebPageProcessor;

import org.jsoup.nodes.Document;

import java.util.List;

public interface DosarWebPageProcessorService {

    List<String> getPdfUrlsToProcess(Document webPage);
}
